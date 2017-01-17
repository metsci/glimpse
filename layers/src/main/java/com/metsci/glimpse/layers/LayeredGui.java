package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.newButtonPopup;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static com.metsci.glimpse.layers.misc.UiUtils.addToAnimator;
import static com.metsci.glimpse.layers.misc.UiUtils.bindToggleButton;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.listMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.listPlus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.mapWith;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.setMinus;
import static com.metsci.glimpse.util.ImmutableCollectionUtils.setPlus;
import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.var.VarUtils.addElementAddedListener;
import static com.metsci.glimpse.util.var.VarUtils.addElementRemovedListener;
import static com.metsci.glimpse.util.var.VarUtils.addEntryRemovedListener;
import static java.lang.String.format;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

/**
 * A {@link LayeredGui} represents a number of {@link View}s and a number of {@link Layer}s.
 * Each layer is given an opportunity to add a representation of itself to each view. Layers
 * and views can be added and removed dynamically.
 * <p>
 * A layer typically has a different representation in each view -- for example, a TracksLayer
 * can show a spatial representation in a geo view, and a temporal representation in a timeline
 * view. The representation of a particular layer on a particular view is called a {@link Facet}.
 * <p>
 * There can be more than one view of a given type -- for example, two different geo views.
 * A layer may display itself differently on each of these views. For example, if there are
 * two geo views with different projections, the TracksLayer might have separate facets for
 * the two views: one facet showing the track points projected one way, the other facet showing
 * the same track points projected a different way.
 * <p>
 * A view can be configured by modifying its {@link Trait}s, which inform the view's facets
 * about what they should be displaying. For example, a geo trait might contain a projection,
 * and a time trait might contain a selected time. The facets for each view can base their
 * rendering on these traits -- so facets on geo-view #1 can use projection #1 and selected
 * time #1, while facets on geo-view #2 use projection #2 and selected time #2.
 * <p>
 * It is important to note that every view has every trait. A geo view has <em>both</em> a geo
 * trait <em>and</em> a time trait. A timeline view also has both a geo trait and a time trait.
 * <p>
 * It is possible to define a custom trait, and add it to the LayeredGui. Every view will then
 * have its own instance of the custom trait, so that a facet on any view can consult that trait.
 * For example, if a scatter plot has been added, there may be a scatter trait that contains
 * the bounds of the selection window in the XY space of the scatter plot. A layer can then
 * have a facet on a geo view which highlights the geo locations of the points that are selected
 * in the scatter plot.
 * <p>
 * Trait instances can be linked to each other. For example, two timeline views can have their
 * time traits linked together, so that adjusting the selection window in one timeline affects
 * the selection window in the other timeline as well. For another example, a geo view can have
 * its time trait linked to the time trait of a timeline view, so that a facet on the geo can
 * highlight points inside the time window that is selected on the timeline.
 * <p>
 * To be linked, two trait instances must be compatible with each other. Compatibility is checked
 * using the {@link Var#validateFn} of the {@link Trait#parent} var.
 * <p>
 * For flexibility, traits that are owned by views are not linked directly to each other, but
 * are instead linked to a common "linkage." A linkage is simply a trait object that is used
 * as a shared parent for some number of child traits.
 * <p>
 * When a new view is added, if it is missing a trait for which there is an existing linkage,
 * the new view's trait is populated by making a copy of the linkage. This can be used to set
 * default values for a trait: first add a linkage with the desired settings, and then any views
 * subsequently added, which don't already have that trait defined, will inherit the default
 * values from the linkage.
 * <p>
 * When a new view is added, an attempt is made to link each of its traits to an existing linkage.
 * If no compatible linkage is found, the trait is left unlinked -- however, it may end up linked
 * automatically later on, if another view with a compatible trait is added.
 */
public class LayeredGui
{
    public static final Icon layersIcon = requireIcon( "fugue-icons/category.png" );
    public static final String layerCardsViewId = "com.metsci.glimpse.layers.geo.LayeredGui.layerCardsView";


    // Model
    public final Var<ImmutableMap<String,ImmutableList<Trait>>> linkages;
    public final Var<ImmutableMap<String,ImmutableMap<Trait,String>>> linkageNames;
    public final Var<ImmutableSet<View>> views;
    public final Var<ImmutableList<Layer>> layers;

    // View
    protected final Map<View,Disposable> viewDisposables;
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
        this.linkageNames = new Var<>( ImmutableMap.of( ), notNull );
        this.views = new Var<>( ImmutableSet.of( ), notNull );
        this.layers = new Var<>( ImmutableList.of( ), notNull );


        // View
        //

        this.viewDisposables = new HashMap<>( );

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
            public void disposingAllFrames( DockingGroup dockingGroup )
            {
                views.set( ImmutableSet.of( ) );
            }

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
        com.metsci.glimpse.docking.View layersView = new com.metsci.glimpse.docking.View( layerCardsViewId, layerCardsScroller, "Layers", false, null, layersIcon, null );
        this.dockingGroup.addView( layersView );


        // Controller
        //

        addElementRemovedListener( this.views, this::handleViewRemoved );
        addElementAddedListener( this.views, true, this::handleViewAdded );

        addElementRemovedListener( this.layers, this::handleLayerRemoved );
        addElementAddedListener( this.layers, true, this::handleLayerAdded );

        addEntryRemovedListener( this.linkageNames, ( k, v ) -> this.pruneLinkages( ) );
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

