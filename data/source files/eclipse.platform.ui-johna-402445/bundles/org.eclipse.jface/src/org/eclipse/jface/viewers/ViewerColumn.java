/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * 												  fix for bug 163317, 201905
 *     Ralf Ebert - bug 294738
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class represent a column of a {@link ColumnViewer}. Label
 * providers and editing support can be configured for each column separately.
 * Concrete subclasses of {@link ColumnViewer} should implement a matching
 * concrete subclass of {@link ViewerColumn}.
 * @param <E> Type of an element of the model
 * @param <I> Type of the input
 *
 * @since 3.3
 *
 */
public abstract class ViewerColumn<E,I> {

	private CellLabelProvider<E,I> labelProvider;

	static String COLUMN_VIEWER_KEY = Policy.JFACE + ".columnViewer";//$NON-NLS-1$

	private EditingSupport<E,I> editingSupport;

	private ILabelProviderListener<E> listener;

	private boolean listenerRegistered = false;

	private ColumnViewer<E,I> viewer;

	/**
	 * Create a new instance of the receiver at columnIndex.
	 *
	 * @param viewer
	 *            the viewer the column is part of
	 * @param columnOwner
	 *            the widget owning the viewer in case the widget has no columns
	 *            this could be the widget itself
	 */
	protected ViewerColumn(final ColumnViewer<E,I> viewer, Widget columnOwner) {
		this.viewer = viewer;
		columnOwner.setData(ViewerColumn.COLUMN_VIEWER_KEY, this);
		this.listener = new ILabelProviderListener<E>() {

			public void labelProviderChanged(LabelProviderChangedEvent<E> event) {
				viewer.handleLabelProviderChanged(event);
			}

		};
		columnOwner.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				handleDispose(viewer);
			}
		});
	}

	/**
	 * Return the label provider for the receiver.
	 *
	 * @return ViewerLabelProvider
	 */
	/* package */CellLabelProvider<E,I> getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Set the label provider for the column. Subclasses may extend but must
	 * call the super implementation.
	 *
	 * @param labelProvider
	 *            the new {@link CellLabelProvider}
	 */
	public void setLabelProvider(CellLabelProvider<E,I> labelProvider) {
		setLabelProvider(labelProvider, true);
	}

	/**
	 * @param labelProvider
	 * @param registerListener
	 */
	/* package */void setLabelProvider(CellLabelProvider<E,I> labelProvider,
			boolean registerListener) {
		if (listenerRegistered && this.labelProvider != null) {
			this.labelProvider.removeListener(listener);
			listenerRegistered = false;
			if (registerListener) {
				this.labelProvider.dispose(viewer, this);
			}
		}

		this.labelProvider = labelProvider;

		if (registerListener) {
			this.labelProvider.initialize(viewer, this);
			this.labelProvider.addListener(listener);
			listenerRegistered = true;
		}
	}

	/**
	 * Return the editing support for the receiver.
	 *
	 * @return {@link EditingSupport}
	 */
	/* package */EditingSupport<E, I> getEditingSupport() {
		return editingSupport;
	}

	/**
	 * Set the editing support. Subclasses may extend but must call the super
	 * implementation.
	 * <p>
	 * Users setting up an editable {@link TreeViewer} or {@link TableViewer} with more than 1 column <b>have</b>
	 * to pass the SWT.FULL_SELECTION style bit when creating the viewer
	 * </p>
	 * @param editingSupport
	 *            The {@link EditingSupport} to set.
	 */
	public void setEditingSupport(EditingSupport<E,I> editingSupport) {
		this.editingSupport = editingSupport;
	}

	/**
	 * Refresh the cell for the given columnIndex. <strong>NOTE:</strong>the
	 * {@link ViewerCell} provided to this method is no longer valid after this
	 * method returns. Do not cache the cell for future use.
	 *
	 * @param cell
	 *            {@link ViewerCell}
	 */
	/* package */void refresh(ViewerCell<E> cell) {
		CellLabelProvider<E,I> labelProvider = getLabelProvider();
		if (labelProvider == null) {
			Assert.isTrue(false, "Column " + cell.getColumnIndex() + //$NON-NLS-1$
			" has no label provider."); //$NON-NLS-1$
		}
		labelProvider.update(cell);
	}

	/**
	 * Disposes of the label provider (if set), unregisters the listener and
	 * nulls the references to the label provider and editing support. This
	 * method is called when the underlying widget is disposed. Subclasses may
	 * extend but must call the super implementation.
	 */
	protected void handleDispose() {
		boolean disposeLabelProvider = listenerRegistered;
		CellLabelProvider<E,I> cellLabelProvider = labelProvider;
		setLabelProvider(null, false);
		if (disposeLabelProvider) {
			cellLabelProvider.dispose(viewer, this);
		}
		editingSupport = null;
		listener = null;
		viewer = null;
	}

	private void handleDispose(ColumnViewer<E,I> viewer) {
		handleDispose();
		viewer.clearLegacyEditingSetup();
	}

	/**
	 * Returns the viewer of this viewer column.
	 *
	 * @return Returns the viewer.
	 *
	 * @since 3.4
	 */
	public ColumnViewer<E,I> getViewer() {
		return viewer;
	}
}
