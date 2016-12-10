package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.listMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.listPlus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapWith;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.setMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.setPlus;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.var.VarUtils.addElementAddedListener;
import static com.metsci.glimpse.util.var.VarUtils.addElementRemovedListener;
import static java.lang.String.format;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Component;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;
import com.metsci.glimpse.util.var.Var;

public class LayeredGui
{

    // Model
    public final Var<ImmutableMap<String,Supplier<? extends LayeredViewConfig>>> viewConfigurators;
    public final Var<ImmutableSet<LayeredView>> views;
    public final Var<ImmutableList<Layer>> layers;

    // View
    protected final DockingGroup dockingGroup;
    protected final GLAnimatorControl animator;
    protected DockingGroupListener dockingArrSaver;
    protected final Map<String,Integer> dockingViewIdCounters;
    protected final BiMap<LayeredView,View> dockingViews;
    protected final LayerCardsPanel layerCardsPanel;


    public LayeredGui( String frameTitleRoot )
    {
        this( frameTitleRoot, defaultDockingTheme( ) );
    }

    public LayeredGui( String frameTitleRoot, DockingTheme theme )
    {
        // Model
        //

        this.viewConfigurators = new Var<>( ImmutableMap.of( ), notNull );
        this.views = new Var<>( ImmutableSet.of( ), notNull );
        this.layers = new Var<>( ImmutableList.of( ), notNull );


        // View
        //

        this.dockingGroup = new DockingGroup( DISPOSE_ALL_FRAMES, theme );
        this.dockingGroup.addListener( createDefaultFrameTitler( frameTitleRoot ) );

        this.animator = new SwingEDTAnimator( 30 );
        this.dockingGroup.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void disposingAllFrames( DockingGroup dockingGroup )
            {
                animator.stop( );
            }
        } );

        this.dockingArrSaver = null;

        this.dockingViewIdCounters = new HashMap<>( );

        this.dockingViews = HashBiMap.create( );
        this.dockingGroup.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void closingView( DockingGroup dockingGroup, View dockingView )
            {
                // If dockingViews still has this entry, then the layeredView hasn't been removed yet
                LayeredView view = dockingViews.inverse( ).remove( dockingView );
                if ( view != null )
                {
                    views.update( ( v ) -> setMinus( v, view ) );
                }
            }
        } );

        this.layerCardsPanel = new LayerCardsPanel( this.layers );
        JScrollPane layerCardsScroller = new JScrollPane( this.layerCardsPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED );
        View layersView = new View( "layersView", layerCardsScroller, "Layers", false, null, null, null );
        this.dockingGroup.addView( layersView );


        // Controller
        //

        addElementRemovedListener( this.views, true, this::handleViewRemoved );
        addElementAddedListener( this.views, true, this::handleViewAdded );

        addElementRemovedListener( this.layers, true, this::handleLayerRemoved );
        addElementAddedListener( this.layers, true, this::handleLayerAdded );
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
            public void disposingAllFrames( DockingGroup dockingGroup )
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
        // more natural typing, such as LayeredGeoConfig.setDefaultGeoConfigurator().
        //

        this.viewConfigurators.update( ( v ) -> mapWith( v, configKey, configurator ) );
    }

    public void addView( LayeredView view )
    {
        this.views.update( ( v ) -> setPlus( v, view ) );
    }

    public void removeView( LayeredView view )
    {
        this.views.update( ( v ) -> setMinus( v, view ) );
    }

    public void addLayer( Layer layer )
    {
        this.layers.update( ( v ) -> listPlus( v, layer ) );
    }

    public void removeLayer( Layer layer )
    {
        this.layers.update( ( v ) -> listMinus( v, layer ) );
    }

    protected void handleViewAdded( LayeredView view )
    {
        Map<String,LayeredViewConfig> configs = new LinkedHashMap<>( );
        for ( Entry<String,Supplier<? extends LayeredViewConfig>> en : this.viewConfigurators.v( ).entrySet( ) )
        {
            String configKey = en.getKey( );
            Supplier<? extends LayeredViewConfig> configurator = en.getValue( );
            configs.put( configKey, configurator.get( ) );
        }
        view.setConfigs( configs );

        // WIP: Link configs where possible

        for ( Layer layer : this.layers.v( ) )
        {
            view.addLayer( layer );
        }

        GLAutoDrawable glDrawable = view.getGLDrawable( );
        if ( glDrawable != null )
        {
            this.animator.add( glDrawable );
            this.animator.start( );
        }

        JToolBar toolbar = newToolbar( true );
        for ( Component c : view.getToolbarComponents( ) )
        {
            toolbar.add( c );
        }

        // WIP: Add support in docking for wildcard viewIds
        String dockingViewIdRoot = view.getClass( ).getName( );
        int dockingViewIdNumber = this.dockingViewIdCounters.getOrDefault( dockingViewIdRoot, 0 );
        this.dockingViewIdCounters.put( dockingViewIdRoot, dockingViewIdNumber + 1 );
        String dockingViewId = dockingViewIdRoot + ":" + dockingViewIdNumber;

        // XXX: Do this without depending on docking info
        if ( dockingViewIdNumber != 0 )
        {
            view.title.update( ( v ) -> format( "%s (%d)", v, dockingViewIdNumber + 1 ) );
        }

        // XXX: Add support in docking for changing view titles
        View dockingView = new View( dockingViewId, view.getComponent( ), view.title.v( ), true, view.getTooltip( ), view.getIcon( ), toolbar );
        this.dockingGroup.addView( dockingView );

        this.dockingViews.put( view, dockingView );
    }

    protected void handleViewRemoved( LayeredView view )
    {
        GLAutoDrawable glDrawable = view.getGLDrawable( );
        if ( glDrawable != null )
        {
            this.animator.remove( glDrawable );
        }

        view.dispose( );

        // If dockingViews still has this entry, then the dockingView hasn't been closed yet
        View dockingView = this.dockingViews.remove( view );
        if ( dockingView != null )
        {
            this.dockingGroup.closeView( dockingView );
        }
    }

    protected void handleLayerAdded( Layer layer )
    {
        for ( LayeredView view : this.views.v( ) )
        {
            view.addLayer( layer );
        }
    }

    protected void handleLayerRemoved( Layer layer )
    {
        for ( LayeredView view : this.views.v( ) )
        {
            view.removeLayer( layer );
        }
    }

}
