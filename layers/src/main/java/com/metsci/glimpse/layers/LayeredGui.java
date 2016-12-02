package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;

import java.net.URL;

import javax.media.opengl.GLAnimatorControl;

import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LayeredGui
{

    protected final DockingGroup dockingGroup;
    protected final GLAnimatorControl animator;
    protected DockingGroupListener dockingArrSaver;

    protected LayeredScenario scenario;

    protected LayeredGeo geo;
    protected LayeredTimeline timeline;


    public LayeredGui( String frameTitleRoot )
    {
        this( frameTitleRoot, defaultDockingTheme( ) );
    }

    public LayeredGui( String frameTitleRoot, DockingTheme theme )
    {
        this.dockingGroup = new DockingGroup( DISPOSE_ALL_FRAMES, theme );
        this.dockingGroup.addListener( createDefaultFrameTitler( frameTitleRoot ) );

        this.animator = new SwingEDTAnimator( 30 );
        this.dockingGroup.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void disposingAllFrames( DockingGroup group )
            {
                animator.stop( );
            }
        } );
        this.animator.start( );

        this.dockingArrSaver = null;

        this.scenario = ( new LayeredScenario.Builder( ) ).build( );

        this.geo = null;
        this.timeline = null;

        // WIP: Add layer-tree view
    }

    public void arrange( String appName, String defaultArrResource )
    {
        URL defaultArrUrl = getResource( defaultArrResource );
        this.arrange( appName, defaultArrUrl );
    }

    public void arrange( String appName, URL defaultArrUrl )
    {
        if ( this.dockingArrSaver != null )
        {
            this.dockingGroup.removeListener( this.dockingArrSaver );
            this.dockingArrSaver = null;
        }

        GroupArrangement groupArr = loadDockingArrangement( appName, defaultArrUrl );
        this.dockingGroup.setArrangement( groupArr );

        this.dockingArrSaver = new DockingGroupAdapter( )
        {
            @Override
            public void disposingAllFrames( DockingGroup group )
            {
                saveDockingArrangement( appName, dockingGroup.captureArrangement( ) );
            }
        };
        this.dockingGroup.addListener( this.dockingArrSaver );
    }

    public void init( LayeredScenario newScenario )
    {
        // WIP: Remove layers

        this.scenario = newScenario;

        if ( this.geo != null )
        {
            this.geo.init( this.scenario );
        }

        if ( this.timeline != null )
        {
            this.timeline.init( this.scenario );
        }

        // WIP: Add layers back in
    }

    public void addLayer( Layer layer )
    {
        layer.init( this.scenario );

        if ( layer instanceof GeoLayer )
        {
            GeoLayer geoLayer = ( GeoLayer ) layer;
            geoLayer.installToGeo( this.getGeo( ) );
        }

        if ( layer instanceof TimelineLayer )
        {
            TimelineLayer timelineLayer = ( TimelineLayer ) layer;
            timelineLayer.installToTimeline( this.getTimeline( ) );
        }

        // WIP: Add to layer-tree model
    }

    public void removeLayer( Layer layer )
    {
        // WIP: Need active GL context
    }

    protected LayeredGeo getGeo( )
    {
        if ( this.geo == null )
        {
            this.geo = new LayeredGeo( );
            this.geo.init( this.scenario );
            this.animator.add( this.geo.canvas.getGLDrawable( ) );
            this.dockingGroup.addView( this.geo.view );
        }
        return this.geo;
    }

    protected LayeredTimeline getTimeline( )
    {
        if ( this.timeline == null )
        {
            this.timeline = new LayeredTimeline( );
            this.timeline.init( this.scenario );
            this.animator.add( this.timeline.canvas.getGLDrawable( ) );
            this.dockingGroup.addView( this.timeline.view );
        }
        return this.timeline;
    }

}
