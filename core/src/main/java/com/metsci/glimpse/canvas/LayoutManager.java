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
package com.metsci.glimpse.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.metsci.glimpse.layout.GlimpseLayout;

/**
 * GlimpseLayout helper class which manages properly
 * sorting GlimpseLayouts by their integer ordering constant
 * (and by order of addition when two layouts have the
 * same ordering constant).
 *
 * @author ulman
 */
public class LayoutManager
{
    protected LayoutOrderComparator comparator;
    protected Map<GlimpseLayout, LayoutOrder> layoutMap;
    protected List<LayoutOrder> layoutList;
    protected List<GlimpseLayout> unmodifiableLayoutList;

    public LayoutManager( )
    {
        this.comparator = new LayoutOrderComparator( );
        this.layoutList = new ArrayList<LayoutOrder>( );
        this.layoutMap = new LinkedHashMap<GlimpseLayout, LayoutOrder>( );
        this.unmodifiableLayoutList = Collections.emptyList( );
    }

    public synchronized void removeLayout( GlimpseLayout layout )
    {
        LayoutOrder layoutOrder = this.layoutMap.remove( layout );
        this.layoutList.remove( layoutOrder );
        this.updateLayoutList( );
    }

    public synchronized void removeAllLayouts( )
    {
        this.layoutMap.clear( );
        this.layoutList.clear( );
        this.updateLayoutList( );
    }

    public synchronized void addLayout( GlimpseLayout layout )
    {
        this.addLayout( layout, 0 );
    }

    public synchronized void addLayout( GlimpseLayout layout, int zOrder )
    {
        LayoutOrder layoutOrder = new LayoutOrder( layout, zOrder );
        this.layoutMap.put( layout, layoutOrder );
        this.layoutList.add( layoutOrder );
        this.updateLayoutList( );
    }

    public synchronized void setZOrder( GlimpseLayout layout, int zOrder )
    {
        LayoutOrder layoutOrder = this.layoutMap.get( layout );
        if ( layoutOrder != null )
        {
            layoutOrder.setZOrder( zOrder );
            updateLayoutList( );
        }
    }

    public synchronized List<GlimpseLayout> getLayoutList( )
    {
        return this.unmodifiableLayoutList;
    }

    protected void updateLayoutList( )
    {
        Collections.sort( this.layoutList, this.comparator );

        ArrayList<GlimpseLayout> temp = Lists.newArrayListWithCapacity( this.layoutList.size( ) );
        for ( LayoutOrder order : this.layoutList )
        {
            temp.add( order.getLayout( ) );
        }

        this.unmodifiableLayoutList = Collections.unmodifiableList( Lists.newArrayList( temp ) );
    }

    public static class LayoutOrder
    {
        protected GlimpseLayout layout;
        protected int zOrder;

        public LayoutOrder( GlimpseLayout layout )
        {
            this( layout, 0 );
        }

        public LayoutOrder( GlimpseLayout layout, int zOrder )
        {
            this.layout = layout;
            this.zOrder = zOrder;
        }

        public GlimpseLayout getLayout( )
        {
            return layout;
        }

        public int getZOrder( )
        {
            return zOrder;
        }

        public void setZOrder( int order )
        {
            this.zOrder = order;
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( layout == null ) ? 0 : layout.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            LayoutOrder other = ( LayoutOrder ) obj;
            if ( layout == null )
            {
                if ( other.layout != null ) return false;
            }
            else if ( !layout.equals( other.layout ) ) return false;
            return true;
        }
    }

    public static class LayoutOrderComparator implements Comparator<LayoutOrder>
    {
        @Override
        public int compare( LayoutOrder arg0, LayoutOrder arg1 )
        {
            if ( arg0.getZOrder( ) < arg1.getZOrder( ) )
            {
                return -1;
            }
            else if ( arg0.getZOrder( ) > arg1.getZOrder( ) )
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }

    }
}
