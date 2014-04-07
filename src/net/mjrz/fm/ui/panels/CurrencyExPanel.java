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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellRenderer;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddCurrencyMonitorAction;
import net.mjrz.fm.actions.RetrieveForexAction;
import net.mjrz.fm.entity.CurrencyEntityManager;
import net.mjrz.fm.entity.beans.CurrencyMonitor;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.utils.AnimatedLabel;
import net.mjrz.fm.ui.utils.CurrencyTableModel;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class CurrencyExPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private AnimatedLabel jLabel1 = null;

	private JTable exchTable = null;
	private static Logger logger = Logger.getLogger(CurrencyExPanel.class
			.getName());
	private CurrencyEntityManager manager = new CurrencyEntityManager();
	private CurrencyTableModel tableModel;

	private NumberFormat numFormat = NumberFormat.getNumberInstance();

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, MMM d, h:mm a");
	private JMenuItem refreshMenuItem, addMenuItem, removeMenuItem;

	public CurrencyExPanel() {
		super(new BorderLayout(2, 2));
		initialize();
	}

	private void initialize() {
		jLabel1 = new AnimatedLabel(AnimatedLabel.ANIM_LOCATION_LEADING,
				JLabel.TRAILING);
		jLabel1.setPreferredSize(new Dimension(100, 18));
		jLabel1.setText(" ");

		this.add(jLabel1, BorderLayout.SOUTH);
		this.add(getExchTable(), BorderLayout.CENTER);
		setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
	}

	@SuppressWarnings("serial")
	private JScrollPane getExchTable() {
		if (tableModel == null) {
			tableModel = getMonitoredCurrencies();
		}
		if (exchTable == null) {
			exchTable = new JTable(tableModel) {
				public Component prepareRenderer(TableCellRenderer renderer,
						int rowIndex, int vColIndex) {
					JLabel c = (JLabel) super.prepareRenderer(renderer,
							rowIndex, vColIndex);
					if (isCellSelected(rowIndex, vColIndex)) {
						java.awt.Font f = c.getFont();
						c.setFont(new java.awt.Font(f.getName(),
								java.awt.Font.BOLD, f.getSize()));
						c.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
						c.setBorder(BorderFactory.createEmptyBorder());
						return c;
					}
					if (rowIndex % 2 == 0) { // && !isCellSelected(rowIndex,
												// vColIndex)) {
						c.setBackground(new Color(234, 234, 234));
					}
					else {
						c.setBackground(Color.WHITE);
					}
					return c;
				}
			};
			exchTable.setSelectionForeground(Color.BLACK);
			exchTable.setGridColor(new Color(154, 191, 192));
			exchTable.getTableHeader()
					.setDefaultRenderer(
							new TableHeaderRenderer(
									UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
									Color.WHITE));
			exchTable.getTableHeader().setReorderingAllowed(false);
			exchTable.setRowHeight(20);
			exchTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
						showPopupMenu(e.getX(), e.getY());
					}
				}
			});
		}
		JScrollPane sp = new JScrollPane(exchTable);
		sp.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					showPopupMenu(e.getX(), e.getY());
				}
			}
		});
		exchTable.getParent().setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
		return sp;
	}

	@SuppressWarnings("unchecked")
	private CurrencyTableModel getMonitoredCurrencies() {
		CurrencyEntityManager em = new CurrencyEntityManager();
		tableModel = new CurrencyTableModel();
		try {
			java.util.List l = em.getMonitoredCurrencies(1);
			int i = 0;
			for (Object c : l) {
				CurrencyMonitor cm = (CurrencyMonitor) c;
				String[] row = new String[5];
				row[0] = String.valueOf(cm.getId());
				row[1] = cm.getCountry();
				row[2] = cm.getCode() + "(" + cm.getSymbol() + ")";

				row[3] = numFormat.format(cm.getLastUpdateValue());

				row[4] = dateFormat.format(cm.getLastUpdateTs());
				tableModel.insertRow(i, row);
				i++;
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
		return tableModel;
	}

	public static class LocaleCurrencyWrapper implements
			Comparable<LocaleCurrencyWrapper> {
		private Locale l;
		private Currency c;

		public LocaleCurrencyWrapper(Locale locale, Currency currency) {
			l = locale;
			c = currency;
		}

		public int compareTo(LocaleCurrencyWrapper other) {
			LocaleCurrencyWrapper tmp = (LocaleCurrencyWrapper) other;

			String lhs = l.getDisplayCountry();
			String rhs = tmp.l.getDisplayCountry();

			return lhs.compareTo(rhs);
		}

		public Locale getLocale() {
			return l;
		}

		@Override
		public String toString() {
			return l.getDisplayCountry() + " ( " + c.getSymbol() + " ) ";
		}

		@Override
		public int hashCode() {
			return l.hashCode() + c.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof LocaleCurrencyWrapper))
				return false;
			if (this == o)
				return true;
			LocaleCurrencyWrapper tmp = (LocaleCurrencyWrapper) o;

			return this.l.equals(tmp.l)
					&& this.c.getCurrencyCode().equals(tmp.c.getCurrencyCode());
		}
	}

	private void showPopupMenu(int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();
		addMenuItem = new JMenuItem(tr("Add"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/buy.png"));
		addMenuItem.setActionCommand("Add");
		addMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new CurrencySelectorPrompt();
			}
		});
		popup.add(addMenuItem);

		removeMenuItem = new JMenuItem(tr("Remove"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/sell.png"));
		removeMenuItem.setActionCommand("Remove");
		removeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] sel = exchTable.getSelectedRows();
				ArrayList<Integer> removed = new ArrayList<Integer>();
				for (int i = 0; i < sel.length; i++) {
					int modelRow = exchTable.convertRowIndexToModel(sel[i]);
					long id = Long.parseLong(tableModel.getId(modelRow)
							.toString());
					try {
						int del = manager.deleteCurrencyMonitor(id);
						if (del == 1)
							removed.add(sel[i]);
					}
					catch (Exception ex) {
						logger.error(MiscUtils.stackTrace2String(ex));
					}
				}
				for (int i = removed.size() - 1; i >= 0; i--) {
					int r = removed.get(i);
					int s = exchTable.convertRowIndexToModel(r);
					tableModel.removeRow(s);
				}
			}
		});
		if (exchTable.getSelectedRowCount() == 0)
			removeMenuItem.setEnabled(false);
		popup.add(removeMenuItem);

		refreshMenuItem = new JMenuItem(tr("Refresh"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/refresh.png"));
		refreshMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRefresh();
			}
		});
		if (exchTable.getSelectedRowCount() == 0
				|| exchTable.getSelectedRowCount() > 1)
			refreshMenuItem.setEnabled(false);

		popup.add(refreshMenuItem);

		popup.pack();
		popup.show(exchTable, x, y);
	}

	private void updateTableModel(int rowNum, CurrencyMonitor cm) {
		String[] row = new String[5];
		row[0] = String.valueOf(cm.getId());
		row[1] = cm.getCountry();
		row[2] = cm.getCode() + "(" + cm.getSymbol() + ")";
		row[3] = numFormat.format(cm.getLastUpdateValue());
		row[4] = dateFormat.format(cm.getLastUpdateTs());

		tableModel.updateRow(row, rowNum);
	}

	private ActionResponse addCurrencyMonitor(CurrencyMonitor m) {
		try {
			ActionRequest req = new ActionRequest();
			req.setActionName("addCurrencyMonitor");
			req.setProperty("CURRENCYMONITOR", m);

			ActionResponse response = new AddCurrencyMonitorAction()
					.executeAction(req);
			return response;
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
	}

	private void doRefresh() {
		SwingWorker<ActionResponse, Object> worker = new SwingWorker<ActionResponse, Object>() {
			int[] sel = exchTable.getSelectedRows();
			int csel = exchTable.convertRowIndexToModel(sel[0]);

			public ActionResponse doInBackground() throws Exception {
				jLabel1.startAnim();
				jLabel1.setText(" ");
				String id = (String) tableModel.getId(csel);
				ActionRequest req = new ActionRequest();
				req.setActionName("retrieveForexAction");
				req.setProperty("ID", id);

				ActionResponse response = new RetrieveForexAction()
						.executeAction(req);
				return response;
			}

			public void done() {
				try {
					ActionResponse response = get();
					CurrencyMonitor cm = (CurrencyMonitor) response
							.getResult("CURRENCYMONITOR");
					updateTableModel(csel, cm);
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
				}
				finally {
					jLabel1.stopAnim();
				}
			}
		};
		worker.execute();
	}

	class CurrencySelectorPrompt extends JDialog {
		private static final long serialVersionUID = 1L;
		private JComboBox currCb = null;
		private TreeSet<LocaleCurrencyWrapper> locales = null;

		class ButtonHandler implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				process();
			}
		}

		private void process() {
			SwingWorker<ActionResponse, Object> worker = new SwingWorker<ActionResponse, Object>() {
				User u = null;
				CurrencyMonitor m = null;

				public ActionResponse doInBackground() throws Exception {
					ActionResponse resp = null;
					try {
						jLabel1.startAnim();
						jLabel1.setText(" ");
						setVisible(false);

						LocaleCurrencyWrapper w = (LocaleCurrencyWrapper) currCb
								.getSelectedItem();
						m = new CurrencyMonitor();
						m.setCode(w.c.getCurrencyCode());
						m.setSymbol(w.c.getSymbol());
						m.setCountry(w.l.getDisplayCountry());
						m.setLastUpdateTs(new Date());
						m.setOwnerid(net.mjrz.fm.services.SessionManager
								.getSessionUserId());
						m.setLastUpdateValue(new BigDecimal(0));

						resp = addCurrencyMonitor(m);
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
					}
					finally {
						jLabel1.stopAnim();
					}
					return resp;
				}

				public void done() {
					try {
						ActionResponse resp = get();
						if (resp.getErrorCode() == ActionResponse.NOERROR
								&& m != null) {
							String[] row = new String[5];
							row[0] = String.valueOf(m.getId());
							row[1] = m.getCountry();
							row[2] = m.getCode() + "(" + m.getSymbol() + ")";
							row[3] = numFormat.format(m.getLastUpdateValue());
							row[4] = dateFormat.format(m.getLastUpdateTs());

							tableModel.insertRow(tableModel.getRowCount(), row);
						}
						else {
							jLabel1.setForeground(Color.RED);
							jLabel1.setText(resp.getErrorMessage());
						}
						dispose();
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
					}
				}
			};
			worker.execute();
		}

		CurrencySelectorPrompt() {
			locales = getAvailableCurrencies();
			getContentPane().setLayout(
					new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(new JLabel("Country: "));
			panel.add(Box.createHorizontalStrut(5));
			panel.add(getCurrCb());
			panel.setBorder(BorderFactory.createLineBorder(Color.black));

			getContentPane().add(panel);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel,
					BoxLayout.LINE_AXIS));
			buttonPanel.add(Box.createHorizontalGlue());

			JButton ok = new JButton("Ok");
			ok.setActionCommand("ok");
			ok.setMinimumSize(new Dimension(80, 25));
			ok.addActionListener(new ButtonHandler());

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			cancel.setMinimumSize(new Dimension(80, 25));

			buttonPanel.add(ok);
			buttonPanel.add(Box.createHorizontalStrut(10));
			buttonPanel.add(cancel);
			buttonPanel.add(Box.createHorizontalGlue());

			getContentPane().add(buttonPanel);

			setModal(true);
			setTitle("Select");
			pack();
			setLocationRelativeTo(CurrencyExPanel.this);
			setVisible(true);
		}

		private JComboBox getCurrCb() {
			if (currCb == null) {
				Object[] arr = locales.toArray();
				int i = 0;
				for (; i < arr.length; i++) {
					LocaleCurrencyWrapper l = (LocaleCurrencyWrapper) arr[i];
					String slocale = l.getLocale().getLanguage() + "_"
							+ l.getLocale().getCountry();
					if (slocale.equals("en_US")) {
						break;
					}
				}
				currCb = new JComboBox(locales.toArray());
				currCb.setSelectedIndex(i);
			}
			return currCb;
		}

		private TreeSet<LocaleCurrencyWrapper> getAvailableCurrencies() {
			Locale[] loc = Locale.getAvailableLocales();
			TreeSet<LocaleCurrencyWrapper> tmp = new TreeSet<LocaleCurrencyWrapper>();

			for (int i = 0; i < loc.length; i++) {
				try {
					Locale l = loc[i];
					Currency c = Currency.getInstance(l);
					if (c != null) {
						LocaleCurrencyWrapper w = new LocaleCurrencyWrapper(l,
								c);
						tmp.add(w);
					}
				}
				catch (Exception e) {
					// ignore
				}
			}
			return tmp;
		}
	}
}
