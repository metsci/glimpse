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
package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.layers.misc.UiUtils.bindLabel;
import static com.metsci.glimpse.layers.misc.UiUtils.bindToggleButton;
import static javax.swing.BorderFactory.createLineBorder;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.util.var.DisposableGroup;

import net.miginfocom.swing.MigLayout;

public class LayerCard extends JPanel
{

    protected final Layer layer;

    protected final DisposableGroup disposables;


    public LayerCard( Layer layer )
    {
        this.layer = layer;

        this.disposables = new DisposableGroup( );

        this.setBorder( createLineBorder( this.getBackground( ).darker( ), 1 ) );
        this.setLayout( new MigLayout( "fillx", "[][][push,grow]" ) );

        // Layer
        //

        JCheckBox layerVisibleCheck = new JCheckBox( );
        this.disposables.add( bindToggleButton( layerVisibleCheck, layer.isVisible ) );
        this.add( layerVisibleCheck, "" );

        JLabel layerTitleLabel = new JLabel( );
        this.disposables.add( bindLabel( layerTitleLabel, layer.title ) );
        this.add( layerTitleLabel, "spanx 2" );

        // Clicking anywhere in the LayerCard toggles the checkbox
        addMouseListener( new MouseAdapter( )
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                layerVisibleCheck.doClick( );
            }
        } );
    }

    public void dispose( )
    {
        this.disposables.dispose( );
    }

}
