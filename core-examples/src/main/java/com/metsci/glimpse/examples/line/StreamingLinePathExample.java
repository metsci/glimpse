/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.examples.line;

import static com.metsci.glimpse.gl.util.GLUtils.*;
import static com.metsci.glimpse.support.FrameUtils.*;
import static com.metsci.glimpse.support.shader.line.LineJoinType.*;
import static com.metsci.glimpse.support.shader.line.LineUtils.*;
import static com.metsci.glimpse.util.GeneralUtils.*;
import static java.lang.Math.*;
import static javax.swing.WindowConstants.*;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.painter.label.GridAxisLabelHandlerSimpleUnits;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorXAxisPainter;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D.AxisInfo;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.shader.line.LineProgram;
import com.metsci.glimpse.support.shader.line.LineStyle;
import com.metsci.glimpse.support.shader.line.StreamingLinePath;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class StreamingLinePathExample
{

    public static void main( String[] args )
    {
        final MultiAxisPlot2D plot = new MultiAxisPlot2D( );
        plot.getCenterAxis( ).lockAspectRatioXY( 1.0 );
        plot.getCenterAxis( ).set( -3, +3, -3, +3 );

        TaggedAxis1D leftAngleAxis = new TaggedAxis1D( );
        AxisInfo leftAngleAxisInfo = plot.createAxisLeft( "leftAngleAxis", leftAngleAxis, new TaggedAxisMouseListener1D( ) );
        leftAngleAxisInfo.setAxisPainter( new TaggedPartialColorYAxisPainter( new GridAxisLabelHandlerSimpleUnits( ) ) );
        Tag leftAngleTag = leftAngleAxis.addTag( "leftAngle", +90 );
        leftAngleAxis.setMin( -360 );
        leftAngleAxis.setMax( +360 );

        TaggedAxis1D rightAngleAxis = new TaggedAxis1D( leftAngleAxis );
        AxisInfo rightAngleAxisInfo = plot.createAxisRight( "rightAngleAxis", rightAngleAxis, new TaggedAxisMouseListener1D( ) );
        rightAngleAxisInfo.setAxisPainter( new TaggedPartialColorYAxisPainter( new GridAxisLabelHandlerSimpleUnits( ) ) );
        Tag rightAngleTag = rightAngleAxis.addTag( "rightAngle", -90 );
        rightAngleAxis.setMin( -360 );
        rightAngleAxis.setMax( +360 );

        TaggedAxis1D thicknessAxis = new TaggedAxis1D( );
        AxisInfo thicknessAxisInfo = plot.createAxisBottom( "thicknessAxis", thicknessAxis, new TaggedAxisMouseListener1D( ) );
        thicknessAxisInfo.setAxisPainter( new TaggedPartialColorXAxisPainter( new GridAxisLabelHandlerSimpleUnits( ) ) );
        Tag thicknessTag = thicknessAxis.addTag( "thickness", 10 );
        thicknessAxis.setMin( 0 );
        thicknessAxis.setMax( 100 );

        TaggedAxis1D featherAxis = new TaggedAxis1D( thicknessAxis );
        AxisInfo featherAxisInfo = plot.createAxisBottom( "featherAxis", featherAxis, new TaggedAxisMouseListener1D( ) );
        featherAxisInfo.setAxisPainter( new TaggedPartialColorXAxisPainter( new GridAxisLabelHandlerSimpleUnits( ) ) );
        Tag featherTag = featherAxis.addTag( "feather", 0.9f );
        featherAxis.setMin( 0 );
        featherAxis.setMax( 100 );

        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new CustomLinesPainter( leftAngleTag, rightAngleTag, thicknessTag, featherTag ) );
        plot.addPainter( new BorderPainter( ) );

        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 30 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "LinePathExample", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    public static class CustomLinesPainter extends GlimpsePainterBase
    {
        protected final StreamingLinePath path;
        protected final LineStyle style;
        protected final LineProgram prog;

        protected final Tag angleTagB_CWDEG;
        protected final Tag angleTagC_CWDEG;
        protected final Tag thicknessTag_PX;
        protected final Tag featherTag_PX;

        public CustomLinesPainter( Tag leftAngleTag_CWDEG, Tag rightAngleTag_CWDEG, Tag thicknessTag_PX, Tag featherTag_PX )
        {
            // Create a path, which will be populated in doPaintTo()
            this.path = new StreamingLinePath( );

            // Set line appearance
            this.style = new LineStyle( );
            this.style.joinType = JOIN_MITER;
            this.style.rgba = floats( 0.7f, 0, 0, 0.5f );

            // Create the shader program for drawing lines
            this.prog = new LineProgram( );

            this.angleTagB_CWDEG = leftAngleTag_CWDEG;
            this.angleTagC_CWDEG = rightAngleTag_CWDEG;
            this.thicknessTag_PX = thicknessTag_PX;
            this.featherTag_PX = featherTag_PX;
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GlimpseBounds bounds = getBounds( context );
            Axis2D axis = requireAxis2D( context );
            GL2ES3 gl = context.getGL( ).getGL2ES3( );

            // Populate the path based on axis tags
            this.path.map( gl, 6, style.stippleEnable, ppvAspectRatio( axis ) );
            try
            {
                double xB = -1;
                double yB = -1;
                double xC = +1;
                double yC = +1;

                double dxBC = xC - xB;
                double dyBC = yC - yB;
                double dxCB = -dxBC;
                double dyCB = -dyBC;

                double angleB_CCWRAD = toRadians( -this.angleTagB_CWDEG.getValue( ) );
                double cosAngleB = cos( angleB_CCWRAD );
                double sinAngleB = sin( angleB_CCWRAD );
                double xA = xB + dxCB*cosAngleB - dyCB*sinAngleB;
                double yA = yB + dxCB*sinAngleB + dyCB*cosAngleB;

                double angleC_CCWRAD = -toRadians( -this.angleTagC_CWDEG.getValue( ) );
                double cosAngleC = cos( angleC_CCWRAD );
                double sinAngleC = sin( angleC_CCWRAD );
                double xD = xC + dxBC*cosAngleC - dyBC*sinAngleC;
                double yD = yC + dxBC*sinAngleC + dyBC*cosAngleC;

                this.path.moveTo( ( float ) xA, ( float ) yA );
                this.path.lineTo( ( float ) xB, ( float ) yB );
                this.path.lineTo( ( float ) xC, ( float ) yC );
                this.path.lineTo( ( float ) xD, ( float ) yD );
            }
            finally
            {
                this.path.seal( gl );
            }

            // Update the style based on axis tags
            this.style.thickness_PX = ( float ) this.thicknessTag_PX.getValue( );
            this.style.feather_PX = ( float ) this.featherTag_PX.getValue( );

            // Draw the path
            enableStandardBlending( gl );
            this.prog.begin( gl );
            try
            {
                this.prog.setViewport( gl, bounds );
                this.prog.setAxisOrtho( gl, axis );
                this.prog.draw( gl, style, path );
            }
            finally
            {
                this.prog.end( gl );
                disableBlending( gl );
            }
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            GL2ES2 gl = context.getGL( ).getGL2ES2( );
            this.path.dispose( gl );
            this.prog.dispose( gl );
        }
    }

}
