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
package com.metsci.glimpse.axis;

import java.util.HashMap;
import java.util.Map;

import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;

/**
 * A delegate class holding two orthogonal one dimensional axes. Contains convenience methods
 * for accessing the individual "x" and "y" {@link Axis1D}.</p>
 *
 * @author ulman
 * @see com.metsci.glimpse.axis.Axis1D
 */
public class Axis2D
{
    protected Axis1D x;
    protected Axis1D y;

    protected Map<AxisListener2D,AxisListener1D> listeners;

    public Axis2D( final Axis1D x, final Axis1D y )
    {
        this.x = x;
        this.y = y;

        this.listeners = new HashMap<>( );
    }

    public Axis2D( )
    {
        this( new Axis1D( ), new Axis1D( ) );
    }

    @Override
    public Axis2D clone( )
    {
        Axis1D newX = x.clone( );
        Axis1D newY = y.clone( );

        if ( x.getLockedAspectAxis( ) == y )
        {
            newX.lockAspectRatio( newY, x.getLockedAspectRatio( ) );
        }

        if ( y.getLockedAspectAxis( ) == x )
        {
            newY.lockAspectRatio( newX, y.getLockedAspectRatio( ) );
        }

        return new Axis2D( newX, newY );
    }

    // synchronize to maintain thread safe access to listeners map
    public synchronized void removeAxisListener( final AxisListener2D listener )
    {
        AxisListener1D listener1D = listeners.remove( listener );
        x.removeAxisListener( listener1D );
        y.removeAxisListener( listener1D );
    }

    // synchronize to maintain thread safe access to listeners map
    public synchronized void addAxisListener( final AxisListener2D listener )
    {
        AxisListener1D listener1D = new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D axis )
            {
                listener.axisUpdated( Axis2D.this );
            }
        };

        listeners.put( listener, listener1D );
        x.addAxisListener( listener1D );
        y.addAxisListener( listener1D );
    }

    public void lockAspectRatioXY( double x_to_y_ratio )
    {
        x.lockAspectRatio( y, x_to_y_ratio );
        y.lockAspectRatio( x, 1.0 / x_to_y_ratio );

        validate( );
    }

    public void validate( )
    {
        applyConstraints( );
        updateLinkedAxes( );
    }

    public void applyConstraints( )
    {
        x.applyConstraints( );
        y.applyConstraints( );
    }

    public void updateLinkedAxes( )
    {
        x.updateLinkedAxes( y );
        y.updateLinkedAxes( );
    }

    public void unlockAspectRatioXY( )
    {
        x.unlockAspectRatio( );
        y.unlockAspectRatio( );
    }

    public void centerOnPoint( double newCenterX, double newCenterY )
    {
        double minX = getMinX( );
        double maxX = getMaxX( );
        double minY = getMinY( );
        double maxY = getMaxY( );

        double centerX = ( maxX - minX ) / 2 + minX;
        double centerY = ( maxY - minY ) / 2 + minY;

        double transX = newCenterX - centerX;
        double transY = newCenterY - centerY;

        x.setMin( minX + transX );
        x.setMax( maxX + transX );

        y.setMin( minY + transY );
        y.setMax( maxY + transY );

        x.validate( );
        y.validate( );
    }

    public void set( double minX, double maxX, double minY, double maxY )
    {
        x.setMin( minX );
        x.setMax( maxX );
        y.setMin( minY );
        y.setMax( maxY );
    }

    public void lock( )
    {
        x.lock( );
        y.lock( );
    }

    public void unlock( )
    {
        x.unlock( );
        y.unlock( );
    }

    public double getMinX( )
    {
        return x.getMin( );
    }

    public double getMaxX( )
    {
        return x.getMax( );
    }

    public double getMinY( )
    {
        return y.getMin( );
    }

    public double getMaxY( )
    {
        return y.getMax( );
    }

    public Axis1D getAxisX( )
    {
        return x;
    }

    public Axis1D getAxisY( )
    {
        return y;
    }

    public void setParent( Axis2D parent )
    {
        if ( x != null ) x.setParent( parent == null ? null : parent.getAxisX( ) );
        if ( y != null ) y.setParent( parent == null ? null : parent.getAxisY( ) );
    }

    public void setLinkChildren( boolean link )
    {
        if ( x != null ) x.setLinkChildren( link );
        if ( y != null ) y.setLinkChildren( link );
    }

    public void setSizePixels( GlimpseTargetStack stack )
    {
        setSizePixels( stack.getBounds( ) );
    }

    public void setSizePixels( GlimpseBounds bounds )
    {
        if ( x != null ) x.setSizePixels( bounds.getWidth( ), false );
        if ( y != null ) y.setSizePixels( bounds.getHeight( ), false );
        if ( x != null ) x.setInitialized( );
        if ( y != null ) y.setInitialized( );
    }

    @Override
    public String toString( )
    {
        return String.format( "[%s %s]", x, y );
    }
}
