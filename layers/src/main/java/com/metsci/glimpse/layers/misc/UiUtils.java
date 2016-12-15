package com.metsci.glimpse.layers.misc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.ItemSelectable;
import java.awt.event.ItemListener;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.AbstractButton;
import javax.swing.JLabel;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.InvalidValueException;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public class UiUtils
{

    public static final Color invalidValueBg = new Color( 255, 175, 175 );


    public static Disposable bindLabel( JLabel c, ReadableVar<String> var )
    {
        return var.addListener( true, ( ) ->
        {
            c.setText( var.v( ) );
        } );
    }

    public static Disposable bindToggleButton( AbstractButton c, Var<Boolean> var )
    {
        DisposableGroup listeners = new DisposableGroup( );

        listeners.add( var.addListener( true, ( ) ->
        {
            c.setSelected( var.v( ) );
        } ) );

        Color origBackground = c.getBackground( );
        listeners.add( addItemListener( c, ( ev ) ->
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
        } ) );

        return listeners;
    }

    public static Disposable addComponent( Container container, Component child )
    {
        return addComponent( container, child, null );
    }

    public static Disposable addComponent( Container container, Component child, int index )
    {
        return addComponent( container, child, null, index );
    }

    public static Disposable addComponent( Container container, Component child, Object constraints )
    {
        return addComponent( container, child, constraints, -1 );
    }

    public static Disposable addComponent( Container container, Component child, Object constraints, int index )
    {
        container.add( child, constraints, index );

        return ( ) ->
        {
            container.remove( child );
        };
    }

    public static Disposable addItemListener( ItemSelectable itemSelectable, ItemListener itemListener )
    {
        itemSelectable.addItemListener( itemListener );

        return ( ) ->
        {
            itemSelectable.removeItemListener( itemListener );
        };
    }

    public static Disposable addToAnimator( GLAutoDrawable glDrawable, GLAnimatorControl animator )
    {
        animator.add( glDrawable );

        return ( ) ->
        {
            animator.remove( glDrawable );
        };
    }

}
