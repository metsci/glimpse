package com.metsci.glimpse.layers;

import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;

import javax.swing.JToolBar;

import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public class LayeredTimeline
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final JToolBar toolbar;
    public final View view;

    public final CollapsibleTimePlot2D plot;


    public LayeredTimeline( )
    {
        this.plot = new CollapsibleTimePlot2D( );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbar = newToolbar( true );

        this.view = new View( "timelineView", this.canvas, "Timeline", false, null, requireIcon( "LayeredTimeline/open-icons/time.png" ), this.toolbar );
    }

}
