package net.mjrz.fm.ui.utils;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class REditor extends JPanel {
	private static final long serialVersionUID = 1L;

	private JEditorPane display = null;
	private HTMLEditorKit eKit = null;
	private JToggleButton bold, it, ul;
	private JButton tableB, ol, link;

	private static final Logger logger = Logger.getLogger(REditor.class);

	public REditor() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		initializeTextPane();

		add(new JScrollPane(display), BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.PAGE_START);

		// SwingUtilities.invokeLater(new Runnable() {
		// @Override
		// public void run() {
		// display.requestFocusInWindow();
		// }
		// });
	}

	private void initializeTextPane() {
		display = new JEditorPane();
		display.setContentType("text/html");
		eKit = new HTMLEditorKit();
		HTMLDocument doc = (HTMLDocument) (eKit.createDefaultDocument());

		display.setEditorKit(eKit);
		display.setDocument(doc);
		display.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				doCaretUpdateAction(e);
			}
		});

		display.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					handleTabEvent(e);
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleEnterEvent(e);
				}
			}
		});
	}

	private void handleTabEvent(KeyEvent e) {
		int pos = display.getCaretPosition();

		HTMLDocument doc = (HTMLDocument) display.getDocument();

		Element start = doc.getParagraphElement(pos);

		Element elem = null;
		if (isType(start, HTML.Tag.TD)) {
			elem = start;
		}
		else {
			elem = start.getParentElement();
		}

		try {
			if (isType(elem, HTML.Tag.TD) || isType(elem, HTML.Tag.TH)) {
				handleTabInTableCell(doc, elem);
				/* Consume this event so it is not processed anymore */
				e.consume();
			}
		}
		catch (Exception e1) {
			logger.error(e1);
		}
	}

	private void handleEnterEvent(KeyEvent e) {
		int pos = display.getCaretPosition();

		HTMLDocument doc = (HTMLDocument) display.getDocument();

		Element start = doc.getParagraphElement(pos);
		Element elem = null;
		if (isType(start, HTML.Tag.LI)) {
			elem = start;
		}
		else {
			elem = start.getParentElement();
		}

		try {
			if (isType(elem, HTML.Tag.LI)) {
				handleEnterInListItem(doc, elem);
				e.consume();
			}
		}
		catch (Exception e1) {
			logger.error(e1);
		}
	}

	private void handleEnterInListItem(HTMLDocument doc, Element element)
			throws Exception {
		Element ol = element.getParentElement();
		if (isType(ol, HTML.Tag.OL)) {
			insertListItem(ol);
		}
	}

	private void handleTabInTableCell(HTMLDocument doc, Element element)
			throws Exception {
		Element row = element.getParentElement();
		Element table = row.getParentElement();

		if (isType(row, HTML.Tag.TR)) {
			logger.debug("Now at table row.. so what?");
			int count = row.getElementCount();
			logger.debug("Found " + count + " cells.");

			for (int i = 0; i < count; i++) {
				int next = i + 1;
				Element c = row.getElement(i);
				if (c == element) {
					logger.debug("Found current cell at index " + i);
					if (next < count) {
						Element nextEl = row.getElement(i + 1);
						int nextPos = nextEl.getStartOffset();
						logger.debug("Setting caret to next cell. " + next
								+ " @ " + nextPos);
						display.setCaretPosition(nextPos);
						break;
					}
					else if (next >= count) {
						if (isType(table, HTML.Tag.TABLE)) {
							int rowCount = table.getElementCount();
							/*
							 * Only add a new row if we are at the last row,
							 * last col
							 */
							if (table.getElement(rowCount - 1) == row) {
								insertTableRowHTML(row, count);
							}
							else {
								display.setCaretPosition(c.getEndOffset());
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void debugElement(Element elem) throws Exception {
		AttributeSet as = elem.getAttributes();
		Enumeration<?> en = as.getAttributeNames();
		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			Object v = as.getAttribute(o);
			logger.debug("@@@" + v + "(" + v.getClass().getName() + ") "
					+ " , " + o + "(" + o.getClass().getName() + ")");
		}
	}

	private String getType(Element elem) {
		Object o = elem.getAttributes().getAttribute(
				javax.swing.text.StyleConstants.NameAttribute);
		return o.getClass().getName() + "," + o.toString();
	}

	private boolean isType(Element elem, HTML.Tag tag) {
		Object o = elem.getAttributes().getAttribute(
				javax.swing.text.StyleConstants.NameAttribute);
		return o.equals(tag);
	}

	private void doCaretUpdateAction(CaretEvent e) {
		int pos = e.getDot();
		HTMLDocument doc = (HTMLDocument) display.getDocument();

		javax.swing.text.Element elem = doc.getCharacterElement(pos - 1);
		if (elem == null) {
			return;
		}
		AttributeSet as = elem.getAttributes();

		boolean isItalic = false;
		boolean isBold = false;
		boolean isUL = false;

		Enumeration<?> en = as.getAttributeNames();

		while (en.hasMoreElements()) {
			Object o = en.nextElement();
			String attrName = o.toString();
			String attrValue = as.getAttribute(o).toString();
			if (attrName.equals("font-style")) {
				if (attrValue.equals("italic")) {
					isItalic = true;
				}
			}
			if (attrName.equals("font-weight")) {
				if (attrValue.equals("bold")) {
					isBold = true;
				}
			}
			if (attrName.equals("text-decoration")) {
				if (attrValue.equals("underline")) {
					isUL = true;
				}
			}
		}

		it.setSelected(isItalic);
		bold.setSelected(isBold);
		ul.setSelected(isUL);
	}

	private void performAction(ActionEvent e, String action) {
		if (action.equals("Bold")) {
			StyledEditorKit.BoldAction a = new StyledEditorKit.BoldAction();
			a.actionPerformed(e);
		}
		if (action.equals("Italics")) {
			StyledEditorKit.ItalicAction a = new StyledEditorKit.ItalicAction();
			a.actionPerformed(e);
		}
		if (action.equals("Underline")) {
			StyledEditorKit.UnderlineAction a = new StyledEditorKit.UnderlineAction();
			a.actionPerformed(e);
		}
		if (action.equals("OrderedList")) {
			insertOrderedList(e);
		}
		if (action.equals("InsertTable")) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					TableParamsDialog d = new TableParamsDialog(SwingUtilities
							.getWindowAncestor(REditor.this));
					d.pack();
					d.setSize(new Dimension(300, 150));
					d.setLocationRelativeTo(REditor.this);
					d.setVisible(true);
					d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				}
			});
		}
		if (action.equals("InsertLink")) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					LinkParamsDialog d = new LinkParamsDialog(SwingUtilities
							.getWindowAncestor(REditor.this));
					d.pack();
					d.setSize(new Dimension(300, 150));
					d.setLocationRelativeTo(REditor.this);
					d.setVisible(true);
					d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				}
			});
		}
		display.requestFocusInWindow();
	}

	private void insertOrderedList(ActionEvent e) {
		logger.debug("Inserting ordered list");

		String html = "<OL><LI></LI></OL>";
		HTMLDocument doc = (HTMLDocument) display.getDocument();
		javax.swing.text.Element ep = doc.getParagraphElement(display
				.getSelectionStart());
		try {
			doc.insertAfterEnd(ep, html);
			int caretPos = display.getCaretPosition();
			display.setCaretPosition(caretPos - 1);
		}
		catch (Exception ex) {
			logger.error(ex);
		}
	}

	private void insertListItem(Element elem) {
		logger.debug("Inserting list item to " + getType(elem));

		String html = "<LI></LI>";
		HTMLDocument doc = (HTMLDocument) display.getDocument();
		try {
			doc.insertBeforeEnd(elem, html);
			int count = elem.getElementCount();
			display.setCaretPosition(elem.getElement(count - 1)
					.getStartOffset());
		}
		catch (Exception ex) {
			logger.error(ex);
		}
	}

	private void insertTableRowHTML(Element parentRow, int cols) {
		logger.debug("Inserting table row: ," + cols);
		HTMLDocument doc = (HTMLDocument) display.getDocument();
		try {
			doc.insertAfterEnd(parentRow, getTableRowString(cols));
			display.setCaretPosition(parentRow.getEndOffset());
		}
		catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void insertTableHTML(int rows, int cols) {
		String html = getTableHtml(rows, cols);
		HTMLDocument doc = (HTMLDocument) display.getDocument();
		int pos = display.getCaretPosition();
		try {
			eKit.insertHTML(doc, pos, html, 0, 0, HTML.Tag.TABLE);
		}
		catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void insertLinkHTML(String linkName, String linkTarget) {
		String html = getLinkHtml(linkName, linkTarget);
		HTMLDocument doc = (HTMLDocument) display.getDocument();
		int pos = display.getCaretPosition();
		try {
			eKit.insertHTML(doc, pos, html, 0, 0, HTML.Tag.A);
		}
		catch (BadLocationException e1) {
			logger.error(e1);
		}
		catch (IOException e1) {
			logger.error(e1);
		}
	}

	private String getLinkHtml(String linkName, String linkTarget) {
		StringBuilder ret = new StringBuilder();
		ret.append("<a href=");
		ret.append(linkTarget);
		ret.append(">");
		ret.append(linkName);
		ret.append("</a>");
		return ret.toString();
	}

	private String getTableHtml(int rows, int cols) {
		StringBuilder ret = new StringBuilder();

		ret.append("<table style=\"border-width:1px; border-color:black; width:50%\">");
		ret.append(getTableHeaderString(cols));
		for (int i = 0; i < rows; i++) {
			ret.append(getTableRowString(cols));
		}
		ret.append("</table>");

		return ret.toString();
	}

	private String getTableHeaderString(int cols) {
		int tdWidth = 100 / cols;
		StringBuilder ret = new StringBuilder();
		ret.append("<tr bgcolor=\"#6F8BBA\">");
		for (int j = 0; j < cols; j++) {
			ret.append("<th style=\"border-style:solid; border-width:1px; padding:2px;color:white;\" width=");
			ret.append(tdWidth);
			ret.append("%>");
			ret.append("Column " + (j + 1));
			ret.append("</th>");
		}
		ret.append("</tr>");
		return ret.toString();
	}

	private String getTableRowString(int cols) {
		int tdWidth = 100 / cols;
		StringBuilder ret = new StringBuilder();
		ret.append("<tr>");
		for (int j = 0; j < cols; j++) {
			ret.append("<td style=\"border-style:solid; border-width:1px;\" width=");
			ret.append(tdWidth);
			ret.append("%>");
			ret.append("</td>");
		}
		ret.append("</tr>");
		return ret.toString();
	}

	public void setText(String text) {
		display.setText(text);
	}

	public String getText() {
		return display.getText();
	}

	public String getPlainText() {
		HTMLDocument doc = (HTMLDocument) display.getDocument();
		try {
			return doc.getText(0, doc.getLength());
		}
		catch (BadLocationException e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		return "";
	}

	class TableParamsDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		private JTextField colsTf, rowsTf;
		private JButton ok, cancel;
		private int rows = 0;
		private int cols = 0;

		int getRows() {
			return rows;
		}

		int getCols() {
			return cols;
		}

		public TableParamsDialog(Window parent) {
			super(parent);
			initialize();
		}

		private void initialize() {
			colsTf = new JTextField(3);
			colsTf.setText(String.valueOf(3));
			rowsTf = new JTextField();
			rowsTf.setText(String.valueOf(2));

			getContentPane().setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(new JLabel("Columns"), gbc);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(colsTf, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(new JLabel("Rows"), gbc);

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(rowsTf, gbc);

			gbc.gridy = 3;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.CENTER;
			getContentPane().add(getButtonPanel(), gbc);

			getRootPane().setDefaultButton(ok);
		}

		private JPanel getButtonPanel() {
			JPanel ret = new JPanel();
			ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

			ok = new JButton("Ok");
			ok.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						rows = Integer.parseInt(rowsTf.getText());
					}
					catch (Exception ex) {
						rowsTf.setText("<Invalid number>");
						return;
					}
					try {
						cols = Integer.parseInt(colsTf.getText());
					}
					catch (Exception ex) {
						colsTf.setText("<Invalid number>");
						return;
					}

					REditor.this.insertTableHTML(rows, cols);

					dispose();
				}
			});

			cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			ret.add(Box.createHorizontalGlue());
			ret.add(ok);
			ret.add(Box.createHorizontalStrut(10));
			ret.add(cancel);
			ret.add(Box.createHorizontalStrut(10));
			return ret;
		}
	}

	class LinkParamsDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		private JTextField linkName, linkTarget;
		private JButton ok, cancel;

		public LinkParamsDialog(Window parent) {
			super(parent);
			initialize();
		}

		private void initialize() {
			linkName = new JTextField();
			linkTarget = new JTextField();
			linkTarget.setText("http://");

			getContentPane().setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(new JLabel("Link URL"), gbc);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(linkTarget, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(new JLabel("Link text"), gbc);

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.insets = new Insets(10, 10, 10, 10);
			getContentPane().add(linkName, gbc);

			gbc.gridy = 3;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.CENTER;
			getContentPane().add(getButtonPanel(), gbc);

			getRootPane().setDefaultButton(ok);
		}

		private JPanel getButtonPanel() {
			JPanel ret = new JPanel();
			ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

			ok = new JButton("Ok");
			ok.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String lName = linkName.getText();
					String lTarget = linkTarget.getText();

					REditor.this.insertLinkHTML(lName, lTarget);

					dispose();
				}
			});

			cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			ret.add(Box.createHorizontalGlue());
			ret.add(ok);
			ret.add(Box.createHorizontalStrut(10));
			ret.add(cancel);
			ret.add(Box.createHorizontalStrut(10));
			return ret;
		}
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {

			@SuppressWarnings("serial")
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				final REditor re = new REditor();
				JFrame f = new JFrame() {
					@Override
					public void dispose() {
						logger.debug(re.getText());
						super.dispose();
					}
				};
				f.getContentPane().setLayout(new BorderLayout());
				f.getContentPane().add(re, BorderLayout.CENTER);
				f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				f.setPreferredSize(new java.awt.Dimension(500, 300));
				f.pack();
				f.setVisible(true);
			}
		});
	}

	private JToolBar getButtonPanel() {
		bold = new JToggleButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/text_bold.png"));
		bold.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performAction(e, "Bold");
			}
		});
		bold.setToolTipText(tr("Bold"));

		it = new JToggleButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/text_italic.png"));
		it.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performAction(e, "Italics");
			}
		});
		it.setToolTipText(tr("Italic"));

		ul = new JToggleButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/text_underline.png"));
		ul.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performAction(e, "Underline");
			}
		});
		ul.setToolTipText(tr("Underline"));

		tableB = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/table.png"));
		tableB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performAction(e, "InsertTable");
			}
		});
		tableB.setToolTipText(tr("Table"));

		ol = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/text_list_numbers.png"));
		ol.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performAction(e, "OrderedList");
			}
		});
		ol.setToolTipText(tr("Numbered list"));

		link = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/link.png"));
		link.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performAction(e, "InsertLink");
			}
		});
		link.setToolTipText(tr("Link"));

		JToolBar toolBar = new JToolBar();

		toolBar.add(bold);
		toolBar.add(it);
		toolBar.add(ul);
		toolBar.add(tableB);
		toolBar.add(ol);
		toolBar.add(link);

		toolBar.setFloatable(false);

		return toolBar;
	}

	static class MyHTMLWriter extends HTMLWriter {

		public MyHTMLWriter(Writer w, HTMLDocument doc) {
			super(w, doc);
		}

		@Override
		protected void startTag(Element elem) throws IOException,
				BadLocationException {
			super.startTag(elem);
		}

		@Override
		protected void text(Element elem) throws IOException,
				BadLocationException {
			super.text(elem);
		}
	}
}
