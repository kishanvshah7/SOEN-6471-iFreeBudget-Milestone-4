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

import net.mjrz.fm.ui.utils.notifications.INotificationElementVisitor;
import net.mjrz.fm.ui.utils.notifications.NotificationDisplay;

public class ScheduledTxNotification implements INotificationElement {
	private long txId;
	private int notificationType;
	private String message;

	public ScheduledTxNotification(String taskName, long txId) {
		notificationType = WARN;
		StringBuilder sb = new StringBuilder();
		sb.append("<html><br>&nbsp;&nbsp;");
		sb.append("Scheduled transaction : ");
		sb.append(taskName);
		sb.append("</br></html>");
		message = sb.toString();
		this.txId = txId;
	}

	public ScheduledTxNotification(int type, String msg) {
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

	public long getTxId() {
		return txId;
	}

	public void setTxId(long txId) {
		this.txId = txId;
	}

    @Override
    public NotificationDisplay accept(INotificationElementVisitor visitor) {
        return visitor.visit(this);//To change body of generated methods, choose Tools | Templates.
    }
}
