package com.metsci.glimpse.layers.misc;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.ItemSelectable;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.gl.GLEventAdapter;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
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

    public static Disposable addToAnimator( GLAutoDrawable glDrawable, GLAnimatorControl glAnimator )
    {
        glAnimator.add( glDrawable );

        return ( ) ->
        {
            glAnimator.remove( glDrawable );
        };
    }

    public static Disposable addGLEventListener( GlimpseCanvas canvas, GLEventListener glListener )
    {
        return addGLEventListener( canvas.getGLDrawable( ), glListener );
    }

    public static Disposable addGLEventListener( GLAutoDrawable glDrawable, GLEventListener glListener )
    {
        glDrawable.addGLEventListener( glListener );

        return ( ) ->
        {
            glDrawable.removeGLEventListener( glListener );
        };
    }

    public static Disposable onGLInit( GlimpseCanvas canvas, Consumer<GLAutoDrawable> initFn )
    {
        return onGLInit( canvas.getGLDrawable( ), initFn );
    }

    public static Disposable onGLInit( GLAutoDrawable glDrawable, Consumer<GLAutoDrawable> initFn )
    {
        return addGLEventListener( glDrawable, new GLEventAdapter( )
        {
            @Override
            public void init( GLAutoDrawable glDrawable )
            {
                initFn.accept( glDrawable );
            }
        } );
    }

    public static Disposable onGLDispose( GlimpseCanvas canvas, Consumer<GLAutoDrawable> disposeFn )
    {
        return onGLDispose( canvas.getGLDrawable( ), disposeFn );
    }

    public static Disposable onGLDispose( GLAutoDrawable glDrawable, Consumer<GLAutoDrawable> disposeFn )
    {
        return addGLEventListener( glDrawable, new GLEventAdapter( )
        {
            @Override
            public void dispose( GLAutoDrawable glDrawable )
            {
                disposeFn.accept( glDrawable );
            }
        } );
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

    public static Disposable addPainter( GlimpseLayout layout, GlimpsePainter painter )
    {
        layout.addPainter( painter );

        return ( ) ->
        {
            layout.removePainter( painter );
        };
    }

    public static Disposable addPainter( DelegatePainter delegatePainter, GlimpsePainter painter )
    {
        delegatePainter.addPainter( painter );

        return ( ) ->
        {
            delegatePainter.removePainter( painter );
        };
    }

    public static void requireSwingThread( )
    {
        if ( !SwingUtilities.isEventDispatchThread( ) )
        {
            throw new RuntimeException( "This operation is only allowed on the Swing/AWT event-dispatch thread" );
        }
    }

}
