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
package net.mjrz.fm.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.mjrz.fm.services.SessionManager;

public class Messages {
	private static final String BUNDLE_NAME = "resources.MessagesBundle"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
			BUNDLE_NAME, SessionManager.getCurrentLocale());

	private Messages() {
	}

	public static void initializeMessages() {
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,
				SessionManager.getCurrentLocale());
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e) {
			return key;
		}
	}

	public static String tr(String key) {
		return getString(key);
	}

	public static boolean hasTranslation(String key) {
		try {
			return RESOURCE_BUNDLE.containsKey(key);
		}
		catch (NullPointerException e) {
			return false;
		}
	}
}
