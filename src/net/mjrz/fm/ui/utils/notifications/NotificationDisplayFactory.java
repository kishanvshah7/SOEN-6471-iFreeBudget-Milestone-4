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
package net.mjrz.fm.ui.utils.notifications;

import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.notifications.types.AlertNotification;
import net.mjrz.fm.ui.utils.notifications.types.MissedTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.ScheduledTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.UINotification;
import net.mjrz.fm.ui.utils.notifications.types.UpdateCheckNotification;

public class NotificationDisplayFactory {

	public static NotificationDisplay getNotificationDisplay(
			final UINotification notification, final FinanceManagerUI frame) {
		if (notification instanceof AlertNotification) {
			return new AlertNotificationFrame(notification, frame);
		}
		if (notification instanceof UpdateCheckNotification) {
			return new UpdateCheckNotificationFrame(notification, frame);
		}
		if (notification instanceof ScheduledTxNotification) {
			return new ScheduledTxNotificationFrame(notification, frame);
		}
		if (notification instanceof MissedTxNotification) {
			return new MissedTxNotificationFrame(notification, frame);
		}
		return null;
	}

}
