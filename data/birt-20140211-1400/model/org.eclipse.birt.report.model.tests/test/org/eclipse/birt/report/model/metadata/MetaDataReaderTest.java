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

package org.eclipse.birt.report.model.metadata;

import org.eclipse.birt.report.model.i18n.ThreadResources;

import com.ibm.icu.util.ULocale;

/**
 * Test case for MetaDataReader.
 * 
 * 1) Test read a rom file that doesn't exist, an exception should be thrown.
 *  
 */
public class MetaDataReaderTest extends AbstractMetaTest
{

	protected MetaDataReader reader = null;

	/*
	 * @see TestCase#setUp()
	 */

	protected void setUp( ) throws Exception
	{
		super.setUp( );
		reader = new MetaDataReader( );
		MetaDataDictionary.reset( );
		MetaDataDictionary.getInstance( );
		ThreadResources.setLocale( ULocale.ENGLISH );
	}

	/**
	 * test reading a file that doesn't exist.
	 */

	public void testReadNotExistingFile( )
	{
		try
		{
			MetaDataReader.read( "notexistingfile.xml" ); //$NON-NLS-1$
			fail( );
		}
		catch ( MetaDataParserException e )
		{
			assertEquals( MetaDataParserException.DESIGN_EXCEPTION_FILE_NOT_FOUND, e
					.getErrorCode( ) );
		}
	}

	/**
	 * test reading a file having incorrect format.
	 */
	public void testIncorrentFile( )
	{
		// test reading the file with incorrecot format

		try
		{
			MetaDataReader.read( MetaDataReaderTest.class
					.getResourceAsStream( "input/MetaDataReaderTest.xml" ) ); //$NON-NLS-1$
			fail( );
		}
		catch ( MetaDataParserException e )
		{
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */

	protected void tearDown( ) throws Exception
	{
		super.tearDown( );
		reader = null;
	}
}
