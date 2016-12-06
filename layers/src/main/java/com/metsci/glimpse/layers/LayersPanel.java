package com.metsci.glimpse.layers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class LayersPanel extends JPanel
{

    protected final Map<Layer,JComponent> layerCards;


    public LayersPanel( )
    {
        this.layerCards = new HashMap<>( );
        this.setLayout( new MigLayout( "fillx" ) );
    }

    public void setLayers( Collection<Layer> layers )
    {
        this.removeAll( );

        this.layerCards.keySet( ).retainAll( new HashSet<>( layers ) );

        for ( Layer layer : layers )
        {
            JComponent card = this.layerCards.computeIfAbsent( layer, this::createLayerCard );
            this.add( card, "wrap" );
        }
    }

    protected JComponent createLayerCard( Layer layer )
    {
        JLabel label = new JLabel( layer.title( ) );

        JPanel card = new JPanel( new MigLayout( "fillx" ) );
        card.add( label, "" );

        return card;
    }

}
