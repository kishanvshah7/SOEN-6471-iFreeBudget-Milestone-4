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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.mjrz.fm.entity.TaskEntityManager;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.scheduler.Scheduler;
import net.mjrz.scheduler.db.entities.ScheduleConstraint;
import net.mjrz.scheduler.db.entities.TaskEntity;
import net.mjrz.scheduler.db.entities.TaskSchedule;
import net.mjrz.scheduler.task.constraints.Constraint;

import org.apache.log4j.Logger;
import org.hibernate.util.SerializationHelper;

public class ScheduledTasksDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable tasksTable;
	private DefaultTableModel model;
	private JLabel timeLbl = null;
	private final String[] COLS = { "Name", "Next reminder", "Ends on" };
	private SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss aaa");
	private static Logger logger = Logger.getLogger(ScheduledTasksDialog.class
			.getName());

	public ScheduledTasksDialog(JFrame parent) {
		super(parent, "Scheduled transactions", true);
		initialize();
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JScrollPane sp = new JScrollPane(getTable());

		cp.add(sp, BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);

		loadData();

		GuiUtilities.addWindowClosingActionMap(this);

		Thread t = new Thread(new MyTimer());
		t.start();
	}

	@SuppressWarnings("serial")
	private JTable getTable() {
		model = new DefaultTableModel(COLS, 0) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tasksTable = new JTable(model) {
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
		tasksTable.setSelectionForeground(Color.BLACK);
		tasksTable.setGridColor(new Color(154, 191, 192));
		tasksTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
		tasksTable.setRowHeight(30);
		tasksTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		tasksTable.getTableHeader().setReorderingAllowed(false);

		return tasksTable;
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		JButton c = new JButton("Close");
		c.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JButton r = new JButton("Refresh");
		r.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadData();
			}
		});

		JButton n = new JButton("Delete");
		n.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteScheduledTask(e);
			}
		});

		timeLbl = new JLabel();
		Font f = ScheduledTasksDialog.this.getFont();
		timeLbl.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));

		ret.add(Box.createHorizontalStrut(3));
		ret.add(timeLbl);
		ret.add(Box.createHorizontalGlue());
		ret.add(n);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(r);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(c);
		return ret;
	}

	private void deleteScheduledTask(ActionEvent e) {
		int sel = tasksTable.getSelectedRow();
		if (sel >= 0) {
			sel = tasksTable.convertRowIndexToModel(sel);
		}
		String name = (String) model.getValueAt(sel, 0);

		try {
			Scheduler.cancelTask(name);
			new TaskEntityManager().deleteTask(name);
			loadData();
		}
		catch (Exception ex) {
			logger.error(MiscUtils.stackTrace2String(ex));
		}
	}

	private void loadData() {
		try {
			model.setRowCount(0);
			List<TaskEntity> list = new TaskEntityManager().getTasks();
			for (TaskEntity e : list) {
				Set<TaskSchedule> set = e.getSchedules();
				if (set == null || set.size() == 0)
					continue;

				TaskSchedule ts = (TaskSchedule) set.toArray()[0];

				Set<ScheduleConstraint> cc = ts.getConstraints();
				if (cc != null && cc.size() > 0) {
					for (ScheduleConstraint sc : cc) {
						byte[] bb = sc.getConstraint();
						Constraint s = (Constraint) SerializationHelper
								.deserialize(bb);
					}
				}

				Vector<String> data = new Vector<String>();

				data.add(e.getName());

				// data.add(sdf.format(e.getStartTime()));

				data.add(sdf.format(ts.getNextRunTime()));

				// if(ts.getLastRunTime() != null)
				// data.add(sdf.format(ts.getLastRunTime()));
				// else
				// data.add("");

				Date end = e.getEndTime();
				Calendar c = Calendar.getInstance();
				c.setTime(end);
				if (c.get(Calendar.YEAR) == 9999) {
					data.add("<never>");
				}
				else {
					data.add(sdf.format(e.getEndTime()));
				}
				model.addRow(data);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyTimer implements Runnable {
		MyTimer() {
		}

		@Override
		public void run() {
			try {
				while (true) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							timeLbl.setText(sdf.format(new Date()));
						}
					});
					Thread.sleep(1000);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
