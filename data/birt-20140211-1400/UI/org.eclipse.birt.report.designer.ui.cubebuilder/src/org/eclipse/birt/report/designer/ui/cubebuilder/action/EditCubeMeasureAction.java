/*******************************************************************************
 * Copyright (c) 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.cubebuilder.action;

import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.designer.internal.ui.views.actions.AbstractElementAction;
import org.eclipse.birt.report.designer.ui.cubebuilder.nls.Messages;
import org.eclipse.birt.report.designer.ui.cubebuilder.page.CubeBuilder;
import org.eclipse.birt.report.model.api.olap.MeasureHandle;
import org.eclipse.birt.report.model.api.olap.TabularCubeHandle;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.PlatformUI;

/**
 * TODO: Please document
 * 
 * @version $Revision: 1.3 $ $Date: 2007/04/23 03:30:22 $
 */
public class EditCubeMeasureAction extends AbstractElementAction
{

	public static final String ID = "org.eclipse.birt.report.designer.ui.actions.EditCubeMeasureAction"; //$NON-NLS-1$

	/**
	 * @param selectedObject
	 */
	public EditCubeMeasureAction( Object selectedObject )
	{
		super( selectedObject );
		setId( ID );
	}

	/**
	 * @param selectedObject
	 * @param text
	 */
	public EditCubeMeasureAction( Object selectedObject, String text )
	{
		super( selectedObject, text );
		setId( ID );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.actions.AbstractElementAction#doAction()
	 */
	protected boolean doAction( ) throws Exception
	{
		if ( Policy.TRACING_ACTIONS )
		{
			System.out.println( "Edit Measure action >> Runs ..." ); //$NON-NLS-1$
		}
		MeasureHandle measureHandle = (MeasureHandle) getSelection( );
		CubeBuilder dialog = new CubeBuilder( PlatformUI.getWorkbench( )
				.getDisplay( )
				.getActiveShell( ), (TabularCubeHandle) measureHandle.getContainer( )
				.getContainer( ) );
		dialog.showPage( CubeBuilder.GROUPPAGE );
		return ( dialog.open( ) == IDialogConstants.OK_ID );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	public boolean isEnabled( )
	{
		return ( (MeasureHandle) getSelection( ) ).canEdit( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.actions.AbstractElementAction#getTransactionLabel()
	 */
	protected String getTransactionLabel( )
	{
		return Messages.getFormattedString( "cube.measure.edit", new String[]{( (MeasureHandle) getSelection( ) ).getName( )} ); //$NON-NLS-1$
	}
}
