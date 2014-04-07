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
package net.mjrz.fm.entity.beans.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.mjrz.fm.utils.crypto.CHelper;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class EDouble implements UserType {

	public final void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.VARBINARY);
		}
		else {
			noNullSet(st, value, index);
		}
	}

	protected void noNullSet(PreparedStatement st, Object value, int index)
			throws SQLException {
		try {
			BigDecimal l = (BigDecimal) value;
			String res = CHelper.encrypt(l.toString());
			st.setString(index, res);
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		try {
			String val = rs.getString(names[0]);
			String res = CHelper.decrypt(val);
			if (res == null) {
				return new BigDecimal(0d);
			}
			BigDecimal l = new BigDecimal(res);
			return l;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		}
		if (x == null) {
			return false;
		}
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return target;
	}

	public final int[] sqlTypes() {
		return new int[] { Types.LONGVARBINARY };
	}

	public Class returnedClass() {
		return String.class;
	}
}
