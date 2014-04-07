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

public class UpdateCheckNotification implements UINotification {
	public static final int INFO = 1;
	public static final int WARN = 2;
	public static final int ERROR = 3;

	private int id;
	private int notificationType;
	private String message;

	private UpdateCheckNotification() {
	}

	public UpdateCheckNotification(String message) {
		notificationType = INFO;
		this.message = message;
	}

	public UpdateCheckNotification(int type, String msg) {
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
}
