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

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.mjrz.fm.entity.beans.AccountCategory;

import org.apache.log4j.Logger;

public class AccountsTreeDragSource implements DragSourceListener,
		DragGestureListener {

	private DragSource source;

	private DragGestureRecognizer recognizer;

	private TransferableTreeNode transferable;

	private AccountCategory oldNode;

	private JTree sourceTree;

	private AccountsTreeModel model;

	public AccountsTreeDragSource(JTree tree, int actions) {
		sourceTree = tree;
		model = (AccountsTreeModel) tree.getModel();

		source = new DragSource();
		recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
				actions, this);
	}

	public synchronized void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = sourceTree.getSelectionPath();
		if ((path == null) || (path.getPathCount() <= 1)) {
			return;
		}
		oldNode = (AccountCategory) path.getLastPathComponent();

		ArrayList<AccountCategory> ancestors = model.getAncestors(oldNode);
		int sz = ancestors.size();
		Long[] array = new Long[sz];
		for (int i = 0; i < sz; i++) {
			array[i] = ancestors.get(i).getCategoryId();
		}
		transferable = new TransferableTreeNode(array);

		try {
			source.startDrag(dge, DragSource.DefaultMoveDrop, transferable,
					this);
		}
		catch (Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	public synchronized void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess()
				&& (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
		}
	}
}
