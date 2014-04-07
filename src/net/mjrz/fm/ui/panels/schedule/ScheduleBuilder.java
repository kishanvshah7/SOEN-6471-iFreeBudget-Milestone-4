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
package net.mjrz.fm.ui.panels.schedule;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.mjrz.fm.entity.TaskEntityManager;
import net.mjrz.fm.ui.utils.DateChooser;
import net.mjrz.fm.utils.schedule.ScheduledTx;
import net.mjrz.scheduler.Scheduler;
import net.mjrz.scheduler.task.Schedule;

public class ScheduleBuilder extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel stLbl, enLbl;
	private DateChooser stTf, enTf;
	private JTextField nameTf;
	private JButton cancel, ok;
	private JRadioButton dailyRb, weeklyRb, monthlyRb, minuteRb;
	private JPanel schedulePanel = null;
	private JPanel cards = null;
	private ButtonGroup group;

	private MinuteSchedulePanel minutePanel;
	private DailySchedulePanel dailyPanel;
	private WeeklySchedulePanel weeklyPanel;
	private MonthSchedulePanel monthlyPanel;

	private String NO_END = "<no end date>";

	private SimpleDateFormat defaultSdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private SimpleDateFormat minSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	private long txId;
	private JLabel msgLbl = null;

	public ScheduleBuilder(long txId) {
		super();
		this.txId = txId;
		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());

		group = new ButtonGroup();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		add(getNamePanel(), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		add(getTimesPanel(), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		add(getSchedulePanel(), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		add(getButtonPanel(), c);

		Calendar cal = Calendar.getInstance();
		stTf.setText(minSdf.format(cal.getTime()));

		// c.add(Calendar.MONTH, 2);
		// enTf.setText(sdf.format(c.getTime()));
		enTf.setText(NO_END);
	}

	private JPanel getTimesPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		stLbl = new JLabel("Start");
		enLbl = new JLabel("End");

		stTf = new DateChooser(true, minSdf);

		enTf = new DateChooser(true, minSdf);

		ret.add(stLbl);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(stTf);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(enLbl);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(enTf);
		ret.add(Box.createHorizontalStrut(10));

		ret.setBorder(BorderFactory.createTitledBorder("Time"));

		return ret;
	}

	private JPanel getSchedulePanel() {
		schedulePanel = new JPanel();
		schedulePanel.setLayout(new GridBagLayout());

		cards = new JPanel();
		cards.setLayout(new CardLayout());
		cards.add(getMinutelyPanel(), "Minute");
		cards.add(getDailyPanel(), "Daily");
		cards.add(getWeeklyPanel(), "Weekly");
		cards.add(getMonthlyPanel(), "Monthly");

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		schedulePanel.add(getTypePanel(), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		// c.weightx = 0.6;
		c.weighty = 0.6;
		schedulePanel.add(cards, c);

		CardLayout cl = (CardLayout) cards.getLayout();
		cl.show(cards, "Daily");

		schedulePanel.setBorder(BorderFactory.createTitledBorder("Recurrence"));
		return schedulePanel;
	}

	private JPanel getTypePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		minuteRb = new JRadioButton("Minute");
		minuteRb.setActionCommand("minuteSchedule");
		minuteRb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCard("Minute");
			}
		});
		minuteRb.setEnabled(false);
		minuteRb.setVisible(false);

		dailyRb = new JRadioButton("Daily");
		dailyRb.setActionCommand("dailySchedule");
		dailyRb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCard("Daily");
			}
		});

		weeklyRb = new JRadioButton("Weekly");
		weeklyRb.setActionCommand("weeklySchedule");
		weeklyRb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCard("Weekly");
			}
		});

		monthlyRb = new JRadioButton("Monthly");
		monthlyRb.setActionCommand("monthlySchedule");
		monthlyRb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showCard("Monthly");
			}
		});

		// yearlyRb = new JRadioButton("Monthly (By day)");

		group = new ButtonGroup();
		group.add(minuteRb);
		group.add(dailyRb);
		group.add(weeklyRb);
		group.add(monthlyRb);
		// group.add(yearlyRb);

		ret.add(minuteRb);
		ret.add(dailyRb);
		ret.add(weeklyRb);
		ret.add(monthlyRb);
		// ret.add(yearlyRb);

		dailyRb.setSelected(true);

		return ret;
	}

	private void showCard(String type) {
		CardLayout cl = (CardLayout) cards.getLayout();
		cl.show(cards, type);
	}

	private JPanel getMinutelyPanel() {
		minutePanel = new MinuteSchedulePanel();

		return minutePanel;
	}

	private JPanel getDailyPanel() {
		dailyPanel = new DailySchedulePanel();

		return dailyPanel;
	}

	private JPanel getWeeklyPanel() {
		weeklyPanel = new WeeklySchedulePanel();

		return weeklyPanel;
	}

	private JPanel getMonthlyPanel() {
		monthlyPanel = new MonthSchedulePanel();

		return monthlyPanel;
	}

	private JPanel getNamePanel() {
		JPanel ret = new JPanel();

		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));
		;

		nameTf = new JTextField();

		ret.add(new JLabel("Name"));
		ret.add(Box.createHorizontalStrut(10));
		ret.add(nameTf);
		ret.add(Box.createHorizontalGlue());

		ret.setBorder(BorderFactory.createTitledBorder(""));
		return ret;
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();

		msgLbl = new JLabel();
		msgLbl.setForeground(Color.RED);

		ok = new JButton("Save");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOkButtonActionPerformed(e);
			}
		});
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.getWindowAncestor(ScheduleBuilder.this)
						.dispose();
			}
		});

		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));
		ret.add(msgLbl);
		ret.add(Box.createHorizontalGlue());
		ret.add(ok);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancel);

		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	private boolean createTask(Schedule s) throws Exception {
		boolean success = false;

		// Calendar ca = Calendar.getInstance();
		// Date a = ca.getTime();
		// ca.add(Calendar.YEAR, 1);
		// List<Date> next = s.getRunTimesBetween(s.getStartTime(),
		// ca.getTime());
		// for(Date d : next) {
		// System.out.println(d);
		// }
		// System.out.println("__________");
		// next = s.getRunTimesBetween(a, ca.getTime());
		// for(Date d : next) {
		// System.out.println(d);
		// }

		ScheduledTx t = new ScheduledTx(nameTf.getText(), txId);
		t.setSchedule(s);

		String res = new TaskEntityManager().addTask(t);
		if (res != null) {
			msgLbl.setText(res);
		}
		else {
			Scheduler.scheduleTask(t);
			success = true;
		}
		if (success) {
			SwingUtilities.getWindowAncestor(this).dispose();
		}
		return success;
	}

	private Date getEndDate() throws Exception {
		Date end = null;
		String en = enTf.getText();
		if (en == null || en.trim().length() == 0) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, 9999);
			c.set(Calendar.MONTH, 11);
			c.set(Calendar.DATE, 31);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			end = c.getTime();
		}
		else if (en.equals(NO_END)) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, 9999);
			c.set(Calendar.MONTH, 11);
			c.set(Calendar.DATE, 31);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			end = c.getTime();
		}
		else {
			end = minSdf.parse(this.enTf.getText());
		}
		return end;
	}

	private void doOkButtonActionPerformed(ActionEvent e) {
		String sel = group.getSelection().getActionCommand();
		Schedule s = null;
		try {
			msgLbl.setText("");
			Date st = minSdf.parse(stTf.getText());
			Date en = getEndDate();

			boolean isValid = false;

			String name = nameTf.getText();
			if (name == null || name.length() == 0) {
				isValid = false;
				msgLbl.setText("Name cannot be empty");
				return;
			}
			if (sel.equals("weeklySchedule")) {
				isValid = weeklyPanel.isValidSchedule();
				if (isValid) {
					s = weeklyPanel.getSchedule(st, en);
				}
			}
			else if (sel.equals("minuteSchedule")) {
				isValid = minutePanel.isValidSchedule();
				if (isValid) {
					s = minutePanel.getSchedule(st, en);
				}
			}
			else if (sel.equals("dailySchedule")) {
				isValid = dailyPanel.isValidSchedule();
				if (isValid) {
					s = dailyPanel.getSchedule(st, en);
				}
			}
			else if (sel.equals("monthlySchedule")) {
				isValid = monthlyPanel.isValidSchedule();
				if (isValid) {
					s = monthlyPanel.getSchedule(st, en);
				}
			}
			if (!isValid) {
				JOptionPane.showMessageDialog(this, "Invalid selection",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (s != null) {
				createTask(s);
			}
		}
		catch (java.text.ParseException ex) {
			msgLbl.setText("Invalid date");
		}
		catch (Exception ex) {
			msgLbl.setText("Invalid selection");
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.add(new ScheduleBuilder(1l));
				f.pack();
				f.setSize(500, 300);
				f.setVisible(true);
			}
		});
	}
}
