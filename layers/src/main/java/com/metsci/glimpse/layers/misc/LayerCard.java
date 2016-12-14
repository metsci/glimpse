package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.layers.misc.UiUtils.addComponent;
import static com.metsci.glimpse.layers.misc.UiUtils.bindLabel;
import static com.metsci.glimpse.layers.misc.UiUtils.bindToggleButton;
import static com.metsci.glimpse.util.var.VarUtils.addEntryAddedListener;
import static com.metsci.glimpse.util.var.VarUtils.addEntryRemovedListener;
import static javax.swing.BorderFactory.createLineBorder;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.metsci.glimpse.layers.Facet;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.View;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.ReadableVar;

import net.miginfocom.swing.MigLayout;

public class LayerCard extends JPanel
{

    protected final Layer layer;

    protected final DisposableGroup topLevelDisposables;
    protected final Map<Facet,Disposable> perFacetDisposables;


    public LayerCard( Layer layer )
    {
        this.layer = layer;

        this.topLevelDisposables = new DisposableGroup( );
        this.perFacetDisposables = new HashMap<>( );

        this.setBorder( createLineBorder( this.getBackground( ).darker( ), 1 ) );
        this.setLayout( new MigLayout( "fillx", "[][][push,grow]" ) );


        // Layer
        //

        JCheckBox layerVisibleCheck = new JCheckBox( );
        this.topLevelDisposables.add( bindToggleButton( layerVisibleCheck, layer.isVisible ) );
        this.add( layerVisibleCheck, "" );

        JLabel layerTitleLabel = new JLabel( );
        this.topLevelDisposables.add( bindLabel( layerTitleLabel, layer.title ) );
        this.add( layerTitleLabel, "spanx 2, wrap" );


        // Facets
        //

        ReadableVar<? extends Map<? extends View,? extends Facet>> facets = this.layer.facets( );

        // XXX: Order facets in some sensible way
        this.topLevelDisposables.add( addEntryAddedListener( facets, true, ( view, facet ) ->
        {
            DisposableGroup disposables = new DisposableGroup( );

            JCheckBox facetVisibleCheck = new JCheckBox( );
            disposables.add( bindToggleButton( facetVisibleCheck, facet.isVisible ) );
            disposables.add( addComponent( this, facetVisibleCheck, "gapleft 12, spanx 2" ) );

            JLabel viewTitleLabel = new JLabel( );
            disposables.add( bindLabel( viewTitleLabel, view.title ) );
            disposables.add( addComponent( this, viewTitleLabel, "wrap" ) );

            this.perFacetDisposables.put( facet, disposables );
        } ) );

        this.topLevelDisposables.add( addEntryRemovedListener( facets, true, ( view, facet ) ->
        {
            this.perFacetDisposables.remove( facet ).dispose( );
        } ) );
    }

    public void dispose( )
    {
        this.topLevelDisposables.dispose( );

        for ( Disposable disposable : this.perFacetDisposables.values( ) )
        {
            disposable.dispose( );
        }
    }

}
