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

package org.eclipse.birt.report.designer.ui;

import java.util.List;

import org.eclipse.birt.report.designer.tests.ITestConstants;

import junit.framework.TestCase;

/**
 * 
 */

public class ReportPluginTest extends TestCase
{

	public void testGetReportExtensionNameList( )
	{
		List list = ReportPlugin.getDefault( ).getReportExtensionNameList( );
		assertNotNull( list );
		assertTrue( list.size( ) > 0 );
		assertTrue( list.contains( ITestConstants.TEST_FILE_EXTENSION_NAME ) );
	}

	public void testIsReportFile( )
	{
		assertTrue( ReportPlugin.getDefault( )
				.isReportDesignFile( ITestConstants.TEST_DESIGN_FILE ) );
		assertFalse( ReportPlugin.getDefault( )
				.isReportDesignFile( ITestConstants.TEST_DESIGN_FILE + ".xml" ) ); //$NON-NLS-1$
	}
}
