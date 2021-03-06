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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

/**
 * Example how to place native controls into a viewer with the new JFace-API
 * because has the potential to eat up all your handles you should think about
 * alternate approaches e.g. takeing a screenshot of the control
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet054NativeControlsInViewers {

	private class MyContentProvider implements IStructuredContentProvider<MyModel,List<MyModel>> {

		public MyModel[] getElements(List<MyModel> inputElement) {
			MyModel[] myModels = new MyModel[inputElement.size()];
			return inputElement.toArray(myModels);
		}

		public void dispose() {

		}

		public void inputChanged(Viewer<? extends List<MyModel>> viewer, List<MyModel> oldInput, List<MyModel> newInput) {

		}

	}

	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		public String toString() {
			return "Item " + this.counter;
		}
	}

	public Snippet054NativeControlsInViewers(Shell shell) {
		final TableViewer<MyModel,List<MyModel>> v = new TableViewer<MyModel,List<MyModel>>(shell, SWT.BORDER
				| SWT.FULL_SELECTION);
		v.setContentProvider(new MyContentProvider());
		v.getTable().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));

		TableViewerColumn<MyModel,List<MyModel>> column = new TableViewerColumn<MyModel,List<MyModel>>(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Column 1");
		column.setLabelProvider(new ColumnLabelProvider<MyModel,List<MyModel>>() {

			public String getText(MyModel element) {
				return element.toString();
			}

		});

		column = new TableViewerColumn<MyModel,List<MyModel>>(v, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Column 2");
		column.setLabelProvider(new CellLabelProvider<MyModel,List<MyModel>>() {

			public void update(ViewerCell<MyModel> cell) {
				final TableItem item = (TableItem) cell.getItem();
				DisposeListener listener = new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						if( item.getData("EDITOR") != null ) {
							TableEditor editor = (TableEditor) item.getData("EDITOR");
							editor.getEditor().dispose();
							editor.dispose();
						}
					}

				};

				if (item.getData("EDITOR") != null) {
					TableEditor editor = (TableEditor) item.getData("EDITOR");
					editor.getEditor().dispose();
					editor.dispose();
				}

				if( item.getData("DISPOSELISTNER") != null ) {
					item.removeDisposeListener((DisposeListener) item.getData("DISPOSELISTNER"));
				}

				TableEditor editor = new TableEditor(item.getParent());
				item.setData("EDITOR", editor);
				Composite comp = new Composite(item.getParent(), SWT.NONE);
				comp.setBackground(item.getParent().getBackground());
				comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
				RowLayout l = new RowLayout();
				l.marginHeight = 0;
				l.marginWidth = 0;
				l.marginTop = 0;
				l.marginBottom = 0;
				comp.setLayout(l);
				Button rad = new Button(comp, SWT.RADIO);
				Button rad1 = new Button(comp, SWT.RADIO);
				Button rad2 = new Button(comp, SWT.RADIO);

				editor.grabHorizontal = true;
				editor.setEditor(comp, item, 1);

				item.addDisposeListener(listener);
				item.setData("DISPOSELISTNER",listener);
			}

		});

		List<MyModel> model = createModel(10);
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);

		Button b = new Button(shell,SWT.PUSH);
		b.setText("Modify input");
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				v.setInput(createModel((int)(Math.random() * 10)));
			}

		});

		b = new Button(shell,SWT.PUSH);
		b.setText("Refresh");
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				v.refresh();
			}

		});
	}

	private List<MyModel> createModel(int amount) {
		List<MyModel> elements = new ArrayList<MyModel>(amount);
		for( int i = 0; i < amount; i++ ) {
			elements.add(i,new MyModel(i));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2,true));
		new Snippet054NativeControlsInViewers(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
