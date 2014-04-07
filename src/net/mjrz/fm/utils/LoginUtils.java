package net.mjrz.fm.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

import org.apache.log4j.PropertyConfigurator;

import net.mjrz.fm.Main;
import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.PwdHistory;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.services.SchedulerService;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.utils.MyImageIcon;
import net.mjrz.fm.utils.crypto.CHelper;
import net.mjrz.scheduler.Scheduler;

public class LoginUtils {
	public static User login(String user, char[] pwd) throws Exception {
		String homedir = Profiles.getInstance().getPathForProfile(user);

		initializeLogFile(homedir);
		
		ZProperties.loadUserProperties(homedir.toString());

		ZProperties.replaceRuntimeProperty("FMHOME", homedir.toString());

		HibernateUtils.initialize(user);

		CHelper.init(pwd);

		FManEntityManager em = new FManEntityManager();

		List<Object> objects = em
				.getObjects("PwdHistory", "uid='" + user + "'");

		if (objects != null && objects.size() > 0) {
			int sz = objects.size();
			PwdHistory ph = (PwdHistory) objects.get(sz - 1);
			String base = ph.getBasePassword();
			String dec = CHelper.decrypt(base);

			CHelper.init(dec.toCharArray());
			User u = getInitialUser(user);
			if (u.getUserPassword().equals(dec)) {
				return u;
			}
		}
		else {
			CHelper.init(pwd);
			User u = getInitialUser(user);
			if (Arrays.equals(u.getUserPassword().toCharArray(), pwd)) {
				return u;
			}
		}
		return null;
	}

	public static User getInitialUser(String user) throws Exception {
		try {
			User u = new FManEntityManager().getUser("fmuser");
			return u;
		}
		catch (Exception e) {
			throw e;
		}
	}

	private static void initializeMessages() {
		net.mjrz.fm.utils.Messages.initializeMessages();
	}

	private static Locale getLocaleFromString(String s) {
		if (s == null) {
			return new Locale("en", "US");
		}
		String[] split = s.split("_");
		if (split.length == 2) {
			return new Locale(split[0], split[1]);
		}
		return new Locale("en", "US");
	}

	// public static void initialize(final User u, String profile, Locale
	// locale, Locale currency) throws Exception {
	public static void initialize(final User u, String profile)
			throws Exception {
		try {
			ZProperties.loadUserProperties(ZProperties.getProperty("FMHOME")
					+ Main.PATH_SEPARATOR + "conf");

			String currencyLocaleStr = ZProperties
					.getProperty("CURRENCY.LOCALE");
			Locale currencyLocale = getLocaleFromString(currencyLocaleStr);

			String languageLocaleStr = ZProperties
					.getProperty("LANGUAGE.LOCALE");
			Locale languageLocale = getLocaleFromString(languageLocaleStr);

			SessionManager.startSession(u.getUid(), u.getUserPassword(),
					profile, languageLocale, currencyLocale);
			AlertsCache.initialize();
			Scheduler.initialize();

			LoginUtils.initializeMessages();
			AlertsEntityManager.updateAllAlerts();
			// GuiUtilities.preloadUIPrefs();

			new FManEntityManager().initializeTT(u);

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						FinanceManagerUI mainWindow = new FinanceManagerUI();
						mainWindow
								.setIconImage(java.awt.Toolkit
										.getDefaultToolkit()
										.getImage(
												LoginUtils.class
														.getClassLoader()
														.getResource(
																"icons/icon_money.png")));
						mainWindow.pack();
						mainWindow.setExtendedState(mainWindow
								.getExtendedState() | JFrame.MAXIMIZED_BOTH);
						mainWindow.setVisible(true);
						mainWindow
								.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

						/* Start scheduler service after the ui has started. */
						SchedulerService.start(u);
					}
					catch (Exception e) {
						net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
						System.exit(0);
					}
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
			net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
			System.exit(0);
		}
	}
	
	private static boolean initializeLogFile(String profilePath) {
		try {
			File f = new File(profilePath + Main.PATH_SEPARATOR + "logs");
			if (!f.exists()) {
				if(!f.mkdirs()) {
					return false;
				}
			}
			
			String logFileName = f.getAbsolutePath() + Main.PATH_SEPARATOR + "fm.log";
			System.setProperty("logfile.name", logFileName);
			
			PropertyConfigurator.configure(LoginUtils.class.getClassLoader().getResource("log4j.properties"));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}	
}
