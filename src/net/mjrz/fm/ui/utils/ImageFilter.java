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
package net.mjrz.fm.ui.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ImageFilter extends FileFilter {
	static final String TYPE_JPG = "jpg";
	static final String TYPE_PNG = "png";

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			if (extension.equalsIgnoreCase(ImageFilter.TYPE_JPG)
					|| extension.equalsIgnoreCase(ImageFilter.TYPE_PNG)) {
				return true;
			}
			else {
				return false;
			}
		}

		return false;
	}

	public String getDescription() {
		return "JPG / PNG";
	}

	public static String getExtension(File f) {
		int pos = f.getName().lastIndexOf('.');
		if (pos >= 0)
			return f.getName().substring(pos);
		else
			return "";
	}
}
