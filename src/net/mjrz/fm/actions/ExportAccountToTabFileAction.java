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
package net.mjrz.fm.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Transaction;

public class ExportAccountToTabFileAction extends ExportAccountAction {

	@Override
	void writeFile(long userId, Account exportAccount, String destFile,
			List<Transaction> txList) throws Exception {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(new File(destFile)));

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			FManEntityManager em = new FManEntityManager();
			for (Transaction t : txList) {
				BigDecimal amt = t.getTxAmount();
				Account other = null;
				if (t.getFromAccountId() == exportAccount.getAccountId()) {
					other = em.getAccount(userId, t.getToAccountId());
					amt = amt.multiply(new BigDecimal(String.valueOf(-1)));
				}
				else {
					other = em.getAccount(userId, t.getFromAccountId());
				}

				StringBuilder sb = new StringBuilder();

				sb.append(getQuotedString(sdf.format(t.getTxDate())));
				sb.append("\t");

				sb.append(getQuotedString(other.getAccountName()));
				sb.append("\t");

				sb.append(getQuotedString(amt.toString()));
				sb.append("\t");

				sb.append(getQuotedString(t.getTxNotes()));

				sb.append("\r\n");
				out.write(sb.toString());
			}
		}
		finally {
			if (out != null)
				out.close();
		}
	}

	private String getQuotedString(String val) {
		StringBuilder ret = new StringBuilder("\"");
		ret.append(val);
		ret.append("\"");
		return ret.toString();
	}
}
