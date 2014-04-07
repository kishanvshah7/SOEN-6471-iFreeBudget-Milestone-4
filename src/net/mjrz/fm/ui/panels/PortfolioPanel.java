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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetPortfolioValueAction;
import net.mjrz.fm.actions.RemovePortfolioEntryAction;
import net.mjrz.fm.actions.RetrieveQuoteAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Portfolio;
import net.mjrz.fm.entity.beans.PortfolioEntry;
import net.mjrz.fm.entity.beans.Prefs;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.dialogs.InvestmentEntryDialog;
import net.mjrz.fm.ui.graph.StockHistoryLineGraph;
import net.mjrz.fm.ui.panels.portfolio.StockDetailsPanel;
import net.mjrz.fm.ui.utils.PortfolioTableModel;
import net.mjrz.fm.ui.utils.SortableTableModel;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.TransactionTableModel;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;
import net.mjrz.fm.utils.schedule.PortfolioUpdater;
import net.mjrz.scheduler.Scheduler;
import net.mjrz.scheduler.task.BasicSchedule;
import net.mjrz.scheduler.task.Schedule;

import org.apache.log4j.Logger;

public final class PortfolioPanel extends JFrame {
	private static final long serialVersionUID = 1L;

	private JTable portfolioTable;
	private JFrame parent;
	private User user;
	private FManEntityManager em;
	private PortfolioTableModel tableModel;
	private TableRowSorter<TableModel> rowSorter;
	private JLabel statusLbl;
	private JLabel msgLbl;
	private StockHistoryLineGraph lg;
	private JPanel detailsPanel, graphPanel;
	private JButton details, edit, add, addPortfolio;
	private JLabel graphUpdateLbl;
	private JComboBox portfolioList;
	private StockDetailsPanel stockDetailsPanel;

	private java.util.List<Portfolio> portfolios;

	private HashMap<String, SoftReference<QuoteRetrieveValue>> portfolioDetails = null;

	private static Logger logger = Logger.getLogger(PortfolioPanel.class
			.getName());

	private static final int FETCH_WAIT_TIME = 60 * 1000;

	private static PortfolioPanel instance = null;

