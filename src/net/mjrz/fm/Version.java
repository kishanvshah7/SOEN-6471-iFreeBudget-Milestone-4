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

import net.mjrz.fm.ui.utils.UIDefaults;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public final class Version {
	private String versionName;
	private int vMajor;
	private int vMinor;
	private int vPoint;

	private String relStatus;

	private static Version version = new Version();

	private Version() {
		this.versionName = UIDefaults.PRODUCT_TITLE;
		this.vMajor = 2;
		this.vMinor = 0;
		this.vPoint = 38;
		this.relStatus = "";
	}

	public static Version getVersion() {
		return version;
	}

	public String getVersionId() {
		StringBuffer ret = new StringBuffer();
		ret.append(vMajor);
		ret.append(".");
		ret.append(vMinor);
		ret.append(".");
		ret.append(vPoint);

		return ret.toString();
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(versionName);
		ret.append(", Ver. ");
		ret.append(vMajor);
		ret.append(".");
		ret.append(vMinor);
		ret.append(".");
		ret.append(vPoint);
		ret.append(" ");
		if (relStatus.length() > 0)
			ret.append("(" + relStatus + ")");

		return ret.toString();
	}

	public int getMajorVersion() {
		return vMajor;
	}

	public int getMinorVersion() {
		return vMinor;
	}

	public int getPointVersion() {
		return vPoint;
	}

	public boolean isVersionGreater(String version) {
		if (version == null || version.trim().length() == 0)
			return false;

		java.util.ArrayList<String> split = net.mjrz.fm.utils.MiscUtils
				.splitString(version, ".");
		try {
			Integer otherMajor = Integer.parseInt(split.get(0));
			Integer otherMinor = Integer.parseInt(split.get(1));
			Integer otherPoint = Integer.parseInt(split.get(2));

			if (otherMajor > vMajor)
				return true;
			if (otherMajor < vMajor)
				return false;

			if (otherMinor > vMinor)
				return true;
			if (otherMinor < vMinor)
				return false;

			if (otherPoint > vPoint)
				return true;

			return false;
		}
		catch (Exception e) {
			return false;
		}
	}
}
