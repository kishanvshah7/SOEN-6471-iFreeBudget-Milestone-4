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

import java.awt.geom.Point2D;

public class GraphPoint {
	Point2D point;
	double yValue;
	String xValue;

	public GraphPoint() {
		point = new Point2D.Double(0, 0);
		xValue = "";
		yValue = 0;
	}

	public GraphPoint(Point2D point, String xVal, double yVal) {
		this.point = point;
		this.xValue = xVal;
		this.yValue = yVal;
	}

	public Point2D getPoint() {
		return point;
	}

	public void setPoint(Point2D point) {
		this.point = point;
	}

	public double getYValue() {
		return yValue;
	}

	public void setYValue(double value) {
		yValue = value;
	}

	public String getXValue() {
		return xValue;
	}

	public void setXValue(String value) {
		xValue = value;
	}
}
