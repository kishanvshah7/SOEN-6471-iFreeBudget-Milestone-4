package net.mjrz.fm.utils;

import static net.mjrz.fm.utils.Messages.tr;

import java.util.Arrays;
import java.util.List;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.PwdHistory;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.utils.crypto.CHelper;

import org.apache.log4j.Logger;

public class PwdUtils {
	private static Logger logger = Logger.getLogger(LoginPanel.class.getName());

	public static boolean changePassword(String user, char[] currentPwd,
			char[] newPwd) throws Exception {
		String homedir = Profiles.getInstance().getPathForProfile(user);
		ZProperties.loadUserProperties(homedir.toString());
		ZProperties.replaceRuntimeProperty("FMHOME", homedir.toString());

		net.mjrz.fm.utils.crypto.CHelper.init(currentPwd);
		HibernateUtils.initialize(user);

		FManEntityManager em = new FManEntityManager();
		List<Object> objects = em
				.getObjects("PwdHistory", "uid='" + user + "'");
		if (objects != null && objects.size() > 0) {
			int sz = objects.size();
			PwdHistory ph = (PwdHistory) objects.get(sz - 1);
			String base = ph.getBasePassword();
			String dec = CHelper.decrypt(base);

			CHelper.init(dec.toCharArray());

			User u = em.getUser("fmuser");

			if (u.getUserPassword().equals(dec)) {
				CHelper.init(newPwd);
				String newPassword = CHelper.encrypt(String.valueOf(newPwd));
				String basePassword = CHelper.encrypt(u.getUserPassword());
				ph.setBasePassword(basePassword);
				ph.setPassword(newPassword);
				em.updateObject("PwdHistory", ph);
				return true;
			}
		}
		else {
			User u = em.getUser("fmuser");
			if (Arrays.equals(currentPwd, u.getUserPassword().toCharArray())) {
				CHelper.init(newPwd);
				String basePwd = CHelper.encrypt(String.valueOf(currentPwd));

				PwdHistory hist = new PwdHistory();
				hist.setUid(user);
				hist.setPassword(String.valueOf(newPwd));
				hist.setBasePassword(basePwd);
				em.addObject("PwdHistory", hist);
				return true;
			}
		}
		return false;
	}

	public static String validatePwdPolicy(char[] pass, char[] verify) {
		boolean valid = true;
		String msg = null;
		if (pass.length != verify.length) {
			msg = tr("Passwords do not match");
			valid = false;
			return msg;
		}
		if (pass.length < 6 || pass.length > 20) {
			msg = tr("Passwords must be between 6 and 20 characters\nCannot contain special characters.");
			valid = false;
			return msg;
		}
		if (valid) {
			for (int i = 0; i < pass.length; i++) {
				if (!Character.isLetterOrDigit(pass[i])) {
					msg = tr("Passwords must be between 6 and 20 characters\nCannot contain special characters.");
					valid = false;
					break;
				}
			}
		}
		if (!Arrays.equals(pass, verify)) {
			valid = false;
			msg = tr("Passwords do not match");
		}

		return msg;
	}

}
