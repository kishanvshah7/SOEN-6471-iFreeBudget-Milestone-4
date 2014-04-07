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
package net.mjrz.fm.ui.panels;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.utils.GuiUtilities;

import org.apache.log4j.Logger;

public class TxDetailsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel1 = null;

	private JEditorPane notesTa = null;
	private HTMLEditorKit eKit;
	private FManEntityManager em = null;
	private static Logger logger = Logger.getLogger(TxDetailsPanel.class
			.getName());

	/**
	 * This is the default constructor
	 */
	public TxDetailsPanel() {
		super();
		initialize();
		em = new FManEntityManager();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout(2, 2));

		jLabel1 = new JLabel();
		this.add(jLabel1, BorderLayout.NORTH);

		this.add(getNotesTa(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes notesTa
	 * 
	 * @return javax.swing.JTextArea
	 */
	private JScrollPane getNotesTa() {
		if (notesTa == null) {
			notesTa = new JEditorPane();
			notesTa.setEditable(false);
		}
		notesTa.setContentType("text/html");
		GuiUtilities.setupTextComponent(notesTa);

		Font f = getFont();

		String family = f.getFamily();
		String sz = String.valueOf(f.getSize());

		String rule = "body {color:#000; font-family:" + family
				+ ";margin:10px 4px 4px 4px; font-size: " + sz + ";}";

		eKit = new HTMLEditorKit();

		StyleSheet ss = eKit.getStyleSheet();
		// ss.addRule("body {color:#000; font-family:times; margin: 4px; font-size: 100%; }");
		ss.addRule(rule);

		notesTa.setEditorKit(eKit);

		notesTa.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() != EventType.ACTIVATED) {
					return;
				}
				URL url = e.getURL();
				launchLinkInBrowser(url);
			}
		});

		return new JScrollPane(notesTa);
	}

	private void launchLinkInBrowser(final URL url) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				java.awt.Desktop d = Desktop.getDesktop();
				if (Desktop.isDesktopSupported()) {
					d.browse(url.toURI());
				}
				return null;
			}
		};
		worker.execute();
	}

	public void updateTx(String tx) {
		try {
			HTMLDocument doc = (HTMLDocument) (eKit.createDefaultDocument());
			notesTa.setDocument(doc);

			Transaction t = em.getTransaction(
					SessionManager.getSessionUserId(), Long.parseLong(tx));
			if (t == null)
				return;

			if (t.getActivityBy() != null)
				jLabel1.setText("");

			notesTa.setText(getNotesText(t));
		}
		catch (NumberFormatException e) {
			logger.error(e);
		}
		catch (Exception e) {
			logger.error(net.mjrz.fm.utils.MiscUtils.stackTrace2String(e));
		}
	}

	private String getNotesText(Transaction t) {
		String markup = t.getTxNotesMarkup();
		if (markup == null || markup.length() == 0) {
			return t.getTxNotes();
		}
		return markup;
	}
}
