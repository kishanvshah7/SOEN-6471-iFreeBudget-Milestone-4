/*******************************************************************************
 * Copyright  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mjrz.fm.ui.utils;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.AttachmentRef;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.TxDecorator;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.search.newfilter.Order;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
@SuppressWarnings("serial")
public class TransactionTableModel extends AbstractTableModel implements
		SortableTableModel {
	private static Logger logger = Logger.getLogger(TransactionTableModel.class
			.getName());

	Vector<Object> datas = null; // new Vector();

	String columnNames[] = { "", Messages.getString("Date"),
			Messages.getString("Account"), Messages.getString("Payee"),
			Messages.getString("Amount"), Messages.getString("Balance"),
			Messages.getString("Status"), "Color", "Attachment", "IsParent",
			"IsExpanded", "ParentId" };

	boolean[] visibleColumns = new boolean[columnNames.length];

	int[] sortInfo;

	private NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	private FManEntityManager entityManager;

	public TransactionTableModel() {
		visibleColumns[0] = true;
		visibleColumns[1] = true;
		visibleColumns[2] = true;
		visibleColumns[3] = true;
		visibleColumns[4] = true;
		visibleColumns[5] = false;
		visibleColumns[6] = false;
		visibleColumns[7] = false;
		visibleColumns[8] = false;
		visibleColumns[9] = false;
		visibleColumns[10] = false;
		visibleColumns[11] = false;

		sortInfo = new int[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			sortInfo[i] = NONE;
		}
		datas = new Vector<Object>();
		entityManager = new FManEntityManager();

		addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent evt) {
				handleTableChangedEvent(evt);
			}
		});
	}

	protected int getNumber(int col) {
		int n = col;
		int i = 0;
		do {
			if (!(visibleColumns[i]))
				n++;
			i++;
		}
		while (i < n);
		while (!(visibleColumns[n]))
			n++;
		return n;
	}

	public int getAmountColumnIndex() {
		return 4;
	}

	public int getBalanceColumnIndex() {
		return 5;
	}

	public int getPayeeColumnIndex() {
		return 3;
	}

	// *** TABLE MODEL METHODS ***
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		columnIndex = getNumber(columnIndex);
		if (columnIndex == getAmountColumnIndex()) {
			return true;
		}
		return super.isCellEditable(rowIndex, columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		columnIndex = getNumber(columnIndex);
		if (columnIndex == getAmountColumnIndex()) {
			return String.class;
		}
		if (columnIndex == getBalanceColumnIndex()) {
			return String.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		columnIndex = getNumber(columnIndex);
		if (columnIndex == getAmountColumnIndex()) {
			try {
				List<Transaction> txList = new ArrayList<Transaction>();
				User user = new User();
				user.setUid(SessionManager.getSessionUserId());

				TT tt = getTransaction(rowIndex);
				Transaction t = new Transaction();

				Converter bdConverter = new BigDecimalConverter(new BigDecimal(
						0.0d));
				ConvertUtils.register(bdConverter, BigDecimal.class);
				BeanUtils.copyProperties(t, tt);

				// Add the new value
				BigDecimal val = new BigDecimal(numFormat
						.parse((String) aValue).doubleValue());
				t.setTxAmount(val);

				txList.add(t);

				ActionRequest req = new ActionRequest();
				req.setActionName("addTransaction");
				req.setUser(user);
				req.setProperty("TXLIST", txList);
				req.setProperty("UPDATETX", Boolean.valueOf(true));

				AddNestedTransactionsAction action = new AddNestedTransactionsAction();
				ActionResponse result = action.executeAction(req);
				if (result.getErrorCode() == ActionResponse.NOERROR) {
					tt = FManEntityManager.getTT(t.getTxId());

					Object[] newarr = transactionToArray(tt);

					updateRow(newarr, rowIndex);
				}
				else {
					logger.error("Unable to update transaction amount for txid = "
							+ tt.getTxId());
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}

	@Override
	public int getColumnCount() {
		int n = 0;
		for (int i = 0; i < 6; i++)
			if (visibleColumns[i])
				n++;
		return n;
	}

	public int getRowCount() {
		return datas.size();
	}

	public Object getValueAt(int row, int col) {
		if (datas.size() == 0 || row >= datas.size())
			return null;
		Object[] array = (Object[]) (datas.elementAt(row));
		return array[getNumber(col)];
	}

	public Object getTransactionId(int row) {
		if (row < 0 || row >= datas.size())
			return null;
		Object[] array = (Object[]) (datas.elementAt(row));
		return array[0];
	}

	public TT getTransaction(int row) {
		if (row < 0 || row >= datas.size())
			return null;
		Object[] array = (Object[]) (datas.elementAt(row));
		return (TT) array[array.length - 1];
	}

	public String getColumnName(int col) {
		return columnNames[getNumber(col)];
	}

	public void addRow(Object[] rowData) {
		int sz = datas.size();
		insertRow(sz, rowData);
	}

	public void insertRow(int row, Object[] rowData)
			throws ArrayIndexOutOfBoundsException {

		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (row < datas.size()) {
			datas.insertElementAt(rowData, row);
		}
		if (row >= datas.size()) {
			datas.add(rowData);
			row = datas.size() - 1;
		}
		fireTableRowsInserted(row, row);
	}

	public void updateRow(Object[] rowData, int row)
			throws ArrayIndexOutOfBoundsException {
		if (row < 0 || row >= datas.size()) {
			throw new ArrayIndexOutOfBoundsException("Index: " + row);
		}
		datas.setElementAt(rowData, row);
		this.fireTableRowsUpdated(row, row);
	}

	public void setRowCount(int rowCount) {
		if (rowCount == 0) {
			datas.removeAllElements();
		}
		if (rowCount < datas.size()) {
			int sz = datas.size();
			for (int i = rowCount; i < sz; i++) {
				datas.remove(i);
			}
		}
		this.fireTableRowsDeleted(rowCount - 1, datas.size() - 1);
	}

	public void removeRow(int row) {
		if (row < 0 || row >= datas.size())
			return;
		datas.remove(row);
		this.fireTableRowsDeleted(row, row);
	}

	public void removeRows(int srow, int erow) {
		if (srow < 0 || srow >= datas.size())
			return;
		if (erow < 0 || erow >= datas.size())
			return;
		if (srow > erow)
			return;

		for (int i = erow; i >= srow; i--) {
			datas.remove(i);
		}

		this.fireTableRowsDeleted(srow, erow);
	}

	public Vector getDataVector() {
		return datas;
	}

	/* Methods to manage sort info */
	public void resetSortInfo() {
		for (int i = 0; i < sortInfo.length; i++) {
			sortInfo[i] = NONE;
		}
	}

	public void setSortInfo(int col) {
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length) {
			resetSortInfo();
			return;
		}
		int prev = sortInfo[col];
		resetSortInfo();
		switch (prev) {
		case NONE:
			sortInfo[col] = ASC;
			break;
		case ASC:
			sortInfo[col] = DESC;
			break;
		case DESC:
			sortInfo[col] = ASC;
			break;
		}
	}

	public void setSortInfo(int col, int direction) {
		resetSortInfo();
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length) {
			resetSortInfo();
			return;
		}
		if (direction != ASC && direction != DESC) {
			return;
		}
		sortInfo[col] = direction;
		bucketRows();
	}

	public int getSortInfo(int col) {
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length)
			return -1;
		return sortInfo[col];
	}

	public int getSortColumn() {
		int count = getColumnCount();
		for (int i = 0; i < count; i++) {
			int sort = getSortInfo(i);
			if (sort == TransactionTableModel.NONE) {
				continue;
			}
			return i;
		}
		return 0;
	}

	/**/
	public void removeRowColor(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return;

		Object[] array = (Object[]) (datas.elementAt(rowIndex));
		array[7] = null;
	}

	public void setRowColor(int rowIndex, String color) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return;

		Object[] array = (Object[]) (datas.elementAt(rowIndex));
		array[7] = color;
	}

	public Color getRowColor(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return null;

		if (!isTransactionRow(rowIndex)) {
			return UIDefaults.GROUP_ROW_COLOR;
		}

		try {
			Object[] array = (Object[]) (datas.elementAt(rowIndex));
			String colorStr = (String) array[7];
			if (colorStr != null && colorStr.trim().length() > 0) {
				String[] split = colorStr.split(",");
				if (split != null && split.length == 3) {
					Color c = new Color(Integer.parseInt(split[0]),
							Integer.parseInt(split[1]),
							Integer.parseInt(split[2]));
					return c;
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
		return null;
	}

	public String getRowColorString(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return null;

		if (!isTransactionRow(rowIndex)) {
			return UIDefaults.color2String(UIDefaults.GROUP_ROW_COLOR);
		}

		try {
			Object[] array = (Object[]) (datas.elementAt(rowIndex));
			String colorStr = (String) array[7];
			return colorStr;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public boolean hasDecorator(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return false;

		if (!isTransactionRow(rowIndex)) {
			return false;
		}

		try {
			TT tt = this.getTransaction(rowIndex);
			return tt.isDecorated();
		}
		catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<AttachmentRef> getAttachments(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return null;

		if (!isTransactionRow(rowIndex))
			return null;

		Object[] arr = (Object[]) datas.get(rowIndex);
		if (arr.length < 9)
			return null;

		if (arr.length < 9 || arr[8] == null)
			return null;
		List<AttachmentRef> atts = (List<AttachmentRef>) arr[8];
		return atts;
	}

	public Boolean isParent(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return null;

		if (!isTransactionRow(rowIndex))
			return false;

		Object[] arr = (Object[]) datas.get(rowIndex);
		if (arr.length < 10)
			return null;

		Integer p = (Integer) arr[9];
		return p == TT.IsParent.YES.getVal();
	}

	public Boolean isExpanded(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return null;

		if (!isTransactionRow(rowIndex))
			return false;

		Object[] arr = (Object[]) datas.get(rowIndex);
		if (arr.length < 10)
			return null;

		Boolean p = (Boolean) arr[10];
		return p;
	}

	public Boolean isTransactionRow(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return null;
		Object[] arr = (Object[]) datas.get(rowIndex);
		Object o = arr[arr.length - 1];
		if (o instanceof TT) {
			return true;
		}
		return false;
	}

	public void addChildRows(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return;
		Object[] arr = (Object[]) datas.get(rowIndex);
		Long txId = (Long) arr[0];
		try {
			List<TT> txList = entityManager.getChildTT(
					SessionManager.getSessionUserId(), txId);

			if (txList == null)
				return;

			int offset = 0;
			for (TT t : txList) {
				Object[] data = transactionToArray(t);
				data[7] = UIDefaults
						.color2String(UIDefaults.CHILD_TX_ROW_COLOR);
				insertRow(rowIndex + (++offset), data);
				arr[10] = Boolean.valueOf(true);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void removeChildRows(int rowIndex) {
		if (datas.size() == 0 || rowIndex >= datas.size())
			return;
		Object[] arr = (Object[]) datas.get(rowIndex);
		Long txId = (Long) arr[0];
		try {
			for (int i = rowIndex; i < getRowCount(); i++) {
				Object[] sub = (Object[]) datas.get(i);
				Long parent = (Long) sub[11];
				if (parent.longValue() == txId.longValue()) {
					removeRow(i);
					fireTableRowsDeleted(i, i);
					arr[10] = Boolean.valueOf(false);
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public Object[] transactionToArray(TT t) throws Exception {
		// use the last slot to save the transaction object itself

		Object[] data = new Object[this.columnNames.length + 1];
		data[0] = Long.valueOf(t.getTxId());
		data[1] = SessionManager.getDateFormat().format(t.getTxDate());
		data[2] = FManEntityManager.getAccountName(t.getFromAccountId());
		data[3] = FManEntityManager.getAccountName(t.getToAccountId());
		data[4] = numFormat.format(t.getTxAmount());
		data[5] = numFormat.format(t.getFromAccountEndingBal());
		data[6] = AccountTypes.getTxStatus(t.getTxStatus());

		/* Add the decorator color to the row */
		TxDecorator dec = entityManager.getTxDecorator(t.getTxId());
		if (dec != null) {
			data[7] = dec.getColor();
			t.setDecorated(true);
		}
		else {
			data[7] = "";
			t.setDecorated(false);
		}

		java.util.List<AttachmentRef> att = FManEntityManager.getAttachmentId(t
				.getTxId());

		data[8] = att;
		data[9] = t.getIsParent();
		data[10] = Boolean.valueOf(false);

		Long parentId = t.getParentTxId();
		if (parentId == null) {
			parentId = Long.valueOf(0);
		}
		data[11] = parentId;
		data[12] = t;

		return data;
	}

	private void handleTableChangedEvent(TableModelEvent evt) {
		int col = evt.getColumn();
		int fRow = evt.getFirstRow();
		int lRow = evt.getLastRow();
		if (fRow != lRow)
			return;
		if (col != getAmountColumnIndex()) {
			return;
		}
	}

	public BigDecimal getBucketTotal(int row) {
		BigDecimal ret = new BigDecimal(0);
		int curr = row;
		int sz = datas.size();
		while (true) {
			if (++curr >= sz)
				break;
			if (isTransactionRow(curr)) {
				BigDecimal tmp = this.getTransaction(curr).getTxAmount();
				ret = ret.add(tmp);
			}
			else {
				break;
			}
		}
		return ret;
	}

	public void bucketRows() {
		int sz = datas.size();

		if (sz == 0)
			return;

		int sortedCol = getSortColumn();
		String sortedColumnName = Order.getColumnName(sortedCol);

		if (sortedColumnName.equals("Balance")) {
			return;
		}
		if (sortedColumnName.equals("Amount")) {
			bucketAmountRows();
			return;
		}

		TT last = getTransaction(0);

		Object[] bucketRow = getEmptyRow();

		bucketRow[1] = getBucketLabel(sortedColumnName, last, null);

		insertRow(0, bucketRow);

		int start = 0;
		for (int i = 0; i < datas.size(); i++) {
			if (!isTransactionRow(i)) {
				continue;
			}
			TT tt = this.getTransaction(i);
			if (last == null) {
				last = tt;
			}
			boolean sameBucket = isSameBucket(sortedColumnName, tt, last);
			if (sameBucket) {
				String rc = getRowColorString(i);
				if (rc == null || rc.length() == 0) {
					if (start % 2 == 0) {
						setRowColor(i, UIDefaults.color2String(Color.white));
					}
					else {
						setRowColor(i, UIDefaults.color2String(new Color(234,
								234, 234)));
					}
				}
				else {
					// System.out.println("+++++++++++++++++++" + rc);
					setRowColor(i, rc);
				}
				start++;
				continue;
			}
			else {
				bucketRow = getEmptyRow();
				bucketRow[1] = getBucketLabel(sortedColumnName, tt, last);
				insertRow(i, bucketRow);
				start = 0;
			}
			last = tt;
		}
		// sz = datas.size();
		// for(int i = 0; i < sz; i++) {
		// if(isTransactionRow(i)) {
		// System.out.println(this.getRowColor(i));
		// }
		// else {
		// System.out.println("-------------------------");
		// }
		// }
		// System.out.println("***");
	}

	private void bucketAmountRows() {
		int sz = datas.size();
		if (sz == 0)
			return;

		for (int i = 0; i < datas.size(); i += 6) {
			sz = datas.size();
			int end = i + 4;
			if ((i + 4) >= sz) {
				end = sz - 1;
			}
			TT a = getTransaction(i);
			TT b = getTransaction(end);

			Object[] bucketRow = getEmptyRow();
			bucketRow[1] = getBucketLabel("Amount", a, b);
			insertRow(i, bucketRow);
		}
	}

	private String getBucketLabel(String sortedColumn, TT tt, TT last) {
		if (sortedColumn.equals("Date")) {
			SimpleDateFormat fmt = SessionManager.getDateFormat();
			return tr("Date") + ": " + fmt.format(tt.getTxDate());
		}
		else if (sortedColumn.equals("From")) {
			return tr("Account") + ": " + tt.getFromName();
		}
		else if (sortedColumn.equals("To")) {
			return tr("Payee") + ": " + tt.getToName();
		}
		else if (sortedColumn.equals("Amount")) {
			if (last != null) {
				StringBuilder msg = new StringBuilder(tr("Amount"));
				msg.append(":");
				if (last.getTxAmount().compareTo(tt.getTxAmount()) >= 0) {
					msg.append(" ");
					msg.append(numFormat.format(tt.getTxAmount()));
					msg.append(" to ");
					msg.append(numFormat.format(last.getTxAmount()));
				}
				else {
					msg.append(numFormat.format(last.getTxAmount()));
					msg.append(" ");
					msg.append(" to ");
					msg.append(numFormat.format(tt.getTxAmount()));
				}
				return msg.toString();
			}
			else {
				return tr("Amount") + ": " + numFormat.format(tt.getTxAmount());
			}
		}
		return "";
	}

	private Object[] getEmptyRow() {
		Object[] row = new Object[this.columnNames.length + 1];
		for (int j = 0; j < row.length; j++) {
			row[j] = String.valueOf("");
		}
		return row;
	}

	private boolean isSameBucket(String sortedColumn, TT tt1, TT tt2) {
		if (sortedColumn.equals("Date")) {
			return isSameDate(tt1.getTxDate(), tt2.getTxDate());
		}
		else if (sortedColumn.equals("From")) {
			return isSameAccount(tt1, tt2);
		}
		else if (sortedColumn.equals("To")) {
			return isSamePayee(tt1, tt2);
		}
		else if (sortedColumn.equals("Amount")) {
			return isSameAmount(tt1, tt2);
		}
		return false;
	}

	private boolean isSameDate(Date d1, Date d2) {
		long diff = d1.getTime() - d2.getTime();
		if (diff < 0) {
			diff *= -1;
		}
		return diff < MILLISECS_PER_DAY;
	}

	private boolean isSameAccount(TT tt1, TT tt2) {
		return tt1.getFromName().equals(tt2.getFromName());
	}

	private boolean isSamePayee(TT tt1, TT tt2) {
		return tt1.getToName().equals(tt2.getToName());
	}

	private boolean isSameAmount(TT tt1, TT tt2) {
		BigDecimal bd1 = tt1.getTxAmount();
		BigDecimal bd2 = tt2.getTxAmount();

		BigDecimal diff = bd1.subtract(bd2);
		if (diff.doubleValue() < 0) {
			diff = diff.multiply(new BigDecimal(-1));
		}
		int compare = diff.compareTo(BIGDECIMAL_BUCKET);
		// System.out.println("Comparing " + bd1 + " with " + bd2 + " diff = " +
		// diff + " compare = " + compare);
		return compare <= 0;
	}

	private final BigDecimal BIGDECIMAL_BUCKET = new BigDecimal(10);
	private final long MILLISECS_PER_DAY = 60 * 60 * 24 * 1000;
}
