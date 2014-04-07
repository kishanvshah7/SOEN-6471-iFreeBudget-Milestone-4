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
package net.mjrz.fm.utils.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.TaskEntityManager;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.utils.notifications.types.ScheduledTxNotification;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.scheduler.task.BasicTask;

import org.apache.log4j.Logger;

public class ScheduledTx extends BasicTask {
	private static Logger logger = Logger
			.getLogger(ScheduledTx.class.getName());
	private long txId = -1;
	private ActionResponse resp;

	public ScheduledTx(String name, long txId) {
		super(name);
		this.txId = txId;
	}

	public long getTxId() {
		return txId;
	}

	@Override
	public void executeTask() {
		try {
			logger.debug("Scheduled transaction id = " + txId);
			// createTx();
			submitNotification();

		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		finally {
			done = true;
			cancelled = true;
			runCount++;
			updateTask();
		}
	}

	private void submitNotification() {
		resp = new ActionResponse();
		ScheduledTxNotification notification = new ScheduledTxNotification(
				getName(), txId);
		net.mjrz.fm.ui.utils.NotificationHandler.addToQueue(notification);
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return resp;
	}

	private void createTx() throws Exception {
		FManEntityManager em = new FManEntityManager();
		long uid = SessionManager.getSessionUserId();
		boolean isUpdate = false;
		Date today = new Date();

		ArrayList<Transaction> txList = new ArrayList<Transaction>();
		User user = em.getUser(uid);
		Transaction curr = em.getTransaction(user, txId);
		if (curr != null) {
			curr.setFitid(null);
			curr.setTxDate(today);
			curr.setCreateDate(today);
			curr.setActivityBy("Scheduled transaction");
			txList.add(curr);

			ArrayList<Transaction> tmp = (ArrayList<Transaction>) em
					.getChildTransactions(user, curr.getTxId());
			if (tmp != null && tmp.size() > 0) {
				for (Transaction t : tmp) {
					t.setFitid(null);
					t.setTxDate(today);
					t.setCreateDate(today);
					t.setActivityBy("Scheduled transaction");
					txList.add(t);
				}
			}
		}

		ActionRequest req = new ActionRequest();
		req.setActionName("addNestedTransaction");
		req.setUser(user);
		req.setProperty("TXLIST", txList);
		req.setProperty("UPDATETX", isUpdate);

		resp = new AddNestedTransactionsAction().executeAction(req);
		if (resp.getErrorCode() == ActionResponse.NOERROR) {
			logger.info("Added tx");
			resp.addResult("TXLIST", txList);
		}
		else {
			logger.error("Unable to add scheduled tx");
		}
	}

	private void updateTask() {
		TaskEntityManager tm = new TaskEntityManager();
		try {
			tm.updateTask(this);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}
}
