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
package net.mjrz.fm.ui.utils;

import java.awt.Color;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.notifications.NotificationDisplay;
import net.mjrz.fm.ui.utils.notifications.NotificationDisplayFactory;
import net.mjrz.fm.ui.utils.notifications.types.UINotification;

import org.apache.log4j.Logger;

public class NotificationHandler implements Runnable {

	static final int QUEUE_NOTINITED = 0;
	static final int QUEUE_RUNNING = 1;
	static final int QUEUE_PAUSED = 2;

	private int queueStatus;
	private FinanceManagerUI frame;
	private LinkedBlockingQueue<UINotification> notificationQueue;
	private static Logger logger = Logger.getLogger(NotificationHandler.class
			.getName());

	// private static final Color NOTIF_COLOR = new Color(141, 225, 255);
	public static final Color NOTIF_COLOR = new Color(208, 234, 255);

	private static NotificationHandler instance;

	public synchronized static void initialize(FinanceManagerUI frame) {
		if (instance == null) {
			logger.info("Notification handler instantiated");
			instance = new NotificationHandler(frame);
			new Thread(instance).start();
		}
	}

	public static void shutdown() {
		instance.cleanup();
		instance = null;
	}

	private void cleanup() {
		notificationQueue.clear();
	}

	private NotificationHandler(FinanceManagerUI frame) {
		this.frame = frame;
		notificationQueue = new LinkedBlockingQueue<UINotification>();
		this.queueStatus = QUEUE_RUNNING;
	}

	private void showNotificationFrame(final UINotification notif) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					NotificationDisplay messageDisplayFrame = NotificationDisplayFactory
							.getNotificationDisplay(notif, frame);
					if (messageDisplayFrame != null) {
						messageDisplayFrame.setMessageText("<html>"
								+ notif.getMessage() + "<br></html>");
						frame.showJFrameAsSheet((JFrame) messageDisplayFrame);
					}
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		logger.info("Notification queue started");
		while (true) {
			try {
				if (frame.getStatus() == AnimatingSheetHolder.INPROGRESS) {
					Thread.sleep(100);
					continue;
				}
				final UINotification notif = notificationQueue.take();
				showNotificationFrame(notif);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void enqueue(UINotification notification) {
		if (queueStatus != NotificationHandler.QUEUE_RUNNING) {
			return;
		}
		notificationQueue.offer(notification);
	}

	private synchronized void setQueueStatus(int status) {
		this.queueStatus = status;
	}

	public static void pauseQueue(boolean paused) {
		if (paused)
			instance.setQueueStatus(NotificationHandler.QUEUE_PAUSED);
		else
			instance.setQueueStatus(NotificationHandler.QUEUE_RUNNING);
	}

	public static void addToQueue(UINotification notification) {
		/*
		 * Drop all notifications if the main UI is not inited completely. If
		 * the main window is inited, NotificationHandler will be inited also
		 */
		if (instance == null) {
			return;
		}
		instance.enqueue(notification);
	}
}
