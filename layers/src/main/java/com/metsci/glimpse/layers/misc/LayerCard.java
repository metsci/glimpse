package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.layers.misc.UiUtils.bindLabel;
import static com.metsci.glimpse.layers.misc.UiUtils.bindToggleButton;
import static javax.swing.BorderFactory.createLineBorder;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.util.var.DisposableGroup;

import net.miginfocom.swing.MigLayout;

public class LayerCard extends JPanel
{

    protected final Layer layer;

    protected final DisposableGroup disposables;


    public LayerCard( Layer layer )
    {
        this.layer = layer;

        this.disposables = new DisposableGroup( );

        this.setBorder( createLineBorder( this.getBackground( ).darker( ), 1 ) );
        this.setLayout( new MigLayout( "fillx", "[][][push,grow]" ) );


        // Layer
        //

        JCheckBox layerVisibleCheck = new JCheckBox( );
        this.disposables.add( bindToggleButton( layerVisibleCheck, layer.isVisible ) );
        this.add( layerVisibleCheck, "" );

        JLabel layerTitleLabel = new JLabel( );
        this.disposables.add( bindLabel( layerTitleLabel, layer.title ) );
        this.add( layerTitleLabel, "spanx 2" );
    }

    public void dispose( )
    {
        this.disposables.dispose( );
    }

}
