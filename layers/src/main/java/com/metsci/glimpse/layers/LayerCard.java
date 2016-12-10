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
    protected final Map<LayerRepr,Runnable> reprRemovers;

    protected final Runnable reprRemovedListener;
    protected final Runnable reprAddedListener;


    public LayerCard( Layer layer )
    {
        this.layer = layer;

        this.bindings = new HashMap<>( );
        this.reprRemovers = new HashMap<>( );

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


        // Reprs
        //

        ReadableVar<? extends Map<? extends LayeredView,? extends LayerRepr>> reprs = this.layer.reprs( );

        this.reprRemovedListener = addEntryRemovedListener( reprs, true, ( view, repr ) ->
        {
            this.reprRemovers.remove( repr ).run( );
            this.bindings.remove( repr.isVisible ).unbind( );
            this.bindings.remove( view.title ).unbind( );
        } );

        // WIP: Respect layer ordering
        this.reprAddedListener = addEntryAddedListener( reprs, true, ( view, repr ) ->
        {
            JCheckBox reprVisibleCheck = new JCheckBox( );
            this.bindings.put( repr.isVisible, bindToggleButton( reprVisibleCheck, repr.isVisible ) );
            this.add( reprVisibleCheck, "gapleft 12, spanx 2" );

            JLabel viewTitleLabel = new JLabel( );
            this.bindings.put( view.title, bindLabel( viewTitleLabel, view.title ) );
            this.add( viewTitleLabel, "wrap" );

            this.reprRemovers.put( repr, ( ) ->
            {
                this.remove( reprVisibleCheck );
                this.remove( viewTitleLabel );
            } );
        } );
    }

    public void dispose( )
    {
        ReadableVar<? extends Map<? extends LayeredView,? extends LayerRepr>> reprs = this.layer.reprs( );
        reprs.removeListener( this.reprRemovedListener );
        reprs.removeListener( this.reprAddedListener );

        for ( ListenerBinding binding : this.bindings.values( ) )
        {
            binding.unbind( );
        }
    }

}
