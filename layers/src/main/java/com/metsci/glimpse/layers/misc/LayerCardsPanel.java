/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.util.var.VarUtils.addElementAddedListener;
import static com.metsci.glimpse.util.var.VarUtils.addElementRemovedListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.Var;

import net.miginfocom.swing.MigLayout;

public class LayerCardsPanel extends JPanel
{

    protected final Var<? extends List<? extends Layer>> layers;
    protected final DisposableGroup disposables;

    protected final Map<Layer,JComponent> cards;


    public LayerCardsPanel( Var<? extends List<? extends Layer>> layers )
    {
        this.layers = layers;
        this.disposables = new DisposableGroup( );
        this.cards = new HashMap<>( );

        this.setLayout( new MigLayout( "fillx" ) );
        this.setBackground( UIManager.getColor( "List.background" ) );

        this.disposables.add( addElementAddedListener( this.layers, true, ( layer ) ->
        {
            JComponent card = new LayerCard( layer );
            this.cards.put( layer, card );
            this.add( card, "growx, wrap" );
        } ) );

        this.disposables.add( addElementRemovedListener( this.layers, ( layer ) ->
        {
            JComponent card = this.cards.remove( layer );
            this.remove( card );
        } ) );
    }

    public void dispose( )
    {
        this.disposables.dispose( );
    }

}
