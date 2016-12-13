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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JButton;
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
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.layers.misc.LayerCardsPanel;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;
import com.metsci.glimpse.util.var.Var;

public class LayeredGui
{

    // Model
    public final Var<ImmutableMap<String,ImmutableList<Linkage>>> linkages;
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
        com.metsci.glimpse.docking.View layersView = new com.metsci.glimpse.docking.View( "layersView", layerCardsScroller, "Layers", false, null, this.layerCardsPanel.getIcon( ), null );
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

    public void addLinkage( String traitKey, Trait master )
    {
        Linkage linkage = new Linkage( master, false );
        this.addLinkage( traitKey, linkage );
    }

    protected void addLinkage( String traitKey, Linkage linkage )
    {
        ImmutableList<Linkage> oldLinkages = this.linkages.v( ).get( traitKey );
        ImmutableList<Linkage> newLinkages;
        if ( oldLinkages == null )
        {
            newLinkages = ImmutableList.of( linkage );
        }
        else
        {
            // WIP: Insertion index
            newLinkages = listPlus( oldLinkages, linkage );
        }

        this.linkages.update( ( v ) -> mapWith( v, traitKey, newLinkages ) );
    }

    public void addView( View view )
    {
        this.views.update( ( v ) -> setPlus( v, view ) );
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
        Map<String,Trait> oldTraits = view.traits.v( );
        Map<String,Trait> newTraits = new LinkedHashMap<>( );
        for ( Entry<String,ImmutableList<Linkage>> en : this.linkages.v( ).entrySet( ) )
        {
            String traitKey = en.getKey( );
            List<Linkage> linkages = en.getValue( );
            Trait trait = oldTraits.get( traitKey );

            // If the view doesn't have this trait yet, create one compatible with the default linkage
            if ( trait == null )
            {
                trait = linkages.get( 0 ).create( );
            }

            // If the trait doesn't have a parent, look for a compatible linkage
            if ( trait.parent( ).v( ) == null )
            {
                for ( Linkage linkage : linkages )
                {
                    if ( linkage.canAdd( trait ) )
                    {
                        linkage.add( trait );
                        break;
                    }
                }
            }

            // If the trait still doesn't have a parent, create an implicit linkage
            if ( trait.parent( ).v( ) == null )
            {
                Linkage linkage = new Linkage( trait.createClone( ), true );
                this.addLinkage( traitKey, linkage );
                linkage.add( trait );
            }

            newTraits.put( traitKey, trait );
        }
        view.setTraits( newTraits );

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

        JButton cloneViewButton = new JButton( "Clone" );
        cloneViewButton.setToolTipText( "Clone This View" );
        cloneViewButton.addActionListener( ( ev ) ->
        {
            View clone = view.createClone( );

            Map<String,Trait> cloneTraits = new LinkedHashMap<>( );

            for ( Entry<String,Trait> en : view.traits.v( ).entrySet( ) )
            {
                String traitKey = en.getKey( );
                Trait trait = en.getValue( );
                cloneTraits.put( traitKey, trait.createClone( ) );
            }
            clone.setTraits( cloneTraits );

            // WIP: Link clone with original
//            for ( Entry<String,Trait> en : view.traits.v( ).entrySet( ) )
//            {
//                String traitKey = en.getKey( );
//                Trait trait = en.getValue( );
//
//                if ( trait.getParent( ) == null )
//                {
//                    Trait parent = trait.createClone( );
//
//                    // WIP: Register parent with LayeredGui, as a "linkage"
//
//                    trait.setParent( parent );
//                }
//
//                Trait cloneTrait = cloneTraits.get( traitKey );
//
//                cloneTrait.setParent( trait.getParent( ) );
//            }

            this.addView( clone );
        } );

        JToolBar toolbar = newToolbar( true );
        toolbar.add( cloneViewButton );
        for ( Component c : view.getToolbarComponents( ) )
        {
            toolbar.add( c );
        }

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
        com.metsci.glimpse.docking.View dockingView = new com.metsci.glimpse.docking.View( dockingViewId, view.getComponent( ), view.title.v( ), true, view.getTooltip( ), view.getIcon( ), toolbar );
        this.dockingGroup.addView( dockingView );

        this.dockingViews.put( view, dockingView );
    }

    protected void handleViewRemoved( View view )
    {
        // WIP: Remove abandoned implicit linkages

        GLAutoDrawable glDrawable = view.getGLDrawable( );
        if ( glDrawable != null )
        {
            this.animator.remove( glDrawable );
        }

        view.dispose( );

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
