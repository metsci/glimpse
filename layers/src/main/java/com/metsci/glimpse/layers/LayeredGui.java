package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.newButtonPopup;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static com.metsci.glimpse.layers.misc.UiUtils.bindToggleButton;
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

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingGroupListener;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.layers.misc.LayerCardsPanel;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.Var;

public class LayeredGui
{
    public static final Icon layersIcon = requireIcon( "fugue-icons/category.png" );


    // Model
    public final Var<ImmutableMap<String,ImmutableList<Trait>>> linkages;
    public final Var<ImmutableSet<View>> views;
    public final Var<ImmutableList<Layer>> layers;

    // View
    protected final DockingGroup dockingGroup;
    protected final GLAnimatorControl animator;
    protected DockingGroupListener dockingArrSaver;
    protected final Map<String,Integer> dockingViewIdCounters;
    protected final BiMap<View,com.metsci.glimpse.docking.View> dockingViews;
    protected final LayerCardsPanel layerCardsPanel;


    public LayeredGui( String frameTitleRoot )
    {
        this( frameTitleRoot, defaultDockingTheme( ) );
    }

    public LayeredGui( String frameTitleRoot, DockingTheme theme )
    {
        // Model
        //

        this.linkages = new Var<>( ImmutableMap.of( ), notNull );
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
            public void closingView( DockingGroup dockingGroup, com.metsci.glimpse.docking.View dockingView )
            {
                // If dockingViews still has this entry, then the layeredView hasn't been removed yet
                View view = dockingViews.inverse( ).remove( dockingView );
                if ( view != null )
                {
                    views.update( ( v ) -> setMinus( v, view ) );
                }
            }
        } );

        this.layerCardsPanel = new LayerCardsPanel( this.layers );
        JScrollPane layerCardsScroller = new JScrollPane( this.layerCardsPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED );
        com.metsci.glimpse.docking.View layersView = new com.metsci.glimpse.docking.View( "layersView", layerCardsScroller, "Layers", false, null, layersIcon, null );
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

    public Trait addLinkage( String traitKey, Trait template )
    {
        Trait linkage = template.copy( true );

        ImmutableList<Trait> oldLinkages = this.linkages.v( ).get( traitKey );
        ImmutableList<Trait> newLinkages;
        if ( oldLinkages == null )
        {
            newLinkages = ImmutableList.of( linkage );
        }
        else
        {
            // WIP: What should insertion index be?
            newLinkages = listPlus( oldLinkages, linkage );
        }

        this.linkages.update( ( v ) -> mapWith( v, traitKey, newLinkages ) );

        return linkage;
    }

    public void addView( View view )
    {
        this.views.update( ( v ) -> setPlus( v, view ) );
    }

    public void cloneView( View view )
    {
        Map<String,Trait> newTraits = new LinkedHashMap<>( );
        for ( Entry<String,Trait> en : view.traits.v( ).entrySet( ) )
        {
            String traitKey = en.getKey( );

            Trait oldTrait = en.getValue( );
            if ( oldTrait.parent.v( ) == null )
            {
                // XXX: Mark linkage as implicit
                Trait linkage = this.addLinkage( traitKey, oldTrait );
                oldTrait.parent.set( linkage );
            }

            Trait newTrait = oldTrait.copy( false );
            newTrait.parent.set( oldTrait.parent.v( ) );

            newTraits.put( traitKey, newTrait );
        }

        View newView = view.copy( );
        newView.setTraits( newTraits );

        this.addView( newView );
    }

    public void removeView( View view )
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

    protected void handleViewAdded( View view )
    {
        // Fill in traits the view doesn't already have
        Map<String,Trait> newTraits = new LinkedHashMap<>( view.traits.v( ) );
        for ( String traitKey : this.linkages.v( ).keySet( ) )
        {
            newTraits.computeIfAbsent( traitKey, ( k ) ->
            {
                // The default linkage is the one at index 0
                return this.linkages.v( ).get( traitKey ).get( 0 ).copy( false );
            } );
        }
        view.setTraits( newTraits );

        // Link traits that aren't already linked
        for ( Entry<String,Trait> en : view.traits.v( ).entrySet( ) )
        {
            String traitKey = en.getKey( );
            Trait trait = en.getValue( );

            // If the trait doesn't have a parent, look for a compatible linkage
            if ( trait.parent.v( ) == null )
            {
                for ( Trait linkage : this.linkages.v( ).get( traitKey ) )
                {
                    if ( trait.parent.validateFn.test( linkage ) )
                    {
                        trait.parent.set( linkage );
                        break;
                    }
                }
            }

            // If the trait still doesn't have a parent, create an implicit linkage
            if ( trait.parent.v( ) == null )
            {
                // XXX: Mark the linkage as implicit
                Trait linkage = this.addLinkage( traitKey, trait );
                trait.parent.set( linkage );
            }
        }

        GLAutoDrawable glDrawable = view.getGLDrawable( );
        if ( glDrawable != null )
        {
            this.animator.add( glDrawable );
            this.animator.start( );
        }

        for ( Layer layer : this.layers.v( ) )
        {
            view.addLayer( layer );
        }

        JToggleButton facetsButton = new JToggleButton( layersIcon );
        facetsButton.setToolTipText( "Show Layers" );
        JPopupMenu facetsPopup = newButtonPopup( facetsButton );

        DisposableGroup facetBindings = new DisposableGroup( );
        Disposable facetsListener = view.facets.addListener( true, ( ) ->
        {
            facetBindings.dispose( );
            facetBindings.clear( );
            facetsPopup.removeAll( );

            for ( Entry<Layer,Facet> en : view.facets.v( ).entrySet( ) )
            {
                Layer layer = en.getKey( );
                Facet facet = en.getValue( );

                // XXX: Handle title changes
                JMenuItem facetToggle = new JCheckBoxMenuItem( layer.title.v( ) );
                facetBindings.add( bindToggleButton( facetToggle, facet.isVisible ) );
                facetsPopup.add( facetToggle );
            }
        } );

        JButton cloneButton = new JButton( "Clone" );
        cloneButton.setToolTipText( "Clone This View" );
        cloneButton.addActionListener( ( ev ) ->
        {
            this.cloneView( view );
        } );

        view.toolbar.add( cloneButton );
        view.toolbar.add( facetsButton );

        // XXX: Add support in docking for wildcard viewIds
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
        com.metsci.glimpse.docking.View dockingView = new com.metsci.glimpse.docking.View( dockingViewId, view.getComponent( ), view.title.v( ), true, view.getTooltip( ), view.getIcon( ), view.toolbar );
        this.dockingGroup.addView( dockingView );

        // When the user closes a dockingView, we will need to know the corresponding view
        this.dockingViews.put( view, dockingView );

        // Clean up after ourselves when the view closes
        this.dockingGroup.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void closingView( DockingGroup group, com.metsci.glimpse.docking.View closingDockingView )
            {
                if ( closingDockingView == dockingView )
                {
                    facetsListener.dispose( );
                    facetBindings.dispose( );
                }
            }
        } );
    }

    protected void handleViewRemoved( View view )
    {
        GLAutoDrawable glDrawable = view.getGLDrawable( );
        if ( glDrawable != null )
        {
            this.animator.remove( glDrawable );
        }

        view.dispose( );

        // WIP: Remove implicit linkages that don't have any children

        // If dockingViews still has this entry, then the dockingView hasn't been closed yet
        com.metsci.glimpse.docking.View dockingView = this.dockingViews.remove( view );
        if ( dockingView != null )
        {
            this.dockingGroup.closeView( dockingView );
        }
    }

    protected void handleLayerAdded( Layer layer )
    {
        for ( View view : this.views.v( ) )
        {
            view.addLayer( layer );
        }
    }

    protected void handleLayerRemoved( Layer layer )
    {
        for ( View view : this.views.v( ) )
        {
            view.removeLayer( layer );
        }
    }

}
