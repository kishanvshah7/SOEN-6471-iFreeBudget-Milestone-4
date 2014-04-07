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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.mjrz.fm.ui.widgets.MButton;

public class FExplorer extends JFrame {
	private static final long serialVersionUID = 1L;
	private MButton close, ok, up, back;
	private JCheckBox showHidden = null;

	private JList fList;
	private FListModel fListModel;

	private static final Color color = new Color(234, 234, 234);
	private static final Color selectedRowColor = new Color(149, 185, 199);

	private ArrayList<String> history = null;

	private ArrayList<File> roots = null;

	private JComboBox rootCb = null;

	public FExplorer() {
		history = new ArrayList<String>();
		FileWrapper f = new FileWrapper(System.getProperty("user.home"));
		String currDir = f.getAbsolutePath();
		setTitle(getDirName(currDir));
		history.add(currDir);

		loadRoots();

		initComponents();
	}

	private void loadRoots() {
		roots = new ArrayList<File>();
		File[] flist = File.listRoots();
		for (File f : flist) {
			roots.add(f);
		}
	}

	private String getDirName(String path) {
		FileWrapper f = new FileWrapper(path);
		return f.getName();
	}

	private void initComponents() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		add(getSouthPanel(), BorderLayout.SOUTH);
		add(getNorthPanel(), BorderLayout.NORTH);
		add(new JScrollPane(getCenterPanel()), BorderLayout.CENTER);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(new Dimension(400, 400));
		setVisible(true);
	}

	private JPanel getCenterPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		fListModel = new FListModel();
		reloadCurrentDir(showHidden.isSelected());

		fList = new JList(fListModel);
		fList.setCellRenderer(new FListCellRenderer());
		fList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int sel = fList.getSelectedIndex();
					if (sel < 0)
						return;
					FileWrapper f = (FileWrapper) fList.getSelectedValue();
					addToHistory(f.getAbsolutePath());
					FExplorer.this.reloadCurrentDir(showHidden.isSelected());
				}
			}
		});
		fList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					int sel = fList.getSelectedIndex();
					if (sel < 0)
						return;
					FileWrapper f = (FileWrapper) fList.getSelectedValue();
					addToHistory(f.getAbsolutePath());
					FExplorer.this.reloadCurrentDir(showHidden.isSelected());
				}
			}
		});
		ret.add(fList);

		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	private void addToHistory(String path) {
		history.add(path);
		if (history.size() >= 2) {
			while (true) {
				if (history.size() <= 2)
					break;
				history.remove(0);
			}
		}
	}

	private String getCurrentDir() {
		int sz = history.size();
		if (sz > 0) {
			return history.get(sz - 1);
		}
		return null;
	}

	private String getPreviousDir() {
		int sz = history.size();
		if (sz > 0) {
			return history.get(0);
		}
		return null;
	}

	private void clearPrevious() {
		while (true) {
			if (history.size() == 1)
				break;
			history.remove(0);
		}
		back.setEnabled(false);
	}

	private JPanel getNorthPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		back = new MButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/go-previous.png"));
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileWrapper curr = new FileWrapper(getPreviousDir());
				FileWrapper parent = new FileWrapper(curr.getAbsolutePath());
				if (parent != null) {
					addToHistory(parent.getAbsolutePath());
					reloadCurrentDir(showHidden.isSelected());
					clearPrevious();
				}
			}
		});
		back.setToolTipText("Go to the previous location");

		up = new MButton(
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/go-up.png"));
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileWrapper curr = new FileWrapper(getCurrentDir());
				FileWrapper parent = new FileWrapper(curr.getParentFile()
						.getAbsolutePath());
				if (parent != null) {
					addToHistory(parent.getAbsolutePath());
					reloadCurrentDir(showHidden.isSelected());
				}
			}
		});
		up.setToolTipText("Open the parent folder");

		JButton home = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/go-home.png"));
		home.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileWrapper home = new FileWrapper(System
						.getProperty("user.home"));
				addToHistory(home.getAbsolutePath());
				reloadCurrentDir(showHidden.isSelected());
			}
		});
		home.setToolTipText("Open home folder");

		rootCb = new JComboBox(roots.toArray());

		JLabel l = new JLabel("Drive: ");
		ret.add(l);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(rootCb);
		ret.add(Box.createHorizontalGlue());
		ret.add(back);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(up);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(home);
		ret.add(Box.createHorizontalStrut(5));

		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	private JPanel getSouthPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		showHidden = new JCheckBox("Show hidden folders");
		showHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reloadCurrentDir(showHidden.isSelected());
			}
		});
		showHidden.setSelected(false);

		close = new MButton("Cancel");
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		ok = new MButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(FExplorer.this);
				// int sel = fList.getSelectedIndex();
				// FileWrapper f = (FileWrapper) fListModel.getElementAt(sel);
				// if(f != null) {
				// System.out.println(f.getAbsolutePath());
				// }
			}
		});

		ret.add(showHidden);
		ret.add(Box.createHorizontalGlue());
		ret.add(ok);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(close);
		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	private void reloadCurrentDir(boolean showHidden) {
		FileWrapper path = new FileWrapper(getCurrentDir());
		setTitle(path.getName());
		Vector<FileWrapper> list = getDirListing(path, showHidden);
		fListModel.setData(list);
		if (fList != null)
			fList.clearSelection();
	}

	private Vector<FileWrapper> getDirListing(FileWrapper directory,
			boolean showHidden) {
		Vector<FileWrapper> ret = new Vector<FileWrapper>();
		if (!directory.isDirectory()) {
			return ret;
		}
		File[] list = directory.listFiles();
		if (list == null)
			return ret;

		for (File f : list) {
			if (f.isDirectory()) {
				if (!showHidden) {
					if (f.isHidden())
						continue;
				}
				ret.add(new FileWrapper(f.getAbsolutePath()));
			}
		}
		if (directory.getParent() == null) {
			up.setEnabled(false);
		}
		else {
			up.setEnabled(true);
		}

		if (history.size() > 1)
			back.setEnabled(true);
		else
			back.setEnabled(false);

		Collections.sort(ret);
		return ret;
	}

	@SuppressWarnings("serial")
	static class FListModel extends DefaultListModel {
		Vector<FileWrapper> data = null;

		public FListModel() {
			data = new Vector<FileWrapper>();
		}

		public void setData(Vector<FileWrapper> data) {
			this.data = data;
			this.fireContentsChanged(this, 0, data.size());
		}

		public void clear() {
			this.data.clear();
		}

		@Override
		public Object getElementAt(int index) {
			if (index < 0 || index >= data.size())
				return null;
			return data.get(index);
		}

		@Override
		public int getSize() {
			return data.size();
		}
	}

	public static void main(String args[]) {
		try {
			LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();
			int i = 0;
			for (; i < laf.length; i++) {
				if (laf[i].getClassName().indexOf("Nimbus") >= 0) {
					break;
				}
			}
			if (i < laf.length - 1) {
				UIManager.setLookAndFeel(laf[i].getClassName());
			}
			else {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		new FExplorer();
	}

	@SuppressWarnings("serial")
	static class FListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if (index % 2 != 0) {
				setBackground(color);
			}
			if (isSelected) {
				setBackground(selectedRowColor);
				java.awt.Font f = getFont();
				setFont(new java.awt.Font(f.getName(), java.awt.Font.BOLD,
						f.getSize()));
			}
			setPreferredSize(new Dimension(super.getWidth(), 30));
			return this;
		}
	}

	@SuppressWarnings("serial")
	class FileWrapper implements Comparable<FExplorer.FileWrapper> {
		private File file;

		public FileWrapper(String pathname) {
			file = new File(pathname);
		}

		public String getName() {
			return file.getName();
		}

		public String getAbsolutePath() {
			return file.getAbsolutePath();
		}

		public File getParentFile() {
			return file.getParentFile();
		}

		public String getParent() {
			return file.getParent();
		}

		public File[] listFiles() {
			return file.listFiles();
		}

		public boolean isDirectory() {
			return file.isDirectory();
		}

		public String toString() {
			return file.getAbsolutePath();
		}

		public int compareTo(FileWrapper o) {
			String name = this.file.getName();
			String oname = o.file.getName();

			if (name.indexOf('.') == 0 && oname.indexOf('.') != 0) {
				return 1;
			}
			if (name.indexOf('.') != 0 && oname.indexOf('.') == 0) {
				return -1;
			}
			if (name.indexOf('.') == 0 && oname.indexOf('.') == 0) {
				return name.compareTo(oname);
			}
			return name.compareTo(oname);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof FileWrapper)) {
				return false;
			}
			FileWrapper other = (FileWrapper) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (file == null) {
				if (other.file != null) {
					return false;
				}
			}
			else if (!file.equals(other.file)) {
				return false;
			}
			return true;
		}

		private FExplorer getOuterType() {
			return FExplorer.this;
		}
	}
}
