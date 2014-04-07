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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import net.mjrz.fm.Main;
import net.mjrz.fm.ui.utils.UIDefaults;

public class PropertiesUtils {
	public static void saveSettings(String propertiesFile, String property,
			String value) throws Exception {
		FileWriter out = null;
		FileReader in = null;
		try {
			String dir = ZProperties.getProperty("FMHOME")
					+ Main.PATH_SEPARATOR + "conf";
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
			File pfile = new File(f, propertiesFile);

			Properties p = new Properties();

			if (pfile.exists()) {
				in = new FileReader(pfile);
				p.load(in);
			}
			p.setProperty(property, value);

			out = new FileWriter(pfile);

			p.store(out, UIDefaults.FINANCE_MANAGER_TITLE
					+ " Properties - Do not edit");
			ZProperties.replaceRuntimeProperty(property, value);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
		finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
		}
	}
}
