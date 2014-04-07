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

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.panels.ofx.TxObject;
import net.mjrz.fm.utils.indexer.IndexedEntity;
import net.mjrz.fm.utils.indexer.Indexer;
import net.mjrz.fm.utils.indexer.MatchedEntity;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class XMLProcessor {
	private Document document;

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Account account;

	private FManEntityManager fman;

	private User user;

	private ArrayList<TxObject> txList;

	private InputSource src = null;

	private String sourceType = null;

	private ArrayList accountsList = null;

	private BigDecimal ledgerBalance = null;

	private String balanceAsOfDate = null;

	private static Logger logger = Logger.getLogger(XMLProcessor.class
			.getName());

	public XMLProcessor(String xml, User user) throws Exception {
		StringReader in = null;
		txList = new ArrayList<TxObject>();
		this.user = user;

		try {
			fman = new FManEntityManager();
			in = new StringReader(xml);
			src = new InputSource(in);

			accountsList = (ArrayList) fman.getAccountsForUser(SessionManager
					.getSessionUserId());

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(src);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (in != null)
				in.close();
		}
	}

	public String validate() {
		String msg = "";

		NodeList nlist = document.getElementsByTagName("CODE");
		int i = 0;
		for (; i < nlist.getLength(); i++) {
			Node n = nlist.item(i).getParentNode();
			if (n != null) {
				Node gp = n.getParentNode();
				String nname = gp.getNodeName();
				if (nname.equalsIgnoreCase("STMTTRNRS")) {
					break;
				}
				if (nname.equalsIgnoreCase("CCSTMTTRNRS")) {
					break;
				}
			}
		}
		if (i >= nlist.getLength()) {
			msg = "No transactions to import";
			return msg;
		}
		if (i < nlist.getLength()) {
			Node x = nlist.item(i);
			if (!x.getTextContent().trim().equals("0")) {
				Node parent = x.getParentNode(); // Status node
				NodeList nl = parent.getChildNodes();
				for (int j = 0; j < nl.getLength(); j++) {
					Node tmp = nl.item(j);
					if (tmp.getNodeName().equalsIgnoreCase("MESSAGE")) {
						return tmp.getTextContent();
					}
				}
				return "Unknown error";
			}
		}
		return "";
	}

	public void processXML() throws Exception {
		try {
			String from = getAccountNumber();
			account = fman.getAccountFromNumber(user.getUid(), from);

			if (account == null) {
				throw new IllegalArgumentException("Account doesnot exist yet");
			}

			getTransactions();
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void getTransactions() {
		NodeList list = document.getElementsByTagName("STMTTRN");
		int sz = list.getLength();
		for (int i = 0; i < sz; i++) {
			buildTxObject(list.item(i));
		}
	}

	private void getLedgerBal() {
		NodeList nlist = document.getElementsByTagName("LEDGERBAL");
		int i = 0;
		for (; i < nlist.getLength(); i++) {
			Node n = nlist.item(i);
			NodeList children = n.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node x = children.item(j);
				if (x.getNodeType() == Node.ELEMENT_NODE) {
					String val = x.getTextContent();
					if (x.getNodeName().equals("BALAMT")) {
						ledgerBalance = new BigDecimal(val);
					}
					if (x.getNodeName().equals("DTASOF")) {
						balanceAsOfDate = val;
					}
				}
			}
		}
	}

	public ArrayList<TxObject> getTxList() {
		return txList;
	}

	public BigDecimal getLedgerBalance() {
		getLedgerBal();
		return ledgerBalance;
	}

	private void buildTxObject(Node n) {
		NodeList children = n.getChildNodes();

		BigDecimal amt = new BigDecimal(0d);
		Date dt = new Date();
		String dest = String.valueOf("");
		String notes = String.valueOf("");
		String fitid = String.valueOf("");

		for (int j = 0; j < children.getLength(); j++) {
			Node x = children.item(j);
			if (x.getNodeType() == Node.ELEMENT_NODE) {
				String val = x.getTextContent();
				if (x.getNodeName().equals("TRNAMT")) {
					amt = new BigDecimal(val);
					continue;
				}
				if (x.getNodeName().equals("DTPOSTED")) {
					try {
						dt = sdf.parse(parseDate(val));
					}
					catch (java.text.ParseException e) {
						System.out.println(e.getMessage());
						dt = new Date();
					}
					continue;
				}
				if (x.getNodeName().equals("NAME")) {
					dest = buildValidAccountName(val);
					/* Acct names are limited to 30 chars */
					if (dest.length() >= 30) {
						dest = dest.substring(0, 30);
					}
					continue;
				}
				if (x.getNodeName().equals("MEMO")) {
					notes = val;
					continue;
				}
				if (x.getNodeName().equals("FITID")) {
					fitid = val;
					continue;
				}
			}
		}

		try {
			Indexer indexer = Indexer.getIndexer();

			String line = dest + " " + notes;

			MatchedEntity me = indexer.match(Indexer.IndexType.Account, line);

			if (me == null) {
				IndexedEntity ie = new IndexedEntity();
				ie.setName(dest);
				ie.setCount(1);

				if (amt.doubleValue() < 0) { // debig
					ie.setType(AccountTypes.ACCT_TYPE_EXPENSE);
				}
				else { // credit
					ie.setType(AccountTypes.ACCT_TYPE_INCOME);
				}

				me = new MatchedEntity(ie, new BigDecimal(1));
			}
			TxObject tx = new TxObject();
			tx.setSource(account);
			tx.setDate(dt);
			tx.setAmount(amt);
			tx.setNotes(line);
			tx.setMatch(me.getEntity());
			tx.setDoImport(true);
			tx.setFitId(fitid);
			tx.setOfxLine(line);

			txList.add(tx);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public Account getAccount() {
		if (account == null) {
			String from = getAccountNumber();
			try {
				Account account = fman
						.getAccountFromNumber(user.getUid(), from);
				return account;
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}
		return account;
	}

	public String getAccountNumber() {
		NodeList nlist = document.getElementsByTagName("BANKACCTFROM");
		sourceType = "BANKACCT";

		if (nlist == null || nlist.getLength() == 0) {
			nlist = document.getElementsByTagName("CCACCTFROM");
			sourceType = "CCACCT";
		}

		if (nlist != null && nlist.getLength() > 0) {
			Node n = nlist.item(0);
			NodeList children = n.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node x = children.item(i);
				if (x.getNodeName().equals("ACCTID")) {
					return x.getTextContent();
				}
			}
		}
		return null;
	}

	private String parseDate(String in) throws ParseException {
		StringBuffer dt = new StringBuffer();
		if (in.length() < 8)
			throw new ParseException("Invalid date format", in.length());

		dt.append(in.substring(0, 4));
		dt.append("-");
		dt.append(in.substring(4, 6));
		dt.append("-");
		dt.append(in.substring(6, 8));

		return dt.toString();
	}

	private String buildValidAccountName(String val) {
		StringBuilder ret = new StringBuilder();

		String[] split = val.split(" ");

		for (int i = 0; i < split.length; i++) {
			String tok = split[i];
			if (isValidToken(tok)) {
				ret.append(tok);
				ret.append(" ");
			}
		}
		return ret.toString().trim();
	}

	private boolean isValidToken(String tok) {
		boolean isValid = false;
		for (int j = 0; j < tok.length(); j++) {
			if (Character.isLetter(tok.charAt(j))) {
				isValid = true;
				break;
			}
		}
		return isValid;
	}
}
