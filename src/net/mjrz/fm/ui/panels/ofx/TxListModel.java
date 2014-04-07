package net.mjrz.fm.ui.panels.ofx;

import static net.mjrz.fm.utils.Messages.tr;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.indexer.IndexedEntity;

import org.apache.log4j.Logger;

public class TxListModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private List<List<Object>> data = null;

	static final int IDX_SKIP = 0;
	static final int IDX_ERR_MSG = 1;
	static final int IDX_PAYEE = 2;
	static final int IDX_DATE = 3;
	static final int IDX_AMOUNT = 4;

	static final int IDX_ACCOUNT = 5;
	static final int IDX_NOTES = 6;
	static final int IDX_NOTES_MARKUP = 7;
	static final int IDX_TYPE = 8;
	static final int IDX_FITID = 9;

	static final int IDX_OXF_LINE = 10;

	static final int OBJ_SIZE = 11;

	private String[] columnNames = { "", "", tr("Payee"), tr("Date"),
			tr("Amount"), };

	private Class<?>[] columnClass = { Boolean.class, String.class,
			IndexedEntity.class, String.class, String.class, };

	private Logger logger = Logger.getLogger(TxListModel.class);

	public TxListModel() {
		super();
		data = new ArrayList<List<Object>>();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		if (col >= columnNames.length)
			return null;
		if (row >= data.size())
			return null;

		return data.get(row).get(col);
	}

	public Class<?> getColumnClass(int c) {
		return columnClass[c];
	}

	public boolean isCellEditable(int row, int col) {
		if (col == IDX_SKIP || col == IDX_PAYEE)
			return true;
		return false;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data.get(rowIndex).set(columnIndex, aValue);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	public void addTxObject(TxObject obj) {
		List<Object> row = object2List(obj);
		int sz = data.size();
		data.add(row);
		super.fireTableRowsInserted(sz, sz + 1);
	}

	public void removeRow(int rowIndex) {
		if (rowIndex >= data.size())
			return;

		data.remove(rowIndex);
		super.fireTableRowsDeleted(rowIndex, rowIndex);
	}

	public void clearAll() {
		while (data.size() > 0) {
			this.removeRow(0);
		}
	}

	public TxObject getTxObject(int row) {
		if (row >= data.size())
			return null;

		List<Object> objArr = data.get(row);
		return list2Object(objArr);
	}

	public TxObject list2Object(List<Object> list) {
		TxObject obj = new TxObject();
		obj.setType((TxObject.TxType) list.get(IDX_TYPE));

		obj.setSource((Account) list.get(IDX_ACCOUNT));

		obj.setMatch((IndexedEntity) list.get(IDX_PAYEE));

		try {
			NumberFormat numFormat = NumberFormat
					.getCurrencyInstance(SessionManager.getCurrencyLocale());
			BigDecimal bd = new BigDecimal(numFormat.parse(
					(String) list.get(IDX_AMOUNT)).doubleValue());
			obj.setAmount(bd);
		}
		catch (Exception e) {
			logger.error(e.getMessage());
		}

		try {
			Date d = SessionManager.getDateFormat().parse(
					(String) list.get(IDX_DATE));
			obj.setDate(d);
		}
		catch (ParseException e) {
			logger.error(e.getMessage());
		}

		obj.setNotes((String) list.get(IDX_NOTES));

		obj.setMarkup((String) list.get(IDX_NOTES_MARKUP));

		obj.setDoImport((Boolean) list.get(IDX_SKIP));

		obj.setFitId((String) list.get(IDX_FITID));

		obj.setOfxLine((String) list.get(IDX_OXF_LINE));

		obj.setImportStatusMessage((String) list.get(IDX_ERR_MSG));

		return obj;
	}

	public List<Object> object2List(TxObject tx) {
		Object[] row = new Object[OBJ_SIZE];

		NumberFormat numFormat = NumberFormat
				.getCurrencyInstance(SessionManager.getCurrencyLocale());

		row[IDX_TYPE] = tx.getType();
		row[IDX_PAYEE] = tx.getMatch();
		row[IDX_AMOUNT] = numFormat.format(tx.getAmount());
		row[IDX_DATE] = SessionManager.getDateFormat().format(tx.getDate());
		row[IDX_ACCOUNT] = tx.getSource();
		row[IDX_NOTES] = tx.getNotes();
		row[IDX_NOTES_MARKUP] = tx.getMarkup();
		row[IDX_SKIP] = tx.isDoImport();
		row[IDX_FITID] = tx.getFitId();
		row[IDX_OXF_LINE] = tx.getOfxLine();
		row[IDX_ERR_MSG] = tx.getImportStatusMessage();

		return Arrays.asList(row);
	}
}
