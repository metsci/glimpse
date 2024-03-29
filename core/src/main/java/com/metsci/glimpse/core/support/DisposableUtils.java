/*
 * Copyright (c) 2020, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.core.support;

import java.awt.Component;
import java.awt.Container;
import java.awt.ItemSelectable;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.Axis2D;
import com.metsci.glimpse.core.axis.listener.AxisListener1D;
import com.metsci.glimpse.core.axis.listener.AxisListener2D;
import com.metsci.glimpse.core.axis.tagged.Constraint;
import com.metsci.glimpse.core.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.core.canvas.GlimpseCanvas;
import com.metsci.glimpse.core.event.key.GlimpseKeyListener;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseMotionListener;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseWheelListener;
import com.metsci.glimpse.core.gl.GLEventAdapter;
import com.metsci.glimpse.core.layout.GlimpseLayout;
import com.metsci.glimpse.core.painter.base.GlimpsePainter;
import com.metsci.glimpse.core.painter.group.DelegatePainter;
import com.metsci.glimpse.core.painter.group.WrappedPainter;
import com.metsci.glimpse.core.plot.timeline.event.EventPlotInfo;
import com.metsci.glimpse.core.plot.timeline.event.listener.EventPlotListener;
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

    public static Disposable onWindowClosing( Window window, Consumer<? super WindowEvent> fn )
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

    public static Disposable onWindowClosed( Window window, Consumer<? super WindowEvent> fn )
    {
        return addWindowListener( window, new WindowAdapter( )
        {
            @Override
            public void windowClosed( WindowEvent ev )
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

    public static Disposable addItemListener( ItemSelectable itemSelectable, Runnable listener )
    {
        return addItemListener( itemSelectable, ( ev ) -> listener.run( ) );
    }

    public static Disposable addItemListener( boolean runImmediately, ItemSelectable itemSelectable, Runnable listener )
    {
        if ( runImmediately )
        {
            listener.run( );
        }

        return addItemListener( itemSelectable, listener );
    }

    public static Disposable addActionListener( AbstractButton button, ActionListener actionListener )
    {
        button.addActionListener( actionListener );

        return ( ) ->
        {
            button.removeActionListener( actionListener );
        };
    }

    public static Disposable addActionListener( AbstractButton button, Runnable listener )
    {
        return addActionListener( button, ( ev ) -> listener.run( ) );
    }

    public static Disposable addActionListener( boolean runImmediately, AbstractButton button, Runnable listener )
    {
        if ( runImmediately )
        {
            listener.run( );
        }

        return addActionListener( button, listener );
    }

    public static Disposable addPropertyChangeListener( Component c, String propertyName, Runnable listener )
    {
        return addPropertyChangeListener( c, propertyName, ( ev ) -> listener.run( ) );
    }

    public static Disposable addPropertyChangeListener( Component c, PropertyChangeListener listener )
    {
        c.addPropertyChangeListener( listener );

        return ( ) ->
        {
            c.removePropertyChangeListener( listener );
        };
    }

    public static Disposable addPropertyChangeListener( Component c, String propertyName, PropertyChangeListener listener )
    {
        c.addPropertyChangeListener( propertyName, listener );

        return ( ) ->
        {
            c.removePropertyChangeListener( propertyName, listener );
        };
    }

    public static Disposable addTableModelListener( TableModel model, TableModelListener listener )
    {
        model.addTableModelListener( listener );

        return ( ) ->
        {
            model.removeTableModelListener( listener );
        };
    }

    public static Disposable onFocusGained( boolean runImmediately, Component c, Runnable listener )
    {
        if ( runImmediately )
        {
            listener.run( );
        }

        FocusListener focusListener = new FocusAdapter( )
        {
            @Override
            public void focusGained( FocusEvent ev )
            {
                listener.run( );
            }
        };

        c.addFocusListener( focusListener );

        return ( ) ->
        {
            c.removeFocusListener( focusListener );
        };
    }

    public static Disposable onFocusLost( boolean runImmediately, Component c, Runnable listener )
    {
        if ( runImmediately )
        {
            listener.run( );
        }

        FocusListener focusListener = new FocusAdapter( )
        {
            @Override
            public void focusLost( FocusEvent ev )
            {
                listener.run( );
            }
        };

        c.addFocusListener( focusListener );

        return ( ) ->
        {
            c.removeFocusListener( focusListener );
        };
    }

    public static Disposable addTextListener( JTextComponent c, Runnable listener )
    {
        DocumentListener docListener = new DocumentListener( )
        {
            public void insertUpdate( DocumentEvent ev ) { listener.run( ); }
            public void removeUpdate( DocumentEvent ev ) { listener.run( ); }
            public void changedUpdate( DocumentEvent ev ) { listener.run( ); }
        };

        PropertyChangeListener propChangeListener = ( ev ) ->
        {
            Document oldDoc = ( Document ) ev.getOldValue( );
            if ( oldDoc != null )
            {
                oldDoc.removeDocumentListener( docListener );
            }

            Document newDoc = ( Document ) ev.getNewValue( );
            if ( newDoc != null )
            {
                newDoc.addDocumentListener( docListener );
            }

            listener.run( );
        };
        c.addPropertyChangeListener( "document", propChangeListener );

        Document doc = c.getDocument( );
        if ( doc != null )
        {
            doc.addDocumentListener( docListener );
        }

        return ( ) ->
        {
            c.removePropertyChangeListener( "document", propChangeListener );

            Document doc2 = c.getDocument( );
            if ( doc2 != null )
            {
                doc2.removeDocumentListener( docListener );
            }
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

    public static Disposable addComponent( JMenu menu, Component child )
    {
        menu.add( child );

        return ( ) ->
        {
            menu.remove( child );
        };
    }

    public static Disposable addMouseListener( Component component, MouseListener listener )
    {
        component.addMouseListener( listener );

        return ( ) ->
        {
            component.removeMouseListener( listener );
        };
    }

    public static Disposable addMouseMotionListener( Component component, MouseMotionListener listener )
    {
        component.addMouseMotionListener( listener );

        return ( ) ->
        {
            component.removeMouseMotionListener( listener );
        };
    }

    public static Disposable addMouseWheelListener( Component component, MouseWheelListener listener )
    {
        component.addMouseWheelListener( listener );

        return ( ) ->
        {
            component.removeMouseWheelListener( listener );
        };
    }

    public static Disposable onMousePress( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseListener( component, new MouseAdapter( )
        {
            @Override
            public void mousePressed( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseRelease( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseReleased( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseClick( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseClicked( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseDrag( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseDragged( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseMove( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseMotionListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseMoved( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseEnter( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseEntered( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseExit( Component component, Consumer<? super MouseEvent> fn )
    {
        return addMouseListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseExited( MouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable onMouseWheel( Component component, Consumer<? super MouseWheelEvent> fn )
    {
        return addMouseWheelListener( component, new MouseAdapter( )
        {
            @Override
            public void mouseWheelMoved( MouseWheelEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable addToolkitListener( Toolkit toolkit, long eventMask, AWTEventListener listener )
    {
        toolkit.addAWTEventListener( listener, eventMask );

        return ( ) ->
        {
            toolkit.removeAWTEventListener( listener );
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

    public static Disposable onGLInit( GlimpseCanvas canvas, Consumer<? super GLAutoDrawable> fn )
    {
        return onGLInit( canvas.getGLDrawable( ), fn );
    }

    public static Disposable onGLInit( GLAutoDrawable glDrawable, Consumer<? super GLAutoDrawable> fn )
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

    public static Disposable onGLDispose( GlimpseCanvas canvas, Consumer<? super GLAutoDrawable> fn )
    {
        return onGLDispose( canvas.getGLDrawable( ), fn );
    }

    public static Disposable onGLDispose( GLAutoDrawable glDrawable, Consumer<? super GLAutoDrawable> fn )
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

    public static Disposable addGlimpsePainter( GlimpseLayout layout, int zOrder, GlimpsePainter painter )
    {
        layout.addPainter( painter, zOrder );

        return ( ) ->
        {
            layout.removePainter( painter );
        };
    }

    public static Disposable putGlimpsePainter( GlimpseLayout layout, Object key, int zOrder, GlimpsePainter painter )
    {
        layout.addPainter( key, painter, zOrder );

        return ( ) ->
        {
            layout.removePainter( painter );
        };
    }

    public static Disposable addGlimpsePainter( WrappedPainter wrappedPainter, GlimpsePainter painter )
    {
        wrappedPainter.addPainter( painter );

        return ( ) ->
        {
            wrappedPainter.removePainter( painter );
        };
    }

    public static Disposable addGlimpsePainter( WrappedPainter wrappedPainter, int zOrder, GlimpsePainter painter )
    {
        wrappedPainter.addPainter( painter, zOrder );

        return ( ) ->
        {
            wrappedPainter.removePainter( painter );
        };
    }

    public static Disposable putGlimpsePainter( WrappedPainter wrappedPainter, Object key, int zOrder, GlimpsePainter painter )
    {
        wrappedPainter.putPainter( key, painter, zOrder );

        return ( ) ->
        {
            wrappedPainter.removePainter( painter );
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

    public static Disposable addGlimpseMouseListener( GlimpseLayout layout, GlimpseMouseListener listener )
    {
        layout.addGlimpseMouseListener( listener );

        return ( ) ->
        {
            layout.removeGlimpseMouseListener( listener );
        };
    }

    public static Disposable onGlimpseMousePress( GlimpseLayout layout, Consumer<? super GlimpseMouseEvent> fn )
    {
        return addGlimpseMouseListener( layout, new GlimpseMouseAdapter( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent ev )
            {
                fn.accept( ev );
            }
        } );
    }

    public static Disposable addGlimpseMouseMotionListener( GlimpseLayout layout, GlimpseMouseMotionListener listener )
    {
        layout.addGlimpseMouseMotionListener( listener );

        return ( ) ->
        {
            layout.removeGlimpseMouseMotionListener( listener );
        };
    }

    public static Disposable addGlimpseMouseWheelListener( GlimpseLayout layout, GlimpseMouseWheelListener listener )
    {
        layout.addGlimpseMouseWheelListener( listener );

        return ( ) ->
        {
            layout.removeGlimpseMouseWheelListener( listener );
        };
    }

    public static Disposable addGlimpseMouseAllListener( GlimpseLayout layout, GlimpseMouseAllListener listener )
    {
        layout.addGlimpseMouseAllListener( listener );

        return ( ) ->
        {
            layout.removeGlimpseMouseAllListener( listener );
        };
    }

    public static Disposable addGlimpseKeyListener( GlimpseLayout layout, GlimpseKeyListener listener )
    {
        layout.addGlimpseKeyListener( listener );

        return ( ) ->
        {
            layout.removeGlimpseKeyListener( listener );
        };
    }

    public static Disposable addAxisListener1D( Axis1D axis, AxisListener1D listener )
    {
        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

    public static Disposable addAxisListener1D( Axis1D axis, Runnable listener )
    {
        return addAxisListener1D( axis, ( x ) -> listener.run( ) );
    }

    public static Disposable addAxisListener2D( Axis2D axis, AxisListener2D listener )
    {
        axis.addAxisListener( listener );

        return ( ) ->
        {
            axis.removeAxisListener( listener );
        };
    }

    public static Disposable addAxisListener2D( Axis2D axis, Runnable listener )
    {
        return addAxisListener2D( axis, ( x ) -> listener.run( ) );
    }

    public static Disposable addAxisConstraint( TaggedAxis1D axis, Constraint constraint )
    {
        axis.addConstraint( constraint );

        return ( ) ->
        {
            axis.removeConstraint( constraint.getName( ) );
        };
    }

    public static Disposable addEventPlotListener( EventPlotInfo eventPlotInfo, EventPlotListener listener )
    {
        eventPlotInfo.addEventPlotListener( listener );

        return ( ) ->
        {
            eventPlotInfo.removeEventPlotListener( listener );
        };
    }

}
