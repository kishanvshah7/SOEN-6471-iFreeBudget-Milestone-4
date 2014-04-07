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
package net.mjrz.fm.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetAccountListAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.graph.PieChart;
import net.mjrz.fm.ui.utils.ImageFilter;
import net.mjrz.fm.utils.Messages;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class PieChartDialog extends JDialog implements ActionListener {
	User user;
	JFrame parent;
	int accountType = -1;
	static final long serialVersionUID = 0L;
	PieChart pieChart;
	JFileChooser fc;
	JButton b1, b2;

	public PieChartDialog(JFrame p, User u, int acctType) throws Exception {
		super(
				p,
				Messages.getString("Distribution - ") + AccountTypes.getAccountType(acctType), true); //$NON-NLS-1$
		parent = p;
		user = u;
		accountType = acctType;
		try {
			initialize();
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void initialize() throws Exception {
		try {
			Container cp = getContentPane();
			cp.setLayout(new BorderLayout());

			ActionRequest req = new ActionRequest();
			req.setActionName("getAccountList"); //$NON-NLS-1$
			req.setUser(user);
			req.setProperty("ACCOUNT_TYPE", accountType); //$NON-NLS-1$
			req.setProperty("LISTTYPE", "ACCOUNTS"); //$NON-NLS-1$ //$NON-NLS-2$

			ActionResponse aresp = new GetAccountListAction()
					.executeAction(req);
			java.util.List list = (java.util.List) aresp.getResult("RESULTSET"); //$NON-NLS-1$
			JComponent disp = null;
			if (list.size() == 0) {
				disp = getEmptyPanel();
				cp.add(getButtonPane(false), BorderLayout.SOUTH);
			}
			else {
				this.setPreferredSize(new Dimension(700, 700));

				this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

				JPanel chartPanel = getChartPane(list);
				JScrollPane sp = new JScrollPane(chartPanel,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

				sp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				disp = sp;
				cp.add(getButtonPane(true), BorderLayout.SOUTH);
			}
			cp.add(disp, BorderLayout.CENTER);

			fc = new JFileChooser();
			fc.addChoosableFileFilter(new ImageFilter());
			net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
		}
		catch (Exception e) {
			throw e;
		}
	}

	private JPanel getEmptyPanel() {
		JPanel ret = new JPanel();
		JLabel lbl = new JLabel(Messages.getString("No values to display")); //$NON-NLS-1$
		ret.add(lbl, BorderLayout.CENTER);
		return ret;
	}

	private JPanel getChartPane(java.util.List list) throws Exception {

		pieChart = new PieChart(list);

		return pieChart;
	}

	private boolean saveAsImage(File f) {
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			BufferedImage image = new BufferedImage(pieChart.getWidth(),
					pieChart.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = (Graphics2D) image.createGraphics();

			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);

			String ext = ImageFilter.getExtension(f);
			if (ext == null || ext.length() == 0) {
				f = new File(f.getParent(), f.getName() + ".PNG"); //$NON-NLS-1$
			}

			pieChart.paint(g2D);
			javax.imageio.ImageIO.write(image, "PNG", f); //$NON-NLS-1$
			return true;
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					Messages.getString("Error saving image"),
					Messages.getString("Error"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	private JPanel getButtonPane(boolean showSaveButton) {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		b1 = new JButton(Messages.getString("Save")); //$NON-NLS-1$
		b1.setActionCommand("Save");
		b1.addActionListener(this);

		b2 = new JButton(Messages.getString("Close")); //$NON-NLS-1$
		b2.setActionCommand("Close");
		b2.addActionListener(this);

		ret.add(Box.createHorizontalGlue());
		if (showSaveButton) {
			ret.add(b1);
			ret.add(Box.createHorizontalStrut(10));
		}
		ret.add(b2);

		ret.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		return ret;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Save")) { //$NON-NLS-1$
			int returnVal = fc.showSaveDialog(PieChartDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					int ans = JOptionPane
							.showConfirmDialog(
									PieChartDialog.this,
									Messages.getString("File with same name already exists. Replace?"),
									Messages.getString("Save As"),
									JOptionPane.YES_NO_OPTION);
					if (ans == JOptionPane.OK_OPTION) {
						saveAsImage(file);
					}
					else
						return;
				}
				else {
					saveAsImage(file);
				}
			}
		}
		else {
			dispose();
		}
	}
}
