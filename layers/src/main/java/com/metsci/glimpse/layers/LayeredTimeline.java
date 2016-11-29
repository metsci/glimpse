package com.metsci.glimpse.layers;

import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;

import javax.swing.JToolBar;

import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.var.Var;

public class LayeredTimeline
{

    public final NewtSwingEDTGlimpseCanvas canvas;
    public final JToolBar toolbar;
    public final View view;

    /**
     * NOTE: Do not call {@code LayeredTimeline.plot.setEpoch()} directly -- it
     * will not notify listeners. Use {@code LayeredTimeline.epoch.set()} instead.
     */
    public final CollapsibleTimePlot2D plot;

    public final Var<LayeredScenario> scenario;


    public LayeredTimeline( Var<LayeredScenario> scenario )
    {
        this.plot = new CollapsibleTimePlot2D( );

        this.canvas = new NewtSwingEDTGlimpseCanvas( );
        this.canvas.addLayout( this.plot );

        this.toolbar = newToolbar( true );

        this.view = new View( "timelineView", this.canvas, "Timeline", false, null, requireIcon( "LayeredTimeline/open-icons/time.png" ), this.toolbar );

        this.scenario = scenario;
        this.scenario.addListener( true, ( ) ->
        {
            plot.setEpoch( this.scenario.v( ).timelineEpoch );
        } );
    }

}
