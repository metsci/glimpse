package com.metsci.glimpse.layers.misc;

import static com.metsci.glimpse.support.DisposableUtils.addItemListener;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.media.opengl.GLAnimatorControl;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.InvalidValueException;
import com.metsci.glimpse.util.var.ReadableVar;
import com.metsci.glimpse.util.var.Var;

public class UiUtils
{

    public static final Color invalidValueBg = new Color( 255, 175, 175 );


    public static void ensureAnimating( GLAnimatorControl animator )
    {
        // This might need to do something more involved one day, like check
        // the return value. For now, the idea is just to have a method name
        // that makes it clear that multiple calls are okay.
        animator.start( );
    }

    public static Disposable bindLabel( JLabel c, ReadableVar<String> var )
    {
        return var.addListener( true, ( ) ->
        {
            c.setText( var.v( ) );
        } );
    }

    public static Disposable bindButtonText( AbstractButton c, ReadableVar<String> var )
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

    /**
     * The component must be part of a tree that is either displayable, or whose root component's
     * {@link Component#addNotify()} method has been called -- otherwise layout invalidation doesn't
     * work right.
     *
     * For a tree that will only be painted offscreen, {@link Component#addNotify()} should be called
     * on the root component right after it gets instantiated. In some cases it may be convenient to
     * do this in an instance initializer:
     *
     *     JPanel offscreenRoot = new JPanel( )
     *     {{
     *         // We want to paint this component without making it displayable, so
     *         // addNotify() must be called for layout invalidation to work right
     *         addNotify( );
     *     }};
     *
     */
    public static BufferedImage paintComponentToImage( Component c )
    {
        return paintComponentToImage( c, c.getPreferredSize( ) );
    }

    /**
     * The component must be part of a tree that is either displayable, or whose root component's
     * {@link Component#addNotify()} method has been called -- otherwise layout invalidation doesn't
     * work right.
     *
     * For a tree that will only be painted offscreen, {@link Component#addNotify()} should be called
     * on the root component right after it gets instantiated. In some cases it may be convenient to
     * do this in an instance initializer:
     *
     *     JPanel offscreenRoot = new JPanel( )
     *     {{
     *         // We want to paint this component without making it displayable, so
     *         // addNotify() must be called for layout invalidation to work right
     *         addNotify( );
     *     }};
     *
     */
    public static BufferedImage paintComponentToImage( Component c, Dimension size )
    {
        return paintComponentToImage( c, size.width, size.height );
    }

    /**
     * The component must be part of a tree that is either displayable, or whose root component's
     * {@link Component#addNotify()} method has been called -- otherwise layout invalidation doesn't
     * work right.
     *
     * For a tree that will only be painted offscreen, {@link Component#addNotify()} should be called
     * on the root component right after it gets instantiated. In some cases it may be convenient to
     * do this in an instance initializer:
     *
     *     JPanel offscreenRoot = new JPanel( )
     *     {{
     *         // We want to paint this component without making it displayable, so
     *         // addNotify() must be called for layout invalidation to work right
     *         addNotify( );
     *     }};
     *
     */
    public static BufferedImage paintComponentToImage( Component c, int width, int height )
    {
        c.setBounds( 0, 0, width, height );

        layoutTree( c );
        c.validate( );

        BufferedImage image = new BufferedImage( width, height, TYPE_INT_ARGB );

        Graphics2D g = image.createGraphics( );
        c.paint( g );
        g.dispose( );

        return image;
    }

    public static void layoutTree( Component root )
    {
        root.doLayout( );
        if ( root instanceof Container )
        {
            Container container = ( Container ) root;
            for ( Component child : container.getComponents( ) )
            {
                layoutTree( child );
            }
        }
    }

    public static void requireSwingThread( )
    {
        if ( !SwingUtilities.isEventDispatchThread( ) )
        {
            throw new RuntimeException( "This operation is only allowed on the Swing/AWT event-dispatch thread" );
        }
    }

}
