/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.support.popup;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.util.var.Notifier;

public class GlimpseAxisLayout2DWithPopup extends GlimpseAxisLayout2D implements GlimpsePopupMenuTarget
{

    protected JPopupMenu popupMenu;
    protected Notifier<GlimpseMouseEvent> popupMenuNotifier;


    public GlimpseAxisLayout2DWithPopup( GlimpseLayout parent, String name, Axis2D axis )
    {
        super( parent, name, axis );

        this.popupMenu = new JPopupMenu( )
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void addImpl( Component c, Object constraints, int index )
            {
                if ( c instanceof JComponent )
                {
                    // If menu items have different values for alignmentX, the menu ends up
                    // ridiculously wide ... for now just left-align everything; maybe make
                    // this alignment configurable in the future
                    ( ( JComponent ) c ).setAlignmentX( 0f );
                }

                super.addImpl( c, constraints, index );
            }
        };

        this.popupMenuNotifier = new Notifier<>( );
    }

    @Override
    public JPopupMenu getPopupMenu( )
    {
        return this.popupMenu;
    }

    @Override
    public Notifier<GlimpseMouseEvent> getPopupMenuNotifier( )
    {
        return this.popupMenuNotifier;
    }

}
