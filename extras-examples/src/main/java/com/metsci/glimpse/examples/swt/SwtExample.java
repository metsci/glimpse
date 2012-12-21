/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.examples.swt;

import static com.metsci.glimpse.gl.util.GLPBufferUtils.createPixelBuffer;

import java.util.logging.Logger;

import javax.media.opengl.GLContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.swt.canvas.SwtGlimpseCanvas;
import com.metsci.glimpse.swt.misc.SwtLookAndFeel;

/**
 * @author ulman
 */
public abstract class SwtExample
{
    protected static final Logger logger = Logger.getLogger( SwtExample.class.getName( ) );

    public static void showWithSwt( GlimpseLayoutProvider layoutProvider ) throws Exception
    {
        Jogular.initJogl( );

        GLContext context = createPixelBuffer( 1, 1 ).getContext( );

        Display display = new Display( );
        Shell shell = new Shell( display );
        shell.setText( "Glimpse Example (SWT)" );
        shell.setLayout( new FillLayout( ) );

        final SwtGlimpseCanvas canvas = new SwtGlimpseCanvas( shell, context, SWT.NO_BACKGROUND );
        canvas.addLayout( layoutProvider.getLayout( ) );
        canvas.setLookAndFeel( new SwtLookAndFeel( ) );

        final RepaintManager manager = RepaintManager.newRepaintManager( canvas );

        shell.setSize( 800, 800 );
        shell.setLocation( 0, 0 );
        shell.open( );
        shell.moveAbove( null );
        
        shell.addDisposeListener( new DisposeListener( )
        {
            @Override
            public void widgetDisposed( DisposeEvent event )
            {
                canvas.dispose( manager );
            }
        });

        while ( !shell.isDisposed( ) )
            if ( !display.readAndDispatch( ) ) display.sleep( );
        
        return;
    }

    public static void showWithSwt( GlimpseLayoutProvider layoutProviderA, GlimpseLayoutProvider layoutProviderB ) throws Exception
    {
        Jogular.initJogl( );

        GLContext context = createPixelBuffer( 1, 1 ).getContext( );

        Display display = new Display( );
        Shell shellA = new Shell( display );
        shellA.setText( "Glimpse Example (SWT)" );
        shellA.setLayout( new FillLayout( ) );

        SwtGlimpseCanvas canvasA = new SwtGlimpseCanvas( shellA, context, SWT.NO_BACKGROUND );
        canvasA.addLayout( layoutProviderA.getLayout( ) );
        canvasA.setLookAndFeel( new SwtLookAndFeel( ) );

        RepaintManager.newRepaintManager( canvasA );

        shellA.setSize( 800, 800 );
        shellA.setLocation( 0, 0 );
        shellA.open( );
        shellA.moveAbove( null );

        Shell shellB = new Shell( display );
        shellB.setText( "Glimpse Example (SWT)" );
        shellB.setLayout( new FillLayout( ) );

        SwtGlimpseCanvas canvasB = new SwtGlimpseCanvas( shellB, context, SWT.NO_BACKGROUND );
        canvasB.addLayout( layoutProviderB.getLayout( ) );
        canvasB.setLookAndFeel( new SwtLookAndFeel( ) );

        RepaintManager.newRepaintManager( canvasB );

        shellB.setSize( 800, 800 );
        shellB.setLocation( 0, 0 );
        shellB.open( );
        shellB.moveAbove( null );

        while ( !shellA.isDisposed( ) && !shellB.isDisposed( ) )
            if ( !display.readAndDispatch( ) ) display.sleep( );

        return;
    }
}
