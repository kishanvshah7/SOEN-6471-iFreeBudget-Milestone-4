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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ZProperties {
	private static HashMap<String, String> properties = new HashMap<String, String>();
	private static HashMap<String, String> systemProperties = new HashMap<String, String>();

	private ZProperties() {
	}

	public static String getProperty(String key) {
		String val = systemProperties.get(key);
		if (val != null)
			return val;
		return properties.get(key);
	}

	private static void loadProperties(String fname,
			Map<String, String> propertyMap) {
		InputStream fis = null;
		try {
			fis = ZProperties.class.getClassLoader().getResourceAsStream(fname);
			if (fis != null) {
				Properties props = new Properties();
				props.load(fis);
				Set keys = props.keySet();
				for (Object k : keys) {
					propertyMap.put(k.toString(),
							props.getProperty(k.toString()));
				}
				fis.close();
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		finally {
			if (fis != null)
				try {
					fis.close();
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
		}
	}

	public static void loadSystemProperties() {
		loadProperties("addrbookheader.properties", systemProperties);
		loadProperties("errormsgs.properties", systemProperties);
		loadProperties("log4j.properties", systemProperties);
		loadProperties("resources/localesettings.properties", systemProperties);
	}

	public static boolean loadUserProperties(String path) {
		File list[] = new File(path).listFiles();
		if (list == null || list.length == 0)
			return false;

		for (int i = 0; i < list.length; i++) {
			File f = list[i];
			if (isPropertiesFile(f)) {
				BufferedInputStream in = null;
				try {
					in = new BufferedInputStream(new FileInputStream(f));
					Properties props = new Properties();
					props.load(in);
					Set<String> keys = props.stringPropertyNames();
					for (Object k : keys) {
						properties.put(k.toString(),
								props.getProperty(k.toString()));
					}
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					return false;
				}
				finally {
					if (in != null) {
						try {
							in.close();
						}
						catch (Exception e) {
							Logger.getLogger(ZProperties.class.getName())
									.error(e);
						}
					}
				}
			}
		}
		return true;
	}

	private static boolean isPropertiesFile(File f) {
		String fname = f.getName();
		int pos = fname.lastIndexOf('.');
		if (pos >= 0) {
			String ext = fname.substring(pos + 1, fname.length());
			return (ext.equals("properties"));
		}
		return false;
	}

	public static int getNumProperties() {
		return properties.size();
	}

	public static void initialize() {
		properties.clear();
	}

	public static void addRuntimeProperty(String key, String value)
			throws Exception {
		if (properties.containsKey(key)) {
			throw new Exception("Key already exists");
		}
		properties.put(key, value);
	}

	public static void removeRuntimeProperty(String key) throws Exception {
		properties.remove(key);
	}

	public static void replaceRuntimeProperty(String key, String value) {
		properties.put(key, value);
	}
}
