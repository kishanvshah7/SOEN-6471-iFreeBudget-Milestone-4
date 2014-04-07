package net.mjrz.fm.actions;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;

import org.hibernate.Session;

public class ValidateTxAction extends AddTransactionAction {

	@Override
	public ActionResponse executeAction(ActionRequest req) throws Exception {
		Session s = null;
		try {
			User u = req.getUser();
			Transaction t = (Transaction) req.getProperty("TRANSACTION");

			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			u = (User) s.load(User.class, u.getUid());

			Account from = em.getAccount(s, u.getUid(), t.getFromAccountId());
			Account to = em.getAccount(s, u.getUid(), t.getToAccountId());

			ActionResponse resp = new ActionResponse();
			super.validate(s, t, from, to, resp);

			return resp;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
