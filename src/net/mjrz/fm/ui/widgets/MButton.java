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
package net.mjrz.fm.ui.widgets;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class MButton extends JButton {

	private static final long serialVersionUID = 1L;

	public MButton() {
		super();
		initDefaults();
	}

	public MButton(String text) {
		super(text);
		initDefaults();
	}

	public MButton(ImageIcon icon) {
		super(icon);
	}

	private void initDefaults() {
		this.setPreferredSize(new Dimension(80, 25));
	}
}
