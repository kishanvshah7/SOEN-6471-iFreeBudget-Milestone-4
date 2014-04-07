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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddPortfolioEntryAction;
import net.mjrz.fm.entity.beans.PortfolioEntry;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.panels.PortfolioPanel;
import net.mjrz.fm.ui.panels.prefs.PreferencesPanel;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;

/**
 * 
 * @author mjrz
 */
public class InvestmentEntryDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = 1L;

	String currencyList[] = { "USD", "GBP", "Euro", "Rupee", "AUD" };

	String scaleList[] = { "None", "100" };

	private User user;
	private PortfolioPanel parent;
	private long portfolioId = 0;
	private static Logger logger = Logger.getLogger(InvestmentEntryDialog.class
			.getName());
	private TreeSet<Exchange> exchanges = null;

	/** Creates new form PortfolioEntryDialog */
	public InvestmentEntryDialog(JFrame parent, User user, long portfolioId) {
		super(parent, tr("Record investment"), true);
		exchanges = getExchanges();

		this.user = user;
		this.parent = (PortfolioPanel) parent;
		this.portfolioId = portfolioId;
		initComponents();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowCloseKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
	}

	TreeSet<Exchange> exchs = new TreeSet<Exchange>();

	private TreeSet<Exchange> getExchanges() {
		InputStream in = null;
		try {
			in = InvestmentEntryDialog.class.getClassLoader().getResourceAsStream("resources/se.properties");
			Properties props = new Properties();
			props.load(in);
			Enumeration<Object> e = props.keys();
			while (e.hasMoreElements()) {
				String key = e.nextElement().toString();
				String val = props.getProperty(key);
				ArrayList<String> split = MiscUtils.splitString(val, "^");
				if (split.size() != 4)
					continue;
				Exchange exch = new Exchange();
				exch.code = key;
				exch.name = split.get(0);
				exch.extension = split.get(2);
				exch.locale = split.get(3);
				exchs.add(exch);
			}
			return exchs;
		}
		catch (Exception e) {
			logger.error(e);
			return exchs;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

	private ArrayList<String> getExchList() {
		InputStream in = null;
		ArrayList<String> exchs = new ArrayList<String>();
		try {
			in = InvestmentEntryDialog.class.getClassLoader().getResourceAsStream("resources/se.properties");
			Properties props = new Properties();
			props.load(in);
			Enumeration<Object> keys = props.keys();
			while (keys.hasMoreElements()) {
				String line = (String) keys.nextElement();
				exchs.add(line);
			}
			return exchs;
		}
		catch (Exception e) {
			logger.error(e);
			return exchs;
		}
		finally {
			try {
				in.close();
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();

		symbolTf = new javax.swing.JTextField();
		GuiUtilities.setupTextComponent(symbolTf);
		((AbstractDocument) symbolTf.getDocument())
				.setDocumentFilter(new UpperCaseFilter());

		jLabel3 = new javax.swing.JLabel();

		numSharesTf = new javax.swing.JTextField();
		GuiUtilities.setupTextComponent(numSharesTf);

		marketCb = new javax.swing.JComboBox();
		jLabel4 = new javax.swing.JLabel();

		currencyCb = new javax.swing.JLabel();
		currencyHelpB = new javax.swing.JButton();
		jLabel5 = new javax.swing.JLabel();

		scaleCb = new javax.swing.JComboBox();
		scaleHelpB = new javax.swing.JButton();

		saveB = new javax.swing.JButton();
		saveB.setMnemonic(KeyEvent.VK_S);
		cancelB = new javax.swing.JButton();
		cancelB.setMnemonic(KeyEvent.VK_C);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jLabel1.setFont(new java.awt.Font("DejaVu LGC Sans", 1, 13));
		jLabel1.setText(tr("Enter details"));

		jLabel2.setText(tr("Symbol"));

		jLabel3.setText(tr("Number of shares"));

		String lastExchange = ZProperties
			.getProperty(PreferencesPanel.LAST_STOCK_EXCH);		
		Exchange[] exchArr = new Exchange[exchanges.size()];
		int i = 0;
		int defaultExch = 0;
		for(Exchange e : exchanges) {
			exchArr[i] = e;
			if(lastExchange != null && e.code.equals(lastExchange)) {
				defaultExch = i;
			}
			i++;
		}
		marketCb.setModel(new javax.swing.DefaultComboBoxModel(exchanges
				.toArray()));
		marketCb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				scaleCb.setVisible(false);
				jLabel5.setVisible(false);
				scaleHelpB.setVisible(false);
				Exchange ex = (Exchange) marketCb.getSelectedItem();
				if (ex != null) {
					if (ex.code.equals("LSE")) {
						currencyCb.setText("GBP");
						scaleCb.setVisible(true);
						jLabel5.setVisible(true);
						scaleHelpB.setVisible(true);
					}
					else if (ex.code.equals("BSE")) {
						currencyCb.setText("Rupee");
					}
					else if (ex.code.equals("FRA") || ex.code.equals("XETRA")
							|| ex.code.equals("PAR")) {
						currencyCb.setText("Euro");
					}
					else if (ex.code.equals("ASX")) {
						currencyCb.setText("AUD");
					}
					else {
						currencyCb.setText("USD");
					}
					ZProperties.replaceRuntimeProperty(PreferencesPanel.LAST_STOCK_EXCH,
							ex.code);
				}
			}
		});
		marketCb.setSelectedIndex(defaultExch);
		jLabel4.setText(tr("Currency"));

		currencyCb.setText(currencyList[0]);

		currencyHelpB.setText("?");
		currencyHelpB.setVisible(false);

		jLabel5.setText(tr("Scale factor"));
		jLabel5.setVisible(false);

		scaleCb.setModel(new javax.swing.DefaultComboBoxModel(scaleList));
		scaleCb.setVisible(false);

		scaleHelpB.setText("?");
		scaleHelpB.setVisible(false);
		scaleHelpB.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				scaleHelpBActionPerformed(evt);
			}
		});

		saveB.setText(tr("Save"));
		saveB.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveBActionPerformed(evt);
			}
		});

		cancelB.setText(tr("Cancel"));
		cancelB.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelBActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														jLabel1,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														532, Short.MAX_VALUE)
												.addGroup(
														layout.createSequentialGroup()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.TRAILING,
																				false)
																				.addComponent(
																						jLabel4,
																						javax.swing.GroupLayout.Alignment.LEADING,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						Short.MAX_VALUE)
																				.addComponent(
																						jLabel3,
																						javax.swing.GroupLayout.Alignment.LEADING,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						Short.MAX_VALUE)
																				.addComponent(
																						jLabel2,
																						javax.swing.GroupLayout.Alignment.LEADING,
																						javax.swing.GroupLayout.DEFAULT_SIZE,
																						129,
																						Short.MAX_VALUE))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING,
																												false)
																												.addComponent(
																														currencyCb,
																														0,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														Short.MAX_VALUE)
																												.addComponent(
																														numSharesTf)
																												.addComponent(
																														symbolTf,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														116,
																														Short.MAX_VALUE))
																								.addGap(18,
																										18,
																										18)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addGroup(
																														layout.createSequentialGroup()
																																.addComponent(
																																		jLabel5)
																																.addPreferredGap(
																																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																.addComponent(
																																		scaleCb,
																																		javax.swing.GroupLayout.PREFERRED_SIZE,
																																		101,
																																		javax.swing.GroupLayout.PREFERRED_SIZE)
																																.addPreferredGap(
																																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																																.addComponent(
																																		scaleHelpB))
																												.addComponent(
																														marketCb,
																														javax.swing.GroupLayout.PREFERRED_SIZE,
																														240,
																														javax.swing.GroupLayout.PREFERRED_SIZE)))
																				.addGroup(
																						layout.createSequentialGroup()
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addComponent(
																										saveB,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										94,
																										javax.swing.GroupLayout.PREFERRED_SIZE)
																								.addGap(18,
																										18,
																										18)
																								.addComponent(
																										cancelB,
																										javax.swing.GroupLayout.PREFERRED_SIZE,
																										94,
																										javax.swing.GroupLayout.PREFERRED_SIZE)))
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																		17,
																		Short.MAX_VALUE)))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jLabel1,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										26,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														symbolTf,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel2)
												.addComponent(
														marketCb,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel3)
												.addComponent(
														numSharesTf,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel4)
												.addComponent(
														currencyCb,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel5)
												.addComponent(
														scaleCb,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(scaleHelpB))
								.addGap(28, 28, 28)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(saveB)
												.addComponent(cancelB))
								.addGap(29, 29, 29)));

		pack();
	}// </editor-fold>

	private void scaleHelpBActionPerformed(java.awt.event.ActionEvent evt) {
		JOptionPane.showMessageDialog(InvestmentEntryDialog.this,
				"London stock exchange trades in pence instead of pounds.\n"
						+ "Select the scale factor as 100 in that case",
				tr("Message"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void saveBActionPerformed(java.awt.event.ActionEvent evt) {
		save();
	}

	private void cancelBActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private String validateFields() {
		try {
			double num = Double.parseDouble(numSharesTf.getText());

			if (num <= 0)
				return tr("Number of shares cannot be less than zero");
		}
		catch (NumberFormatException e) {
			return tr("Invalid number for number of shares");
		}
		String symbol = symbolTf.getText();
		if (symbol == null || symbol.trim().length() == 0) {
			return tr("Symbol can not be blank");
		}
		return null;
	}

	public void save() {
		String msg = validateFields();
		if (msg != null) {
			JOptionPane.showMessageDialog(InvestmentEntryDialog.this, msg,
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
		else {
			saveB.setEnabled(false);
			SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {
				public ActionResponse doInBackground() throws Exception {
					Exchange market = (Exchange) marketCb.getSelectedItem();

					String scale = (String) scaleCb.getSelectedItem();
					if (scale == null || scale.equals("None"))
						scale = "1";

					ActionRequest req = new ActionRequest();

					PortfolioEntry pe = new PortfolioEntry();
					pe.setSymbol(symbolTf.getText());
					pe.setPortfolioId(portfolioId);
					pe.setNumShares(new BigDecimal(numSharesTf.getText()));
					pe.setMarket(market.code);
					pe.setCurrencyLocale(currencyCb.getText());
					pe.setSf(Integer.parseInt(scale));

					logger.info("Adding portfolio entry: " + pe + "*"
							+ pe.getSymbol() + ":" + pe.getNumShares());

					req.setProperty("ENTRY", pe);
					req.setProperty("MARKET", market.code);
					req.setUser(user);
					AddPortfolioEntryAction action = new AddPortfolioEntryAction();
					ActionResponse resp = action.executeAction(req);
					return resp;
				}

				public void done() {
					try {
						saveB.setEnabled(true);
						ActionResponse result = (ActionResponse) get();
						if (result.getErrorCode() == ActionResponse.NOERROR) {
							InvestmentEntryDialog.this.dispose();
							PortfolioEntry pe = (PortfolioEntry) result
									.getResult("ENTRY");
							pe.setChange("");
							pe.setDaysGain(new BigDecimal(0));
							parent.updatePortfolio(pe);
						}
						else {
							JOptionPane.showMessageDialog(
									InvestmentEntryDialog.this,
									result.getErrorMessage(), tr("Error"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
						return;
					}
				}
			};
			worker.execute();
		}
	}

	// Variables declaration - do not modify
	private javax.swing.JButton cancelB;
	private javax.swing.JLabel currencyCb;
	private javax.swing.JButton currencyHelpB;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JComboBox marketCb;
	private javax.swing.JTextField numSharesTf;
	private javax.swing.JButton saveB;
	private javax.swing.JComboBox scaleCb;
	private javax.swing.JButton scaleHelpB;
	private javax.swing.JTextField symbolTf;

	// End of variables declaration

	static class UpperCaseFilter extends DocumentFilter {
		public void insertString(DocumentFilter.FilterBypass fb, int offset,
				String text, AttributeSet attr) throws BadLocationException {
			fb.insertString(offset, text.toUpperCase(), attr);
		}

		public void replace(DocumentFilter.FilterBypass fb, int offset,
				int length, String text, AttributeSet attr)
				throws BadLocationException {
			fb.replace(offset, length, text.toUpperCase(), attr);
		}
	}

	private class Exchange implements Comparable<Exchange> {
		String code;
		String name;
		String extension;
		String locale;

		public Exchange() {
			code = "";
			name = "";
			extension = "";
			locale = "";
		}

		public String toString() {
			return name + " ( " + code + ")";
		}

		@Override
		public int compareTo(Exchange o) {
			return code.compareTo(o.code);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Exchange other = (Exchange) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (code == null) {
				if (other.code != null)
					return false;
			}
			else if (!code.equals(other.code))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			}
			else if (!name.equals(other.name))
				return false;
			return true;
		}

		private InvestmentEntryDialog getOuterType() {
			return InvestmentEntryDialog.this;
		}
	}
}
