/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.item.crosstab.internal.ui.editors.model;

import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;

/**
 * Measure header adapter.Maybe  several adapters share a CrosstabCellHandle.
 */
//This class is a specific cell handle adapter, some HeaderCellHandleAdapters 
//maybe share a CrosstabCellHandle
public class HeaderCrosstabCellHandleAdapter extends CrosstabCellAdapter
{

	/**Constructor
	 * @param handle
	 */
	public HeaderCrosstabCellHandleAdapter( CrosstabCellHandle handle )
	{
		super( handle );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.core.model.schematic.crosstab.BaseCrosstabAdapter#equals(java.lang.Object)
	 */
	public boolean equals( Object obj )
	{
		return super.equals( obj );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.core.model.schematic.crosstab.BaseCrosstabAdapter#hashCode()
	 */
	public int hashCode( )
	{
		return super.hashCode( );
	}
}
