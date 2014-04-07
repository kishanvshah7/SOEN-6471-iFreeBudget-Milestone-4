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

import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.mjrz.scheduler.task.MonthSchedule;
import net.mjrz.scheduler.task.MonthScheduleDayBased;
import net.mjrz.scheduler.task.Schedule;
import net.mjrz.scheduler.task.Schedule.DayOfWeek;
import net.mjrz.scheduler.task.Schedule.RepeatType;
import net.mjrz.scheduler.task.Schedule.WeekOfMonth;
import net.mjrz.scheduler.task.constraints.Constraint;
import net.mjrz.scheduler.task.constraints.MonthConstraint;
import net.mjrz.scheduler.task.constraints.MonthConstraintDayBased;

public class MonthSchedulePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JRadioButton dayRb, dateRb;
	private ButtonGroup type;
	private JComboBox weekCb, dayCb;

	private WeekOfMonth[] weekS = { WeekOfMonth.First, WeekOfMonth.Second,
			WeekOfMonth.Third, WeekOfMonth.Fourth, WeekOfMonth.Last };
	// private String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday",
	// "Thursday", "Friday", "Saturday"};
	private DayOfWeek[] dayS = { DayOfWeek.Sunday, DayOfWeek.Monday,
			DayOfWeek.Tuesday, DayOfWeek.Wednesday, DayOfWeek.Thursday,
			DayOfWeek.Friday, DayOfWeek.Saturday };
	private JTextField dateTf, monthTf, monthTf1;

	public MonthSchedulePanel() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createVerticalGlue());
		add(getByDayPanel());
		add(Box.createVerticalStrut(30));
		add(getByDatePanel());
		add(Box.createVerticalGlue());
		type = new ButtonGroup();
		type.add(dayRb);
		type.add(dateRb);
	}

	private JPanel getByDatePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		dateTf = new JTextField();
		monthTf = new JTextField();

		dateRb = new JRadioButton("Day");
		ret.add(dateRb);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(dateTf);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(new JLabel("of every"));
		ret.add(Box.createHorizontalStrut(5));
		ret.add(monthTf);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(new JLabel(" month(s)"));
		ret.add(Box.createHorizontalGlue());

		return ret;
	}

	private JPanel getByDayPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));
		weekCb = new JComboBox(weekS);
		dayCb = new JComboBox(dayS);
		monthTf1 = new JTextField(5);
		dayRb = new JRadioButton("The");
		ret.add(dayRb);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(weekCb);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(dayCb);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(new JLabel("of every"));
		ret.add(Box.createHorizontalStrut(5));
		ret.add(monthTf1);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(new JLabel(" month(s)"));
		ret.add(Box.createHorizontalGlue());
		return ret;
	}

	public boolean isValidSchedule() {
		if (dayRb.isSelected()) {
			return validateByDay();
		}
		else {
			return validateByDate();
		}
	}

	private boolean validateByDay() {
		try {
			Integer num = Integer.parseInt(monthTf1.getText());
			if (num > 12)
				return false;
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private boolean validateByDate() {
		try {
			int num = Integer.parseInt(dateTf.getText());
			if (num > 31) {
				return false;
			}
			num = Integer.parseInt(monthTf.getText());
			if (num > 12) {
				return false;
			}
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public Schedule getSchedule(Date st, Date en) throws Exception {
		if (dateRb.isSelected()) {
			return getByDateSchedule(st, en);
		}
		return getByDaySchedule(st, en);
	}

	private Schedule getByDaySchedule(Date st, Date en) throws Exception {
		MonthScheduleDayBased s = new MonthScheduleDayBased(st, en);
		WeekOfMonth wom = (WeekOfMonth) weekCb.getSelectedItem();
		DayOfWeek dow = (DayOfWeek) dayCb.getSelectedItem();
		Constraint co = new MonthConstraintDayBased(wom, dow);

		s.setRepeatType(RepeatType.MONTH, 1);
		s.setConstraint(co);

		return s;
	}

	private Schedule getByDateSchedule(Date st, Date en) throws Exception {
		int date = Integer.parseInt(dateTf.getText());
		int month = Integer.parseInt(monthTf.getText());

		MonthSchedule s = new MonthSchedule(st, en);
		Constraint co = new MonthConstraint(date);

		s.setRepeatType(RepeatType.MONTH, month);
		s.setConstraint(co);
		return s;
	}
}
