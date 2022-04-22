/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: Actuate Corporation -
 * initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.tests.chart.regression;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.FontDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.InsetsImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.attribute.impl.TextAlignmentImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.birt.report.tests.chart.ChartTestCase;

/**
 * Regression description:
 * </p>
 * Chart border setting doesn't work.
 * </p>
 * Test description:
 * <p>
 * Set chart border, view the jpg
 * </p>
 */

public class Regression_78433 extends ChartTestCase
{

	private static String OUTPUT = "Regression_78433.jpg"; //$NON-NLS-1$

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A chart model instance
	 */
	private Chart cm = null;

	/**
	 * The jpg rendering device
	 */
	private IDeviceRenderer dRenderer = null;

	private GeneratedChartState gcs = null;

	/**
	 * execute application
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		new Regression_78433( );
	}

	/**
	 * Constructor
	 */
	public Regression_78433( )
	{
		final PluginSettings ps = PluginSettings.instance( );
		try
		{
			dRenderer = ps.getDevice( "dv.JPG" );//$NON-NLS-1$

		}
		catch ( ChartException ex )
		{
			ex.printStackTrace( );
		}
		cm = createBarChart( );
		BufferedImage img = new BufferedImage(
				500,
				500,
				BufferedImage.TYPE_INT_ARGB );
		Graphics g = img.getGraphics( );

		Graphics2D g2d = (Graphics2D) g;
		dRenderer.setProperty( IDeviceRenderer.GRAPHICS_CONTEXT, g2d );
		dRenderer.setProperty( IDeviceRenderer.FILE_IDENTIFIER, this
				.genOutputFile( OUTPUT )
				  ); //$NON-NLS-1$
		Bounds bo = BoundsImpl.create( 0, 0, 500, 500 );
		bo.scale( 72d / dRenderer.getDisplayServer( ).getDpiResolution( ) );

		Generator gr = Generator.instance( );

		try
		{
			gcs = gr.build(
					dRenderer.getDisplayServer( ),
					cm,
					bo,
					null,
					null,
					null );
			gr.render( dRenderer, gcs );
			fail( );
		}
		catch ( ChartException e )
		{
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// success
		}
	}

	public void test_regression_78433( ) throws Exception
	{
		new Regression_78433( );
	}

	/**
	 * Creates a bar chart model as a reference implementation
	 * 
	 * @return An instance of the simulated runtime chart model (containing
	 *         filled datasets)
	 */
	public static final Chart createBarChart( )
	{
		ChartWithAxes cwaBar = ChartWithAxesImpl.create( );

		// Chart Type
		cwaBar.setType( "Bar Chart" );
		cwaBar.setSubType( "Stacked" );

		// Title
		cwaBar.getTitle( ).getLabel( ).getCaption( ).setValue(
				"Computer Hardware Sales" ); //$NON-NLS-1$
		cwaBar.getTitle( ).setOutline(
				LineAttributesImpl.create( ColorDefinitionImpl.create(
						239,
						33,
						3 ), LineStyle.DASH_DOTTED_LITERAL, 3 ) );
		cwaBar.getTitle( ).setInsets( InsetsImpl.create( 0, 10, 20, 10 ) );
		cwaBar.getBlock( ).setBackground( ColorDefinitionImpl.WHITE( ) );
		cwaBar.getBlock( ).setOutline(
				LineAttributesImpl.create( ColorDefinitionImpl.create(
						239,
						33,
						3 ), LineStyle.DOTTED_LITERAL, 2 ) );

		// Plot
		cwaBar.getPlot( ).getClientArea( ).getOutline( ).setVisible( false );
		cwaBar.getPlot( ).getClientArea( ).setBackground(
				ColorDefinitionImpl.create( 255, 255, 225 ) );

		// X-Axis
		Axis xAxisPrimary = ( (ChartWithAxesImpl) cwaBar ).getPrimaryBaseAxes( )[0];
		xAxisPrimary.getTitle( ).setVisible( false );

		xAxisPrimary.setType( AxisType.TEXT_LITERAL );
		xAxisPrimary.getOrigin( ).setType( IntersectionType.MAX_LITERAL );
		xAxisPrimary.getLabel( ).getCaption( ).setColor(
				ColorDefinitionImpl.GREEN( ).darker( ) );

		// Y-Axis
		Axis yAxisPrimary = ( (ChartWithAxesImpl) cwaBar )
				.getPrimaryOrthogonalAxis( xAxisPrimary );
		yAxisPrimary.getLabel( ).getCaption( ).setValue( "Sales Growth" ); //$NON-NLS-1$
		FontDefinition fd = FontDefinitionImpl.create(
				"Arial",
				(float) 30.0,
				true,
				true,
				false,
				true,
				false,
				30.0,
				TextAlignmentImpl.create( ) );
		yAxisPrimary.getLabel( ).getCaption( ).setFont( fd );
		yAxisPrimary.getLabel( ).getCaption( ).setColor(
				ColorDefinitionImpl.BLUE( ) );

		yAxisPrimary.getTitle( ).setVisible( false );
		yAxisPrimary.setType( AxisType.LINEAR_LITERAL );
		yAxisPrimary.getOrigin( ).setType( IntersectionType.MAX_LITERAL );
		yAxisPrimary.getScale( ).setMin( NumberDataElementImpl.create( 120 ) );
		yAxisPrimary.getScale( ).setMax( NumberDataElementImpl.create( 110 ) );

		// Data Set
		TextDataSet dsStringValue = TextDataSetImpl.create( new String[]{
				"Keyboards", "Moritors", "Printers", "Mortherboards"} );
		NumberDataSet dsNumericValues1 = NumberDataSetImpl
				.create( new double[]{143.26, 156.55, 95.25, 47.56} );

		// X-Series
		Series seBase = SeriesImpl.create( );
		seBase.setDataSet( dsStringValue );

		SeriesDefinition sdX = SeriesDefinitionImpl.create( );
		xAxisPrimary.getSeriesDefinitions( ).add( sdX );
		sdX.getSeries( ).add( seBase );

		// Y-Series
		BarSeries bs = (BarSeries) BarSeriesImpl.create( );
		bs.setSeriesIdentifier( "Actuate" ); //$NON-NLS-1$
		bs.getLabel( ).getCaption( ).setColor( ColorDefinitionImpl.RED( ) );
		bs.getLabel( ).setBackground( ColorDefinitionImpl.CYAN( ) );
		bs.getLabel( ).setVisible( true );
		bs.setDataSet( dsNumericValues1 );
		bs.setStacked( true );

		SeriesDefinition sdY = SeriesDefinitionImpl.create( );
		yAxisPrimary.getSeriesDefinitions( ).add( sdY );
		sdY.getSeriesPalette( ).update( ColorDefinitionImpl.BLUE( ) );
		sdY.getSeries( ).add( bs );

		return cwaBar;

	}
}
