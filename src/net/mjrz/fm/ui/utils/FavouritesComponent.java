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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Favourites;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.dialogs.NewTransactionDialog;
import net.mjrz.fm.ui.dialogs.QuickTransactionDialog;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class FavouritesComponent extends JToolBar {
	private static final long serialVersionUID = 1L;
	private FinanceManagerUI parent = null;
	private static Logger logger = Logger.getLogger(FavouritesComponent.class
			.getName());
	private JComboBox favsCb;
	private java.util.List<Favourites> favs = null;
	private FManEntityManager em = null;
	private Favourites dummy = null;
	private JButton delete = null;

	public FavouritesComponent(FinanceManagerUI parent) {
		this.parent = parent;

		dummy = new Favourites();
		dummy.setId(0);
		dummy.setName("<" + tr("Select") + ">");
		dummy.setTxId(0);

		initialize();
		this.setMaximumSize(new Dimension(300, 25));
	}

	private void initialize() {
		em = new FManEntityManager();
		try {
			favs = em.getFavourites(SessionManager.getSessionUserId());
			favs.add(0, dummy);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}

		JLabel l = new JLabel(tr("Favourites") + " ");
		favsCb = new JComboBox(favs.toArray());
		favsCb.setPreferredSize(new Dimension(225, 25));
		favsCb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFavsCbActionPerformed(e);
			}
		});

		delete = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/sell.png"));
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doDeleteButtonActionPerformed(e);
			}
		});
		delete.setToolTipText(tr("Delete"));
		delete.setEnabled(false);

		addSeparator();
		add(l);
		add(favsCb);
		add(delete);
		add(Box.createHorizontalGlue());
		addSeparator();

		setFloatable(false);
		this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
	}

	private void doDeleteButtonActionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int sel = favsCb.getSelectedIndex();
				/* Ignore the 0th item. It is a dummy */
				if (sel == 0)
					return;

				Favourites t = (Favourites) favsCb.getSelectedItem();
				if (t == null)
					return;
				deleteFavourite(t);
			}
		});
	}

	private void doFavsCbActionPerformed(final ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int idx = favsCb.getSelectedIndex();
				/* Ignore the 0th item. It is a dummy */
				if (idx <= 0) {
					delete.setEnabled(false);
					return;
				}
				delete.setEnabled(true);
				Favourites t = (Favourites) favsCb.getSelectedItem();
				if (t == null)
					return;
				try {
					QuickTransactionDialog d = new QuickTransactionDialog(
							parent, parent.getUser(), t.getTxId(),
							NewTransactionDialog.NEW_TX);

					d.pack();
					d.setLocationRelativeTo(parent);
					d.setVisible(true);
				}
				catch (Exception e) {
					deleteFavourite(t);
					logger.error(MiscUtils.stackTrace2String(e));
					JOptionPane.showMessageDialog(parent,
							tr("Transaction not found, may have been deleted")
									+ "\n" + t.getName() + " "
									+ tr("will be removed from favourites"),
							tr("Error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	private void deleteFavourite(Favourites fav) {
		try {
			int num = em.deleteFavourites(fav.getId());
			if (num == 1) {
				favsCb.setSelectedIndex(0);
				favsCb.removeItem(fav);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void addFavourite(long txId, String name) {
		try {
			if (name.length() > 30) {
				name = name.substring(0, 31);
			}
			Favourites f = new Favourites();
			f.setName(name);
			f.setTxId(txId);
			f.setUid(SessionManager.getSessionUserId());
			em.addFavourite(f);
			favsCb.setSelectedIndex(0);
			favsCb.addItem(f);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}
}
