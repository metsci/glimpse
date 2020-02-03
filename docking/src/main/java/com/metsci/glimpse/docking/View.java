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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_AUTO_CLOSEABLE;
import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_NOT_CLOSEABLE;

import java.awt.Component;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JToolBar;

import com.metsci.glimpse.util.var.Var;

public class View
{

    public final String viewId;
    public final Var<Component> component;
    public final Var<String> title;
    public final ViewCloseOption closeOption;
    public final Var<String> tooltip;
    public final Var<Icon> icon;
    public final JToolBar toolbar;


    public View( String viewId, Component component, String title )
    {
        this( viewId, component, title, false );
    }

    public View( String viewId, Component component, String title, boolean autoCloseable )
    {
        this( viewId, component, title, autoCloseable, null );
    }

    public View( String viewId, Component component, String title, boolean autoCloseable, String tooltip )
    {
        this( viewId, component, title, autoCloseable, tooltip, ( Icon ) null );
    }

    public View( String viewId, Component component, String title, boolean autoCloseable, String tooltip, URL iconUrl )
    {
        this( viewId, component, title, autoCloseable, tooltip, requireIcon( iconUrl ) );
    }

    public View( String viewId, Component component, String title, boolean autoCloseable, String tooltip, Icon icon )
    {
        this( viewId, component, title, autoCloseable, tooltip, icon, null );
    }

    public View( String viewId, Component component, String title, boolean autoCloseable, String tooltip, URL iconUrl, JToolBar toolbar )
    {
        this( viewId, component, title, autoCloseable, tooltip, requireIcon( iconUrl ), toolbar );
    }

    public View( String viewId, Component component, String title, boolean autoCloseable, String tooltip, Icon icon, JToolBar toolbar )
    {
        this( viewId, component, title, ( autoCloseable ? VIEW_AUTO_CLOSEABLE : VIEW_NOT_CLOSEABLE ), tooltip, icon, toolbar );
    }

    public View( String viewId, Component component, String title, ViewCloseOption closeOption )
    {
        this( viewId, component, title, closeOption, null );
    }

    public View( String viewId, Component component, String title, ViewCloseOption closeOption, String tooltip )
    {
        this( viewId, component, title, closeOption, tooltip, ( Icon ) null );
    }

    public View( String viewId, Component component, String title, ViewCloseOption closeOption, String tooltip, URL iconUrl )
    {
        this( viewId, component, title, closeOption, tooltip, requireIcon( iconUrl ) );
    }

    public View( String viewId, Component component, String title, ViewCloseOption closeOption, String tooltip, Icon icon )
    {
        this( viewId, component, title, closeOption, tooltip, icon, null );
    }

    public View( String viewId, Component component, String title, ViewCloseOption closeOption, String tooltip, URL iconUrl, JToolBar toolbar )
    {
        this( viewId, component, title, closeOption, tooltip, requireIcon( iconUrl ), toolbar );
    }

    public View( String viewId, Component component, String title, ViewCloseOption closeOption, String tooltip, Icon icon, JToolBar toolbar )
    {
        this.viewId = viewId;
        this.component = new Var<>( component );
        this.title = new Var<>( title );
        this.closeOption = closeOption;
        this.tooltip = new Var<>( tooltip );
        this.icon = new Var<>( icon );
        this.toolbar = toolbar;
    }
}
