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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.RetrieveQuoteAction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.graph.QuoteLineGraph;
import net.mjrz.fm.utils.Credentials;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ViewQuoteDialog extends JDialog {

	static final long serialVersionUID = 0L;

	private static final String[] labels = { "Open", "Market Cap", "High",
			"Low", "52Wk High", "52Wk Low", "Volume", "Average Vol" };

	private JLabel valueLabels[] = null;
	private JLabel updateLbl = null;
	private JLabel rangeDisplayLbl = null;

	private JLabel titleLabel;
	private JTextField symbolTf;
	private JPanel graphPanel;
	private JButton qb, prev, next;
	private QuoteLineGraph lg = null;

	private ArrayList<String> data;
	private ArrayList<String> dates;
	private ArrayList<Double> price;

	private static int FORWARD = 1;
	private static int BACK = 2;

	private int PAGE_SIZE = 15;

	private int lastdirection = -1;

	private FinanceManagerUI parent = null;

	private User user = null;

	private Range range = new Range();

	private String connType, proxyHost, proxyPort;

	private Credentials credentials;

	private NumberFormat numberFormat;

	private static Logger logger = Logger.getLogger(ViewQuoteDialog.class
			.getName());

	public ViewQuoteDialog() {
		initialize();
	}

	public ViewQuoteDialog(JFrame parent, User user) {
		super(parent, "Quote viewer", true);
		this.parent = (FinanceManagerUI) parent;
		this.user = user;

		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		valueLabels = new JLabel[labels.length];

		setLayout(new BorderLayout());

		graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setOpaque(false);
		top.setLayout(new GridLayout(2, 1));

		updateLbl = new JLabel("", JLabel.CENTER);
		updateLbl.setFont(new Font("Verdana", Font.PLAIN, 10));
		updateLbl.setForeground(Color.BLACK);

		rangeDisplayLbl = new JLabel("", JLabel.CENTER);
		rangeDisplayLbl.setFont(new Font("Verdana", Font.BOLD, 12));
		rangeDisplayLbl.setForeground(Color.BLACK);

		top.add(rangeDisplayLbl);
		top.add(updateLbl);

		graphPanel.add(top, BorderLayout.NORTH);

		graphPanel.setPreferredSize(new Dimension(800, 500));

		add(getDataPanel(), BorderLayout.NORTH);
		add(graphPanel, BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);

		numberFormat = NumberFormat.getNumberInstance();
		getCurrentSettings();
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

	public static final String getChangeString(String c1, String p2) {
		StringBuffer ret = new StringBuffer();

		c1 = MiscUtils.trimChars(c1.trim(), '"');
		p2 = MiscUtils.trimChars(p2.trim(), '"');

		if (c1.charAt(0) == '+') {
			ret.append("<font color=\"green\">" + c1 + "</font>");
			ret.append(" (");
			ret.append("<font color=\"green\">" + p2 + "</font>");
			ret.append(")");
		}
		else {
			ret.append("<font color=\"red\">" + c1 + "</font>");
			ret.append(" (");
			ret.append("<font color=\"red\">" + p2 + "</font>");
			ret.append(")");
		}
		return ret.toString();
	}

	private ActionResponse getQuote(String symbol) {
		try {
			ActionRequest req = new ActionRequest();
			req.setActionName("retrieveQuoteAction");
			req.setProperty("SYMBOL", symbol);

			RetrieveQuoteAction action = new RetrieveQuoteAction();
			ActionResponse resp = action.executeAction(req);
			if (resp.getErrorCode() == ActionResponse.NOERROR) {
				return resp;
			}
			return null;
		}
		catch (java.net.UnknownHostException e) {
			logger.error(MiscUtils.stackTrace2String(e));
			ActionResponse response = new ActionResponse();
			response.setErrorCode(ActionResponse.GENERAL_ERROR);
			response.setErrorMessage(tr("Server not found.\nPlease check network status"));
			return response;
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			ActionResponse response = new ActionResponse();
			response.setErrorCode(ActionResponse.GENERAL_ERROR);
			response.setErrorMessage(e.getMessage());
			return response;
		}
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		JButton pfAdd = new JButton("Add to my portfolio");
		pfAdd.addActionListener(new ButtonHandler());
		pfAdd.setEnabled(false);
		pfAdd.setVisible(false);

		prev = new JButton("<");
		prev.addActionListener(new ButtonHandler());
		prev.setVisible(false);

		next = new JButton(">");
		next.addActionListener(new ButtonHandler());
		next.setVisible(false);

		JPanel r2 = new JPanel();
		r2.setLayout(new BoxLayout(r2, BoxLayout.X_AXIS));

		r2.add(Box.createHorizontalGlue());
		r2.add(prev);
		r2.add(Box.createHorizontalGlue());
		r2.add(pfAdd);
		r2.add(Box.createHorizontalGlue());
		r2.add(next);
		r2.add(Box.createHorizontalGlue());

		JPanel r3 = new JPanel();
		r3.add(new JLabel(
				"<html><font color=\"red\"><b>Data retrieved from Yahoo! Finance. Quotes delayed 20 minutes</b></font></html>"));
		r3.add(Box.createHorizontalGlue());

		ret.add(r3, BorderLayout.SOUTH);
		ret.add(r2, BorderLayout.CENTER);

		ret.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void setData(ActionResponse response) {
		if (response != null
				&& response.getErrorCode() == ActionResponse.NOERROR) {
			data = (ArrayList<String>) response.getResult("QUOTEDATA");
			dates = (ArrayList<String>) response.getResult("HISTDATES");
			price = (ArrayList<Double>) response.getResult("HISTPRICE");

			if (dates == null || dates.size() == 0 || price == null
					|| price.size() == 0) {
				String msg = "Error retrieving quote data";
				logger.warn(msg);
				JOptionPane.showMessageDialog(this, msg, "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String name = net.mjrz.fm.utils.MiscUtils.trimChars(data.remove(0),
					'"');

			String change = getChangeString(data.remove(data.size() - 2),
					data.remove(data.size() - 1));

			StringBuffer html = new StringBuffer();
			html.append("<html><font color=\"black\" size=\"+1\">" + name
					+ "&nbsp;&nbsp;" + data.remove(0) + "</font>&nbsp;&nbsp;"
					+ change + "</html>");

			titleLabel.setText(html.toString());

			next.setVisible(true);
			prev.setVisible(true);

			for (int i = 0; i < data.size(); i++) {
				if (i == 6 || i == 7) {
					try {
						String num = numberFormat.format(Double
								.parseDouble(data.get(i)));
						valueLabels[i].setText(num);
					}
					catch (Exception e) {
						valueLabels[i].setText(data.get(i));
					}
				}
				else {
					valueLabels[i].setText(data.get(i));
				}
			}

			if (dates != null && price != null) {
				java.util.List<String> xlist = new ArrayList<String>();
				java.util.List<Double> ylist = new ArrayList<Double>();

				for (int i = 0; i < PAGE_SIZE; i++) {
					xlist.add(0, dates.get(i));
					ylist.add(0, price.get(i));
				}

				xlist.add(0, "");
				ylist.add(0, new Double(0));

				if (lg == null) {
					lg = new QuoteLineGraph(xlist, ylist, 400, true);
					lg.setTrackXValue(true);
					lg.setTrackYValue(true);

					lg.setPreferredSize(new Dimension(graphPanel.getWidth(),
							graphPanel.getHeight()));

					lg.setBorder(BorderFactory.createLineBorder(Color.GRAY));
					graphPanel.add(lg, BorderLayout.CENTER);

					lg.setUpdateLabel(updateLbl);

					rangeDisplayLbl.setText(xlist.get(1) + " - "
							+ xlist.get(xlist.size() - 1) + "   ");

					range.setE(0);
					range.setS(PAGE_SIZE);
				}
				else {
					range.reset();
					range.setS(PAGE_SIZE);

					rangeDisplayLbl.setText(xlist.get(1) + " - "
							+ xlist.get(xlist.size() - 1) + "   ");
					lg.updatePoints(xlist, ylist, 400);
					graphPanel.updateUI();
				}
			}
			else {
				range.reset();

				ArrayList<String> xp = new ArrayList<String>();
				xp.add("");
				ArrayList<Double> yp = new ArrayList<Double>();
				yp.add(new Double(0));

				lg.updatePoints(xp, yp, 400);
				graphPanel.updateUI();
				next.setVisible(false);
				prev.setVisible(false);
			}
		}
		else {
			next.setVisible(false);
			prev.setVisible(false);

			range.reset();
			String msg = "Unknown error";
			if (response != null && response.getErrorMessage() != null) {
				msg = response.getErrorMessage().trim();
			}
			JOptionPane.showMessageDialog(this, msg, "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private JPanel getDataPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		JPanel title = new JPanel();
		title.setLayout(new BorderLayout());

		titleLabel = new JLabel();

		title.add(titleLabel, BorderLayout.WEST);

		JPanel r1 = new JPanel();
		r1.setLayout(new BoxLayout(r1, BoxLayout.X_AXIS));
		r1.add(new JLabel("Symbol"));
		r1.add(Box.createHorizontalStrut(10));

		symbolTf = new JTextField(10);
		net.mjrz.fm.ui.utils.GuiUtilities.removeCustomMouseListener(symbolTf);
		((AbstractDocument) symbolTf.getDocument())
				.setDocumentFilter(new DocumentFilter() {
					public void insertString(DocumentFilter.FilterBypass fb,
							int offset, String text, AttributeSet attr)
							throws BadLocationException {
						fb.insertString(offset, text.toUpperCase(), attr);
					}

					public void replace(DocumentFilter.FilterBypass fb,
							int offset, int length, String text,
							AttributeSet attr) throws BadLocationException {
						fb.replace(offset, length, text.toUpperCase(), attr);
					}
				});
		symbolTf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					getAuth();
				}
			}
		});
		r1.add(symbolTf);

		r1.add(Box.createHorizontalStrut(10));

		qb = new JButton("Get quotes");
		qb.setPreferredSize(new Dimension(100, 20));
		qb.addActionListener(new ButtonHandler());
		r1.add(qb);

		title.add(r1, BorderLayout.EAST);

		JPanel empty = new JPanel();
		empty.add(new JSeparator(SwingConstants.HORIZONTAL));

		title.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);

		JPanel body = new JPanel();
		body.setLayout(new GridLayout(4, 2, 25, 2));

		for (int i = 0; i < labels.length; i++) {
			JLabel l = new JLabel(labels[i], JLabel.LEADING);
			l.setPreferredSize(new Dimension(100, 15));

			JLabel t = new JLabel("", JLabel.LEADING);
			t.setPreferredSize(new Dimension(100, 15));
			t.setBorder(BorderFactory.createLineBorder(QuoteLineGraph.gcolor));
			valueLabels[i] = t;

			body.add(l);
			body.add(t);
		}

		ret.add(title, BorderLayout.NORTH);
		ret.add(body, BorderLayout.CENTER);

		ret.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.GRAY), "Details"));
		return ret;
	}

	private void updateGraph(int direction) {
		if (dates == null || dates.size() == 0) {
			return;
		}
		java.util.List<String> xlist = new ArrayList<String>();
		java.util.List<Double> ylist = new ArrayList<Double>();
		try {
			if (direction == FORWARD) {
				range.updateRange(PAGE_SIZE, FORWARD);
				int dsz = dates.size();
				for (int i = range.getE(); i < range.getS(); i++) {
					if (i >= 0 && i < dsz) {
						xlist.add(0, dates.get(i));
						ylist.add(0, price.get(i));
					}
				}
			}
			if (direction == BACK) {
				range.updateRange(PAGE_SIZE, BACK);
				int dsz = dates.size();
				for (int i = range.getE(); i < range.getS(); i++) {
					if (i >= 0 && i < dsz) {
						xlist.add(0, dates.get(i));
						ylist.add(0, price.get(i));
					}
				}
			}
			if (xlist.size() > 0 && ylist.size() > 0) {
				rangeDisplayLbl.setText(xlist.get(0) + " to "
						+ xlist.get(xlist.size() - 1));
				xlist.add(0, "");
				ylist.add(0, new Double(0));

				lg.updatePoints(xlist, ylist, 400);
				lg.updateUI();
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return;
		}
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("<")) {
				updateGraph(FORWARD);
				return;
			}
			if (cmd.equals(">")) {
				updateGraph(BACK);
				return;
			}
			if (cmd.equals("Get quotes")) {
				getAuth();
				return;
			}
			if (cmd.equals("Add to my portfolio")) {
				return;
			}
		}
	}

	private void getAuth() {
		getQuotes();

		// connType = ZProperties.getProperty("CONNECTION.TYPE");
		// proxyHost = ZProperties.getProperty("PROXY.HOST");
		// proxyPort = ZProperties.getProperty("PROXY.PORT");
		// String user = ZProperties.getProperty("PROXY.USER");
		// String pass = ZProperties.getProperty("PROXY.PASSWORD");
		//
		// if(proxyHost != null && proxyHost.length() > 0) {
		// if(credentials != null) {
		// getQuotes();
		// }
		// if( (user == null || pass == null) && credentials == null) {
		// credentials = new Credentials();
		// AuthenticationDialog.getCredentials(ViewQuoteDialog.this,
		// credentials);
		// }
		// }
		// else {
		// getQuotes();
		// }
	}

	void getQuotes() {
		final String symbol = symbolTf.getText();
		if (symbol == null || symbol.length() == 0)
			return;
		qb.setEnabled(false);
		SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {
			ActionResponse response = null;

			public ActionResponse doInBackground() throws Exception {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					response = getQuote(symbol.trim());
					return response;
				}
				finally {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void done() {
				try {
					qb.setEnabled(true);
					setData(response);
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
				}
				finally {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		};
		worker.execute();
	}

	class Range {
		int s;
		int e;

		Range() {
			s = 0;
			e = 0;
		}

		void setS(int s) {
			this.s = s;
		}

		void setE(int e) {
			this.e = e;
		}

		public void updateRange(int size, int direction) {
			if (direction == ViewQuoteDialog.FORWARD) {
				if (s + size >= dates.size() || e + size >= dates.size())
					return;

				s += size;
				e += size;
			}
			if (direction == ViewQuoteDialog.BACK) {
				if (s - size < 0 || e - size < 0)
					return;

				s -= size;
				e -= size;
			}
		}

		public void reset() {
			s = 0;
			e = 0;
		}

		public int getS() {
			return s;
		}

		public int getE() {
			return e;
		}
	}

	private void getCurrentSettings() {
		try {
			proxyHost = ZProperties.getProperty("PROXY.HOST");
			proxyPort = ZProperties.getProperty("PROXY.PORT");
			if (proxyHost == null)
				connType = "direct";
			else
				connType = "proxy";
		}
		catch (Exception e) {
			Logger.getLogger(getClass()).error(e);
			return;
		}
	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		ViewQuoteDialog f = new ViewQuoteDialog();
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setTitle("Quote Viewer");
		// f.add(new ViewQuoteDialog());
		f.setSize(400, 400);
		f.pack();
		f.setVisible(true);
	}
}
