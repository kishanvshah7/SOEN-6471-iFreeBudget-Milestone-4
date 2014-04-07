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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class NScale {
	// public static void main(String args[]) {
	// Random r = new Random();
	// ArrayList<Double> jlist = new ArrayList<Double>();
	// for(int i = 0; i < 10; i++) {
	// jlist.add(r.nextDouble() * 1000 + 1);
	// }
	// Collections.sort(jlist);
	// scale(jlist, 540);
	// }

	public static ArrayList<Point2D> scale(ArrayList<Double> list, int gW,
			double mFac, int pageSize) {
		int sz = list.size();
		ArrayList<Double> xaxis = new ArrayList<Double>();
		ArrayList<Double> yaxis = new ArrayList<Double>();

		ArrayList<Double> temp = new ArrayList<Double>(list);
		Collections.sort(temp);

		double max = 0;
		double div = 1;

		for (int i = 0; i < sz; i++) {
			double curr = list.get(i);
			if (curr > 0 && curr < 1.0)
				curr = Math.ceil(curr);
			if (curr > 0 && curr < -1.0)
				curr = Math.ceil(curr);

			/* Handle special cases. Log 0 is inf. */
			double a = i;
			int pos = temp.indexOf(curr);
			if (pos > 0) {
				div = pos;
			}
			double s = 0;
			if (curr == 0) {
				s = 0;
			}
			else if (curr == 1) {
				s = (Math.log(2) / 2) * div;
			}
			else {
				s = Math.log(curr) * div;
			}
			xaxis.add(a);
			yaxis.add(s);

			if (s > max) {
				max = s;
			}
		}
		double xsf = gW / xaxis.get(pageSize - 1);
		double ysf = (gW / max) / 2;

		ArrayList<Point2D> ret = new ArrayList<Point2D>();
		int xtmp = 0;
		for (int i = 0; i < xaxis.size(); i++) {
			double c = ysf * yaxis.get(i);
			// Point2D.Double p = new Point2D.Double((xaxis.get(i) * xsf), c);
			if (xtmp >= pageSize) {
				xtmp = 0;
			}
			Point2D.Double p = new Point2D.Double(xtmp * xsf, c);
			ret.add(p);
			xtmp++;
		}
		return ret;
	}

	public static ArrayList<Point2D> scale(ArrayList<Double> list, int gW,
			double mFac) {
		int sz = list.size();
		ArrayList<Double> xaxis = new ArrayList<Double>();
		ArrayList<Double> yaxis = new ArrayList<Double>();

		ArrayList<Double> temp = new ArrayList<Double>(list);
		Collections.sort(temp);

		double max = 0;
		double div = 1;
		for (int i = 0; i < sz; i++) {
			// double curr = list.get(i) * mFac;

			double curr = list.get(i);
			if (curr > 0 && curr < 1.0)
				curr = Math.ceil(curr);
			if (curr > 0 && curr < -1.0)
				curr = Math.ceil(curr);

			/* Handle special cases. Log 0 is inf. */
			double a = i;
			int pos = temp.indexOf(curr);
			if (pos > 0) {
				div = pos;
			}
			double s = 0;
			if (curr == 0) {
				s = 0;
			}
			else if (curr == 1) {
				s = (Math.log(2) / 2) * div;
			}
			else {
				s = Math.log(curr) * div;
			}
			xaxis.add(a);
			yaxis.add(s);

			if (s > max) {
				max = s;
			}
		}
		double xsf = gW / xaxis.get(xaxis.size() - 1);
		double ysf = (gW / max) / 2;

		ArrayList<Point2D> ret = new ArrayList<Point2D>();
		for (int i = 0; i < xaxis.size(); i++) {
			double c = ysf * yaxis.get(i);
			Point2D.Double p = new Point2D.Double((xaxis.get(i) * xsf), c);
			ret.add(p);
		}
		return ret;
	}
}