    public Trait addLinkage( String traitKey, String name, Trait template )
    {
        Trait linkage = template.copy( true );

        if ( name != null )
        {
            this.putLinkageName( traitKey, name, linkage );
        }

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

    protected void putLinkageName( String traitKey, String name, Trait linkage )
    {
        ImmutableMap<Trait,String> oldNames = this.linkageNames.v( ).get( traitKey );
        ImmutableMap<Trait,String> newNames;
        if ( oldNames == null )
        {
            newNames = ImmutableMap.of( linkage, name );
        }
        else
        {
            newNames = mapWith( oldNames, linkage, name );
        }

        this.linkageNames.update( ( v ) -> mapWith( v, traitKey, newNames ) );
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
                // Unnamed linkage -- will get removed if all its children get removed
                Trait linkage = this.addLinkage( traitKey, null, oldTrait );
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
        // Keep track of disposables that need to run when this view gets removed
        DisposableGroup disposables = new DisposableGroup( );

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
        disposables.add( view.traits.addListener( true, ( ) ->
        {
            for ( Entry<String,Trait> en : view.traits.v( ).entrySet( ) )
            {
                String traitKey = en.getKey( );
                Trait trait = en.getValue( );

                // If the trait doesn't have a parent, look for a compatible linkage
                if ( trait.parent.v( ) == null )
                {
                    for ( Trait linkage : this.linkages.v( ).get( traitKey ) )
                    {
                        if ( trait.parent.isValid( linkage ) )
                        {
                            trait.parent.set( linkage );
                            break;
                        }
                    }
                }

                // If the trait still doesn't have a parent, create an unnamed linkage
                if ( trait.parent.v( ) == null )
                {
                    // Unnamed linkage -- will get removed if all its children get removed
                    Trait linkage = this.addLinkage( traitKey, null, trait );
                    trait.parent.set( linkage );
                }
            }
        } ) );

        for ( Layer layer : this.layers.v( ) )
        {
            view.addLayer( layer );
        }

        JToggleButton facetsButton = new JToggleButton( layersIcon );
        facetsButton.setToolTipText( "Show Layers" );
        JPopupMenu facetsPopup = newButtonPopup( facetsButton );

        DisposableGroup facetDisposables = disposables.add( new DisposableGroup( ) );
        disposables.add( view.facets.addListener( true, ( ) ->
        {
            facetDisposables.dispose( );
            facetDisposables.clear( );
            facetsPopup.removeAll( );

            for ( Entry<Layer,Facet> en : view.facets.v( ).entrySet( ) )
            {
                Layer layer = en.getKey( );
                Facet facet = en.getValue( );

                // XXX: Handle title changes
                JMenuItem facetToggle = new JCheckBoxMenuItem( layer.title.v( ) );
                facetDisposables.add( bindToggleButton( facetToggle, facet.isVisible ) );
                facetsPopup.add( facetToggle );
            }
        } ) );

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

        disposables.add( view::dispose );

        GLAutoDrawable glDrawable = view.getGLDrawable( );
        if ( glDrawable != null )
        {
            this.animator.start( );
            disposables.add( addToAnimator( glDrawable, this.animator ) );
        }

        this.viewDisposables.put( view, disposables );
    }

    protected void handleViewRemoved( View view )
    {
        this.viewDisposables.remove( view ).dispose( );

        // If dockingViews still has this entry, then the dockingView hasn't been closed yet
        com.metsci.glimpse.docking.View dockingView = this.dockingViews.remove( view );
        if ( dockingView != null )
        {
            this.dockingGroup.closeView( dockingView );
        }

        this.pruneLinkages( );
    }

    protected void pruneLinkages( )
    {
        ImmutableMap<String,ImmutableList<Trait>> prunedLinkages = pruneLinkages( this.linkages.v( ), this.linkageNames.v( ), this.views.v( ) );
        this.linkages.set( prunedLinkages );
    }

    protected static ImmutableMap<String,ImmutableList<Trait>> pruneLinkages( Map<String,? extends Collection<Trait>> linkages, Map<String,? extends Map<Trait,String>> linkageNames, Collection<View> views )
    {
        Map<String,ImmutableList<Trait>> result = new LinkedHashMap<>( );
        for ( String traitKey : linkages.keySet( ) )
        {
            List<Trait> linkagesToKeep = pruneLinkages( traitKey, linkages.get( traitKey ), linkageNames.get( traitKey ), views );
            result.put( traitKey, ImmutableList.copyOf( linkagesToKeep ) );
        }
        return ImmutableMap.copyOf( result );
    }

    protected static List<Trait> pruneLinkages( String traitKey, Collection<Trait> linkages, Map<Trait,String> linkageNames, Collection<View> views )
    {
        Set<Trait> linkagesInUse = findLinkagesInUse( views, traitKey );

        List<Trait> linkagesToKeep = new ArrayList<>( );
        for ( Trait linkage : linkages )
        {
            // Never auto-remove a named linkage
            boolean isNamed = ( linkageNames != null && linkageNames.containsKey( linkage ) );
            if ( isNamed || linkagesInUse.contains( linkage ) )
            {
                linkagesToKeep.add( linkage );
            }
        }
        return linkagesToKeep;
    }

    protected static Set<Trait> findLinkagesInUse( Collection<View> views, String traitKey )
    {
        Set<Trait> linkages = new LinkedHashSet<>( );
        for ( View view : views )
        {
            Trait trait = view.traits.v( ).get( traitKey );
            if ( trait != null )
            {
                Trait linkage = trait.parent.v( );
                if ( linkage != null )
                {
                    linkages.add( linkage );
                }
            }
        }
        return linkages;
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
            view.removeLayer( layer, false );
        }
    }

}
