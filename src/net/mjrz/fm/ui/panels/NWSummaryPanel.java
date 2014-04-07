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
package net.mjrz.fm.ui.panels;

import java.math.BigDecimal;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetNetWorthAction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.utils.UIDefaults;

public final class NWSummaryPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel l1, t1, l2, t2, l3, t3;
	private JSeparator sp1;

	public NWSummaryPanel() {
		super();
		setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
		initialize();
	}

	protected void updateSummary(User user) {
		try {
			NumberFormat numberFormat = NumberFormat
					.getCurrencyInstance(SessionManager.getCurrencyLocale());

			ActionRequest req = new ActionRequest();
			req.setActionName("getNetWorth");
			req.setUser(user);
			GetNetWorthAction action = new GetNetWorthAction();
			ActionResponse resp = action.executeAction(req);
			if (!resp.hasErrors()) {
				BigDecimal nw = (BigDecimal) resp.getResult("NET_VALUE");
				BigDecimal av = (BigDecimal) resp.getResult("ASSET_VALUE");
				BigDecimal lv = (BigDecimal) resp.getResult("LIAB_VALUE");

				t1.setText(numberFormat.format(av));
				t2.setText(numberFormat.format(lv));
				if (nw.doubleValue() >= 0)
					t3.setText("<html><font color=\"black\"><b>"
							+ numberFormat.format(nw) + "</b></font></html>");
				else
					t3.setText("<html><font color=\"red\"><b>"
							+ numberFormat.format(nw) + "</b></font></html>");
			}
		}
		catch (Exception e) {
			t1.setText("-NA-");
			t2.setText("-NA-");
			t3.setText("-NA-");
		}
	}

	private void initialize() {
		l1 = new javax.swing.JLabel();
		t1 = new javax.swing.JLabel();
		l2 = new javax.swing.JLabel();
		t2 = new javax.swing.JLabel();
		sp1 = new javax.swing.JSeparator();
		l3 = new javax.swing.JLabel();
		t3 = new javax.swing.JLabel();

		l1.setText("Assets");

		t1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		t1.setText("jLabel2");

		l2.setText("Liabilities");

		t2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		t2.setText("jLabel4");

		l3.setText("Net value");

		t3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		t3.setText("jLabel6");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														sp1,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														376, Short.MAX_VALUE)
												.addGroup(
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addComponent(
																						l1,
																						javax.swing.GroupLayout.PREFERRED_SIZE,
																						120,
																						javax.swing.GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						l2))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addComponent(
																						t2,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						250,
																						Short.MAX_VALUE)
																				.addComponent(
																						t1,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						250,
																						Short.MAX_VALUE)))
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		l3)
																.addGap(73, 73,
																		73)
																.addComponent(
																		t3,
																		javax.swing.GroupLayout.DEFAULT_SIZE,
																		252,
																		Short.MAX_VALUE)))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(l1)
												.addComponent(t1))
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(l2)
												.addComponent(t2))
								.addGap(18, 18, 18)
								.addComponent(sp1,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(l3)
												.addComponent(t3))
								.addContainerGap(192, Short.MAX_VALUE)));
	}// </editor-fold>
}
