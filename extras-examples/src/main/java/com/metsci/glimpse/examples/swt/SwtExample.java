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

import static com.metsci.glimpse.gl.util.GLPBufferUtils.*;

import java.util.logging.Logger;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.swt.canvas.NewtSwtGlimpseCanvas;
import com.metsci.glimpse.swt.misc.SwtLookAndFeel;
import com.metsci.glimpse.swt.repaint.SwtRepaintManager;

/**
 * @author ulman
 */
public abstract class SwtExample
{
    protected static final Logger logger = Logger.getLogger( SwtExample.class.getName( ) );

    public static void showWithSwt( GlimpseLayoutProvider layoutProvider ) throws Exception
    {
        // generate a GLContext by constructing a small offscreen pixel buffer
        final GLOffscreenAutoDrawable pBuffer = createPixelBuffer( 1, 1 );
        final GLContext context = pBuffer.getContext( );

        Display display = new Display( );
        Shell shell = new Shell( display );
        shell.setText( "Glimpse Example (SWT)" );
        shell.setLayout( new FillLayout( ) );

        final NewtSwtGlimpseCanvas canvas = new NewtSwtGlimpseCanvas( shell, GLProfile.GL2GL3, context, SWT.NO_BACKGROUND );
        canvas.addLayout( layoutProvider.getLayout( ) );
        canvas.setLookAndFeel( new SwtLookAndFeel( ) );

        final RepaintManager manager = SwtRepaintManager.newRepaintManager( canvas );

        canvas.addDisposeListener( shell, pBuffer );

        shell.setSize( 800, 800 );
        shell.setLocation( 0, 0 );
        shell.open( );
        shell.moveAbove( null );

        while ( !shell.isDisposed( ) )
            if ( !display.readAndDispatch( ) ) display.sleep( );

        // shutdown the Glimpse repaint manager
        manager.shutdown( );

        return;
    }
}