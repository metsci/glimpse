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

    public static interface Model
    {
        Collection<Layer> getLayers( );
        void showLayer( Layer layer, boolean show );
    }


    protected final Model model;
    protected final Map<Layer,JComponent> layerCards;


    public LayersPanel( Model model )
    {
        this.model = model;
        this.layerCards = new HashMap<>( );

        this.setLayout( new MigLayout( "fillx" ) );
        this.setBackground( UIManager.getColor( "List.background" ) );

        this.refresh( );
    }

    public void refresh( )
    {
        Collection<Layer> layers = this.model.getLayers( );

        // Remove all cards from UI
        this.removeAll( );

        // Remove unneeded cards from map
        this.layerCards.keySet( ).retainAll( new HashSet<>( layers ) );

        for ( Layer layer : layers )
        {
            // Create card if needed, and add to map
            JComponent card = this.layerCards.computeIfAbsent( layer, this::createLayerCard );

            // Add card to UI
            this.add( card, "growx, wrap" );
        }
    }

    protected JComponent createLayerCard( Layer layer )
    {
        JCheckBox check = new JCheckBox( );
//        check.setSelected( true ); // WIP: Hack
        check.addItemListener( ( ev ) ->
        {
            this.model.showLayer( layer, check.isSelected( ) );
        } );

        JLabel label = new JLabel( layer.title( ) );

        JPanel card = new JPanel( new MigLayout( "fillx" ) );
        card.setBorder( createLineBorder( card.getBackground( ).darker( ), 1 ) );
        card.add( check, "" );
        card.add( label, "pushx" );

        return card;
    }

}
