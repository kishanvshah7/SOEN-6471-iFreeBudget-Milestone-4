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
package net.mjrz.fm.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.mjrz.fm.ui.panels.prefs.PreferencesPanel;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class SessionManager {
	private static long sessionId;
	private static long sessionUserId;
	private static Date sessionStartTime;
	private static String sessionPassword;
	private static Locale currentLocale;
	private static Locale currencyLocale;
	private static String sessionProfile;
	private static String dateFormat;
	private static SessionManager instance;

	private SessionManager() {
	}

	public synchronized static void startSession(long uid, String password,
			String profile, Locale locale, Locale currency) {
		clearSession();
		if (instance == null) {
			GuiUtilities.preloadUIPrefs();

			instance = new SessionManager();
			sessionUserId = uid;
			sessionStartTime = new Date();
			sessionId = new java.util.Random().nextLong();
			sessionPassword = password;
			currentLocale = locale;
			currencyLocale = currency;
			sessionProfile = profile;
			net.mjrz.fm.utils.crypto.CHelper.init(password.toCharArray());
			initializeDateFormat();
		}
	}

	private static void clearSession() {
		sessionId = 0;
		sessionUserId = 0;
		sessionStartTime = null;
		sessionPassword = null;
		currentLocale = null;
		currencyLocale = null;
		sessionProfile = null;
		instance = null;
	}

	public static Locale getCurrentLocale() {
		if (currentLocale == null) {
			currentLocale = new Locale("en", "US");
		}
		return currentLocale;
	}

	public static Locale getCurrencyLocale() {
		if (currencyLocale == null) {
			currencyLocale = new Locale("en", "US");
		}
		return currencyLocale;
	}

	public static boolean isSessionAlive() {
		return instance == null;
	}

	public static long getSessionUserId() {
		return sessionUserId;
	}

	public static String getSessionProfile() {
		return sessionProfile;
	}

	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat(dateFormat);
	}

	public static void setDateFormat(String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			if (format == null || format.length() == 0) {
				dateFormat = PreferencesPanel.DEFAULT_DATE_FORMAT;
			}
			else {
				sdf.applyPattern(format);
				dateFormat = format;
			}
		}
		catch (Exception e) {
			Logger.getLogger(SessionManager.class).error(
					"Invalid format for date : " + format);
			Logger.getLogger(SessionManager.class).error(e.getMessage());
			dateFormat = PreferencesPanel.DEFAULT_DATE_FORMAT;
		}
	}

	private static void initializeDateFormat() {
		try {
			String format = ZProperties
					.getProperty(PreferencesPanel.DATE_FORMAT_PROPERTY);
			if (format == null || format.length() == 0) {
				format = PreferencesPanel.DEFAULT_DATE_FORMAT;
			}
			// SimpleDateFormat sdf = new SimpleDateFormat(format);
			ZProperties.replaceRuntimeProperty(
					PreferencesPanel.DATE_FORMAT_PROPERTY, format);
			dateFormat = format;
		}
		catch (Exception e) {
			dateFormat = PreferencesPanel.DEFAULT_DATE_FORMAT;
		}
	}
}
