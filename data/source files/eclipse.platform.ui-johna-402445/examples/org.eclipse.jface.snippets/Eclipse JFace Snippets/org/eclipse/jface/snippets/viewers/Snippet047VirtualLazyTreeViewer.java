/*******************************************************************************
 * Copyright (c) 2006, 2013 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TreeViewer to demonstrate usage of an ILazyContentProvider.
 *
 */
public class Snippet047VirtualLazyTreeViewer {
	private class MyContentProvider implements ILazyTreeContentProvider<Object,List<IntermediateNode>>  {
		private TreeViewer<Object,List<IntermediateNode>>  viewer;
		private List<IntermediateNode> elements;

		public MyContentProvider(TreeViewer<Object,List<IntermediateNode>>  viewer) {
			this.viewer = viewer;
		}

		public void dispose() {

		}

		public void inputChanged(Viewer<? extends List<IntermediateNode>> viewer, List<IntermediateNode> oldInput, List<IntermediateNode> newInput) {
			this.elements = newInput;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof LeafNode)
				return ((LeafNode) element).parent;
			return elements;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java.lang.Object,
		 *      int)
		 */
		public void updateChildCount(Object element, int currentChildCount) {

			int length = 0;
			if (element instanceof IntermediateNode) {
				IntermediateNode node = (IntermediateNode) element;
				length =  node.children.size();
			}
			if(element == elements)
				length = elements.size();
			viewer.setChildCount(element, length);


		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object,
		 *      int)
		 */
		public void updateElement(Object parent, int index) {

			Object element;
			if (parent instanceof IntermediateNode)
				element = ((IntermediateNode) parent).children.get(index);

			else
				element =  elements.get(index);
			viewer.replace(parent, index, element);
			updateChildCount(element, -1);

		}

	}

	public class LeafNode {
		public int counter;
		public IntermediateNode parent;

		public LeafNode(int counter, IntermediateNode parent) {
			this.counter = counter;
			this.parent = parent;
		}

		public String toString() {
			return "Leaf " + this.counter;
		}
	}

	public class IntermediateNode {
		public int counter;
		public List<LeafNode> children = new ArrayList<LeafNode>();

		public IntermediateNode(int counter) {
			this.counter = counter;
		}

		public String toString() {
			return "Node " + this.counter;
		}

		public void generateChildren(int i) {
			for (int j = 0; j < i; j++) {
				children.add(j, new LeafNode(j, this));
			}

		}
	}

	public Snippet047VirtualLazyTreeViewer(Shell shell) {
		final TreeViewer<Object,List<IntermediateNode>> v = new TreeViewer<Object,List<IntermediateNode>>(shell, SWT.VIRTUAL | SWT.BORDER);
		v.setLabelProvider(new LabelProvider<Object>());
		v.setContentProvider(new MyContentProvider(v));
		v.setUseHashlookup(true);
		List<IntermediateNode> model = createModel();
		v.setInput(model);
		v.getTree().setItemCount(model.size());

	}

	private List<IntermediateNode> createModel() {
		List<IntermediateNode> elements = new ArrayList<IntermediateNode>();

		for (int i = 0; i < 10; i++) {
			elements.add(i,new IntermediateNode(i));
			elements.get(i).generateChildren(1000);
		}

		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet047VirtualLazyTreeViewer(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}