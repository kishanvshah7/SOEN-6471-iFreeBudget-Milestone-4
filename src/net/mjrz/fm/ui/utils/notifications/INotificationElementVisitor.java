/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mjrz.fm.ui.utils.notifications;

import net.mjrz.fm.ui.utils.notifications.types.AlertNotification;
import net.mjrz.fm.ui.utils.notifications.types.MissedTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.ScheduledTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.UpdateCheckNotification;

/**
 *
 * @author Musers
 */
public interface INotificationElementVisitor {
    NotificationDisplay visit(AlertNotification alertNotification);
    NotificationDisplay visit(MissedTxNotification missedTxNotification);
    NotificationDisplay visit(ScheduledTxNotification scheduledTxNotification);
    NotificationDisplay visit(UpdateCheckNotification updateCheckNotification);
}
