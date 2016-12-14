package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.util.var.VarUtils.addElementAddedListener;
import static com.metsci.glimpse.util.var.VarUtils.addElementRemovedListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.Var;

import net.miginfocom.swing.MigLayout;

public class LayerCardsPanel extends JPanel
{

    protected final Var<? extends List<? extends Layer>> layers;
    protected final DisposableGroup disposables;

    protected final Map<Layer,JComponent> cards;


    public LayerCardsPanel( Var<? extends List<? extends Layer>> layers )
    {
        this.layers = layers;
        this.disposables = new DisposableGroup( );
        this.cards = new HashMap<>( );

        this.setLayout( new MigLayout( "fillx" ) );
        this.setBackground( UIManager.getColor( "List.background" ) );

        this.disposables.add( addElementAddedListener( this.layers, true, ( layer ) ->
        {
            JComponent card = new LayerCard( layer );
            this.cards.put( layer, card );
            this.add( card, "growx, wrap" );
        } ) );

        this.disposables.add( addElementRemovedListener( this.layers, true, ( layer ) ->
        {
            JComponent card = this.cards.remove( layer );
            this.remove( card );
        } ) );
    }

    public void dispose( )
    {
        this.disposables.dispose( );
    }

}
