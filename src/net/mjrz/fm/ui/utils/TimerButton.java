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

import javax.swing.JButton;
import javax.swing.SwingUtilities;

public final class TimerButton extends JButton {
	private static final long serialVersionUID = 1L;
	private int seconds;
	private String originalText = null;

	public TimerButton(String text, int seconds) {
		super(text);
		this.seconds = seconds;
		this.originalText = text;
		Thread t = new Thread(new MyTimer());
		t.start();
	}

	class MyTimer implements Runnable {
		MyTimer() {
		}

		@Override
		public void run() {
			try {
				while (true) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							seconds -= 1;
							if (seconds <= 0)
								seconds = 0;
							String txt = originalText;
							txt = txt + " (" + seconds + ")";
							setText(txt);
						}
					});
					Thread.sleep(1000);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
