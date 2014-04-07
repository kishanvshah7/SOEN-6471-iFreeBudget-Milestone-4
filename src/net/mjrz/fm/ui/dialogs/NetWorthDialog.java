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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetNetWorthHistoryAction;
import net.mjrz.fm.entity.beans.NetWorthHistory;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.graph.LineGraph;
import net.mjrz.fm.ui.utils.ImageFilter;
import net.mjrz.fm.utils.Messages;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class NetWorthDialog extends JDialog implements ActionListener {
	User user;
	JFrame parent;
	static final long serialVersionUID = 0L;
	int type = 1;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	LineGraph lineGraph = null;
	JFileChooser fc = null;

	public NetWorthDialog(JFrame p, User u, int type) throws Exception {
		super(p, Messages.getString("Net worth history"), true); //$NON-NLS-1$
		parent = p;
		user = u;

		this.type = type;

		try {
			initialize();
		}
		catch (Exception e) {
			throw e;
		}
	}

	private ArrayList<String> getDateRange(int type) {

		GregorianCalendar gc = new GregorianCalendar();
		// gc.add(Calendar.DATE, 1);

		int calField = -1;
		int incCount = 1;

		ArrayList<String> ret = new ArrayList<String>();

		if (type == GetNetWorthHistoryAction.HISTORY_10DAYS) {
			calField = Calendar.DATE;
			incCount = -1;
		}
		if (type == GetNetWorthHistoryAction.HISTORY_10WEEKS) {
			calField = Calendar.DATE;
			incCount = -7;
		}
		if (type == GetNetWorthHistoryAction.HISTORY_10MONTHS) {
			calField = Calendar.MONTH;
			incCount = -1;
		}
		for (int i = 0; i < 10; i++) {
			ret.add(sdf.format(gc.getTime()));
			gc.add(calField, incCount);
		}

		return ret;
	}

	private void initialize() throws Exception {
		try {

			ActionRequest req = new ActionRequest();
			req.setActionName("getNetWorthHistory"); //$NON-NLS-1$

			ArrayList range = getDateRange(type);

			req.setProperty("FROM_DATE", range.get(range.size() - 1)); //$NON-NLS-1$
			req.setProperty("TO_DATE", range.get(0)); //$NON-NLS-1$
			req.setUser(user);

			GetNetWorthHistoryAction action = new GetNetWorthHistoryAction();
			ActionResponse resp = action.executeAction(req);
			java.util.List hl = resp.getResultList();

			ArrayList<Double> vals = new ArrayList<Double>();
			ArrayList<String> dates = new ArrayList<String>();
			dates.add(""); //$NON-NLS-1$
			vals.add((double) 1);

			SimpleDateFormat xsdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

			for (int i = 0; i < hl.size(); i++) {
				NetWorthHistory hist = (NetWorthHistory) hl.get(i);
				String dt = xsdf.format(hist.getDate());
				if (range.contains(dt)) {
					vals.add(hist.getNetWorth().doubleValue());
					dt = dt.substring(5);
					dates.add(dt.replace('-', '/'));
				}
			}

			Container cp = getContentPane();
			cp.setLayout(new BorderLayout());

			this.setMinimumSize(new Dimension(700, 700));

			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			cp.add(getGraphPane(dates, vals), BorderLayout.CENTER);
			cp.add(getButtonPane(), BorderLayout.SOUTH);

			fc = new JFileChooser();
			fc.addChoosableFileFilter(new ImageFilter());
			net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
		}
		catch (Exception e) {
			throw e;
		}
	}

	private JPanel getGraphPane(ArrayList<String> x, ArrayList<Double> y)
			throws Exception {
		lineGraph = new LineGraph(x, y, 540); // Graph width
		return lineGraph;
	}

	private JPanel getButtonPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		JButton closeB = new JButton(Messages.getString("Close")); //$NON-NLS-1$
		closeB.setActionCommand("Close");
		closeB.addActionListener(this);

		JButton saveB = new JButton(Messages.getString("Save")); //$NON-NLS-1$
		saveB.setActionCommand("Save");
		saveB.addActionListener(this);

		ret.add(Box.createHorizontalGlue());
		ret.add(saveB);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(closeB);

		ret.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		return ret;
	}

	private boolean saveAsImage(File f) {
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			BufferedImage image = new BufferedImage(lineGraph.getWidth(),
					lineGraph.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2D = (Graphics2D) image.createGraphics();

			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);

			String ext = ImageFilter.getExtension(f);
			if (ext == null || ext.length() == 0) {
				f = new File(f.getParent(), f.getName() + ".PNG"); //$NON-NLS-1$
			}

			lineGraph.paint(g2D);
			javax.imageio.ImageIO.write(image, "PNG", f); //$NON-NLS-1$
			return true;
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					Messages.getString("Error saving image") + e.getMessage(),
					Messages.getString("Error"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Save")) { //$NON-NLS-1$
			int returnVal = fc.showSaveDialog(NetWorthDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					int ans = JOptionPane
							.showConfirmDialog(
									NetWorthDialog.this,
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
