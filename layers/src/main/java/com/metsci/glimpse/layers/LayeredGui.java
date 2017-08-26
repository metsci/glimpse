/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.layers;

import static com.google.common.io.Resources.getResource;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingGroupUtils.findArrTileContaining;
import static com.metsci.glimpse.docking.DockingThemes.defaultDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.newButtonPopup;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static com.metsci.glimpse.docking.Side.RIGHT;
import static com.metsci.glimpse.layers.FpsOption.findFps;
import static com.metsci.glimpse.layers.StandardGuiOption.HIDE_LAYERS_PANEL;
import static com.metsci.glimpse.layers.StandardViewOption.HIDE_CLONE_BUTTON;
import static com.metsci.glimpse.layers.StandardViewOption.HIDE_CLOSE_BUTTON;
import static com.metsci.glimpse.layers.StandardViewOption.HIDE_FACETS_MENU;
import static com.metsci.glimpse.layers.misc.UiUtils.bindButtonText;
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
import java.util.regex.Pattern;

import com.jogamp.opengl.GLAnimatorControl;
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
import com.metsci.glimpse.docking.DockingGroupUtils.BesideExistingNeighbor;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacement;
import com.metsci.glimpse.docking.DockingGroupUtils.ViewPlacementRule;
import com.metsci.glimpse.docking.DockingTheme;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
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
    public static final Icon cloneIcon = requireIcon( "fugue-icons/cards.png" );
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
    protected String dockingAppName;
    protected final Map<String,Integer> dockingViewIdCounters;
    protected final BiMap<View,com.metsci.glimpse.docking.View> dockingViews;


    public LayeredGui( String frameTitleRoot, GuiOption... guiOptions )
    {
        this( frameTitleRoot, defaultDockingTheme( ), guiOptions );
    }

    public LayeredGui( String frameTitleRoot, DockingTheme theme, GuiOption... guiOptions )
    {
        this( frameTitleRoot, theme, ImmutableSet.copyOf( guiOptions ) );
    }

    public LayeredGui( String frameTitleRoot, DockingTheme theme, Collection<? extends GuiOption> guiOptions )
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

        // Don't start the animator here, since we might not ever get any views that
        // use it -- see the javadocs for {@link View#setGLAnimator(GLAnimatorControl)}
        double fps = findFps( guiOptions, 60 );
        this.animator = new SwingEDTAnimator( fps );

        this.dockingViewIdCounters = new HashMap<>( );

        this.dockingViews = HashBiMap.create( );

        this.dockingGroup.addListener( new DockingGroupAdapter( )
        {
            @Override
            public void disposingAllFrames( DockingGroup dockingGroup )
            {
                if ( dockingAppName != null )
                {
                    saveDockingArrangement( dockingAppName, dockingGroup.captureArrangement( ) );
                }

                views.set( ImmutableSet.of( ) );

                animator.stop( );
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

        if ( !guiOptions.contains( HIDE_LAYERS_PANEL ) )
        {
            LayerCardsPanel layerCardsPanel = new LayerCardsPanel( this.layers );
            JScrollPane layerCardsScroller = new JScrollPane( layerCardsPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED );
            com.metsci.glimpse.docking.View layersView = new com.metsci.glimpse.docking.View( layerCardsViewId, layerCardsScroller, "Layers", false, null, layersIcon, null );
            this.dockingGroup.addView( layersView );
        }


        // Controller
        //

        addElementRemovedListener( this.views, this::handleViewRemoved );
        addElementAddedListener( this.views, true, this::handleViewAdded );

        addElementRemovedListener( this.layers, this::handleLayerRemoved );
        addElementAddedListener( this.layers, true, this::handleLayerAdded );

        addEntryRemovedListener( this.linkageNames, ( k, v ) -> this.pruneLinkages( ) );
    }

    public DockingGroup getDockingGroup( )
    {
        return this.dockingGroup;
    }

    public void stopAnimator( )
    {
        animator.stop( );
    }

    public void startAnimator( )
    {
        animator.start( );
    }

    public void arrange( String appName, String defaultArrResource )
    {
        URL defaultArrUrl = getResource( defaultArrResource );
        this.arrange( appName, defaultArrUrl );
    }

    public void arrange( String appName, URL defaultArrUrl )
    {
        GroupArrangement groupArr = loadDockingArrangement( appName, defaultArrUrl );
        this.dockingGroup.setArrangement( groupArr );
        this.dockingAppName = appName;
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

    /**
     * {@link LayeredGui} generates a docking viewId near the <em>end</em> of the process
     * of adding a {@link View}. Sometimes, though, we want to know <em>before</em> we add
     * a {@link View} what viewId it will have -- e.g. to pre-insert a {@link ViewPlacement}
     * in the docking {@link GroupArrangement}.
     * <p>
     * This method returns the docking viewId that will be used when the given {@link View}
     * gets added to this {@link LayeredGui} -- assuming no other {@link View}s get added in
     * the meantime.
     */
    public String predictDockingViewId( View view )
    {
        return this.genDockingViewId( view, false );
    }

    protected String claimDockingViewId( View view )
    {
        return this.genDockingViewId( view, true );
    }

    protected String genDockingViewId( View view, boolean claim )
    {
        String stem = dockingViewIdStem( view.getClass( ) );
        int number = this.dockingViewIdCounters.getOrDefault( stem, 0 );

        if ( claim )
        {
            this.dockingViewIdCounters.put( stem, number + 1 );
        }

        return ( stem + ":" + number );
    }

    public static String dockingViewIdStem( Class<? extends View> viewClass )
    {
        return viewClass.getName( );
    }

    public static Pattern dockingViewIdPattern( Class<? extends View> viewClass )
    {
        String stem = dockingViewIdStem( viewClass );
        return Pattern.compile( "^" + Pattern.quote( stem ) + ":[0-9]+$" );
    }

    public static boolean dockingViewIdMatches( Class<? extends View> viewClass, String viewId )
    {
        return dockingViewIdPattern( viewClass ).matcher( viewId ).matches( );
    }

    public void addView( View view )
    {
        this.addView( view, null );
    }

    /**
     * If {@code placementRule} is non-null, it will be called to choose a docking destination
     * for {@code view}.
     */
    public void addView( View view, ViewPlacementRule placementRule )
    {
        if ( placementRule != null )
        {
            String viewId = this.predictDockingViewId( view );
            this.dockingGroup.addViewPlacement( viewId, placementRule );
        }

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

        this.addView( newView, ( planArr, existingViewIds ) ->
        {
            // Split the original tile, and put the clone in the right half of the split
            String viewId = this.dockingViews.get( view ).viewId;
            DockerArrangementTile tile = findArrTileContaining( planArr, viewId );
            return new BesideExistingNeighbor( null, null, tile, RIGHT, 0.5 );
        } );
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

        view.setGLAnimator( this.animator );

        if ( !view.viewOptions.contains( HIDE_CLONE_BUTTON ) )
        {
            JButton cloneButton = new JButton( cloneIcon );
            cloneButton.setToolTipText( "Clone This View" );
            cloneButton.addActionListener( ( ev ) ->
            {
                this.cloneView( view );
            } );

            view.toolbar.add( cloneButton );
        }

        if ( !view.viewOptions.contains( HIDE_FACETS_MENU ) )
        {
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

                    JMenuItem facetToggle = new JCheckBoxMenuItem( );
                    facetDisposables.add( bindButtonText( facetToggle, layer.title ) );
                    facetDisposables.add( bindToggleButton( facetToggle, facet.isVisible ) );
                    facetsPopup.add( facetToggle );

                    // Redo popup menu layout after a layer title change
                    facetDisposables.add( layer.title.addListener( true, ( ) ->
                    {
                        facetsPopup.pack( );
                        facetsPopup.repaint( );
                    } ) );
                }
            } ) );

            view.toolbar.add( facetsButton );
        }

        String viewId = this.claimDockingViewId( view );
        boolean closeable = ( !view.viewOptions.contains( HIDE_CLOSE_BUTTON ) );
        com.metsci.glimpse.docking.View dockingView = new com.metsci.glimpse.docking.View( viewId, view.getComponent( ), "", closeable, view.getTooltip( ), view.getIcon( ), view.toolbar );
        disposables.add( view.title.addListener( true, ( ) ->
        {
            dockingView.title.set( view.title.v( ) );
        } ) );
        this.dockingGroup.addView( dockingView );

        // When the user closes a dockingView, we will need to know the corresponding view
        this.dockingViews.put( view, dockingView );

        disposables.add( view::dispose );

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
