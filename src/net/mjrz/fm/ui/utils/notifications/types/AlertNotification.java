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

import static net.mjrz.fm.utils.Messages.tr;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.ui.utils.notifications.INotificationElementVisitor;
import net.mjrz.fm.ui.utils.notifications.NotificationDisplay;

public class AlertNotification implements INotificationElement {
	private int id;
	private int notificationType;
	private String message;

	private AlertNotification() {
	}

	public AlertNotification(Alert alert) {
		notificationType = INFO;

		StringBuilder msg = new StringBuilder();
		if (alert.getStatus() == Alert.ALERT_RAISED) {
			msg.append(tr("Alert raised on "));
		}
		else if (alert.getStatus() == Alert.ALERT_CLEARED) {
			msg.append(tr("Alert cleared on "));
		}
		else {
			msg.append(tr("Alert on "));
		}
		try {
			long accountId = alert.getAccountId();
			String accountName = FManEntityManager.getAccountNameFromId(
					accountId, false);
			msg.append(accountName);
		}
		catch (Exception e) {
			msg.append("unknown account");
		}
		msg.append("<br>");
		msg.append(alert.toString());
		message = msg.toString();
	}

	public AlertNotification(int type, String msg) {
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

    @Override
    public NotificationDisplay accept(INotificationElementVisitor visitor) {
                        return visitor.visit(this);
    }//To change body of generated methods, choose Tools | Templates.
}
