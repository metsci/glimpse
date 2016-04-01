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
package com.metsci.glimpse.layout;

import java.util.Collection;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.AxisNotSetException;
import com.metsci.glimpse.axis.factory.AxisFactory1D;
import com.metsci.glimpse.axis.factory.AxisFactory2D;
import com.metsci.glimpse.axis.factory.DefaultAxisFactory1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.matcher.TargetStackMatcher;

/**
 * A GlimpseLayout which can provide axes to its child
 * {@link com.metsci.glimpse.painter.base.GlimpsePainter}s.
 * GlimpseAxisLayout1D is often used for displaying timelines
 * or plot axes where dimensions only matter along one
 * orientation (the other orientation is measured simply
 * in pixel space). This is contrasted with
 * {@link GlimpseAxisLayout2D} where both horizontal and
 * verical coordinates of the GlimpseLayout have associated
 * {@link com.metsci.glimpse.axis.Axis1D} axes.
 *
 * @author ulman
 */
public abstract class GlimpseAxisLayout1D extends GlimpseLayout
{
    protected GlimpseLayoutCache<Axis1D> cache;
    protected Axis1D axis;
    protected AxisFactory1D factory;

    public GlimpseAxisLayout1D( GlimpseLayout parent, String name, Axis1D axis )
    {
        super( parent, name );

        this.axis = axis;
        this.cache = new GlimpseLayoutCache<Axis1D>( );
    }

    public GlimpseAxisLayout1D( GlimpseLayout parent, Axis1D axis )
    {
        this( parent, null, axis );
    }

    public GlimpseAxisLayout1D( String name, Axis1D axis )
    {
        this( null, name, axis );
    }

    public GlimpseAxisLayout1D( Axis1D axis )
    {
        this( null, null, axis );
    }

    public GlimpseAxisLayout1D( GlimpseLayout parent, String name )
    {
        this( parent, name, null );
    }

    public GlimpseAxisLayout1D( GlimpseLayout parent )
    {
        this( parent, null, null );
    }

    public GlimpseAxisLayout1D( )
    {
        this( null, null, null );
    }

    @Override
    protected void preLayout( GlimpseTargetStack stack, GlimpseBounds bounds )
    {
        Axis1D contextAxis = getAxis( stack );

        if ( contextAxis == null ) throw new AxisNotSetException( stack );

        contextAxis.setSizePixels( getSize( bounds ) );
    }

    public abstract boolean isHorizontal( );

    protected abstract Axis1D getAxis( Axis2D axis );

    protected abstract int getSize( GlimpseBounds bounds );

    public synchronized void clearCache( )
    {
        // remove parent links for all the cached axes then clear the cache
        for ( Axis1D axis : this.cache.getValues( ) )
        {
            axis.setParent( null );
        }
        this.cache.clear( );

        // descend recursively clearing caches
        // stop if a child has its' axis explicitly set
        // (because it is not using its' parent's axes)
        for ( GlimpseTarget target : getTargetChildren( ) )
        {
            if ( target instanceof GlimpseAxisLayout1D )
            {
                GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
                if ( !layout.isAxisSet( ) )
                {
                    layout.clearCache( );
                }
            }
        }
    }

    public void setAxis( Axis1D axis )
    {
        // set the axis for all contexts, reset the cache
        this.clearCache( );
        this.axis = axis;
    }

    public synchronized void setAxis( GlimpseTargetStack stack, Axis1D axis )
    {
        this.cache.setValue( stack, axis );
    }

    public synchronized void setAxis( GlimpseContext context, Axis1D axis )
    {
        this.cache.setValue( context, axis );
    }

    public AxisFactory1D getAxisFactory( )
    {
        return this.factory;
    }

    public void setAxisFactory( AxisFactory1D factory )
    {
        this.factory = factory;
    }

    public boolean isAxisSet( )
    {
        return this.axis != null;
    }

    public boolean isAxisFactorySet( )
    {
        return this.factory != null;
    }

    public Axis1D getAxis( )
    {
        return this.axis;
    }

    public Axis1D getAxis( GlimpseContext context )
    {
        return getAxis( context.getTargetStack( ) );
    }

    // search up through the stack until a layout with an axis is found
    // then retrieve or create a version of that axis for the current stack and return it
    public synchronized Axis1D getAxis( GlimpseTargetStack stack )
    {
        if ( !stack.getTarget( ).equals( this ) )
        {
            throw new AxisNotSetException( String.format( "GlimpseAxisLayout1D %s is not on top of GlimpseTargetStack %s. Cannot provide Axis1D", getName( ), stack ) );
        }

        AxisFactory1D factory = getAxisFactory0( stack );

        for ( GlimpseTarget target : stack.getTargetList( ) )
        {
            if ( target instanceof GlimpseAxisLayout2D )
            {
                GlimpseAxisLayout2D layout = ( GlimpseAxisLayout2D ) target;
                if ( layout.isAxisSet( ) )
                {
                    return getCachedAxis0( getAxis( layout.getAxis( ) ), factory, stack );
                }
            }
            else if ( target instanceof GlimpseAxisLayout1D )
            {
                GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
                if ( layout.isAxisSet( ) )
                {
                    return getCachedAxis0( layout.getAxis( ), factory, stack );
                }
            }
        }

        return null;
    }

    public synchronized Collection<Axis1D> getAxis( TargetStackMatcher matcher )
    {
        return this.cache.getMatching( matcher );
    }

    protected AxisFactory1D getAxisFactory0( GlimpseTargetStack stack )
    {
        for ( GlimpseTarget target : stack.getTargetList( ) )
        {
            if ( target instanceof GlimpseAxisLayout2D )
            {
                GlimpseAxisLayout2D layout = ( GlimpseAxisLayout2D ) target;
                if ( layout.isAxisFactorySet( ) )
                {
                    AxisFactory2D factory = layout.getAxisFactory( );
                    return isHorizontal( ) ? factory.getAxisFactoryX( stack ) : factory.getAxisFactoryY( stack );
                }
            }
            else if ( target instanceof GlimpseAxisLayout1D )
            {
                GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
                if ( layout.isAxisFactorySet( ) )
                {
                    return layout.getAxisFactory( );
                }
            }
        }

        return null;
    }

    // retrieve an axis for the given stack from the cache if it exists
    // otherwise, create an axis using the given parent_axis and factory and store it in the cache
    protected Axis1D getCachedAxis0( Axis1D parent_axis, AxisFactory1D factory, GlimpseTargetStack stack )
    {
        Axis1D newAxis = cache.getValueNoBoundsCheck( stack );

        if ( newAxis == null )
        {
            newAxis = getNewAxis0( parent_axis, factory, stack );
            newAxis.setSizePixels( getSize( stack.getBounds( ) ) );
            cache.setValue( stack, newAxis );
        }

        return newAxis;
    }

    protected Axis1D getCachedAxis0( Axis1D parent_axis, AxisFactory1D factory, GlimpseContext context )
    {
        return getCachedAxis0( parent_axis, factory, context.getTargetStack( ) );
    }

    protected Axis1D getNewAxis0( Axis1D parent_axis, AxisFactory1D factory, GlimpseTargetStack stack )
    {
        if ( factory != null )
        {
            return factory.newAxis( stack, parent_axis );
        }
        else
        {
            return DefaultAxisFactory1D.newAxis( parent_axis );
        }
    }
}
