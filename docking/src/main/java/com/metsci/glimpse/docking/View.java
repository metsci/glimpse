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
package com.metsci.glimpse.docking;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JToolBar;

public class View
{

    public final String viewId;
    public final Component component;
    public final String title;
    public final boolean closeable;
    public final String tooltip;
    public final Icon icon;
    public final JToolBar toolbar;


    public View( String viewId, Component component, String title )
    {
        this( viewId, component, title, false, null, null, null );
    }

    public View( String viewId, Component component, String title, boolean closeable )
    {
        this( viewId, component, title, closeable, null, null, null );
    }

    public View( String viewId, Component component, String title, boolean closeable, String tooltip )
    {
        this( viewId, component, title, closeable, tooltip, null, null );
    }

    public View( String viewId, Component component, String title, boolean closeable, String tooltip, Icon icon )
    {
        this( viewId, component, title, closeable, tooltip, icon, null );
    }

    public View( String viewId, Component component, String title, boolean closeable, String tooltip, Icon icon, JToolBar toolbar )
    {
        this.viewId = viewId;
        this.component = component;
        this.title = title;
        this.closeable = closeable;
        this.tooltip = tooltip;
        this.icon = icon;
        this.toolbar = toolbar;
    }

}
