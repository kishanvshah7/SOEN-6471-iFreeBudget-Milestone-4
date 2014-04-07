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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.mjrz.scheduler.task.Schedule;
import net.mjrz.scheduler.task.Schedule.DayOfWeek;
import net.mjrz.scheduler.task.Schedule.RepeatType;
import net.mjrz.scheduler.task.WeekSchedule;
import net.mjrz.scheduler.task.constraints.WeekConstraint;

public class WeeklySchedulePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField num;
	private DayOfWeek[] DAYS = { DayOfWeek.Sunday, DayOfWeek.Monday,
			DayOfWeek.Tuesday, DayOfWeek.Wednesday, DayOfWeek.Thursday,
			DayOfWeek.Friday, DayOfWeek.Saturday, };
	private JCheckBox[] daysCb = null;

	public WeeklySchedulePanel() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());

		num = new JTextField(5);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 10;
		add(getRecurPanel(), c);

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		add(getDaysPanel(), c);
		num.setText("1");
	}

	private JPanel getRecurPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 10;
		ret.add(new JLabel("Every "), c);

		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 1;
		c.gridy = 0;
		ret.add(num, c);

		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 2;
		c.gridy = 0;
		ret.add(new JLabel(" weeks(s)"), c);

		return ret;
	}

	private JPanel getDaysPanel() {
		daysCb = new JCheckBox[DAYS.length];
		JPanel ret = new JPanel();
		Calendar c = Calendar.getInstance();
		ret.setLayout(new GridLayout(2, 4));
		for (int i = 0; i < DAYS.length; i++) {
			JCheckBox cb = new JCheckBox(DAYS[i].name());
			if (c.get(Calendar.DAY_OF_WEEK) == i + 1) {
				cb.setSelected(true);
			}
			daysCb[i] = cb;
			ret.add(cb);
		}
		ret.add(new JLabel());
		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	public boolean isValidSchedule() {
		String val = num.getText();
		if (val == null || val.length() == 0)
			return false;
		try {
			boolean sel = false;
			for (int i = 0; i < daysCb.length; i++) {
				if (daysCb[i].isSelected()) {
					sel = true;
					break;
				}
			}
			if (!sel) {
				return false;
			}
			int i = Integer.parseInt(val);
			return i > 0;
		}
		catch (Exception e) {
			return false;
		}
	}

	public Schedule getSchedule(Date st, Date en) throws Exception {
		WeekSchedule s = new WeekSchedule(st, en);

		WeekConstraint co = new WeekConstraint();
		for (int i = 0; i < daysCb.length; i++) {
			if (daysCb[i].isSelected()) {
				DayOfWeek dow = DAYS[i];
				co.addDay(dow);
			}
		}

		s.setRepeatType(RepeatType.WEEK, Integer.parseInt(num.getText()));
		s.setConstraint(co);

		return s;
	}
}
