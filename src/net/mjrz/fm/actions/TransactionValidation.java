/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mjrz.fm.actions;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Transaction;
import org.hibernate.Session;

/**
 *
 * @author Musers
 */
public class TransactionValidation {
    FManEntityManager em = null;
    public void validate(Session s, Transaction t, Account from, Account to,
			ActionResponse resp) throws Exception {
		String fitid = t.getFitid();
		if (fitid != null && fitid.trim().length() > 0) {
			if (em.fitIdExists(s, t.getInitiatorId(), t.getFromAccountId(),
					t.getToAccountId(), t.getFitid())) {
				resp.setErrorCode(ActionResponse.TX_EXISTS_ERROR);
				
			}
		}
		/* Basic error checking... */
		if (t.getTxAmount().doubleValue() < 0) {
			resp.setErrorCode(ActionResponse.INVALID_TX);
			resp.setErrorMessage("Transaction amount must be greater than zero");
			
		}
		if (from.getAccountId() == to.getAccountId()) {
			resp.setErrorCode(ActionResponse.INVALID_TX);
			resp.setErrorMessage("To and from accounts are same");
			
		}
		if (from.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
			resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
			resp.setErrorMessage("Account is locked [" + from.getAccountName()
					+ "]");
			
		}
		if (to.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
			resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
			resp.setErrorMessage("Account is locked [" + to.getAccountName()
					+ "]");
		
		}
	}
    
}
