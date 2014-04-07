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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

public class AnimatingSheetHolder extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	static final int INCOMING = 1;
	static final int OUTGOING = -1;
	static final float ANIMATION_DURATION = 1000f;
	static final int ANIMATION_SLEEP = 50;
	static final int WAIT_DURATION = 15000;

	private JComponent sheet;
	private JPanel glass;
	private AnimatingSheet animSheet;
	private boolean animating;
	private int animDirection;
	private long animStart;
	private Timer animTimer;
	private Timer waitTimer;

	static final int NONE = 0;
	static final int INPROGRESS = 2;
	static final int DISPOSED = 3;

	private int status = NONE;

	public AnimatingSheetHolder(String name) {
		super(name);
	}

	public void initializeAnimatingSheet() {
		glass = (JPanel) getGlassPane();
		glass.setLayout(new GridBagLayout());
		animSheet = new AnimatingSheet();
		animSheet.setBorder(new LineBorder(Color.BLACK, 1));
	}

	public JComponent showJDialogAsSheet(JDialog frame) {
		status = INPROGRESS;

		sheet = (JComponent) frame.getContentPane();
		sheet.setBorder(new LineBorder(Color.BLACK, 1));
		glass.removeAll();
		animDirection = INCOMING;
		startAnim();

		return sheet;
	}

	public JComponent showJFrameAsSheet(JFrame frame) {
		status = INPROGRESS;

		sheet = (JComponent) frame.getContentPane();
		glass.removeAll();
		animDirection = INCOMING;
		startAnim();

		return sheet;
	}

	private void startAnim() {
		glass.repaint();
		animSheet.setSource(sheet);
		glass.removeAll();
		//
		// GridBagConstraints gbc = new GridBagConstraints();
		// gbc.anchor = GridBagConstraints.NORTHEAST;
		//
		// gbc.gridx = 1;
		// gbc.weightx = Integer.MAX_VALUE;
		// glass.add(Box.createGlue(), gbc);
		//
		// glass.add(animSheet, gbc);
		// gbc.gridy = 1;
		// gbc.weightx = Integer.MAX_VALUE;
		// glass.add(Box.createGlue(), gbc);
		//
		glass.setVisible(true);

		animStart = System.currentTimeMillis();
		if (animTimer == null) {
			animTimer = new Timer(ANIMATION_SLEEP, this);
		}
		animating = true;
		animTimer.start();
	}

	private void stopAnim() {
		animTimer.stop();
		animating = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (animating) {
			float animPercent = (System.currentTimeMillis() - animStart)
					/ ANIMATION_DURATION;
			animPercent = Math.min(1.0f, animPercent);
			int animatingHeight = 0;

			if (animDirection == INCOMING) {
				animatingHeight = (int) (animPercent * sheet.getHeight());
			}
			else {
				animatingHeight = (int) ((1.0f - animPercent) * sheet
						.getHeight());
			}
			animSheet.setAnimatingHeight(animatingHeight);
			animSheet.repaint();
			if (animPercent >= 1.0f) {
				stopAnim();
				if (animDirection == INCOMING) {
					finishShowingSheet();
				}
				else {
					glass.removeAll();
					glass.setVisible(true);
				}
			}
		}
	}

	public void hideSheet() {
		animDirection = OUTGOING;
		startAnim();
		status = DISPOSED;
	}

	private void finishShowingSheet() {
		glass.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHEAST;

		gbc.gridx = 1;
		gbc.weightx = Integer.MAX_VALUE;
		glass.add(Box.createGlue(), gbc);

		glass.add(sheet, gbc);
		gbc.gridy = 1;
		gbc.weighty = Integer.MAX_VALUE;
		glass.add(Box.createGlue(), gbc);

		glass.revalidate();
		glass.repaint();
		if (waitTimer == null) {
			waitTimer = new Timer(WAIT_DURATION, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Wait time expired... hiding");
					waitTimer.stop();
					hideSheet();
				}
			});
		}
		waitTimer.start();
	}

	public synchronized int getStatus() {
		return status;
	}
}
