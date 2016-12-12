package com.metsci.glimpse.layers;

import static com.metsci.glimpse.layers.UiUtils.bindLabel;
import static com.metsci.glimpse.layers.UiUtils.bindToggleButton;
import static com.metsci.glimpse.util.var.VarUtils.addEntryAddedListener;
import static com.metsci.glimpse.util.var.VarUtils.addEntryRemovedListener;
import static javax.swing.BorderFactory.createLineBorder;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.metsci.glimpse.layers.UiUtils.ListenerBinding;
import com.metsci.glimpse.util.var.ReadableVar;

import net.miginfocom.swing.MigLayout;

public class LayerCard extends JPanel
{

    protected final Layer layer;

    // XXX: The "unbinder" and "remover" ideas are similar -- would be nice to unify them
    protected final Map<Object,ListenerBinding> bindings;
    protected final Map<Facet,Runnable> facetRemovers;

    protected final Runnable facetRemovedListener;
    protected final Runnable facetAddedListener;


    public LayerCard( Layer layer )
    {
        this.layer = layer;

        this.bindings = new HashMap<>( );
        this.facetRemovers = new HashMap<>( );

        this.setBorder( createLineBorder( this.getBackground( ).darker( ), 1 ) );
        this.setLayout( new MigLayout( "fillx", "[][][push,grow]" ) );


        // Layer
        //

        JCheckBox layerVisibleCheck = new JCheckBox( );
        this.bindings.put( layer.isVisible, bindToggleButton( layerVisibleCheck, layer.isVisible ) );
        this.add( layerVisibleCheck, "" );

        JLabel layerTitleLabel = new JLabel( );
        this.bindings.put( layer.title, bindLabel( layerTitleLabel, layer.title ) );
        this.add( layerTitleLabel, "spanx 2, wrap" );


        // Facets
        //

        ReadableVar<? extends Map<? extends View,? extends Facet>> facets = this.layer.facets( );

        this.facetRemovedListener = addEntryRemovedListener( facets, true, ( view, facet ) ->
        {
            this.facetRemovers.remove( facet ).run( );
            this.bindings.remove( facet.isVisible ).unbind( );
            this.bindings.remove( view.title ).unbind( );
        } );

        // WIP: Respect layer ordering
        this.facetAddedListener = addEntryAddedListener( facets, true, ( view, facet ) ->
        {
            JCheckBox facetVisibleCheck = new JCheckBox( );
            this.bindings.put( facet.isVisible, bindToggleButton( facetVisibleCheck, facet.isVisible ) );
            this.add( facetVisibleCheck, "gapleft 12, spanx 2" );

            JLabel viewTitleLabel = new JLabel( );
            this.bindings.put( view.title, bindLabel( viewTitleLabel, view.title ) );
            this.add( viewTitleLabel, "wrap" );

            this.facetRemovers.put( facet, ( ) ->
            {
                this.remove( facetVisibleCheck );
                this.remove( viewTitleLabel );
            } );
        } );
    }

    public void dispose( )
    {
        ReadableVar<? extends Map<? extends View,? extends Facet>> facets = this.layer.facets( );
        facets.removeListener( this.facetRemovedListener );
        facets.removeListener( this.facetAddedListener );

        for ( ListenerBinding binding : this.bindings.values( ) )
        {
            binding.unbind( );
        }
    }

}
