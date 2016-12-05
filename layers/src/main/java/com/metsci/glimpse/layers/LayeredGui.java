package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;

import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GLAnimatorControl;
import javax.swing.JToolBar;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.View;
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

    protected final Set<Layer> layers;


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

        this.dockingArrSaver = null;

        this.scenario = ( new LayeredScenario.Builder( ) ).build( );

        this.geo = null;
        this.timeline = null;

        this.layers = new LinkedHashSet<>( );

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
        // Remember the layers
        List<Layer> layers = new ArrayList<>( this.layers );

        // Remove all layers
        for ( Layer layer : layers )
        {
            this.removeLayer( layer );
        }

        // Change scenario
        this.scenario = newScenario;

        // Initialize views
        if ( this.geo != null )
        {
            this.geo.init( this.scenario );
        }

        if ( this.timeline != null )
        {
            this.timeline.init( this.scenario );
        }

        // Add layers back in
        for ( Layer layer : layers )
        {
            this.addLayer( layer );
        }
    }

    public void addLayer( Layer layer )
    {
        if ( this.layers.add( layer ) )
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
        }
    }

    public void removeLayer( Layer layer )
    {
        if ( this.layers.remove( layer ) )
        {
            if ( layer instanceof GeoLayer )
            {
                GeoLayer geoLayer = ( GeoLayer ) layer;
                this.geo.canvas.getGLDrawable( ).invoke( true, ( glDrawable ) ->
                {
                    GlimpseContext context = this.geo.canvas.getGlimpseContext( );
                    geoLayer.uninstallFromGeo( this.geo, context );
                    return false;
                } );
            }

            if ( layer instanceof TimelineLayer )
            {
                TimelineLayer timelineLayer = ( TimelineLayer ) layer;
                this.timeline.canvas.getGLDrawable( ).invoke( true, ( glDrawable ) ->
                {
                    GlimpseContext context = this.timeline.canvas.getGlimpseContext( );
                    timelineLayer.uninstallFromTimeline( this.timeline, context );
                    return false;
                } );
            }
        }
    }

    protected LayeredGeo getGeo( )
    {
        if ( this.geo == null )
        {
            this.geo = new LayeredGeo( );
            this.geo.init( this.scenario );

            this.animator.add( this.geo.canvas.getGLDrawable( ) );
            this.animator.start( );

            JToolBar geoToolbar = newToolbar( true );
            for ( Component c : this.geo.toolbarComponents )
            {
                geoToolbar.add( c );
            }

            View geoView = new View( "geoView", this.geo.canvas, "Geo", false, null, requireIcon( "LayeredGeo/fugue-icons/map.png" ), geoToolbar );
            this.dockingGroup.addView( geoView );
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
            this.animator.start( );

            JToolBar timelineToolbar = newToolbar( true );
            for ( Component c : this.timeline.toolbarComponents )
            {
                timelineToolbar.add( c );
            }

            View timelineView = new View( "timelineView", this.timeline.canvas, "Timeline", false, null, requireIcon( "LayeredTimeline/open-icons/time.png" ), timelineToolbar );
            this.dockingGroup.addView( timelineView );
        }
        return this.timeline;
    }

}
