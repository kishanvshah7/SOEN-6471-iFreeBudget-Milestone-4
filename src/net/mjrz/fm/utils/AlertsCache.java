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

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.ui.utils.notifications.types.AlertNotification;

import org.apache.log4j.Logger;

public class AlertsCache {
	private HashMap<Long, Alert> cache;
	private static AlertsCache instance;
	private static Logger logger = Logger
			.getLogger(AlertsCache.class.getName());

	private AlertsCache() {
		cache = new HashMap<Long, Alert>();
	}

	public synchronized static void initialize() {
		if (instance == null) {
			instance = new AlertsCache();
			loadCache();
		}
	}

	public synchronized static AlertsCache getInstance()
			throws java.lang.Exception {
		if (instance == null) {
			throw new RuntimeException(
					"Cache not initialized. Please call AlertsCache.initialize() first.");
		}
		return instance;
	}

	public synchronized static void disposeInstance() {
		if (instance == null)
			return;
		instance.cleanup();
		instance = null;
	}

	private void cleanup() {
		AlertsEntityManager em = new AlertsEntityManager();
		Set<Long> keys = cache.keySet();
		for (Long l : keys) {
			Alert a = cache.get(l);
			try {
				if (a == null) {
					System.out.println("Null alert for" + l);
				}
				else {
					em.saveAlert(a);
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}
		cache.clear();
		cache = null;
	}

	private static void loadCache() {
		if (instance == null)
			return;
		instance.populate();
	}

	private void populate() {
		AlertsEntityManager em = new AlertsEntityManager();
		if (cache == null) {
			cache = new HashMap<Long, Alert>();
		}
		try {
			List<Alert> alerts = em.getAllAlerts();
			if (alerts == null)
				return;
			for (Alert a : alerts) {
				cache.put(a.getAccountId(), a);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void addToCache(Alert alert) {
		cache.put(alert.getAccountId(), alert);
	}

	public void setAlert(Long accountId, int status) {
		Alert a = cache.get(accountId);
		if (a == null) {
			return;
		}
		a.setStatus(status);

		/* Add to notification queue to be displayed */
		if (alertRaised(accountId)) {
			AlertNotification notification = new AlertNotification(a);
			net.mjrz.fm.ui.utils.NotificationHandler.addToQueue(notification);
		}
	}

	public boolean alertCleared(Long accountId) {
		Alert a = cache.get(accountId);
		if (a == null)
			return false;
		return a.getStatus() == Alert.ALERT_CLEARED;
	}

	public boolean alertRaised(Long accountId) {
		Alert a = cache.get(accountId);
		if (a == null)
			return false;
		return a.getStatus() == Alert.ALERT_RAISED;
	}

	public void removeAlert(Long accountId) {
		cache.remove(accountId);
	}
}
