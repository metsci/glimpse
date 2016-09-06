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
package com.metsci.glimpse.painter.base;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.gl.util.GLErrorUtils;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.support.settings.LookAndFeel;

public abstract class GlimpsePainterImpl implements GlimpsePainter
{
    public static final Logger logger = Logger.getLogger( GlimpsePainterImpl.class.getName( ) );

    public static final int BYTES_PER_FLOAT = 4;

    protected volatile boolean disposed = false;
    protected final ReentrantLock disposeLock;

    protected volatile boolean displayOn = true;
    protected volatile boolean doErrorHandling = true;

    protected final String errorPrefix = "GL ERROR: " + getClass( ).getName( );

    public GlimpsePainterImpl( )
    {
        this.disposeLock = new ReentrantLock( );
    }
    
    public static GL3 getGL3( GlimpseContext context )
    {
        return context.getGL( ).getGL3( );
    }

    public static Axis2D getAxis2D( GlimpseContext context )
    {
        GlimpseTarget target = context.getTargetStack( ).getTarget( );
        if ( target instanceof GlimpseAxisLayout2D )
        {
            GlimpseAxisLayout2D layout = ( GlimpseAxisLayout2D ) target;
            Axis2D axis = layout.getAxis( context );

            if ( axis == null )
            {
                // Some GlimpseAxisLayout2D in the GlimpseContext must define an Axis2D
                throw new AxisNotSetException( context );
            }

            return axis;
        }
        else
        {
            // GlimpsePainter2D instances must be painted to GlimpseAxisLayout2D instances
            throw new AxisNotSetException( context );
        }
    }

    public static Axis1D getAxis1D( GlimpseContext context )
    {
        GlimpseTarget target = context.getTargetStack( ).getTarget( );
        if ( target instanceof GlimpseAxisLayout1D )
        {
            GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
            Axis1D axis = layout.getAxis( context );

            if ( axis == null )
            {
                // Some GlimpseAxisLayout2D in the GlimpseContext must define an Axis2D
                throw new AxisNotSetException( context );
            }

            return axis;
        }
        else
        {
            // GlimpsePainter2D instances must be painted to GlimpseAxisLayout2D instances
            throw new AxisNotSetException( context );
        }
    }

    public static GlimpseBounds getBounds( GlimpseContext context )
    {
        return context.getTargetStack( ).getBounds( );
    }

    public void setErrorHandling( boolean doErrorHandling )
    {
        this.doErrorHandling = doErrorHandling;
    }

    @Override
    public void setVisible( boolean show )
    {
        this.displayOn = show;
    }

    @Override
    public boolean isVisible( )
    {
        return displayOn;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        // default behavior is to simply do nothing
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        // Double-checked locking works fine with a volatile var (as of Java 5)
        if ( !this.disposed )
        {
            this.disposeLock.lock( );
            try
            {
                if ( !this.disposed )
                {
                    this.disposed = true;
                    this.disposeOnce( context );
                }
            }
            finally
            {
                this.disposeLock.unlock( );
            }
        }
    }
    
    protected abstract void disposeOnce( GlimpseContext context );

    @Override
    public boolean isDisposed( )
    {
        return this.disposed;
    }

    protected boolean glHandleError( GL gl )
    {
        return glHandleError( gl, "GL ERROR" );
    }

    protected boolean glHandleError( GL gl, String prefix )
    {
        return GLErrorUtils.logGLError( logger, gl, prefix );
    }
}
