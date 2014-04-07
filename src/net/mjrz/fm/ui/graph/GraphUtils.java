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
package net.mjrz.fm.ui.graph;

public class GraphUtils {
	public static final double roundDouble(double d, int places) {
		return Math.round(d * Math.pow(10, (double) places))
				/ Math.pow(10, (double) places);
	}

	public static double getM(double d) {
		double x = 10;
		while (true) {
			double tmp = d * x;
			if (Math.ceil(tmp) == tmp)
				break;
			else
				x = x * 10;
		}
		return x;
	}
}