	public synchronized static PortfolioPanel getInstance(JFrame parent,
			User user) {
		if (instance == null) {
			instance = new PortfolioPanel(parent, user);
		}
		try {
			PortfolioUpdater updater = new PortfolioUpdater(
					"Portfolio Updater", instance, user);
			Calendar end = Calendar.getInstance();
			end.add(Calendar.DATE, 1);

			Schedule sch = new BasicSchedule(new Date(), end.getTime());
			sch.setRepeatType(Schedule.RepeatType.MINUTE, 1);
			updater.setSchedule(sch);

			Scheduler.scheduleTask(updater);

			String defaultSort = ZProperties
					.getProperty("UIPrefs.PortfolioManager.DefaultSortCol");
			if (defaultSort != null) {
				instance.setDefaultSort(defaultSort);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		return instance;
	}

	public synchronized static void disposeInstance() {
		if (instance == null)
			return;

		instance.dispose();
		instance = null;
	}

	private PortfolioPanel() {

	}

	private synchronized void setDefaultSort(String col) {
		try {
			String[] split = col.split("\\^");

			int colInt = Integer.parseInt(split[0]);
			int colDir = Integer.parseInt(split[1]);

			java.util.List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

			if (colDir == SortableTableModel.ASC) {
				sortKeys.add(new RowSorter.SortKey(colInt, SortOrder.ASCENDING));
			}
			else if (colDir == SortableTableModel.DESC) {
				sortKeys.add(new RowSorter.SortKey(colInt, SortOrder.DESCENDING));
			}
			else {
				sortKeys.add(new RowSorter.SortKey(colInt, SortOrder.UNSORTED));

			}

			rowSorter.setSortKeys(sortKeys);

			tableModel.setSortInfo(colInt, colDir);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private PortfolioPanel(JFrame parent, User user) {
		super(UIDefaults.PORTFOLIO_MANAGER_TITLE);
		this.parent = parent;
		this.user = user;
		this.portfolioDetails = new HashMap<String, SoftReference<QuoteRetrieveValue>>();
		em = new FManEntityManager();

		initialize();
	}

	private void initialize() {
		loadPortfolioList();
		setLayout(new BorderLayout());

		add(getToolBarPanel(), BorderLayout.NORTH);

		final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				getListPanel(), new JScrollPane(getDetailsPanel()));
		sp.setDividerSize(10);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sp.setDividerLocation(0.7D);
			}
		});

		add(sp, BorderLayout.CENTER);
		add(getSouthPanel(), BorderLayout.SOUTH);

		setPreferredSize(new Dimension(parent.getWidth() - 10,
				parent.getHeight() - 10));
		super.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				PortfolioPanel.class.getClassLoader().getResource(
						"icons/icon_money.png")));
	}

	private JPanel getSouthPanel() {
		statusLbl = new JLabel();
		msgLbl = new JLabel();
		msgLbl.setHorizontalAlignment(JLabel.TRAILING);

		JPanel south = new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));

		south.add(statusLbl);
		south.add(Box.createHorizontalGlue());
		south.add(msgLbl);
		return south;
	}

	@SuppressWarnings("serial")
	private JPanel getListPanel() {
		loadPortfolio();
		portfolioTable = new JTable(tableModel) {
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
		portfolioTable.setSelectionForeground(Color.BLACK);
		portfolioTable.setGridColor(new Color(154, 191, 192));
		portfolioTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
		portfolioTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		portfolioTable.getTableHeader()
				.addMouseListener(new SortInfoListener());
		portfolioTable.getTableHeader().setReorderingAllowed(false);
		portfolioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		portfolioTable.getColumnModel().getColumn(2)
				.setCellRenderer(new PriceColumnRenderer());
		portfolioTable.getColumnModel().getColumn(3)
				.setCellRenderer(new CurrencyColumnRenderer());
		portfolioTable.getColumnModel().getColumn(4)
				.setCellRenderer(new GainColumnRenderer());
		portfolioTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						try {
							if (e.getValueIsAdjusting()) {
								return;
							}
							int row = portfolioTable.getSelectedRow();
							if(row < 0) {
								return;
							}
							row = portfolioTable.convertRowIndexToModel(row);
							PortfolioEntry pe = em.getPortfolioEntry(tableModel
									.getRowId(row));
							getDetails(pe.getSymbol());
						}
						catch (Exception ex) {
							logger.error(MiscUtils.stackTrace2String(ex));
						}
					}
				});
		portfolioTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					int row = portfolioTable.rowAtPoint(e.getPoint());
					portfolioTable.getSelectionModel().setSelectionInterval(
							row, row);
					showPopupMenu(e.getX(), e.getY());
				}
			}
		});

		portfolioTable.setRowHeight(30);

		setupRowSorter();

		// portfolioTable.setBorder(BorderFactory.createLineBorder(Color.black));

		JPanel center = new JPanel(new GridLayout(1, 1));

		center.add(new JScrollPane(portfolioTable));

		center.setBorder(BorderFactory.createTitledBorder("Portfolio")); //$NON-NLS-1$

		return center;
	}

	@SuppressWarnings({ "rawtypes" })
	private void loadPortfolio() {
		if (tableModel == null)
			tableModel = new PortfolioTableModel();
		else
			tableModel.setRowCount(0);

		java.util.List entries = getEntriesList();

		if (entries == null)
			return;

		int sz = entries.size();

		for (int i = 0; i < sz; i++) {
			PortfolioEntry c = (PortfolioEntry) entries.get(i);
			Object[] row = { c.getId(), c.getName(), c.getNumShares(),
					c.getPricePerShare(), c.getMarketValue(), c.getDaysGain(),
					c.getChange(), c.getCurrencyLocale(), c.getSf() };

			tableModel.insertRow(i, row);
		}

		updatePortfolioData();
	}

	@SuppressWarnings("unchecked")
	private void loadPortfolioList() {
		try {
			portfolios = (java.util.List<Portfolio>) em.getPortfolio(user
					.getUid());
			if (portfolios == null)
				portfolios = new ArrayList<Portfolio>();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private java.util.List getEntriesList() {
		if (portfolioList.getItemCount() <= 0)
			return new ArrayList();

		try {
			Portfolio p = (Portfolio) portfolioList.getSelectedItem();
			java.util.List contacts = em.getPortfolioEntries(p.getId());

			return contacts;
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
	}

	private void executeSale() throws Exception {
		final String s = (String) JOptionPane
				.showInputDialog(tr("Number of shares to sell") + ":");
		if (s == null)
			return;

		int sel = portfolioTable.getSelectedRow();
		if (sel < 0) {
			showGenericErrorDialog(tr("Please select a row first"));
			return;
		}
		final int modelRow = portfolioTable.convertRowIndexToModel(sel);

		SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {
			@Override
			protected ActionResponse doInBackground() throws Exception {
				ActionResponse response = new ActionResponse();
				ActionRequest req = new ActionRequest();
				req.setActionName("removePortfolioEntryAction");

				try {
					final BigDecimal numToSell = new BigDecimal(s);

					long id = tableModel.getRowId(modelRow);

					PortfolioEntry entry = em.getPortfolioEntry(id);
					req.setProperty("ENTRY", entry);
					req.setProperty("NUMTOSELL", numToSell);

					RemovePortfolioEntryAction action = new RemovePortfolioEntryAction();

					response = action.executeAction(req);
				}
				catch (NumberFormatException e) {
					logger.error(MiscUtils.stackTrace2String(e));
					response.setErrorCode(ActionResponse.GENERAL_ERROR);
					response.setErrorMessage(tr("Invalid number for number of shares to sell"));
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
					response.setErrorCode(ActionResponse.GENERAL_ERROR);
					response.setErrorMessage(e.getMessage());
					throw e;
				}
				return response;
			}

			public void done() {
				try {
					ActionResponse response = get();
					if (response != null && !response.hasErrors()) {
						PortfolioEntry pe = (PortfolioEntry) response
								.getResult("ENTRY");
						if (pe.getNumShares().intValue() > 0) {
							tableModel.updateEntry(pe, true);
						}
						else {
							tableModel.removeRow(modelRow);
						}
						updatePortfolioData();
					}
					else {
						showGenericErrorDialog((response == null) ? tr("Unexpected exception occured, response was null")
								: response.getErrorMessage());
					}
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
				}
			}
		};
		worker.execute();
	}

	private void addPortfolio() throws Exception {
		String s = (String) JOptionPane.showInputDialog("Name");
		if (s == null)
			return;
		s = s.trim();
		if (s.length() == 0)
			return;

		boolean valid = true;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c)) {
				valid = false;
				break;
			}
		}

		if (!valid) {
			showGenericErrorDialog(tr("Only letters or numbers allowed"));
		}
		else {
			Portfolio p = new Portfolio();
			p.setPortfolioName(s.trim());
			p.setUid(user.getUid());
			p.setCreateDate(new Date());
			long id = em.addPortfolio(p);
			if (id != 0) {
				int sz = portfolioList.getItemCount();
				portfolioList.insertItemAt(p, sz);
			}
		}
	}

	public void showPortfolioEntryDialog() {
		if (portfolioList.getSelectedIndex() < 0)
			return;

		Portfolio p = (Portfolio) portfolioList.getSelectedItem();
		InvestmentEntryDialog d = new InvestmentEntryDialog(this, user,
				p.getId());
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void updatePortfolio(PortfolioEntry pe) {
		tableModel.updateEntry(pe, true);
		updatePortfolioData();
	}

	public void updatePortfolio(PortfolioEntry pe, boolean insertNew) {
		tableModel.updateEntry(pe, insertNew);
		updatePortfolioData();
	}

	public void removePortfolioEntry(PortfolioEntry pe) {
		int rowId = tableModel.getRowIdForPortfolioId(pe.getId());
		tableModel.removeRow(rowId);
	}

	public void updateMsg(String msg, boolean start) {
		msgLbl.setText(msg);
	}

	private JPanel getDetailsPanel() {
		detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(2, 1, 2, 2));

		graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.setBackground(Color.WHITE);
		graphPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		stockDetailsPanel = new StockDetailsPanel();
		detailsPanel.add(stockDetailsPanel);
		detailsPanel.add(graphPanel);

		detailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));

		return detailsPanel;
	}

	private JPanel getToolBarPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());

		add = new JButton(
				tr("Add symbol"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/buy.png")); //$NON-NLS-1$ //$NON-NLS-2$
		add.setMnemonic(KeyEvent.VK_B);
		add.setActionCommand("Buy");
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPortfolioEntryDialog();
			}
		});

		edit = new JButton(
				tr("Sell"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/sell.png")); //$NON-NLS-1$ //$NON-NLS-2$
		edit.setActionCommand("Sell");
		edit.setMnemonic(KeyEvent.VK_S);
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					executeSale();
				}
				catch (Exception ex) {
					logger.error(MiscUtils.stackTrace2String(ex));
				}
			}
		});

		details = new JButton(
				tr("Details"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/details.png")); //$NON-NLS-1$ //$NON-NLS-2$
		details.setActionCommand("Details");
		details.setMnemonic(KeyEvent.VK_D);
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});

		addPortfolio = new JButton(tr("New portfolio"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/addpf.png"));
		addPortfolio.setActionCommand("Details");
		addPortfolio.setMnemonic(KeyEvent.VK_P);
		addPortfolio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addPortfolio();
				}
				catch (Exception ex) {
					logger.error(MiscUtils.stackTrace2String(ex));
				}
			}
		});

		JButton cancel = new JButton(
				tr("Close"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/cancel.png")); //$NON-NLS-1$ //$NON-NLS-2$
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		portfolioList = new JComboBox(portfolios.toArray());
		portfolioList.setPreferredSize(new Dimension(25, 20));
		portfolioList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					loadPortfolio();
					if (portfolioTable == null)
						return;
					portfolioTable.getColumnModel().getColumn(2)
							.setCellRenderer(new PriceColumnRenderer());
					portfolioTable.getColumnModel().getColumn(3)
							.setCellRenderer(new CurrencyColumnRenderer());
					portfolioTable.getColumnModel().getColumn(4)
							.setCellRenderer(new GainColumnRenderer());
				}
				catch (Exception ex) {
					logger.error(MiscUtils.stackTrace2String(ex));
				}
			}
		});
		if (portfolioList.getItemCount() > 0) {
			portfolioList.setSelectedIndex(0);
		}

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.LINE_START;
		gbc1.insets = new Insets(0, 10, 0, 3);
		gbc1.weighty = 0.5;
		gbc1.weightx = 0;
		gbc1.ipadx = 200;
		gbc1.ipady = 3;
		ret.add(portfolioList, gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 0;
		gbc1.gridheight = 2;
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.LINE_START;
		gbc1.insets = new Insets(5, 10, 5, 3);
		gbc1.weighty = 0;
		gbc1.weightx = 0;
		gbc1.ipadx = 0;
		ret.add(addPortfolio, gbc1);

		gbc1.gridx = 2;
		gbc1.gridy = 0;
		gbc1.gridheight = 2;
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.LINE_START;
		gbc1.insets = new Insets(5, 10, 5, 3);
		gbc1.weighty = 0;
		gbc1.weightx = 0;
		gbc1.ipadx = 0;
		ret.add(add, gbc1);

		// gbc1.gridx = 3;
		// gbc1.gridy = 0;
		// gbc1.gridheight = 2;
		// gbc1.fill = GridBagConstraints.NONE;
		// gbc1.anchor = GridBagConstraints.LINE_START;
		// gbc1.insets = new Insets(5, 10, 5, 3);
		// gbc1.weighty = 0;
		// gbc1.weightx = 0;
		// ret.add(cancel, gbc1);

		gbc1.gridx = 3;
		gbc1.gridy = 0;
		gbc1.gridheight = 2;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.LINE_START;
		gbc1.insets = new Insets(5, 10, 5, 3);
		gbc1.weighty = 0;
		gbc1.weightx = 1;
		ret.add(Box.createHorizontalStrut(10), gbc1);

		return ret;
	}

	private void getDetails(final String symbol) {
		try {
			SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {

				@Override
				protected ActionResponse doInBackground() throws Exception {
					logger.info("Size of details cache (before retrieve): "
							+ portfolioDetails.size());
					ActionResponse response = null;

					SoftReference<QuoteRetrieveValue> val = portfolioDetails
							.get(symbol);
					if (val == null || val.get() == null) {
						response = getData(symbol);
					}
					else {
						long last = val.get().lastFetch;
						if (System.currentTimeMillis() - last > (FETCH_WAIT_TIME)) {
							logger.info("Time expired... retrieving agin");
							response = getData(symbol);
						}
						else {
							response = val.get().response;
						}
					}
					return response;
				}

				@Override
				protected void done() {
					try {
						ActionResponse response = get();
						if (response.getErrorCode() != ActionResponse.NOERROR) {
							showGenericErrorDialog(response.getErrorMessage());
							logger.error("Error getting quote details: "
									+ response.getErrorMessage());
							return;
						}
						String symbol = (String) response.getResult("SYMBOL");
						QuoteRetrieveValue v = new QuoteRetrieveValue();
						v.lastFetch = System.currentTimeMillis();
						v.response = response;
						portfolioDetails.put(symbol,
								new SoftReference<QuoteRetrieveValue>(v));
						logger.info("Size of details cache (after adding): "
								+ portfolioDetails.size());
						updateDetailsPanel(response);
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
					}
				}

			};
			worker.execute();
		}
		catch (Exception ex) {
			logger.error(MiscUtils.stackTrace2String(ex));
		}
	}

	@SuppressWarnings("unchecked")
	private void updateDetailsPanel(ActionResponse response) {
		ArrayList<String> data = (ArrayList<String>) response
				.getResult("QUOTEDATA");
		ArrayList<String> dates = (ArrayList<String>) response
				.getResult("HISTDATES");
		ArrayList<BigDecimal> price = (ArrayList<BigDecimal>) response
				.getResult("HISTPRICE");

		cleanupGraphPanel();
		if (data == null || dates == null || price == null)
			return;

		java.util.Collections.reverse(dates);
		java.util.Collections.reverse(price);

		lg = new StockHistoryLineGraph(dates, price, 270);
		graphPanel.add(lg, BorderLayout.CENTER);
		graphPanel.validate();

		if (data != null && data.size() > 0) {
			stockDetailsPanel.setData(data);
		}

	}

	private void cleanupGraphPanel() {
		if (lg == null)
			return;

		if (graphUpdateLbl != null)
			graphUpdateLbl.setText("");

		MouseMotionListener[] list = lg.getMouseMotionListeners();
		if (list != null) {
			for (MouseMotionListener ml : list) {
				lg.removeMouseMotionListener(ml);
			}
		}
		MouseListener[] mlist = lg.getMouseListeners();
		if (mlist != null) {
			for (MouseListener ml : mlist) {
				lg.removeMouseListener(ml);
			}
		}
		graphPanel.remove(lg);
	}

	private ActionResponse getData(String symbol) {
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

	private void updatePortfolioData() {
		if (portfolioList.getSelectedIndex() < 0)
			return;

		Portfolio p = (Portfolio) portfolioList.getSelectedItem();

		ActionRequest req = new ActionRequest();
		req.setActionName("getPortfolioValueAction");
		req.setProperty("PORTFOLIOID", p.getId());

		GetPortfolioValueAction action = new GetPortfolioValueAction();
		try {
			ActionResponse resp = action.executeAction(req);
			if (resp.getErrorCode() == ActionResponse.NOERROR) {
				String val = (String) resp.getResult("PVALUE");
				if(statusLbl != null) {
					statusLbl.setText(val);
				}
			}
			else {
				logger.error("Unable to calculate portfolio value"
						+ resp.getErrorMessage());
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			statusLbl.setText("");
		}
	}

	private void setupRowSorter() {
		rowSorter = new TableRowSorter<TableModel>(portfolioTable.getModel());
		rowSorter.setComparator(4, bigDecimalComparator);
		rowSorter.setComparator(3, bigDecimalComparator);
		rowSorter.setComparator(2, bigDecimalComparator);
		rowSorter.setComparator(1, bigDecimalComparator);
		rowSorter.setComparator(0, stringComparator);
		portfolioTable.setRowSorter(rowSorter);
	}

	private transient Comparator<BigDecimal> bigDecimalComparator = new Comparator<BigDecimal>() {
		public int compare(BigDecimal s1, BigDecimal s2) {
			return s1.compareTo(s2);
		}
	};

	private transient Comparator<String> stringComparator = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return s1.compareTo(s2);
		}
	};

	private void showGenericErrorDialog(String msg) {
		JOptionPane.showMessageDialog(this, msg,
				tr("Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	public void dispose() {
		try {
			saveUIPrefs();
			portfolioDetails.clear();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			super.dispose();
		}
	}

	private void showPopupMenu(int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();

		JMenuItem mItem = new JMenuItem(
				Messages.getString("Buy"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/buy.png")); //$NON-NLS-2$
		mItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPortfolioEntryDialog();
			}
		});

		popup.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Sell"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/sell.png")); //$NON-NLS-2$
		mItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					executeSale();
				}
				catch (Exception ex) {
					logger.error(MiscUtils.stackTrace2String(ex));
				}
			}
		});
		popup.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Details"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/details.png")); //$NON-NLS-2$
		mItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		popup.add(mItem);

		popup.pack();
		popup.show(portfolioTable, x, y);
	}

	private void saveUIPrefs() throws Exception {
		FManEntityManager entityManager = new FManEntityManager();

		String propName = "UIPrefs.PortfolioManager.DefaultSortCol";
		int i = 0;
		int dir = SortableTableModel.NONE;
		int colCount = tableModel.getColumnCount();
		for (; i < colCount; i++) {
			int sortInfo = tableModel.getSortInfo(i);
			if (sortInfo != SortableTableModel.NONE) {
				dir = sortInfo;
				break;
			}
		}
		if (i == colCount)
			i = 0;

		String propVal = String.valueOf(i) + "^" + String.valueOf(dir);
		if (propVal != null) {
			Prefs p = (Prefs) entityManager.getObject("Prefs", " propName = '"
					+ propName + "'");
			if (p != null) {
				p.setPropValue(propVal);
				entityManager.updateObject("Prefs", p);
			}
			else {
				p = new Prefs();
				p.setUid(SessionManager.getSessionUserId());
				p.setPropName(propName);
				p.setPropValue(propVal);
				entityManager.addObject("Prefs", p);
			}
			ZProperties.replaceRuntimeProperty(propName, propVal);
		}
	}

	/* Inner classes */

	@SuppressWarnings("serial")
	public class PriceColumnRenderer extends JLabel implements
			TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int vColIndex) {
			this.setOpaque(true);
			if (rowIndex % 2 == 0) {
				setBackground(new Color(234, 234, 234));
			}
			else {
				setBackground(Color.WHITE);
			}
			if (value == null) {
				setText("");
				setIcon(null);
				return this;
			}

			int modelIndex = portfolioTable.convertRowIndexToModel(rowIndex);
			BigDecimal valueBd = (BigDecimal) value;

			StringBuffer lblValue = new StringBuffer();
			// lblValue.append(numFormat.format(valueBd.doubleValue()));
			lblValue.append(valueBd.doubleValue());
			Object change = tableModel.getChangeValue(modelIndex);
			setIconTextGap(5);
			if (change != null) {
				String s = (String) change;
				if (s.length() >= 2) {
					if (s.charAt(0) == '+') {
						setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
								"icons/arrow_up.png"));
					}
					else if (s.charAt(0) == '-') {
						setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
								"icons/arrow_down.png"));
					}
					else {
						setIcon(null);
					}

					lblValue.append(" (");
					lblValue.append(s);
					lblValue.append(")");
				}
			}
			setText(lblValue.toString());

			if (!isSelected) {
				java.awt.Font f = getFont();
				setFont(new java.awt.Font(f.getName(), java.awt.Font.PLAIN,
						f.getSize()));
				setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
				setBorder(BorderFactory.createEmptyBorder());
			}
			return this;
		}
	}

	@SuppressWarnings("serial")
	public class GainColumnRenderer extends JLabel implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int vColIndex) {
			this.setOpaque(true);
			if (rowIndex % 2 == 0) {
				setBackground(new Color(234, 234, 234));
			}
			else {
				setBackground(Color.WHITE);
			}
			if (value == null) {
				setText("");
				setIcon(null);
				return this;
			}
			BigDecimal valueBd = (BigDecimal) value;
			if (valueBd.compareTo(new BigDecimal(0)) < 0) {
				setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
						"icons/arrow_down.png"));
			}
			else {
				setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
						"icons/arrow_up.png"));
			}

			int sel = table.convertRowIndexToModel(rowIndex);
			Locale loc = Locale.US;

			Object[] row = tableModel.getRowAt(sel);
			if (row.length == 9) {
				Object currency = row[7];
				if (currency != null) {
					if (currency.equals("GBP")) {
						loc = Locale.UK;
					}
					else if (currency.equals("EUR")) {
						loc = Locale.GERMAN;
					}
					else if (currency.equals("Rupee")) {
						loc = new Locale("in", "IN");
					}
				}
			}
			NumberFormat nf = NumberFormat.getCurrencyInstance(loc);
			setText(nf.format(valueBd.doubleValue()));
			//
			//
			// setText(numFormat.format(valueBd.doubleValue()));
			if (!isSelected) {
				java.awt.Font f = getFont();
				setFont(new java.awt.Font(f.getName(), java.awt.Font.PLAIN,
						f.getSize()));
				setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
				setBorder(BorderFactory.createEmptyBorder());
			}

			return this;
		}
	}

	@SuppressWarnings("serial")
	public class CurrencyColumnRenderer extends JLabel implements
			TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int vColIndex) {
			this.setOpaque(true);
			if (rowIndex % 2 == 0) {
				setBackground(new Color(234, 234, 234));
			}
			else {
				setBackground(Color.WHITE);
			}
			if (value == null) {
				setText("");
				setIcon(null);
				return this;
			}

			BigDecimal valueBd = (BigDecimal) value;

			int sel = table.convertRowIndexToModel(rowIndex);
			Locale loc = Locale.US;

			Object[] row = tableModel.getRowAt(sel);
			if (row.length == 9) {
				Object currency = row[7];
				if (currency != null) {
					if (currency.equals("GBP")) {
						loc = Locale.UK;
					}
					else if (currency.equals("EUR")) {
						loc = Locale.GERMAN;
					}
					else if (currency.equals("Rupee")) {
						loc = new Locale("in", "IN");
					}
				}
			}
			NumberFormat nf = NumberFormat.getCurrencyInstance(loc);
			setText(nf.format(valueBd.doubleValue()));

			if (!isSelected) {
				java.awt.Font f = getFont();
				setFont(new java.awt.Font(f.getName(), java.awt.Font.PLAIN,
						f.getSize()));
				setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
				setBorder(BorderFactory.createEmptyBorder());
			}
			return this;
		}
	}

	class SortInfoListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			int col = portfolioTable.columnAtPoint(e.getPoint());

			int sortInfo = tableModel.getSortInfo(col);
			int newSortInfo = SortableTableModel.NONE;

			switch (sortInfo) {
			case TransactionTableModel.NONE:
				newSortInfo = SortableTableModel.ASC;
				break;
			case TransactionTableModel.ASC:
				newSortInfo = SortableTableModel.DESC;
				break;
			case TransactionTableModel.DESC:
				newSortInfo = SortableTableModel.ASC;
				break;
			}
			tableModel.setSortInfo(col, newSortInfo);
		}
	}

	class QuoteRetrieveValue {
		long lastFetch;
		ActionResponse response;
	}
}
