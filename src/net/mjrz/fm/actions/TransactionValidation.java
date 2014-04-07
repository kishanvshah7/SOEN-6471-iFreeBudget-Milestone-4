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
 * @author vivianmichaelgerard
 */
public class TransactionValidation {

    FManEntityManager em = null;

    public void validate(Session s, Transaction t, Account from, Account to,
            ActionResponse resp) throws Exception {
        String fitid = t.getFitid();
        boolean checkLength = fitid != null && fitid.trim().length() > 0;

        if (checkLength) {
            setErrorCode(s, t, resp);
        }
        /* Basic error checking... */
        if (t.getTxAmount().doubleValue() < 0) {
            checkTransactionAmount(resp);
        }

        if (from.getAccountId() == to.getAccountId()) {
            checkAccountDuplication(resp);
        }

        if (from.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
            checkFromAccountStatus(resp, from);
        }

        if (to.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
            checkToAccountStatus(resp, to);
        }

    }

    public void setErrorCode(Session s, Transaction t, ActionResponse resp) throws Exception {
        if (em.fitIdExists(s, t.getInitiatorId(), t.getFromAccountId(), t.getToAccountId(), t.getFitid())) {
            resp.setErrorCode(ActionResponse.TX_EXISTS_ERROR);

        }
    }

    public void checkTransactionAmount(ActionResponse resp) {
        resp.setErrorCode(ActionResponse.INVALID_TX);
        resp.setErrorMessage("Transaction amount must be greater than zero!");

    }

    public void checkAccountDuplication(ActionResponse resp) {
        resp.setErrorCode(ActionResponse.INVALID_TX);
        resp.setErrorMessage("To and from accounts are same");
    }

    public void checkFromAccountStatus(ActionResponse resp, Account from) {
        resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
        resp.setErrorMessage("Account is locked [" + from.getAccountName()
                + "]");
    }

    public void checkToAccountStatus(ActionResponse resp, Account to) {
        resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
        resp.setErrorMessage("Account is locked [" + to.getAccountName()
                + "]");
    }

}
