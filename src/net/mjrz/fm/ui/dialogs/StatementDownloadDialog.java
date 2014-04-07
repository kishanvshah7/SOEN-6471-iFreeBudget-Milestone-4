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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import net.mjrz.fm.entity.beans.ONLBDetails;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.onlinebanking.OFXRequestFactory;
import net.mjrz.fm.onlinebanking.OfxRequest;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.ofx.ImportProgressPanel;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 *
 */
/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class StatementDownloadDialog extends JDialog implements ActionListener,
		PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	private JButton startButton;
	private JEditorPane taskOutput;
	private Task task;
	private FinanceManagerUI parent;
	private ONLBDetails details;
	private User user;
	private String progressMsg;

	private static Logger logger = Logger
			.getLogger(StatementDownloadDialog.class.getName());

	class Task extends SwingWorker<String, Void> {
		@Override
		public String doInBackground() {
			try {
				setProgress(30);
				progressMsg = Messages.getString("Sending request to server"); //$NON-NLS-1$

				OfxRequest request = OFXRequestFactory.getRequest(details);

				logger.info("Ofx Request constructed...");

				String resp = net.mjrz.fm.onlinebanking.MessageProcessor
						.getOfxResponse(details, request);

				logger.info("Received response...");

				progressMsg = Messages
						.getString("Received response from server"); //$NON-NLS-1$

				setProgress(60);

				Thread.sleep(1000);

				progressMsg = Messages.getString("Done!"); //$NON-NLS-1$

				setProgress(100);

				return resp;
			}
			catch (Exception ex) {
				String st = MiscUtils.stackTrace2String(ex);
				logger.error(st);

				String msg = ex.getMessage();
				msg = ((msg == null || msg.length() == 0) ? Messages
						.getString("Unknown error") : msg); //$NON-NLS-1$
				JOptionPane.showMessageDialog(StatementDownloadDialog.this,
						msg,
						Messages.getString("Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				return null;
			}
		}

		@Override
		public void done() {
			try {
				String result = get();
				if (result != null) {
					StatementDownloadDialog.this.dispose();

					ImportProgressPanel imp = new ImportProgressPanel(parent,
							user, result);

					logger.info("Import progress panel constructed...");

					// imp.loadTxList();

					logger.info("Loaded tx list.. Looks good so far");

					imp.pack();

					imp.setLocationRelativeTo(parent);

					imp.setVisible(true);
				}
				else {
					dispose();
					logger.info("Received response is null or empty.");
				}
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));

				JOptionPane op = getNarrowOptionPane(50);
				op.setMessageType(JOptionPane.ERROR_MESSAGE);
				op.setMessage(e.getMessage());
				JDialog dialog = op.createDialog(StatementDownloadDialog.this,
						Messages.getString("Error")); //$NON-NLS-1$
				dialog.pack();
				dialog.setVisible(true);
			}
		}
	}

	public StatementDownloadDialog(FinanceManagerUI parent, User user,
			ONLBDetails details) {
		super(parent, Messages.getString("Statement download"), true); //$NON-NLS-1$

		this.user = user;
		this.details = details;
		this.parent = parent;

		startButton = new JButton(Messages.getString("Start")); //$NON-NLS-1$
		startButton.setActionCommand("start"); //$NON-NLS-1$
		startButton.addActionListener(this);

		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(300, 25));
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		taskOutput = new JEditorPane();
		taskOutput.setMargin(new Insets(5, 5, 5, 5));
		taskOutput.setEditable(false);

		JPanel panel = new JPanel();
		panel.add(startButton);
		panel.add(progressBar);

		add(panel, BorderLayout.PAGE_START);
		add(new JScrollPane(taskOutput), BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(400, 300));
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("start")) { //$NON-NLS-1$
			startButton.setText(Messages.getString("Stop")); //$NON-NLS-1$
			startButton.setActionCommand("stop"); //$NON-NLS-1$
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			task = new Task();
			task.addPropertyChangeListener(this);
			task.execute();
		}
		else {
			startButton.setText(Messages.getString("Start")); //$NON-NLS-1$
			startButton.setActionCommand("start"); //$NON-NLS-1$
			task.cancel(true);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) { //$NON-NLS-1$
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
			taskOutput.setText(taskOutput.getText() + progressMsg + "\n"); //$NON-NLS-1$
		}
	}

	public static JOptionPane getNarrowOptionPane(int maxCharactersPerLineCount) {
		class NarrowOptionPane extends JOptionPane {
			private static final long serialVersionUID = 1L;
			int maxCharactersPerLineCount;

			NarrowOptionPane(int maxCharactersPerLineCount) {
				this.maxCharactersPerLineCount = maxCharactersPerLineCount;
			}

			public int getMaxCharactersPerLineCount() {
				return maxCharactersPerLineCount;
			}
		}
		return new NarrowOptionPane(maxCharactersPerLineCount);
	}
}
