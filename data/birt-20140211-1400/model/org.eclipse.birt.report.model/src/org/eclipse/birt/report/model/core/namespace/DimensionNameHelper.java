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

package org.eclipse.birt.report.model.core.namespace;

import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.core.NameSpace;
import org.eclipse.birt.report.model.elements.olap.Dimension;
import org.eclipse.birt.report.model.metadata.ElementDefn;

/**
 * 
 */
public class DimensionNameHelper extends AbstractNameHelper
{

	protected Dimension dimension = null;

	/**
	 * 
	 * @param dimension
	 */
	public DimensionNameHelper( Dimension dimension )
	{
		super( );
		this.dimension = dimension;
		initialize( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.model.core.namespace.AbstractNameHelper#
	 * getNameSpaceCount()
	 */
	protected int getNameSpaceCount( )
	{
		return Dimension.NAME_SPACE_COUNT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.AbstractNameHelper#initialize
	 * ()
	 */
	protected void initialize( )
	{
		int count = getNameSpaceCount( );
		nameContexts = new INameContext[count];
		for ( int i = 0; i < count; i++ )
		{
			nameContexts[i] = NameContextFactory.createDimensionNameContext(
					dimension, i );
		}
	}

	/**
	 * Adds a element to the cached name space.
	 * 
	 * @param element
	 */
	public void addElement( DesignElement element )
	{
		if ( element == null || element.getName( ) == null )
			return;
		ElementDefn defn = (ElementDefn) element.getDefn( );
		if ( !dimension.getDefn( ).isKindOf(
				defn.getNameConfig( ).getNameContainer( ) ) )
			return;
		int id = defn.getNameSpaceID( );
		NameSpace ns = getCachedNameSpace( id );
		if ( !ns.contains( element.getName( ) ) )
			ns.insert( element );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#addContentName
	 * (int, java.lang.String)
	 */
	public void addContentName( int id, String name )
	{
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameHelper#getElement()
	 */
	public DesignElement getElement( )
	{
		return dimension;
	}
}
