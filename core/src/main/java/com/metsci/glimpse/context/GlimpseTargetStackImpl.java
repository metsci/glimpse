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
package com.metsci.glimpse.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.metsci.glimpse.canvas.GlimpseCanvas;

/**
 *
 * @author ulman
 * @see GlimpseTargetStack
 */
public class GlimpseTargetStackImpl implements GlimpseTargetStack
{
    private LinkedList<GlimpseTarget> targetStack;
    private LinkedList<GlimpseBounds> boundStack;

    public GlimpseTargetStackImpl( GlimpseTarget... targets )
    {
        this.targetStack = new LinkedList<GlimpseTarget>( );
        this.boundStack = new LinkedList<GlimpseBounds>( );

        for ( GlimpseTarget target : targets )
        {
            this.push( target );
        }
    }

    public GlimpseTargetStackImpl( GlimpseCanvas canvas )
    {
        this.targetStack = new LinkedList<GlimpseTarget>( );
        this.boundStack = new LinkedList<GlimpseBounds>( );

        this.push( canvas, canvas.getTargetBounds( ) );
    }

    @Override
    public GlimpseTargetStack push( GlimpseTarget target, GlimpseBounds bounds )
    {
        this.targetStack.push( target );
        this.boundStack.push( bounds );
        return this;
    }

    @Override
    public GlimpseTargetStack push( GlimpseTarget target )
    {
        // since no bounds were specified, use the currently
        // cached bounds for the target under this context if
        // they exist or simply use dummy bounds otherwise
        GlimpseBounds bounds = target.getTargetBounds( this );
        if ( bounds == null )
        {
            bounds = new GlimpseBounds( 0, 0, 0, 0 );
        }

        this.targetStack.push( target );
        this.boundStack.push( bounds );
        return this;
    }

    @Override
    public GlimpseTargetStack push( GlimpseTargetStack stack )
    {
        List<GlimpseTarget> targetList = stack.getTargetList( );
        List<GlimpseBounds> boundsList = stack.getBoundsList( );

        for ( int i = targetList.size( ) - 1 ; i >= 0 ; i-- )
        {
            push( targetList.get( i ), boundsList.get( i ) );
        }

        return this;
    }

    @Override
    public GlimpseTargetStack pop( )
    {
        this.targetStack.pop( );
        this.boundStack.pop( );
        return this;
    }

    @Override
    public GlimpseTarget getTarget( )
    {
        return this.targetStack.peek( );
    }

    @Override
    public GlimpseBounds getBounds( )
    {
        return this.boundStack.peek( );
    }

    @Override
    public List<GlimpseTarget> getTargetList( )
    {
        List<GlimpseTarget> copy = new ArrayList<GlimpseTarget>( targetStack.size( ) );
        copy.addAll( targetStack );
        return Collections.unmodifiableList( copy );
    }

    @Override
    public List<GlimpseBounds> getBoundsList( )
    {
        List<GlimpseBounds> copy = new ArrayList<GlimpseBounds>( boundStack.size( ) );
        copy.addAll( boundStack );
        return Collections.unmodifiableList( copy );
    }

    @Override
    public int getSize( )
    {
        return targetStack.size( );
    }

    @Override
    public int hashCode( )
    {
        return this.targetStack.hashCode( );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;

        GlimpseTargetStackImpl other = ( GlimpseTargetStackImpl ) obj;
        if ( targetStack == null )
        {
            if ( other.targetStack != null ) return false;
        }
        else if ( !targetStack.equals( other.targetStack ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString( )
    {
        StringBuilder b = new StringBuilder( );
        int size = getSize( );

        b.append( "[" );

        for ( int i = 0 ; i < size ; i++ )
        {
            b.append( String.format( "[%s,%s]", targetStack.get( i ), boundStack.get( i ) ) );
            if ( i < size-1 ) b.append( "," );
        }

        b.append( "]" );

        return b.toString( );
    }
}
