package com.metsci.glimpse.docking;

import javax.swing.JFrame;

public class DockingFrame extends JFrame
{

    public final DockingPane docker;


    public DockingFrame( String title, DockingPane docker )
    {
        super( title );
        this.docker = docker;
        setContentPane( docker );
    }

}
