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

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GraphTuple implements Comparable<GraphTuple> {

	private final Object left;
	private final Double right;

	public Double getRight() {
		return right;
	}

	public Object getLeft() {
		return left;
	}

	public GraphTuple(final Object left, final Double right) {
		this.left = left;
		this.right = right;
	}

	public final boolean equals(Object o) {
		if (!(o instanceof GraphTuple))
			return false;

		final GraphTuple other = (GraphTuple) o;
		return left.equals(other.getLeft()) && right.equals(other.getRight());
	}

	public static final boolean equal(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	public int compareTo(GraphTuple p) {
		return right.compareTo(p.getRight());
	}

	public int hashCode() {
		int hLeft = getLeft() == null ? 0 : getLeft().hashCode();
		int hRight = getRight() == null ? 0 : getRight().hashCode();

		return hLeft + (57 * hRight);
	}

	public String toString() {
		return left + "," + right;
	}

	public static void main(String args[]) {
	}
}
