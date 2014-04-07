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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class TxDecoratorSelectorDialog extends JFrame implements
		ListSelectionListener {
	private JList colList = null;
	private String colors[] = null;

	public TxDecoratorSelectorDialog() {
		initialize();
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		colors = new String[3];
		colors[0] = "228,152,121";
		colors[1] = "0,0,0";
		colors[2] = "Red";

		colList = new JList(colors);
		colList.addListSelectionListener(this);
		MyRenderer renderer = new MyRenderer();
		colList.setCellRenderer(renderer);

		cp.add(colList, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	static class MyRenderer extends JLabel implements ListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList arg0, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			String val = (String) value;
			String[] split = val.split(",");
			if (split != null && split.length == 3) {
				Color c = new Color(Integer.parseInt(split[0]),
						Integer.parseInt(split[1]), Integer.parseInt(split[2]));

				setBackground(c);
			}
			else {
				setBackground(Color.WHITE);
			}

			if (isSelected) {
				setBorder(BorderFactory.createLineBorder(Color.BLACK));
			}
			else {
				setBorder(BorderFactory.createEmptyBorder());
			}
			setPreferredSize(new Dimension(300, 30));
			setOpaque(true);
			return this;
		}
	}

	public static void main(String args[]) {
		TxDecoratorSelectorDialog d = new TxDecoratorSelectorDialog();
		d.pack();
		d.setSize(new Dimension(300, 200));
		d.setVisible(true);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
	}
}
