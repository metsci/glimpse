package com.metsci.glimpse.layers;

import static javax.swing.BorderFactory.createLineBorder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

public class LayersPanel extends JPanel
{

    protected final Map<Layer,JComponent> layerCards;


    public LayersPanel( )
    {
        this.layerCards = new HashMap<>( );

        this.setLayout( new MigLayout( "fillx" ) );
        this.setBackground( UIManager.getColor( "List.background" ) );
    }

    public void refresh( Collection<Layer> layers )
    {
        // Remove all cards from UI
        this.removeAll( );

        // Remove unneeded cards from map
        this.layerCards.keySet( ).retainAll( new HashSet<>( layers ) );

        for ( Layer layer : layers )
        {
            // Add a new card to the map, if necessary
            JComponent card = this.layerCards.computeIfAbsent( layer, LayersPanel::createLayerCard );

            // Add card to UI
            this.add( card, "growx, wrap" );
        }
    }

    protected static JComponent createLayerCard( Layer layer )
    {
        JCheckBox check = new JCheckBox( );
        check.setSelected( layer.isVisible( ) );
        check.addItemListener( ( ev ) ->
        {
            layer.setVisible( check.isSelected( ) );
        } );

        JLabel label = new JLabel( layer.getTitle( ) );

        JPanel card = new JPanel( new MigLayout( "fillx" ) );
        card.setBorder( createLineBorder( card.getBackground( ).darker( ), 1 ) );
        card.add( check, "" );
        card.add( label, "pushx" );

        return card;
    }

}
