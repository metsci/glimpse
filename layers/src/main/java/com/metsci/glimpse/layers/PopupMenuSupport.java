package com.metsci.glimpse.layers;

import static com.jogamp.newt.event.MouseEvent.BUTTON3;

import javax.media.opengl.GLProfile;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.support.swing.MouseWrapperNewtSwingEDT;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.var.Disposable;
import com.metsci.glimpse.util.var.Notifier;

/**
 * Functions to support attaching popup menus to Glimpse layouts.
 * <p>
 * Includes workarounds for https://jogamp.org/bugzilla/show_bug.cgi?id=1127.
 */
public class PopupMenuSupport
{

    /**
     * A canvas created by {@link PopupMenuSupport#createNewtSwingEDTGlimpseCanvasWithPopup(GLProfile)}
     * will show popup menus appropriately for GlimpseTargets that implement this interface.
     */
    public interface GlimpsePopupMenuTarget
    {
        JPopupMenu getPopupMenu( );

        /**
         * A notifier that will fire just before the popup menu becomes visible.
         */
        Notifier<GlimpseMouseEvent> getPopupMenuNotifier( );
    }

    /**
     * Creates a canvas that shows popup menus for any GlimpseTargets it contains that implement
     * {@link PopupMenuSupplier}.
     * <p>
     * <strong>NOTE:</strong> An application that uses this function to create any canvases should
     * use this method to create <em>all</em> its canvases -- because the canvases created here have
     * to cooperate with each other to hide the popup menu appropriately.
     */
    public static NewtSwingEDTGlimpseCanvas createNewtSwingEDTGlimpseCanvasWithPopup( GLProfile glProfile )
    {
        NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( glProfile )
        {
            @Override
            protected MouseWrapperNewt createMouseWrapper( )
            {
                NewtSwingEDTGlimpseCanvas thisCanvas = this;
                return new MouseWrapperNewtSwingEDT( thisCanvas )
                {
                    @Override
                    public boolean mousePressed0( MouseEvent ev )
                    {
                        MenuSelectionManager menuMan = MenuSelectionManager.defaultManager( );
                        boolean isMenuVisible = ( menuMan.getSelectedPath( ) != null && menuMan.getSelectedPath( ).length > 0 );
                        if ( isMenuVisible )
                        {
                            menuMan.clearSelectedPath( );
                            ev.setConsumed( true );
                            return true;
                        }
                        else if ( ev.getButton( ) == BUTTON3 )
                        {
                            GlimpseTargetStack stack = null;
                            GlimpsePopupMenuTarget target = null;
                            for ( GlimpseTargetStack stack0 : this.getContainingTargets( ev ) )
                            {
                                GlimpseTarget target0 = stack0.getTarget( );
                                if ( target0 instanceof GlimpsePopupMenuTarget )
                                {
                                    stack = stack0;
                                    target = ( ( GlimpsePopupMenuTarget ) target0 );
                                    break;
                                }
                            }

                            if ( target != null )
                            {
                                JPopupMenu menu = target.getPopupMenu( );
                                if ( menu != null )
                                {
                                    GlimpseMouseEvent ev2 = this.toGlimpseEvent( ev, stack );
                                    target.getPopupMenuNotifier( ).fire( ev2 );
                                    menu.show( thisCanvas, ev.getX( ), ev.getY( ) );
                                }
                                ev.setConsumed( true );
                                return true;
                            }
                            else
                            {
                                return super.mousePressed0( ev );
                            }
                        }
                        else
                        {
                            return super.mousePressed0( ev );
                        }
                    }
                };
            }
        };

        disableNewtPopupHiding( canvas.getGLWindow( ) );

        return canvas;
    }

    public static Disposable disableNewtPopupHiding( Window window )
    {
        WindowListener listener = new WindowAdapter( )
        {
            @Override
            public void windowGainedFocus( WindowEvent ev )
            {
                ev.setConsumed( true );
            }
        };

        window.addWindowListener( 0, listener );

        return ( ) ->
        {
            window.removeWindowListener( listener );
        };
    }

    public static void hidePopupMenu( )
    {
        MenuSelectionManager menuMan = MenuSelectionManager.defaultManager( );
        boolean isMenuVisible = ( menuMan.getSelectedPath( ) != null && menuMan.getSelectedPath( ).length > 0 );
        if ( isMenuVisible )
        {
            menuMan.clearSelectedPath( );
        }
    }

}
