package com.metsci.glimpse.layers;

import static com.jogamp.newt.event.MouseEvent.BUTTON3;

import java.util.List;

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
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.support.swing.MouseWrapperNewtSwingEDT;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.var.Disposable;

/**
 * Functions to support attaching popup menus to Glimpse layouts.
 * <p>
 * Includes workarounds for https://jogamp.org/bugzilla/show_bug.cgi?id=1127.
 */
public class PopupMenuSupport
{

    /**
     * A canvas created by {@link PopupMenuSupport#createNewtSwingEDTGlimpseCanvasWithPopupMenuSupport(GLProfile)}
     * will show popup menus appropriately for GlimpseTargets that implement this interface.
     */
    public interface PopupMenuSupplier
    {
        JPopupMenu getPopupMenu( );
    }

    /**
     * Creates a canvas that shows popup menus for any GlimpseTargets it contains that implement
     * {@link PopupMenuSupplier}.
     * <p>
     * <strong>NOTE:</strong> An application that uses this function to create any canvases should
     * use this method to create <em>all</em> its canvases -- because the canvases created here have
     * to cooperate with each other to hide the popup menu appropriately.
     */
    public static NewtSwingEDTGlimpseCanvas createNewtSwingEDTGlimpseCanvasWithPopupMenuSupport( GLProfile glProfile )
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
                            JPopupMenu menu = getPopupMenu( this.getContainingTargets( ev ) );
                            if ( menu != null )
                            {
                                menu.show( thisCanvas, ev.getX( ), ev.getY( ) );
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

    public static JPopupMenu getPopupMenu( List<GlimpseTargetStack> stacks )
    {
        for ( GlimpseTargetStack stack : stacks )
        {
            GlimpseTarget target = stack.getTarget( );
            if ( target instanceof PopupMenuSupplier )
            {
                JPopupMenu menu = ( ( PopupMenuSupplier ) target ).getPopupMenu( );
                if ( menu != null )
                {
                    return menu;
                }
            }
        }
        return null;
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
