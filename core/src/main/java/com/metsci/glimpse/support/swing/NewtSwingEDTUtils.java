package com.metsci.glimpse.support.swing;

import static com.metsci.glimpse.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.DEFINITELY_BLOCKED;
import static com.metsci.glimpse.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.DEFINITELY_NOT_BLOCKED;
import static com.metsci.glimpse.support.swing.NewtSwingEDTUtils.ModalBlockedStatus.UNKNOWN;
import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.Component;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.media.nativewindow.NativeWindow;

import com.jogamp.nativewindow.awt.JAWTWindow;
import com.jogamp.newt.event.InputEvent;

public class NewtSwingEDTUtils
{

    public static enum ModalBlockedStatus
    {
        DEFINITELY_BLOCKED, DEFINITELY_NOT_BLOCKED, UNKNOWN
    }

    /**
     * <strong>WARNING:</strong> This method relies on implementation details of both
     * NEWT and AWT. If for any reason it cannot determine whether the event is modal-
     * blocked, it returns {@link ModalBlockedStatus#UNKNOWN}. Client code should be
     * written with this possibility in mind.
     */
    public static ModalBlockedStatus getModalBlockedStatus( InputEvent ev )
    {
        // TODO: Does JOGL always use the window as the event source?
        Object source = ev.getSource( );
        if ( source instanceof NativeWindow )
        {
            // TODO: Does JOGL's window hierarchy always follow this pattern?
            NativeWindow parent = ( ( NativeWindow ) source ).getParent( );
            if ( parent instanceof JAWTWindow )
            {
                Component c = ( ( JAWTWindow ) parent ).getAWTComponent( );
                if ( c != null )
                {
                    Window w = getWindowAncestor( c );
                    if ( w != null )
                    {
                        return getModalBlockedStatus( w );
                    }
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * <strong>WARNING:</strong> This method relies on implementation details of both
     * NEWT and AWT. If for any reason it cannot determine whether the event is modal-
     * blocked, it returns {@link ModalBlockedStatus#UNKNOWN}. Client code should be
     * written with this possibility in mind.
     */
    public static ModalBlockedStatus getModalBlockedStatus( Window w )
    {
        try
        {
            Method method = Window.class.getDeclaredMethod( "isModalBlocked" );
            method.setAccessible( true );
            Object result = method.invoke( w );
            if ( result instanceof Boolean )
            {
                return ( ( Boolean ) result ? DEFINITELY_BLOCKED : DEFINITELY_NOT_BLOCKED );
            }
            else
            {
                return UNKNOWN;
            }
        }
        catch ( Exception e )
        {
            return UNKNOWN;
        }
    }

}
