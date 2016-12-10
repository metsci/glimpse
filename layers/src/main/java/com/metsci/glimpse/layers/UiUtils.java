package com.metsci.glimpse.layers;

import java.awt.Color;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import com.metsci.glimpse.util.var.InvalidValueException;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public class UiUtils
{

    public static final Color invalidValueBg = new Color( 255, 175, 175 );


    public static interface ListenerBinding
    {
        void unbind( );
    }

    public static ListenerBinding bindLabel( JLabel c, ReadableVar<String> var )
    {
        Runnable varListener = var.addListener( true, ( ) ->
        {
            c.setText( var.v( ) );
        } );

        return ( ) ->
        {
            var.removeListener( varListener );
        };
    }

    public static ListenerBinding bindToggleButton( AbstractButton c, Var<Boolean> var )
    {
        Runnable varListener = var.addListener( true, ( ) ->
        {
            c.setSelected( var.v( ) );
        } );

        Color origBackground = c.getBackground( );
        ItemListener uiListener = ( ev ) ->
        {
            try
            {
                var.set( c.isSelected( ) );
                c.setBackground( origBackground );
            }
            catch ( InvalidValueException e )
            {
                c.setBackground( invalidValueBg );
            }
        };
        c.addItemListener( uiListener );

        return ( ) ->
        {
            var.removeListener( varListener );
            c.removeItemListener( uiListener );
        };
    }

}
