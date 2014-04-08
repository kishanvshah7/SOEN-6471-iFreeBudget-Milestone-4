/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mjrz.fm.ui.utils.notifications;

import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.notifications.types.AlertNotification;
import net.mjrz.fm.ui.utils.notifications.types.MissedTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.ScheduledTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.UpdateCheckNotification;

/**
 *
 * @author Musers
 */
public class NotificationElementVisitor implements INotificationElementVisitor{
        
    FinanceManagerUI frame;
        
    public NotificationElementVisitor(FinanceManagerUI frame) {
        this.frame = frame;
    }

    @Override
    public NotificationDisplay visit(AlertNotification alertNotification) {
        return new AlertNotificationFrame(alertNotification, frame);//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NotificationDisplay visit(MissedTxNotification missedTxNotification) {
        return new MissedTxNotificationFrame(missedTxNotification, frame); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NotificationDisplay visit(ScheduledTxNotification scheduledTxNotification) {
        return new ScheduledTxNotificationFrame(scheduledTxNotification, frame);//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NotificationDisplay visit(UpdateCheckNotification updateCheckNotification) {
        return new UpdateCheckNotificationFrame(updateCheckNotification, frame);//To change body of generated methods, choose Tools | Templates.
    }
    
}
