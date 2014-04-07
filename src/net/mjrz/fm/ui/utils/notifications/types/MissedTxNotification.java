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
package net.mjrz.fm.ui.utils.notifications.types;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MissedTxNotification implements UINotification {
	private int notificationType;
	private String message;
	private Map<String, List<Date>> missed;

	public MissedTxNotification(Map<String, List<Date>> missed) {
		notificationType = WARN;
		this.missed = missed;
		StringBuilder sb = new StringBuilder();
		sb.append("<html><br>");
		sb.append(missed.size());
		sb.append(" Scheduled transactions were missed.");
		sb.append("</br></html>");
		message = sb.toString();
	}

	public MissedTxNotification(int type, String msg) {
		notificationType = type;
		message = msg;
	}

	public int getNotificationType() {
		return this.notificationType;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return notificationType + " - " + message;
	}

	public Map<String, List<Date>> getMissed() {
		return missed;
	}

	public void setMissed(Map<String, List<Date>> missed) {
		this.missed = missed;
	}
}
