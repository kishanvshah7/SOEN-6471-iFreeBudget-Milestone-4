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
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class BinaryBlobType implements UserType {
	public int[] sqlTypes() {
		return new int[] { Types.BLOB };
	}

	public Class<?> returnedClass() {
		return byte[].class;
	}

	public boolean equals(Object x, Object y) {
		return (x == y)
				|| (x != null && y != null && java.util.Arrays.equals(
						(byte[]) x, (byte[]) y));
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {
		Blob blob = rs.getBlob(names[0]);
		return blob.getBytes(1, (int) blob.length());
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {
		st.setBlob(index, Hibernate.createBlob((byte[]) value));
	}

	public Object deepCopy(Object value) {
		if (value == null)
			return null;

		byte[] bytes = (byte[]) value;
		byte[] result = new byte[bytes.length];
		System.arraycopy(bytes, 0, result, 0, bytes.length);

		return result;
	}

	public boolean isMutable() {
		return true;
	}

	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return target;
	}

}
