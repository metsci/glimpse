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
package com.metsci.glimpse.painter.group;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A painter which delegates its painting to a collection of
 * delegate {@code GlimpsePainters}. This class can be used
 * to enforce a particular ordering on multiple sets of painter
 * (a background set and a foreground set, for example).
 *
 * @author ulman
 */
public class DelegatePainter implements GlimpsePainter
{
    private List<GlimpsePainter> painters;

    private boolean isVisible = true;
    private boolean isDisposed = false;

    public DelegatePainter( )
    {
        this.painters = new CopyOnWriteArrayList<GlimpsePainter>( );
    }

    public void addPainter( GlimpsePainter painter )
    {
        this.painters.add( painter );
    }

    public void addPainter( GlimpsePainter painter, int index )
    {
        this.painters.add( index, painter );
    }

    public void removePainter( GlimpsePainter painter )
    {
        this.painters.remove( painter );
    }

    public void removeAll( )
    {
        this.painters.clear( );
    }

    public int getNumPainters( )
    {
        return painters.size( );
    }

    public int indexOf( GlimpsePainter painter )
    {
        return painters.indexOf( painter );
    }

    public boolean isVisible( )
    {
        return this.isVisible;
    }

    public void setVisible( boolean visible )
    {
        this.isVisible = visible;
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        if ( !isVisible ) return;

        for ( GlimpsePainter painter : painters )
        {
            painter.paintTo( context );
        }
    }

    @Override
    public void dispose( GlimpseContext context )
    {
        if ( !isDisposed )
        {
            isDisposed = true;

            for ( GlimpsePainter painter : painters )
            {
                painter.dispose( context );
            }
        }
    }

    @Override
    public boolean isDisposed( )
    {
        return isDisposed;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpsePainter painter : painters )
        {
            painter.setLookAndFeel( laf );
        }
    }
}
