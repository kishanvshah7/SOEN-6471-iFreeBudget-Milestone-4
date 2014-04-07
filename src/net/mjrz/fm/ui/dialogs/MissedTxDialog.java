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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.mjrz.fm.entity.TaskEntityManager;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.scheduler.db.entities.TaskEntity;

public class MissedTxDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JButton close;
	private JList missedList;
	private JTable missedTable;
	private JPanel buttonPanel;
	private JLabel numMissedLbl;
	private Map<String, List<Date>> missed;
	private final String[] COLS = { "Date" };
	private DefaultTableModel model = null;

	private final int width = 860;
	private final int height = 500;

	private SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss aaa");

	public MissedTxDialog(JFrame parent, Map<String, List<Date>> missed) {
		super(parent, "Missed transactions", true);
		this.missed = missed;
		initialize();
	}

	private void initialize() {
		initList();
		initTable();
		initButtonPanel();

		initLayout();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(width, height));
		setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				MissedTxDialog.class.getClassLoader().getResource(
						"icons/icon_money.png")));
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private void initLayout() {
		getContentPane().setLayout(new GridBagLayout());

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.weighty = 0.5;
		g1.weightx = 0.5;
		g1.anchor = GridBagConstraints.NORTHEAST;
		g1.fill = GridBagConstraints.BOTH;
		g1.insets = new Insets(10, 10, 0, 10);
		getContentPane().add(new JScrollPane(missedList), g1);

		JScrollPane sp = new JScrollPane(missedTable);
		sp.setBorder(BorderFactory.createTitledBorder(tr("Missed dates")));

		GridBagConstraints g2 = new GridBagConstraints();
		g2.gridx = 1;
		g2.gridy = 0;
		g2.weightx = 0.5;
		g2.anchor = GridBagConstraints.EAST;
		g2.fill = GridBagConstraints.BOTH;
		g2.insets = new Insets(10, 10, 0, 10);
		getContentPane().add(sp, g2);

		GridBagConstraints g3 = new GridBagConstraints();
		g3.gridx = 0;
		g3.gridy = 1;
		g3.anchor = GridBagConstraints.EAST;
		g3.fill = GridBagConstraints.BOTH;
		g3.gridwidth = 2;
		g3.insets = new Insets(10, 10, 0, 10);
		getContentPane().add(buttonPanel, g3);
	}

	private void initList() {
		final DefaultListModel dlm = new DefaultListModel();
		Set<String> keys = missed.keySet();
		for (String k : keys) {
			dlm.addElement(k);
		}
		missedList = new JList(dlm);
		missedList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int idx = e.getFirstIndex();
				if (idx >= 0) {
					String val = (String) dlm.get(idx);
					List<Date> list = missed.get(val);
					loadTableData(val, list);
				}
			}
		});
		missedList.setBorder(BorderFactory
				.createTitledBorder(tr("Missed transactions")));
		missedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	private void initButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		close = new JButton(tr("Close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		numMissedLbl = new JLabel();

		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(numMissedLbl);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(close);

		buttonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
	}

	@SuppressWarnings("serial")
	private void initTable() {
		model = new DefaultTableModel(COLS, 0) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		missedTable = new JTable(model) {
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				JLabel c = (JLabel) super.prepareRenderer(renderer, rowIndex,
						vColIndex);

				if (isCellSelected(rowIndex, vColIndex)) {
					java.awt.Font f = c.getFont();
					c.setFont(new java.awt.Font(f.getName(),
							java.awt.Font.BOLD, f.getSize()));
					c.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
					c.setBorder(BorderFactory.createEmptyBorder());
					return c;
				}
				if (rowIndex % 2 == 0) {
					c.setBackground(new Color(234, 234, 234));
				}
				else {
					c.setBackground(Color.WHITE);
				}
				return c;
			}
		};
		missedTable.setSelectionForeground(Color.BLACK);
		missedTable.setGridColor(new Color(154, 191, 192));
		missedTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
		missedTable.setRowHeight(30);
		missedTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		missedTable.getTableHeader().setReorderingAllowed(false);
	}

	private void loadTableData(String taskName, List<Date> list) {
		try {
			TaskEntity te = new TaskEntityManager().getTask(taskName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		numMissedLbl.setText("Count = " + list.size());
		model.setRowCount(0);
		for (Date d : list) {
			Object rowData[] = new Object[1];
			rowData[0] = sdf.format(d);
			model.addRow(rowData);
		}
	}
}
