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
package net.mjrz.fm.ui;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumnModel;

import net.mjrz.fm.Main;
import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.DeleteTransactionAction;
import net.mjrz.fm.actions.ExecuteFilterAction;
import net.mjrz.fm.actions.GetEarningsReportAction;
import net.mjrz.fm.actions.GetExpReportAction;
import net.mjrz.fm.actions.GetNetWorthHistoryAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.Attachment;
import net.mjrz.fm.entity.beans.AttachmentRef;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.TxDecorator;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.search.newfilter.Filter;
import net.mjrz.fm.search.newfilter.Order;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.dialogs.AboutDialog;
import net.mjrz.fm.ui.dialogs.AddContactDialog;
import net.mjrz.fm.ui.dialogs.BalanceGraphDialog;
import net.mjrz.fm.ui.dialogs.BudgetViewerDialog;
import net.mjrz.fm.ui.dialogs.EarningsReportDialog;
import net.mjrz.fm.ui.dialogs.EditAccountDialog;
import net.mjrz.fm.ui.dialogs.EditTxDialog;
import net.mjrz.fm.ui.dialogs.ExitPromptDialog;
import net.mjrz.fm.ui.dialogs.ExpReportDialog;
import net.mjrz.fm.ui.dialogs.MissedTxDialog;
import net.mjrz.fm.ui.dialogs.NetWorthDialog;
import net.mjrz.fm.ui.dialogs.NewAccountDialog;
import net.mjrz.fm.ui.dialogs.NewTransactionDialog;
import net.mjrz.fm.ui.dialogs.OnlineAccessDetailsDialog;
import net.mjrz.fm.ui.dialogs.PieChartDialog;
import net.mjrz.fm.ui.dialogs.PreferencesDialog;
import net.mjrz.fm.ui.dialogs.ProxySetupDialog;
import net.mjrz.fm.ui.dialogs.QuickTransactionDialog;
import net.mjrz.fm.ui.dialogs.ScheduledTasksDialog;
import net.mjrz.fm.ui.dialogs.SetupAlertDialog;
import net.mjrz.fm.ui.dialogs.ViewContactsDialog;
import net.mjrz.fm.ui.dialogs.ViewQuoteDialog;
import net.mjrz.fm.ui.panels.AccountsTreePanel;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.panels.PortfolioPanel;
import net.mjrz.fm.ui.panels.SummaryPanel;
import net.mjrz.fm.ui.panels.ofx.ImportProgressPanel;
import net.mjrz.fm.ui.panels.prefs.PreferencesPanel;
import net.mjrz.fm.ui.panels.schedule.ScheduleBuilder;
import net.mjrz.fm.ui.utils.AnimatingSheet;
import net.mjrz.fm.ui.utils.DateRangeSelector;
import net.mjrz.fm.ui.utils.FMToolBar;
import net.mjrz.fm.ui.utils.FavouritesComponent;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.NotificationHandler;
import net.mjrz.fm.ui.utils.SearchComponent;
import net.mjrz.fm.ui.utils.TipBrowser;
import net.mjrz.fm.ui.utils.TransactionTableModel;
import net.mjrz.fm.ui.utils.TxHistoryTable;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.utils.notifications.types.UINotification;
import net.mjrz.fm.ui.utils.notifications.types.UpdateCheckNotification;
import net.mjrz.fm.ui.wizards.BudgetCreatorWizard;
import net.mjrz.fm.ui.wizards.TransactionWizard;
import net.mjrz.fm.utils.DateUtils;
import net.mjrz.fm.utils.HtmlReportGen;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;
import net.mjrz.scheduler.Scheduler;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class FinanceManagerUI extends JFrame implements Observer {
	static final long serialVersionUID = 0L;

	private TxHistoryTable txHistoryTable;
	private TransactionTableModel txTableModel;
	private SummaryPanel summaryPane;
	private JLabel numAccts, numTransactions;

	private Filter currFltr = null;
	private AccountsTreePanel accountsTree;

	private ButtonGroup menuItemCbGroup = new ButtonGroup();

	private User user;
	private FManEntityManager entityManager = new FManEntityManager();

	private Insets menuInset = null;

	private Border txHistoryBorder = BorderFactory
			.createLineBorder(Color.LIGHT_GRAY);

	private JLabel currentFilterName = null;

	private SearchComponent search = null;
	private FavouritesComponent favouritesComponent = null;

	private static Logger logger = Logger.getLogger(FinanceManagerUI.class
			.getName());

	private JPanel mainPanel = null;

	/* Animating notification variables */
	static final int INCOMING = 1;
	static final int OUTGOING = -1;
	static final float ANIMATION_DURATION = 1000f;
	static final int ANIMATION_SLEEP = 50;
	public static final int WAIT_DURATION = 15000;

	private JComponent sheet;
	private JPanel glass;
	private AnimatingSheet animSheet;
	private Timer animTimer;
	private Timer waitTimer;
	private boolean animating;
	private int animDirection;
	private long animStart;

	static final int STATUS_NONE = 0;
	static final int STATUS_INPROGRESS = 2;
	static final int STATUS_DISPOSED = 3;

	private int animationStatus = STATUS_NONE;

	private PageControlPanel pageControlPanel;

	private HashMap<String, JMenuItem> mItems = null;

	public static final String TX_HISTORY_VIEW_NAME = "TxHistory";
	public static final String BUDGET_SUMMARY_VIEW_NAME = "BudgetSumamry";

	public FinanceManagerUI() throws Exception {
		super(UIDefaults.FINANCE_MANAGER_TITLE);
		try {
			menuInset = new Insets(1, 1, 1, 1);
			Messages.initializeMessages();
			long uid = SessionManager.getSessionUserId();

			user = entityManager.getUser(uid);

			mItems = new HashMap<String, JMenuItem>();

			initialize();

			initializeAnimatingSheet();

			/* Initialize the notification handler */
			NotificationHandler.initialize(this);
			Scheduler.register(this);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			throw e;
		}
		finally {
			if (SwingUtilities.isEventDispatchThread()) {
				setColumnWidths(txHistoryTable);
			}
			else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setColumnWidths(txHistoryTable);
					}
				});
			}
		}
	}

	public User getUser() {
		return user;
	}

	private JPanel getDisplayPanel() {
		mainPanel = new JPanel();

		CardLayout layout = new CardLayout();
		mainPanel.setLayout(layout);

		mainPanel.add(getTxHistoryPane(), TX_HISTORY_VIEW_NAME);

		return mainPanel;
	}

	public void switchView(String viewName) {
		CardLayout cl = (CardLayout) mainPanel.getLayout();
		cl.show(mainPanel, viewName);
	}

	private Component getMainPanel() {
		JPanel centerPanel = this.getDisplayPanel();

		JPanel acctsPanel = getSummaryPanel();

		JSplitPane txPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				centerPanel, acctsPanel);
		txPanel.setOneTouchExpandable(true);
		txPanel.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				processTxSplitPropertyChange(evt);
			}
		});

		Dimension d = getMainWindowSizePreference();
		setPreferredSize(d);

		/* Set divider locations */
		String val = ZProperties.getProperty(PreferencesPanel.TX_SPLIT_LOC);
		if (val == null || Integer.parseInt(val) <= 0) {
			int loc = 350;
			ZProperties.replaceRuntimeProperty(PreferencesPanel.TX_SPLIT_LOC,
					String.valueOf(loc));
			txPanel.setDividerLocation((int) (this.getPreferredSize()
					.getHeight() - loc));
		}
		else {
			txPanel.setDividerLocation((int) (this.getPreferredSize()
					.getHeight() - Integer.parseInt(val)));
			ZProperties.replaceRuntimeProperty(PreferencesPanel.TX_SPLIT_LOC,
					val);
		}

		return txPanel;
	}

	private Dimension getMainWindowSizePreference() {
		try {
			Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			setPreferredSize(new Dimension((int) d.getWidth() - 50,
					(int) d.getHeight() - 100));
			return d;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public void initialize() throws Exception {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(5, 5));

		pageControlPanel = PageControlPanel.getInstance(this);

		currentFilterName = new JLabel();

		accountsTree = new AccountsTreePanel(this, user);

		JPanel main = new JPanel();
		main.setLayout(new BorderLayout(5, 5));

		main.add(getMainPanel(), BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				accountsTree, main);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);

		FMToolBar toolBar = new FMToolBar(this);
		search = new SearchComponent(this);
		toolBar.add(search);

		favouritesComponent = new FavouritesComponent(this);
		toolBar.add(favouritesComponent);

		cp.add(toolBar, BorderLayout.NORTH);
		cp.add(splitPane, BorderLayout.CENTER);
		cp.add(getStatusPane(), BorderLayout.SOUTH);

		setJMenuBar(createMenuBar());

		updateStatusPane();

		initializeActionMap();

		pageControlPanel.initializePaging();
		// txTableModel.setSortInfo(0, TransactionTableModel.DESC);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String tips = ZProperties.getProperty("TIPBROWSER.SHOWONSTART");
				if (tips == null || tips.equals("true")) {
					showTipDialog();
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				accountsTree.requestFocusInWindow();
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				checkForUpdates();
			}
		});
	}

	private void processTxSplitPropertyChange(PropertyChangeEvent evt) {
		try {
			String propName = evt.getPropertyName();
			if (propName != null && propName.equals("dividerLocation")) {
				Object obj = evt.getNewValue();
				if (obj != null) {
					int h = (int) getPreferredSize().getHeight();
					int loc = h - (Integer) obj;
					ZProperties.replaceRuntimeProperty(
							PreferencesPanel.TX_SPLIT_LOC, String.valueOf(loc));
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	@SuppressWarnings("serial")
	private void initializeActionMap() {
		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		Object obj2 = new Object();

		KeyStroke closeFindStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
				0);

		Action closeFindAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				search.undisplay();
			}
		};

		im.put(closeFindStroke, obj2);
		am.put(obj2, closeFindAction);
	}

	private JPanel getStatusPane() {
		JLabel username = new JLabel("Logged in as: "
				+ SessionManager.getSessionProfile());

		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		numAccts = new JLabel("", JLabel.LEADING);
		numTransactions = new JLabel("", JLabel.LEADING);

		JPanel east = new JPanel();
		east.setLayout(new BoxLayout(east, BoxLayout.LINE_AXIS));

		east.add(Box.createHorizontalStrut(5));
		east.add(numAccts);
		east.add(Box.createHorizontalStrut(5));
		east.add(new JSeparator(SwingConstants.VERTICAL));
		east.add(Box.createHorizontalStrut(5));
		east.add(numTransactions);
		east.add(Box.createHorizontalStrut(5));
		east.add(new JSeparator(SwingConstants.VERTICAL));
		east.add(Box.createHorizontalStrut(5));
		east.add(currentFilterName);
		ret.add(east, BorderLayout.WEST);

		JPanel west = new JPanel();
		west.setLayout(new BoxLayout(west, BoxLayout.LINE_AXIS));
		west.add(username);
		ret.add(west, BorderLayout.EAST);

		Border border = BorderFactory.createTitledBorder("");
		ret.setBorder(border);
		return ret;
	}

	public void updateStatusPane() {
		int na = accountsTree.getNumAccounts();
		numAccts.setText(Messages.getString("Accounts: ") + na);
	}

	public void updateStatusPane(Long txCount) {
		numTransactions.setText(Messages.getString("Transactions: ") + txCount);
	}

	private JPanel getSummaryPanel() {
		summaryPane = new SummaryPanel();
		summaryPane.updateSummary(user);
		summaryPane.setMaximumSize(new Dimension(400, 400));
		return summaryPane;
	}

	private JPanel getTxHistoryPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		txTableModel = new TransactionTableModel();
		txHistoryTable = new TxHistoryTable(txTableModel);
		txHistoryTable.getTableHeader().addMouseListener(
				new TxTableHeaderMouseListener());
		txHistoryTable.getSelectionModel().addListSelectionListener(
				new TxHistorySelectionListener());
		txHistoryTable.addMouseListener(new MouseEventListener());
		txHistoryTable.addKeyListener(new TableKeyEventHandler());

		JScrollPane sp = new JScrollPane(txHistoryTable);
		ret.add(sp, BorderLayout.CENTER);
		ret.add(pageControlPanel, BorderLayout.SOUTH);
		ret.setBorder(txHistoryBorder);

		txHistoryTable.getParent().setBackground(
				UIDefaults.DEFAULT_PANEL_BG_COLOR);
		return ret;
	}

	private void setColumnWidths(JTable table) {
		int tableW = (int) table.getPreferredScrollableViewportSize()
				.getWidth();
		int w = 0;
		int sz = table.getColumnCount();
		for (int i = 0; i < sz; i++) {
			String propVal = ZProperties.getProperty("UIPrefs.TxTableColumn."
					+ i);
			// System.out.println("Col width = " + i + ":" + propVal);
			if (propVal == null) {
				if (i == 0) {
					w = getWidthFromPercent(tableW, 20);
				}
				else if (i == 1) {
					w = getWidthFromPercent(tableW, 85);
				}
				else if (i == 2 || i == 3) {
					w = getWidthFromPercent(tableW, 85);
				}
				else {
					w = getWidthFromPercent(tableW, 50);
				}
			}
			else {
				w = getWidthFromPercent(tableW, Integer.parseInt(propVal));
			}

			if (i == 0) {
				table.getColumnModel().getColumn(i).setPreferredWidth(w);
			}
			else {
				table.getColumnModel().getColumn(i).setPreferredWidth(w);
			}
			// System.out.println(i + ":" +
			// table.getColumnModel().getColumn(i).getHeaderValue() + ":" + w +
			// ":" + tableW);
		}
	}

	private void saveColumnWidths(JTable table) {
		int tableWidth = (int) table.getPreferredScrollableViewportSize()
				.getWidth();
		TableColumnModel cmodel = table.getColumnModel();
		int count = cmodel.getColumnCount();
		for (int i = 0; i < count; i++) {
			int colWidth = cmodel.getColumn(i).getWidth();
			int pct = getPercentFromWidth(tableWidth, colWidth);
			String propName = "UIPrefs.TxTableColumn." + i;
			String propVal = String.valueOf(pct);
			ZProperties.replaceRuntimeProperty(propName, propVal);
		}
	}

	private int getWidthFromPercent(int width, int pct) {
		return (width * pct / 100);
	}

	private int getPercentFromWidth(int totalWidth, int colWidth) {
		return (100 * colWidth / totalWidth);
	}

	private void reloadTxHistory(Filter filter) {
		txTableModel.setRowCount(0);
		PageControlPanel.getInstance(this).reset();
		if (filter != null)
			PageControlPanel.getInstance(this).makePageRequest(filter);
		else
			PageControlPanel.getInstance(this).makePageRequest(
					PageControlPanel.getInstance(this).getDefaultFilter());
		summaryPane.updateSummary(user);
	}

	private void updateTxTableModel(java.util.List results) {
		try {
			int row = 0;
			txTableModel.setRowCount(0);
			for (Object o : results) {
				TT t = (TT) o;
				Object data[] = txTableModel.transactionToArray(t);
				// System.out.println(t.getCreateDate() + ":" +
				// t.getTxAmount());
				txTableModel.insertRow(row, data);
				row++;
			}
			updateStatusPane();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		finally {
			if (SwingUtilities.isEventDispatchThread()) {
				setColumnWidths(txHistoryTable);
			}
			else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setColumnWidths(txHistoryTable);
					}
				});
			}
		}
	}

	public Filter getCurrentFilter() {
		return currFltr;
	}

	@SuppressWarnings("rawtypes")
	public synchronized void executeFilter(Filter f, long start, long size)
			throws Exception {
		currFltr = null;

		ActionRequest req = new ActionRequest();
		req.setActionName("executeFilter");
		req.setProperty("FILTER", f);
		req.setProperty("CURRIDX", start);
		req.setProperty("PAGE_SIZE", size);
		req.setUser(user);

		ActionResponse resp = new ExecuteFilterAction().executeAction(req);
		java.util.List txList = (java.util.List) resp.getResult("TXLIST");
		if (txList != null) {
			updateTxTableModel(txList);
		}
		if (f.getName() != null) {
			updateCurrentFilter(f.getName());
		}

		List<Order> order = f.getOrder();
		if (order != null && order.size() > 0) {
			Order o = order.get(0);
			int colIdx = Order.getColumnIndex(o.getColumn());
			txTableModel.setSortInfo(colIdx, o.getDirection());
		}
		currFltr = f;
	}

	private JMenuBar createMenuBar() {
		JMenuBar mbar = new JMenuBar();

		JMenu menu = new JMenu(Messages.getString("File"));
		menu.setMnemonic(KeyEvent.VK_F);

		JMenu sub = new JMenu(Messages.getString("New"));
		sub.setMargin(menuInset);
		menu.add(sub);

		JMenuItem mItem = new JMenuItem(
				Messages.getString("Account"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/account.png")); //$NON-NLS-2$
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Account");
		mItem.addActionListener(new MenuItemHandler());
		sub.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Transaction"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/tx.png")); //$NON-NLS-2$
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Transaction");
		mItem.addActionListener(new MenuItemHandler());
		sub.add(mItem);

		menu.add(new JSeparator(JSeparator.HORIZONTAL));

		Dimension d = mItem.getPreferredSize();

		mItem = new JMenuItem(Messages.getString("Import OFX/QFX"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/ofx_import.png"));
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.CTRL_MASK));
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Import OFX/QFX");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Save report"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/save.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_S);
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		mItem.setActionCommand("Save report");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Print"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/print.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));
		mItem.setActionCommand("Print");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		menu.add(new JSeparator(JSeparator.HORIZONTAL));

		mItem = new JMenuItem(
				"Logout", new net.mjrz.fm.ui.utils.MyImageIcon("icons/logout.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_L);
		mItem.setActionCommand("Logout");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Exit"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/exit.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_X);
		mItem.setActionCommand("Exit");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mbar.add(menu);

		menu = new JMenu(Messages.getString("Edit"));
		menu.setMnemonic(KeyEvent.VK_E);

		mItem = new JMenuItem(Messages.getString("Select All"));
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_A);
		mItem.setActionCommand("Select All");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);
		mItems.put("Select All", mItem);

		mItem = new JMenuItem(Messages.getString("Cancel Transactions"));
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_T);
		mItem.setActionCommand("Cancel Transactions");
		mItem.addActionListener(new MenuItemHandler());
		mItem.setEnabled(false);
		menu.add(mItem);
		mItems.put("Cancel Transactions", mItem);

		mItem = new JMenuItem(Messages.getString("Preferences"));
		mItem.setActionCommand("Prefs");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mbar.add(menu);

		menu = new JMenu(Messages.getString("Graphs"));
		menu.setMnemonic(KeyEvent.VK_G);

		mItem = new JMenuItem(
				Messages.getString("Assets"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/assets.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Assets");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Liabilities"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/liabs.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Liabilities");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		JMenu submenu = new JMenu(Messages.getString("Net Worth"));
		submenu.setMargin(menuInset);
		mItem = new JMenuItem(Messages.getString("Last 10 days"));
		mItem.setActionCommand("Last 10 days");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Last 10 weeks"));
		mItem.setActionCommand("Last 10 weeks");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Last 10 months"));
		mItem.setActionCommand("Last 10 months");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		menu.add(submenu);
		mbar.add(menu);

		menu = new JMenu(Messages.getString("Budget"));
		menu.setMnemonic(KeyEvent.VK_B);

		mItem = new JMenuItem(Messages.getString("New budget")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("BudgetWizard");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);
		mbar.add(menu);

		mItem = new JMenuItem(Messages.getString("View budgets")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("BudgetViewer");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);
		mbar.add(menu);

		menu = new JMenu(Messages.getString("Reports"));
		menu.setMnemonic(KeyEvent.VK_R);
		mbar.add(menu);

		submenu = new JMenu(Messages.getString("Cash flow"));
		submenu.setMargin(menuInset);
		// submenu.setPreferredSize(new Dimension(150, (int) d.getHeight()));

		mItem = new JMenuItem(Messages.getString("This week"));
		mItem.setActionCommand("ERThisWeek");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		mItem = new JMenuItem(Messages.getString("This month"));
		mItem.setActionCommand("ERThisMonth");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Select range"));
		mItem.setActionCommand("ERRange");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		menu.add(submenu);

		submenu = new JMenu(Messages.getString("Earnings report"));
		submenu.setMargin(menuInset);
		// submenu.setPreferredSize(new Dimension(150, (int) d.getHeight()));

		mItem = new JMenuItem(Messages.getString("This week"));
		mItem.setActionCommand("IRThisWeek");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		mItem = new JMenuItem(Messages.getString("This month"));
		mItem.setActionCommand("IRThisMonth");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Select range"));
		mItem.setActionCommand("IRRange");
		mItem.addActionListener(new MenuItemHandler());
		submenu.add(mItem);

		menu.add(submenu);

		menu = new JMenu(Messages.getString("Tools"));
		menu.setMnemonic(KeyEvent.VK_T);

		mItem = new JMenuItem(Messages.getString("Transaction wizard"));
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				ActionEvent.CTRL_MASK));
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_W);
		mItem.setActionCommand("Transaction wizard");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Reminders"));
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_H);
		mItem.setActionCommand("ViewST");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Address book"));
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				ActionEvent.CTRL_MASK));
		mItem.setMnemonic(KeyEvent.VK_B);
		mItem.setActionCommand("Address book");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem("Portfolio");
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				ActionEvent.CTRL_MASK));
		mItem.setMnemonic(KeyEvent.VK_T);
		mItem.setActionCommand("Portfolio");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Setup connection"));
		mItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.CTRL_MASK));
		mItem.setMnemonic(KeyEvent.VK_E);
		mItem.setActionCommand("Setup connection");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mbar.add(menu);

		menu = new JMenu(Messages.getString("Help"));
		menu.setMnemonic(KeyEvent.VK_H);

		mItem = new JMenuItem(Messages.getString("Online help"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/online_doc.png"));
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_H);
		mItem.setActionCommand("OnlineDoc");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(Messages.getString("Tip of the day"),
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/dyk_menu.png"));
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_A);
		mItem.setActionCommand("Tip of the day");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("About"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/about.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setMnemonic(KeyEvent.VK_A);
		mItem.setActionCommand("About");
		mItem.addActionListener(new MenuItemHandler());
		menu.add(mItem);

		mbar.add(menu);

		return mbar;
	}

	class MenuItemHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			// System.out.println("Cmd=" + cmd);
			if (cmd.equals("Account")) {
				showNewAccountDialog(null);
				return;
			}
			if (cmd.equals("Transaction")) {
				showNewTransactionDialog();
				return;
			}
			if (cmd.equals("Print")) {
				printTable();
				return;
			}
			if (cmd.equals("Logout")) {
				showExitDialog(ExitPromptDialog.LOGOUT);
				return;
			}
			if (cmd.equals("Exit")) {
				showExitDialog(ExitPromptDialog.EXIT);
				return;
			}
			if (cmd.equals("OnlineDoc")) {
				try {
					java.awt.Desktop d = Desktop.getDesktop();
					if (Desktop.isDesktopSupported()) {
						d.browse(new URI(UIDefaults.PRODUCT_DOCUMENTATION_URL));
					}
				}
				catch (Exception ex) {
					FinanceManagerUI.this
							.showGenericErrorDialog("Unable to open default browser");
					logger.error(ex);
				}
				return;
			}
			if (cmd.equals("About")) {
				showAboutDialog();
				return;
			}
			if (cmd.equals("Tip of the day")) {
				showTipDialog();
				return;
			}
			if (cmd.equals("Get quotes")) {
				showQuotesDialog();
				return;
			}
			if (cmd.equals("ERThisWeek")) {
				showExpReportDialog(DateUtils.THIS_WEEK);
				return;
			}
			if (cmd.equals("ERThisMonth")) {
				showExpReportDialog(DateUtils.THIS_MONTH);
				return;
			}
			if (cmd.equals("ERRange")) {
				showRangeSelectorDialog(cmd,
						DateRangeSelector.REPORT_TYPE_EXPENSE);
				return;
			}
			if (cmd.equals("IRThisWeek")) {
				showEarningsReportDialog(DateUtils.THIS_WEEK);
				return;
			}
			if (cmd.equals("IRThisMonth")) {
				showEarningsReportDialog(DateUtils.THIS_MONTH);
				return;
			}
			if (cmd.equals("IRRange")) {
				showRangeSelectorDialog(cmd,
						DateRangeSelector.REPORT_TYPE_INCOME);
				return;
			}
			if (cmd.equals("Address book")) {
				showContactsDialog();
				return;
			}
			if (cmd.equals("Portfolio")) {
				showPortfolioDialog();
				return;
			}
			if (cmd.equals("Setup connection")) {
				showProxySetupDialog();
				return;
			}
			if (cmd.equals("Cancel Transactions")) {
				int ans = JOptionPane.showConfirmDialog(FinanceManagerUI.this,
						"Are you sure?", "Delete transaction",
						JOptionPane.YES_NO_OPTION);

				if (ans != JOptionPane.OK_OPTION) {
					return;
				}
				cancelTransactions();
				return;
			}
			if (cmd.equals("Select All")) {
				if (txTableModel != null) {
					int end = txTableModel.getRowCount();
					txHistoryTable.getSelectionModel().setSelectionInterval(0,
							end - 1);
				}
				return;
			}
			if (cmd.equals("Find")) {
				search.display();
				return;
			}
			if (cmd.equals("ViewST")) {
				showSTDialog();
				return;
			}
			if (cmd.equals("Save report")) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showSaveDialog(FinanceManagerUI.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.exists()) {
						int ans = JOptionPane
								.showConfirmDialog(
										FinanceManagerUI.this,
										Messages.getString("File with same name already exists. Replace?"),
										Messages.getString("Save As"),
										JOptionPane.YES_NO_OPTION);

						if (ans != JOptionPane.OK_OPTION) {
							return;
						}
					}
					ActionRequest req = new ActionRequest();
					req.setActionName("executeFilter");
					req.setProperty("FILTER", currFltr);
					req.setUser(user);

					try {
						ActionResponse resp = new ExecuteFilterAction()
								.executeAction(req);
						java.util.List<Transaction> txList = (java.util.List<Transaction>) resp
								.getResult("TXLIST");
						HtmlReportGen report = new HtmlReportGen();
						String fname = report.generateReport(file, txList,
								currFltr);
						if (fname != null) {
							showGenericMessageDialog(Messages
									.getString("Report saved to : ") + fname);
						}
					}
					catch (Exception ex) {
						logger.error(ex);
						logger.error(Messages.getString("Exception: ")
								+ ex.getMessage());
						showGenericErrorDialog(Messages
								.getString("Failed to save report"));
					}
				}
				return;
			}
			if (cmd.equals("Assets")) {
				showChartDialog(AccountTypes.ACCT_TYPE_CASH);
				return;
			}
			if (cmd.equals("Liabilities")) {
				showChartDialog(AccountTypes.ACCT_TYPE_LIABILITY);
				return;
			}
			if (cmd.equals("Expenses")) {
				showChartDialog(AccountTypes.ACCT_TYPE_EXPENSE);
				return;
			}
			if (cmd.equals("Last 10 days")) {
				showNetWorthHistoryDialog(GetNetWorthHistoryAction.HISTORY_10DAYS);
				return;
			}
			if (cmd.equals("Last 10 weeks")) {
				showNetWorthHistoryDialog(GetNetWorthHistoryAction.HISTORY_10WEEKS);
				return;
			}
			if (cmd.equals("Last 10 months")) {
				showNetWorthHistoryDialog(GetNetWorthHistoryAction.HISTORY_10MONTHS);
				return;
			}
			if (cmd.equals("Import OFX/QFX")) {
				JFileChooser fc = null;
				String key = PreferencesPanel.LAST_DIR_PATH;
				String propVal = ZProperties.getProperty(key);
				if (propVal != null) {
					File f = new File(propVal);
					try {
						if (f.exists()) {
							fc = new JFileChooser(f);
						}
						else {
							fc = new JFileChooser();
						}
					}
					catch (Exception ex) {
						fc = new JFileChooser();
					}
				}
				else {
					fc = new JFileChooser();
				}

				OFXFileFilter filter = new OFXFileFilter();
				fc.addChoosableFileFilter(filter);
				int sel = fc.showOpenDialog(FinanceManagerUI.this);
				if (sel == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					if (filter.accept(f)) {
						showImportDialog(f);
						File dir = f.getParentFile();
						if (dir.isDirectory()) {
							ZProperties.replaceRuntimeProperty(key,
									dir.getAbsolutePath());
						}
					}
					else {
						showGenericErrorDialog(Messages
								.getString("Invalid file selection"));
					}
				}
			}
			if (cmd.equals("Prefs")) {
				showPreferencesDialog();
				return;
			}
			if (cmd.equals("BudgetViewer")) {
				showBudgetViewerDialog();
				return;
			}
			if (cmd.equals("BudgetWizard")) {
				showBudgetWizardDialog();
				return;
			}
			if (cmd.equals("Transaction wizard")) {
				FinanceManagerUI.this.showNewTransactionWizard();
				return;
			}
		}
	}

	private void cancelTransactions() {
		int sel[] = txHistoryTable.getSelectedRows();
		ArrayList<Integer> removed = new ArrayList<Integer>();

		/*
		 * Pause notification queue if cancelling a batch of transactions, to
		 * avoid a burst of notifications
		 */
		if (sel.length > 1) {
			NotificationHandler.pauseQueue(true);
		}
		for (int i = 0; i < sel.length; i++) {
			int idx = txHistoryTable.convertRowIndexToModel(sel[i]);
			if (!txTableModel.isTransactionRow(idx))
				continue;

			Long txId = txTableModel.getTransaction(idx).getTxId();
			/*
			 * This can happen when a filter is applied and there are empty rows
			 * in the table
			 */
			if (txId == null || txId == 0) {
				continue;
			}
			ActionResponse aresp = cancelTransaction(txId);
			if (aresp.getErrorCode() == ActionResponse.NOERROR)
				removed.add(sel[i]);
		}
		for (int i = removed.size() - 1; i >= 0; i--) {
			int r = removed.get(i);
			int s = txHistoryTable.convertRowIndexToModel(r);
			txTableModel.removeRow(s);
		}
		summaryPane.updateSummary(user);
		updateStatusPane();
		NotificationHandler.pauseQueue(false);
	}

	private ActionResponse cancelTransaction(Long txId) {
		ActionResponse aresp = new ActionResponse();
		try {
			Transaction t = entityManager.getTransaction(user.getUid(), txId);
			ActionRequest req = new ActionRequest();
			req.setActionName("deleteTransaction");
			req.setUser(user);
			req.setProperty("TXID", txId);

			aresp = new DeleteTransactionAction().executeAction(req);

			/* Update accounts tree to reflect new totals */
			if (aresp.getErrorCode() == ActionResponse.NOERROR) {
				reloadAccountList(t.getFromAccountId(), false);
				reloadAccountList(t.getToAccountId(), false);
			}
			return aresp;
		}
		catch (Exception ex) {
			aresp.setErrorCode(ActionResponse.GENERAL_ERROR);
			aresp.setErrorMessage(Messages
					.getString("Unable to delete transaction"));
			return aresp;
		}
	}

	class MouseEventListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
				int row = txHistoryTable.rowAtPoint(e.getPoint());
				txHistoryTable.getSelectionModel().setSelectionInterval(row,
						row);
				row = txHistoryTable.convertRowIndexToModel(row);
				if (!txTableModel.isTransactionRow(row)) {
					return;
				}
				List<AttachmentRef> attachments = txTableModel
						.getAttachments(row);
				showPopupMenu(attachments, e.getX(), e.getY());
			}
			else if (e.getClickCount() == 1) {
				int sel = txHistoryTable.getSelectedRow();
				if (sel >= 0) {
					sel = txHistoryTable.convertRowIndexToModel(sel);
				}
				if (!txTableModel.isTransactionRow(sel)) {
					return;
				}
				boolean isParent = txTableModel.isParent(sel);
				boolean isExpanded = txTableModel.isExpanded(sel);
				if (isParent && isExpanded) {
					txTableModel.removeChildRows(sel);
				}
				else if (isParent && !isExpanded) {
					txTableModel.addChildRows(sel);
				}
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	class TableKeyEventHandler implements KeyListener {

		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_DELETE) {
				int sel = txHistoryTable.getSelectedRow();
				if (sel >= 0) {
					sel = txHistoryTable.convertRowIndexToModel(sel);
				}
				Long txId = (Long) txTableModel.getTransactionId(sel);
				/*
				 * This can happen when a filter is applied and there are empty
				 * rows in the table
				 */
				if (txId == null || txId == 0) {
					return;
				}

				int ans = JOptionPane.showConfirmDialog(FinanceManagerUI.this,
						Messages.getString("Are you sure?"),
						Messages.getString("Delete transaction"),
						JOptionPane.YES_NO_OPTION);

				if (ans != JOptionPane.OK_OPTION) {
					return;
				}

				ActionResponse aresp = cancelTransaction(txId);
				if (!aresp.hasErrors()) {
					reloadTxHistory(currFltr);
					summaryPane.updateSummary(user);
					updateStatusPane();
				}
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}

	class PopupMenuHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			int sel = txHistoryTable.getSelectedRow();

			if (sel >= 0) {
				sel = txHistoryTable.convertRowIndexToModel(sel);
			}
			if (!txTableModel.isTransactionRow(sel)) {
				return;
			}
			Long txId = (Long) txTableModel.getTransactionId(sel);
			Boolean isParent = txTableModel.isParent(sel);
			/*
			 * This can happen when a filter is applied and there are empty rows
			 * in the table
			 */
			if (txId == null) {
				return;
			}

			if (cmd.equals("Cancel")) {
				int ans = JOptionPane.showConfirmDialog(FinanceManagerUI.this,
						Messages.getString("Are you sure?"),
						Messages.getString("Delete transaction"),
						JOptionPane.YES_NO_OPTION);

				if (ans != JOptionPane.OK_OPTION) {
					return;
				}

				ActionResponse aresp = cancelTransaction(txId);
				if (!aresp.hasErrors()) {
					reloadTxHistory(currFltr);
					summaryPane.updateSummary(user);
					updateStatusPane();
				}
				return;
			}
			if (cmd.equals("Edit")) {
				showEditTransactionDialog(txId, isParent);
				return;
			}
			if (cmd.equals("HighlightTx")) {
				highlightTx(sel, txId);
				return;
			}
			if (cmd.equals("UnHighlightTx")) {
				removeHighlightTx(sel, txId);
				return;
			}
			if (cmd.equals("AddFavourite")) {
				String s = (String) JOptionPane.showInputDialog(
						FinanceManagerUI.this, "Please choose a name",
						"Favourite", JOptionPane.PLAIN_MESSAGE, null, null,
						null);
				if (s != null && s.trim().length() > 0) {
					s = s.trim();
					favouritesComponent.addFavourite(txId, s);
				}
				return;
			}
			if (cmd.equals("Reminder")) {
				JDialog d = new JDialog(FinanceManagerUI.this);
				GuiUtilities.addWindowClosingActionMap(d);
				d.getContentPane().add(new ScheduleBuilder(txId),
						BorderLayout.CENTER);
				d.pack();
				d.setSize(500, 300);
				d.setLocationRelativeTo(FinanceManagerUI.this);
				d.setModalityType(ModalityType.APPLICATION_MODAL);
				d.setTitle(Messages.getString("Setup reminder"));
				d.setVisible(true);
				return;
			}
		}
	}

	private void removeHighlightTx(int rowIndex, Long txId) {
		try {
			int del = entityManager.deleteTxDecorator(txId);
			if (del >= 0) {
				txTableModel.removeRowColor(rowIndex);
				txHistoryTable.repaint();
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void highlightTx(int rowIndex, Long txId) {
		Color newColor = JColorChooser.showDialog(this,
				"Choose Background Color", UIDefaults.DEFAULT_COLOR);

		if (newColor == null)
			return;

		TxDecorator dec = new TxDecorator();
		StringBuilder color = new StringBuilder();
		color.append(newColor.getRed());
		color.append(",");
		color.append(newColor.getGreen());
		color.append(",");
		color.append(newColor.getBlue());

		dec.setTxId(txId);
		dec.setColor(color.toString());
		try {
			entityManager.addTxDecorator(dec);
			txTableModel.setRowColor(rowIndex, color.toString());
			txHistoryTable.repaint();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void showPopupMenu(List<AttachmentRef> attachments, int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();

		JMenuItem mItem = new JMenuItem(
				Messages.getString("Edit"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/edit.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Edit");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Add to favourites"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/fav.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("AddFavourite");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = getHighlightMenuItem();
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(
				Messages.getString("Reminder"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/time.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Reminder");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		if (attachments != null && attachments.size() > 0) {
			JMenu submenu = new JMenu("Save attachment");
			submenu.setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
					"icons/attach.png"));
			submenu.setMargin(menuInset);

			for (int i = 0; i < attachments.size(); i++) {
				final AttachmentRef aRef = attachments.get(i);
				mItem = new JMenuItem(aRef.getFileName());
				mItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveAttachment(aRef);
					}
				});

				submenu.add(mItem);
			}
			popup.add(submenu);
		}
		popup.add(new JSeparator(JSeparator.HORIZONTAL));

		mItem = new JMenuItem(
				Messages.getString("Delete"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/cancel.png")); //$NON-NLS-2$
		mItem.setMargin(menuInset);
		mItem.setActionCommand("Cancel");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		popup.pack();
		popup.show(txHistoryTable, x, y);
	}

	private JMenuItem getHighlightMenuItem() {
		int sel = txHistoryTable.getSelectedRow();
		if (sel >= 0) {
			sel = txHistoryTable.convertRowIndexToModel(sel);
		}
		boolean isDec = txTableModel.hasDecorator(sel);
		JMenuItem mItem = null;
		if (!isDec) {
			mItem = new JMenuItem(
					Messages.getString("Highlight"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/highlight.png")); //$NON-NLS-2$
			mItem.setMargin(menuInset);
			mItem.setActionCommand("HighlightTx");
		}
		else {
			mItem = new JMenuItem(
					Messages.getString("Remove highlight"), new net.mjrz.fm.ui.utils.MyImageIcon("icons/highlight.png")); //$NON-NLS-2$
			mItem.setMargin(menuInset);
			mItem.setActionCommand("UnHighlightTx");
		}
		return mItem;
	}

	private void saveAttachment(AttachmentRef ref) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		try {
			int sel = fc.showSaveDialog(this);
			if (sel == JFileChooser.APPROVE_OPTION) {
				File dir = fc.getSelectedFile();

				File file = new File(dir.getAbsolutePath()
						+ Main.PATH_SEPARATOR + ref.getFileName());

				// System.out.println("Creating file : " +
				// file.getAbsolutePath());

				Attachment a = (Attachment) entityManager.getObject(
						"Attachment", "id = " + ref.getAttachmentId());

				if (a == null)
					return;

				FileOutputStream fos = new FileOutputStream(file);
				byte[] arr = a.getAttachment();
				fos.write(arr);
				fos.close();

			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			showGenericErrorDialog("Failed to save attachment");
		}
	}

	public void showNewAccountDialog(AccountCategory parent) {
		// NewAccountDialog d = new NewAccountDialog(this, user, parent);
		NewAccountDialog d = NewAccountDialog.getInstance(this, user, parent);
		d.pack();
		d.setDialogFocus();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showEditAccountDialog(Account a) {
		EditAccountDialog d = new EditAccountDialog(this, user, a);
		d.pack();
		d.setDialogFocus();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showAddAlertDialog(int type, Account a) {
		SetupAlertDialog d = new SetupAlertDialog(type, this, user, a);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showNewTransactionDialog() {
		NewTransactionDialog d = new NewTransactionDialog(this, user);
		d.pack();
		d.setSize(new Dimension(650, 450));
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showQuickTransactionDialog(long txId) {
		try {
			QuickTransactionDialog d = new QuickTransactionDialog(this, user,
					txId, NewTransactionDialog.NEW_TX);

			d.pack();
			d.setLocationRelativeTo(this);
			d.setVisible(true);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			JOptionPane.showMessageDialog(this,
					tr("Transaction not found, may have been deleted"),
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showNewTransactionWizard() {
		TransactionWizard d = new TransactionWizard(this);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showEditTransactionDialog(long txId, boolean isParent) {
		try {
			// TransactionDialog d = new TransactionDialog(this, user, txId,
			// TransactionDialog.EDIT_TX);
			EditTxDialog d = new EditTxDialog(this, txId, isParent);
			d.pack();
			d.setSize(400, 350);
			d.setLocationRelativeTo(this);
			d.setVisible(true);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void showNewTransactionDialog(final Account a) {
		NewTransactionDialog d = new NewTransactionDialog(this, user, a);
		// d.setAccount(a);
		d.pack();
		d.setSize(new Dimension(650, 450));
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showSTDialog() {
		// FutureTransactionsDialog d = new FutureTransactionsDialog(this,
		// user);
		ScheduledTasksDialog d = new ScheduledTasksDialog(this);
		d.pack();
		d.setSize(new Dimension(800, 400));
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showChartDialog(final int acctType) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						PieChartDialog d = new PieChartDialog(
								FinanceManagerUI.this, user, acctType);
						d.pack();
						d.setLocationRelativeTo(FinanceManagerUI.this);
						d.setVisible(true);
					}
					catch (Exception e) {
						logger.error(Messages.getString("Exception: ")
								+ e.getMessage());
					}
					finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
		catch (Exception e) {
		}
	}

	private void showImportDialog(final File file) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						ImportProgressPanel d = new ImportProgressPanel(
								FinanceManagerUI.this, user, file);
						// d.loadTxList();

						d.pack();
						d.setLocationRelativeTo(FinanceManagerUI.this);
						d.setVisible(true);
					}
					catch (Exception e) {
						JOptionPane op = getNarrowOptionPane(50);
						op.setMessageType(JOptionPane.ERROR_MESSAGE);
						op.setMessage(e.getMessage());
						JDialog dialog = op.createDialog(FinanceManagerUI.this,
								Messages.getString("Error"));
						dialog.pack();
						dialog.setVisible(true);
						logger.error(MiscUtils.stackTrace2String(e));
					}
					finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
		catch (Exception e) {
		}
	}

	private void showNetWorthHistoryDialog(final int type) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						NetWorthDialog d = new NetWorthDialog(
								FinanceManagerUI.this, user, type);
						d.pack();
						d.setLocationRelativeTo(FinanceManagerUI.this);
						d.setVisible(true);
					}
					catch (Exception e) {
						logger.error(Messages.getString("Exception: ")
								+ e.getMessage());
					}
					finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
		catch (Exception e) {
		}
	}

	private void showAboutDialog() {
		AboutDialog d = new AboutDialog(this);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showTipDialog() {
		TipBrowser d = new TipBrowser(this);
		d.pack();
		d.setSize(new Dimension(450, 300));
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showQuotesDialog() {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
						ViewQuoteDialog d = new ViewQuoteDialog(
								FinanceManagerUI.this, user);
						d.pack();
						d.setLocationRelativeTo(FinanceManagerUI.this);
						d.setVisible(true);
					}
					catch (Exception e) {
						logger.error(Messages.getString("Exception: ")
								+ e.getMessage());
					}
					finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
		catch (Exception e) {
		}
	}

	public void showContactsDialog() {
		ViewContactsDialog d = ViewContactsDialog.getInstance(this, user);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showAddContactDialog() {
		AddContactDialog d = new AddContactDialog(this, user);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showPortfolioDialog() {
		// PortfolioPanel d = new PortfolioPanel(this, user);
		PortfolioPanel d = PortfolioPanel.getInstance(this, user);
		d.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				PortfolioPanel.class.getClassLoader().getResource(
						"icons/icon_money.png")));

		d.pack();
		d.setExtendedState(d.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		d.setVisible(true);
		d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void showProxySetupDialog() {
		ProxySetupDialog d = new ProxySetupDialog(this);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showRangeSelectorDialog(final String cmd, int reportType) {
		JDialog d = DateRangeSelector.getDialog(this, reportType);
		d.setModalityType(ModalityType.APPLICATION_MODAL);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void showExpReportDialog(final int cmd) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));

						String[] range = DateUtils.getDateRange(cmd);

						ActionRequest req = new ActionRequest();
						req.setActionName("getExpenseReport");
						req.setUser(user);
						req.setProperty("DATERANGE", range);

						GetExpReportAction action = new GetExpReportAction();
						ActionResponse resp = action.executeAction(req);

						ExpReportDialog d = new ExpReportDialog(
								FinanceManagerUI.this, resp,
								ExpReportDialog.title);
						d.pack();
						d.setSize(new Dimension(850, 600));
						d.setLocationRelativeTo(FinanceManagerUI.this);
						d.setVisible(true);
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
						showGenericErrorDialog("Error creating expense report.");
					}
					finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	private void showEarningsReportDialog(final int cmd) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));

						String[] range = DateUtils.getDateRange(cmd);

						ActionRequest req = new ActionRequest();
						req.setActionName("getEarningsReport");
						req.setUser(user);
						req.setProperty("DATERANGE", range);

						GetEarningsReportAction action = new GetEarningsReportAction();
						ActionResponse resp = action.executeAction(req);

						EarningsReportDialog d = new EarningsReportDialog(
								FinanceManagerUI.this, resp,
								EarningsReportDialog.title);
						d.pack();
						d.setSize(new Dimension(850, 600));
						d.setLocationRelativeTo(FinanceManagerUI.this);
						d.setVisible(true);
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
						showGenericErrorDialog("Error creating expense report.");
					}
					finally {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	public void showOnlAccessSetupDialog(Account a) {
		OnlineAccessDetailsDialog d = new OnlineAccessDetailsDialog(this, user,
				a);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showPreferencesDialog() {
		PreferencesDialog d = new PreferencesDialog(this);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showBalanceDialog(Account a) {
		try {
			BalanceGraphDialog d = new BalanceGraphDialog(this, user, a);
			d.pack();
			d.setLocationRelativeTo(this);
			d.setVisible(true);
		}
		catch (Exception e) {
			logger.error(e);
			String msg = e.getMessage();
			if (msg != null && msg.length() > 0) {
				showGenericErrorDialog(e.getMessage());
			}
			else {
				showGenericErrorDialog("Unable to display graph");
			}
		}
	}

	public void showBudgetViewerDialog() {
		BudgetViewerDialog d = new BudgetViewerDialog(this);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showBudgetWizardDialog() {
		BudgetCreatorWizard d = new BudgetCreatorWizard(this);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	public void showMissedTxDialog(Map<String, List<Date>> missed) {
		MissedTxDialog d = new MissedTxDialog(this, missed);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}

	private void showGenericMessageDialog(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}

	private void showGenericErrorDialog(String msg) {
		JOptionPane.showMessageDialog(this, msg, Messages.getString("Error"),
				JOptionPane.ERROR_MESSAGE);
	}

	public void reloadAccountList(int accountType) throws Exception {
		try {
			accountsTree.reloadTree();
			summaryPane.updateSummary(user);
		}
		catch (Exception e) {
			throw e;
		}
	}

	public void updateAccountInTree(long accountId) {
		try {
			Account a = entityManager.getAccount(user.getUid(), accountId);
			if (a == null)
				return;
			updateAccountInTree(a);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void updateAccountInTree(Account a) {
		accountsTree.updateTree(a, false);
	}

	public void reloadAccountList(Account a, boolean added) throws Exception {
		try {
			accountsTree.updateTree(a, added);
			summaryPane.updateSummary(user);
			summaryPane.updateAccountTab(a);
		}
		catch (Exception e) {
			throw e;
		}
	}

	public void reloadAccountList(long aid, boolean added) throws Exception {
		try {
			Account a = entityManager.getAccount(user.getUid(), aid);
			if (a == null)
				return;
			reloadAccountList(a, added);
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void printTable() {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						boolean complete = txHistoryTable.print();
						logger.info("Print status = " + complete);
					}
					catch (PrinterException e) {
					}
				}
			});
		}
		catch (Exception e) {

		}
	}

	public void updateSummary() {
		summaryPane.updateSummary(user);
	}

	public void updateSummaryTab(Account account) {
		if (account == null) {
			account = accountsTree.getSelectedAccount();
		}
		if (account == null)
			return;

		summaryPane.updateAccountTab(account);
	}

	public void updateCurrentFilter(String title) {
		if (title.equals(""))
			currentFilterName.setText("");
		else
			currentFilterName.setText("Filter : " + title);
	}

	public void showExitDialog(int type) {
		String show = ZProperties.getProperty("MAIN.EXITWITHPROMPT");
		if (show == null || show.length() == 0 || Boolean.valueOf(show)) {
			ExitPromptDialog d = new ExitPromptDialog(type,
					SessionManager.getSessionProfile(), this);
			d.pack();
			d.setDialogFocus();
			d.setLocationRelativeTo(this);
			d.setVisible(true);
			int n = d.getResponse();
			if (n == ExitPromptDialog.YES) {
				if (type == ExitPromptDialog.EXIT) {
					startShutdown();
				}
				else {
					startLogout();
				}
			}
		}
		else {
			if (type == ExitPromptDialog.EXIT)
				startShutdown();
			else {
				startLogout();
			}
		}
	}

	private void disposeSubWindows() {
		PageControlPanel.disposeInstance();
		PortfolioPanel.disposeInstance();
		ViewContactsDialog.disposeInstance();
		net.mjrz.fm.utils.AlertsCache.disposeInstance();
	}

	private void startShutdown() {
		super.dispose();
		try {
			entityManager.cleanTT();
			GuiUtilities.saveUIPrefs();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
		System.exit(0);
	}

	private void startLogout() {
		disposeSubWindows();
		try {
			entityManager.cleanTT();
			GuiUtilities.saveUIPrefs();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}

		net.mjrz.fm.ui.utils.NotificationHandler.shutdown();
		net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
		updateCheckWorker.cancel(true);
		super.dispose();
		Main.FMMain(null);
	}

	public void dispose() {
		showExitDialog(ExitPromptDialog.EXIT);
	}

	public void disposeNoExit() {
		showExitDialog(ExitPromptDialog.LOGOUT);
	}

	public static JOptionPane getNarrowOptionPane(int maxCharactersPerLineCount) {
		class NarrowOptionPane extends JOptionPane {
			private static final long serialVersionUID = 1L;
			int maxCharactersPerLineCount;

			NarrowOptionPane(int maxCharactersPerLineCount) {
				this.maxCharactersPerLineCount = maxCharactersPerLineCount;
			}

			public int getMaxCharactersPerLineCount() {
				return maxCharactersPerLineCount;
			}
		}
		return new NarrowOptionPane(maxCharactersPerLineCount);
	}

	private void checkForUpdates() {
		updateCheckWorker.execute();
	}

	SwingWorker<Boolean, Object> updateCheckWorker = new SwingWorker<Boolean, Object>() {
		public Boolean doInBackground() throws Exception {
			Thread.sleep(10000);
			String show = ZProperties.getProperty("MAIN.CHECKUPDTONSTARTUP");
			if (show == null || show.length() == 0)
				return true;

			try {
				boolean showBool = Boolean.valueOf(show);
				return showBool;
			}
			catch (Exception e) {
				// in case of exception, just show the dialog anyway.
				return true;
			}
		}

		public void done() {
			try {
				boolean show = get();
				if (show) {
					UINotification notif = new UpdateCheckNotification(
							"Do you want to check for updated version of iFreeBudget?");
					NotificationHandler.addToQueue(notif);
				}
				return;

			}
			catch (java.util.concurrent.CancellationException e) {
				logger.info("Update check cancelled : " + e.getMessage());
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}
	};

	static class OFXFileFilter extends FileFilter {
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			int pos = f.getName().lastIndexOf('.');
			String ext = null;
			if (pos >= 0 && pos + 1 < f.getName().length())
				ext = f.getName().substring(pos + 1);

			if (ext != null) {
				if (ext.equalsIgnoreCase("ofx") || ext.equalsIgnoreCase("qfx")) { //$NON-NLS-2$
					return true;
				}
				else {
					return false;
				}
			}

			return false;
		}

		// The description of this filter
		public String getDescription() {
			return "OFX/QFX files";
		}
	}

	class TxHistorySelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;

			int first = txHistoryTable.getSelectionModel()
					.getMinSelectionIndex();
			int last = txHistoryTable.getSelectionModel()
					.getMaxSelectionIndex();
			first = txHistoryTable.convertRowIndexToModel(first);
			last = txHistoryTable.convertRowIndexToModel(last);

			JMenuItem it = mItems.get("Cancel Transactions");

			// one row was selected
			if (first >= 0 && first == last) {
				if (!txTableModel.isTransactionRow(first)) {
					BigDecimal tmp = (txTableModel.getBucketTotal(first));
					pageControlPanel.setSigma(tmp);
					it.setEnabled(false);
				}
				else {
					Long txId = (Long) txTableModel.getTransactionId(first);
					summaryPane.updateTxDetails(String.valueOf(txId));
					pageControlPanel.hideSigma();
					it.setEnabled(true);
				}
			}
			// multiple rows were selected
			else if (first >= 0 && last >= 0 && last > first) {
				BigDecimal sum = new BigDecimal(0.0d);
				for (int i = first; i <= last; i++) {
					int viewRow = txHistoryTable.convertRowIndexToView(i);
					if (!txHistoryTable.isRowSelected(viewRow)) {
						continue;
					}
					if (!txTableModel.isTransactionRow(i)) {
						BigDecimal tmp = (txTableModel.getBucketTotal(i));
						sum = sum.add(tmp);
						continue;
					}
					TT tt = txTableModel.getTransaction(i);
					sum = sum.add(tt.getTxAmount());
				}
				pageControlPanel.setSigma(sum);
				it.setEnabled(true);
			}
			// no selection
			else {
				pageControlPanel.hideSigma();
				it.setEnabled(false);
			}
		}
	}

	class TxTableHeaderMouseListener extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				return;
			FinanceManagerUI.this.saveColumnWidths(txHistoryTable);
		}

		public void mouseClicked(MouseEvent e) {
			int col = txHistoryTable.columnAtPoint(e.getPoint());

			int sortInfo = txTableModel.getSortInfo(col);

			String colName = txTableModel.getColumnName(col);

			if (colName.equals("")) {
				return;
			}

			int newSortInfo = TransactionTableModel.NONE;

			switch (sortInfo) {
			case TransactionTableModel.NONE:
				newSortInfo = TransactionTableModel.ASC;
				break;
			case TransactionTableModel.ASC:
				newSortInfo = TransactionTableModel.DESC;
				break;
			case TransactionTableModel.DESC:
				newSortInfo = TransactionTableModel.ASC;
				break;
			}

			colName = Order.getColumnName(col);
			if (currFltr != null) {
				currFltr.clearOrder();
				currFltr.addOrder(new Order(colName, newSortInfo));
				currFltr.addOrder(new Order("Date", Order.DESC));
				PageControlPanel.getInstance(FinanceManagerUI.this).reset();
				PageControlPanel.getInstance(FinanceManagerUI.this)
						.makePageRequest(currFltr);
			}
		}
	}

	/* Animating notification methods */
	public void initializeAnimatingSheet() {
		glass = (JPanel) getGlassPane();
		glass.setLayout(new GridBagLayout());
		animSheet = new AnimatingSheet();
		animSheet.setBorder(new LineBorder(Color.BLACK, 1));
	}

	public JComponent showJDialogAsSheet(JDialog frame) {
		animationStatus = STATUS_INPROGRESS;

		sheet = (JComponent) frame.getContentPane();
		sheet.setBorder(new LineBorder(Color.BLACK, 1));
		glass.removeAll();
		animDirection = INCOMING;
		startAnim();

		return sheet;
	}

	public JComponent showJFrameAsSheet(JFrame frame) {
		animationStatus = STATUS_INPROGRESS;

		sheet = (JComponent) frame.getContentPane();
		glass.removeAll();
		animDirection = INCOMING;
		startAnim();

		return sheet;
	}

	private void startAnim() {
		glass.repaint();
		animSheet.setSource(sheet);
		glass.removeAll();
		//
		glass.setVisible(true);

		animStart = System.currentTimeMillis();
		if (animTimer == null) {
			animTimer = new Timer(ANIMATION_SLEEP, new AnimationActionHandler());
		}
		animating = true;
		animTimer.start();
	}

	private void stopAnim() {
		animTimer.stop();
		animating = false;
	}

	public void hideSheet() {
		animDirection = OUTGOING;
		startAnim();
		waitTimer.stop();
		animationStatus = STATUS_DISPOSED;
	}

	private void finishShowingSheet() {
		glass.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHEAST;

		gbc.gridx = 1;
		gbc.weightx = Integer.MAX_VALUE;
		glass.add(Box.createGlue(), gbc);

		glass.add(sheet, gbc);
		gbc.gridy = 1;
		gbc.weighty = Integer.MAX_VALUE;
		glass.add(Box.createGlue(), gbc);

		glass.revalidate();
		glass.repaint();
		if (waitTimer == null) {
			waitTimer = new Timer(WAIT_DURATION, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					waitTimer.stop();
					hideSheet();
				}
			});
		}
		waitTimer.start();
	}

	public synchronized int getStatus() {
		return animationStatus;
	}

	class AnimationActionHandler implements java.awt.event.ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (animating) {
				float animPercent = (System.currentTimeMillis() - animStart)
						/ ANIMATION_DURATION;
				animPercent = Math.min(1.0f, animPercent);
				int animatingHeight = 0;

				if (animDirection == INCOMING) {
					animatingHeight = (int) (animPercent * sheet.getHeight());
				}
				else {
					animatingHeight = (int) ((1.0f - animPercent) * sheet
							.getHeight());
				}
				animSheet.setAnimatingHeight(animatingHeight);
				animSheet.repaint();
				if (animPercent >= 1.0f) {
					stopAnim();
					if (animDirection == INCOMING) {
						finishShowingSheet();
					}
					else {
						glass.removeAll();
						glass.setVisible(true);
					}
				}
			}
		}
	}

	/* End animating notification methods */

	/* Observer interace methods */
	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		System.out.println(arg.getClass().getName());
		// if(arg != null) {
		// Task t = (Task) arg;
		// try {
		// if(!(t instanceof ScheduledTx) ) {
		// return;
		// }
		// ActionResponse r = (ActionResponse) t.get();
		// if(r != null && r.getErrorCode() == ActionResponse.NOERROR) {
		// List<Transaction> txList = (List<Transaction>) r.getResult("TXLIST");
		// if(txList != null) {
		// for(Transaction tx : txList) {
		// TT tt = FManEntityManager.getTT(tx.getTxId());
		//
		// Object[] data = new Object[9];
		// data[0] = "" + tt.getTxId();
		// data[1] = sdf.format(tx.getTxDate());
		// data[2] = FManEntityManager
		// .getAccountName(tx.getFromAccountId());
		// data[3] = FManEntityManager.getAccountName(tx.getToAccountId());
		// data[4] = numFormat.format(tx.getTxAmount());
		// data[5] = numFormat.format(tx.getFromAccountEndingBal());
		// data[6] = AccountTypes.getTxStatus(tx.getTxStatus());
		// data[7] = "132,123,54";
		// txTableModel.insertRow(0, data);
		// }
		// }
		// }
		// }
		// catch (Exception e) {
		// logger.error(MiscUtils.stackTrace2String(e));
		// }
		// }
	}
}
