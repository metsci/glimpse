package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static java.util.Collections.unmodifiableCollection;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GLAnimatorControl;
import javax.swing.JScrollPane;
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

    protected final List<Layer> layers;
    protected final Set<Layer> installedLayers;

    protected final LayersPanel layersPanel;


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

        this.layers = new ArrayList<>( );
        this.installedLayers = new HashSet<>( );

        // XXX: Getting too verbose for an inline anonymous class
        Collection<Layer> layersUnmod = unmodifiableCollection( this.layers );
        this.layersPanel = new LayersPanel( new LayersPanel.Model( )
        {
            @Override
            public Collection<Layer> getLayers( )
            {
                return layersUnmod;
            }

            @Override
            public boolean isLayerVisible( Layer layer )
            {
                return installedLayers.contains( layer );
            }

            @Override
            public void setLayerVisible( Layer layer, boolean visible )
            {
                if ( visible )
                {
                    // XXX: May cause flickering

                    List<Layer> layersToReinstall = new ArrayList<>( layers.subList( layers.indexOf( layer ) + 1, layers.size( ) ) );
                    layersToReinstall.retainAll( installedLayers );

                    for ( Layer layerToReinstall : layersToReinstall )
                    {
                        uninstallLayer( layerToReinstall );
                    }

                    installLayer( layer );

                    for ( Layer layerToReinstall : layersToReinstall )
                    {
                        installLayer( layerToReinstall );
                    }
                }
                else
                {
                    uninstallLayer( layer );
                }
            }
        } );
        JScrollPane layersScroller = new JScrollPane( this.layersPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED );
        View layersView = new View( "layersView", layersScroller, "Layers", false, null, null, null );
        this.dockingGroup.addView( layersView );
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
        // Remember which layers to re-install
        Set<Layer> installedLayers = new HashSet<>( this.installedLayers );

        // Uninstall all layers
        for ( Layer layer : installedLayers )
        {
            this.uninstallLayer( layer );
        }

        // Change scenario (while no layers are installed)
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

        // Re-install appropriate layers
        for ( Layer layer : this.layers )
        {
            if ( installedLayers.contains( layer ) )
            {
                this.installLayer( layer );
            }
        }
    }

    public void addLayer( Layer layer )
    {
        if ( this.layers.add( layer ) )
        {
            this.installLayer( layer );
            this.layersPanel.refresh( );
        }
    }

    public void removeLayer( Layer layer )
    {
        if ( this.layers.remove( layer ) )
        {
            this.uninstallLayer( layer );
            this.layersPanel.refresh( );
        }
    }

    protected void installLayer( Layer layer )
    {
        if ( this.installedLayers.add( layer ) )
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

    protected void uninstallLayer( Layer layer )
    {
        if ( this.installedLayers.remove( layer ) )
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
