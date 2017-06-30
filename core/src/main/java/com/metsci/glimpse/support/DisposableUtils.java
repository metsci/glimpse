package com.metsci.glimpse.support;

import java.awt.Component;
import java.awt.Container;
import java.awt.ItemSelectable;
import java.awt.Window;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.gl.GLEventAdapter;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.util.var.Disposable;

public class DisposableUtils
{

    // Awt and Swing
    //

    public static Disposable addWindowListener( Window window, WindowListener listener )
    {
        window.addWindowListener( listener );

        return ( ) ->
        {
            window.removeWindowListener( listener );
        };
    }

    public static Disposable onWindowClosing( Window window, Consumer<WindowEvent> fn )
    {
        return addWindowListener( window, new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable addItemListener( ItemSelectable itemSelectable, ItemListener itemListener )
    {
        itemSelectable.addItemListener( itemListener );

        return ( ) ->
        {
            itemSelectable.removeItemListener( itemListener );
        };
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


    // JOGL and Glimpse
    //

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

    public static Disposable onGLInit( GlimpseCanvas canvas, Consumer<GLAutoDrawable> fn )
    {
        return onGLInit( canvas.getGLDrawable( ), fn );
    }

    public static Disposable onGLInit( GLAutoDrawable glDrawable, Consumer<GLAutoDrawable> fn )
    {
        return addGLEventListener( glDrawable, new GLEventAdapter( )
        {
            @Override
            public void init( GLAutoDrawable glDrawable )
            {
                fn.accept( glDrawable );
            }
        } );
    }

    public static Disposable onGLDispose( GlimpseCanvas canvas, Consumer<GLAutoDrawable> fn )
    {
        return onGLDispose( canvas.getGLDrawable( ), fn );
    }

    public static Disposable onGLDispose( GLAutoDrawable glDrawable, Consumer<GLAutoDrawable> fn )
    {
        return addGLEventListener( glDrawable, new GLEventAdapter( )
        {
            @Override
            public void dispose( GLAutoDrawable glDrawable )
            {
                fn.accept( glDrawable );
            }
        } );
    }

    public static Disposable addToGLAnimator( GLAnimatorControl glAnimator, GlimpseCanvas canvas )
    {
        return addToGLAnimator( glAnimator, canvas.getGLDrawable( ) );
    }

    public static Disposable addToGLAnimator( GLAnimatorControl glAnimator, GLAutoDrawable glDrawable )
    {
        glAnimator.add( glDrawable );

        return ( ) ->
        {
            glAnimator.remove( glDrawable );
        };
    }

    public static Disposable addGlimpsePainter( GlimpseLayout layout, GlimpsePainter painter )
    {
        layout.addPainter( painter );

        return ( ) ->
        {
            layout.removePainter( painter );
        };
    }

    public static Disposable addGlimpsePainter( DelegatePainter delegatePainter, GlimpsePainter painter )
    {
        delegatePainter.addPainter( painter );

        return ( ) ->
        {
            delegatePainter.removePainter( painter );
        };
    }

}
