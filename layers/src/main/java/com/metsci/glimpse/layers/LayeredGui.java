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
import static java.util.Collections.unmodifiableList;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

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

    protected final Set<LayeredView> views;
    protected final Map<String,Supplier<? extends LayeredViewConfig>> viewConfigurators;

    protected final List<Layer> layers;
    protected final List<Layer> layersUnmod;

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

        this.views = new LinkedHashSet<>( );
        this.viewConfigurators = new LinkedHashMap<>( );

        this.layers = new ArrayList<>( );
        this.layersUnmod = unmodifiableList( this.layers );

        this.layersPanel = new LayersPanel( );
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

    public <T extends LayeredViewConfig> void setDefaultViewConfigurator( String configKey, Class<T> configClass, Supplier<? extends T> configurator )
    {
        // The configClass arg isn't currently used, but it might be used in the future
        // to check supplied config instances at runtime.
        //
        // More importantly, it forces the caller to think about config class, which is
        // important. And as a side benefit, it makes it more cumbersome to call this
        // method directly, which encourages callers to use convenience functions with
        // more natural typing, such as LayeredGeoConfig.setDefaultGeoConfigFn().
        //
        this.viewConfigurators.put( configKey, configurator );
    }

    public void addView( LayeredView view )
    {
        if ( this.views.add( view ) )
        {
            for ( Entry<String,Supplier<? extends LayeredViewConfig>> en : this.viewConfigurators.entrySet( ) )
            {
                String configKey = en.getKey( );
                Supplier<? extends LayeredViewConfig> configurator = en.getValue( );
                LayeredViewConfig config = configurator.get( );
                view.setConfig( configKey, config );
            }

            view.init( );

            this.animator.add( view.canvas.getGLDrawable( ) );
            this.animator.start( );

            JToolBar geoToolbar = newToolbar( true );
            for ( Component c : view.toolbarComponents )
            {
                geoToolbar.add( c );
            }

            View dockingView = new View( "geoView", this.view.canvas, "Geo", false, null, requireIcon( "LayeredGeo/fugue-icons/map.png" ), geoToolbar );
            View timelineView = new View( "timelineView", this.timeline.canvas, "Timeline", false, null, requireIcon( "LayeredTimeline/open-icons/time.png" ), timelineToolbar );
            this.dockingGroup.addView( dockingView );

            for ( Layer layer : this.layers )
            {
                layer.installTo( view );
            }

            this.layersPanel.refresh( this.layersUnmod );
        }
    }

    public void addLayer( Layer layer )
    {
        if ( !this.layers.contains( layer ) )
        {
            this.layers.add( layer );

            for ( LayeredView view : this.views )
            {
                layer.installTo( view );
            }

            this.layersPanel.refresh( this.layersUnmod );
        }
    }

    public void removeLayer( Layer layer )
    {
        if ( this.layers.contains( layer ) )
        {
            this.layers.remove( layer );

            for ( LayeredView view : this.views )
            {
                layer.uninstallFrom( view, false );
            }

            this.layersPanel.refresh( this.layersUnmod );
        }
    }

}
