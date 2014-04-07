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
package net.mjrz.fm.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetFutureTransactions;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.FTTableModel;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class FutureTransactionsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable ftTable;
	private FTTableModel tableModel;
	private FinanceManagerUI parent;
	private User user;
	private static Logger logger = Logger
			.getLogger(FutureTransactionsDialog.class.getName());
	private SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, ''yy");
	private JButton close, delete;
	private FManEntityManager em;

	public FutureTransactionsDialog(FinanceManagerUI parent, User user) {
		super(parent, "Scheduled transactions", true);
		em = new FManEntityManager();
		this.user = user;
		this.parent = parent;
		initialize();
	}

	@SuppressWarnings("unchecked")
	public void initialize() {
		setLayout(new BorderLayout());
		this.add(getTablePanel(), BorderLayout.CENTER);
		this.add(getButtonPanel(), BorderLayout.SOUTH);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(700, 400));
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private JPanel getTablePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		tableModel = new FTTableModel();

		ActionRequest req = new ActionRequest();
		req.setActionName("getFutureTransactions");
		req.setUser(user);

		Calendar dayZeroCal = new GregorianCalendar(1970, 0, 1, 0, 0, 0);

		GetFutureTransactions action = new GetFutureTransactions();
		try {
			ActionResponse response = action.executeAction(req);
			if (response.getErrorCode() == ActionResponse.NOERROR) {
				List results = (List) response.getResult("FTLIST");
				int row = 0;
				for (int i = 0; i < results.size(); i++) {
					Object[] data = (Object[]) results.get(i);
					Date next = (Date) data[4];
					Date end = (Date) data[5];
					if (next != null) {
						data[4] = sdf.format(next);
					}
					if (next != null) {
						if (end.compareTo(dayZeroCal.getTime()) == 0) {
							data[5] = "Never";
						}
						else {
							data[5] = sdf.format(end);
						}
					}
					tableModel.insertRow(row, data);
					row++;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}

		ftTable = new JTable();
		ftTable.setSelectionForeground(Color.BLACK);
		ftTable.setShowVerticalLines(false);
		ftTable.setGridColor(new Color(154, 191, 192));
		ftTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
		ftTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		ftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ftTable.setModel(tableModel);
		ftTable.setRowHeight(20);
		ftTable.setBackground(Color.WHITE);

		ret.add(new JScrollPane(ftTable));

		return ret;
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		delete = new JButton("Delete");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sel = ftTable.getSelectedRow();
				sel = ftTable.convertRowIndexToModel(sel);
				try {
					int num = em.deleteFutureTransaction((Long) tableModel
							.getId(sel));
					if (num == 1) {
						tableModel.removeRow(sel);
					}
				}
				catch (Exception ex) {
					logger.error(MiscUtils.stackTrace2String(ex));
				}
			}
		});
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		ret.add(Box.createHorizontalGlue());
		ret.add(delete);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(close);
		ret.add(Box.createHorizontalGlue());

		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}
}
