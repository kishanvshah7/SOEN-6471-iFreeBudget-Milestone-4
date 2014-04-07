package net.mjrz.fm.ui.utils;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.services.SessionManager;

public class TxIconCellRenderer extends TxDefaultCellrenderer {

	private static final long serialVersionUID = 1L;

	private FManEntityManager em = null;

	public TxIconCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorder(BorderFactory.createEmptyBorder());
		em = new FManEntityManager();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {

		setRowColors(table, value, isSelected, hasFocus, row, col);

		TransactionTableModel model = (TransactionTableModel) table.getModel();

		row = table.convertRowIndexToModel(row);

		TT tt = model.getTransaction(row);

		try {
			String catName = FManEntityManager.getCategoryName(tt
					.getToAccountId());

			Account to = em.getAccount(SessionManager.getSessionUserId(),
					tt.getToAccountId());

			Long catId = to.getCategoryId();

			ImageIcon icon = IconMap.getInstance().getIcon(catId);

			setToolTipText(catName);

			setIcon(icon);
		}
		catch (Exception e) {
			setIcon(null);
		}
		return this;
	}
}
