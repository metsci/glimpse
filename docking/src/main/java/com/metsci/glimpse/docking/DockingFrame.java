package com.metsci.glimpse.docking;

import javax.swing.JFrame;

public class DockingFrame extends JFrame
{

    public final DockingPane docker;


    public DockingFrame( DockingPane docker )
    {
        super( "DockingFrame" );
        this.docker = docker;
        setContentPane( docker );
    }

}
