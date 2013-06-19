package com.metsci.glimpse.docking;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JToolBar;

public class View
{

    public final ViewKey viewKey;
    public final String title;
    public final Icon icon;
    public final String tooltip;
    public final Component component;
    public final JToolBar toolbar;


    public View( String viewId, String title, Component component )
    {
        this( viewId, title, null, null, component, null );
    }

    public View( String viewId, String title, Icon icon, String tooltip, Component component, JToolBar toolbar )
    {
        this.viewKey = new ViewKey( viewId );
        this.title = title;
        this.icon = icon;
        this.tooltip = tooltip;
        this.component = component;
        this.toolbar = toolbar;
    }

}
