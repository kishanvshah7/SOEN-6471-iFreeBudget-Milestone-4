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
package net.mjrz.fm;

import java.io.File;
import java.net.ProxySelector;
import java.util.Date;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.utils.Splash;
import net.mjrz.fm.utils.LoginUtils;
import net.mjrz.fm.utils.NProxySelector;
import net.mjrz.fm.utils.Profiles;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class Main {
	public static String homedir = System.getProperty("user.dir");
		
	public static final String PATH_SEPARATOR = System
			.getProperty("file.separator");

	private static String sessionPassword = null;

	private static Splash splash = null;

	public static boolean profileExists(String uname) {
		return Profiles.getInstance().profileExists(uname);
	}

	static User createInitialUser(String path, String user, char[] password) {
		HibernateUtils.initialize(user);
		User u = new User();
		u.setUid(1);
		u.setCreateDate(new Date());
		u.setUserName("fmuser");
		u.setUserPassword(new String(password));

		try {
			FManEntityManager em = new FManEntityManager();
			em.addUser(u);
			Profiles.getInstance().addProfile(user,
					path + Main.PATH_SEPARATOR + user);
			return u;
		}
		catch (Exception e) {
			return null;
		}
	}

	static boolean checkDataFiles(String path, String user) {
		try {
			File f = new File(path + Main.PATH_SEPARATOR + user);
			boolean ret = false;
			if (f.exists()) {
				ret = true;
			}
			else {
				ret = f.mkdirs();
			}
			ret = checkFilterFiles(path, user);
			ret = checkConfigFiles(path, user);
			ZProperties.addRuntimeProperty("FMHOME", f.getAbsolutePath());
			return ret;
		}
		catch (Exception e) {
			return false;
		}
	}

	private static boolean checkFilterFiles(String homedir, String user) {
		try {
			File f = new File(homedir + PATH_SEPARATOR + user + PATH_SEPARATOR
					+ "filters");

			if (f.exists()) {
				return true;
			}
			return f.mkdir();
		}
		catch (Exception e) {
			return false;
		}
	}

	private static boolean checkLogFiles(String profilePath) {
		try {
			File f = new File(profilePath + Main.PATH_SEPARATOR + "fmdata"
					+ Main.PATH_SEPARATOR + "logs");
			if (!f.exists()) {
				if (!f.mkdirs()) {
					return false;
				}
			}

			String logFileName = f.getAbsolutePath() + Main.PATH_SEPARATOR
					+ "fm.log";
			System.setProperty("logfile.name", logFileName);
			System.out.println("System log file: " + logFileName);
			PropertyConfigurator.configure(LoginUtils.class.getClassLoader()
					.getResource("log4j.properties"));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private static boolean checkProileFiles(String homedir) {
		try {
			File f = new File(homedir + PATH_SEPARATOR + "fmdata"
					+ PATH_SEPARATOR + "profiles");
			if (f.exists()) {
				return true;
			}
			return f.mkdirs();
		}
		catch (Exception e) {
			return false;
		}
	}

	private static boolean checkConfigFiles(String homedir, String user) {
		try {
			File f = new File(homedir + PATH_SEPARATOR + user + PATH_SEPARATOR
					+ "conf");
			if (f.exists()) {
				return true;
			}
			return f.mkdir();
		}
		catch (Exception e) {
			return false;
		}
	}

	public static String getSessionPassword() {
		return sessionPassword;
	}

	private static void initializeUIDefaults() throws Exception {
		LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();
		boolean hasNimbusLaf = false;
		for (int i = 0; i < laf.length; i++) {
			if (laf[i].getClassName().equals(
					"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
				hasNimbusLaf = true;
				break;
			}
		}
		if (hasNimbusLaf) {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		}
		else {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		UIManager.put("TabbedPane.selectedForeground", java.awt.Color.white);
		UIManager.put("TabbedPane.unselectedTabForeground",
				java.awt.Color.black);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	private static void checkFS() {
		checkLogFiles(homedir);
		checkProileFiles(homedir);
	}

	public static void FMMain(String args[]) {
		try {
			ZProperties.initialize();

			ZProperties.loadSystemProperties();

			splash.updateStatus("Loading system properties");

			ProxySelector selector = new NProxySelector();
			ProxySelector.setDefault(selector);
			LoginPanel.getLoginFrame();

			splash.updateStatus("Done");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("Fatal error occured initializing application. Exiting.!!!");
			net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
		}
		finally {
			splash.dispose();
		}
	}

	public static void main(final String args[]) {
		try {
			checkFS();

			splash = Splash.getInstance();

			initializeUIDefaults();

			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					FMMain(args);
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// class ShutdownHook extends Thread {
	// public void run() {
	// net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
	// }
	// }
}
