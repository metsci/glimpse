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
package com.metsci.glimpse.docking;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public abstract class Tile extends JComponent
{

    public static interface TileListener
    {
        void addedView( View view );

        void removedView( View view );

        void selectedView( View view );
    }

    public static class TileAdapter implements TileListener
    {
        @Override
        public void addedView( View view )
        {
        }

        @Override
        public void removedView( View view )
        {
        }

        @Override
        public void selectedView( View view )
        {
        }
    }

    public abstract void addListener( TileListener listener );

    public abstract void removeListener( TileListener listener );

    public abstract int numViews( );

    public abstract View view( int viewNum );

    public abstract View selectedView( );

    public abstract void addView( final View view, int viewNum );

    public abstract void updateView( View view );

    public abstract void removeView( View view );

    public abstract boolean hasView( View view );

    public abstract void selectView( View view );

    public abstract int viewNumForTabAt( int x, int y );

    public abstract Rectangle viewTabBounds( int viewNum );

    public abstract void addDockingMouseAdapter( MouseAdapter mouseAdapter );

}
