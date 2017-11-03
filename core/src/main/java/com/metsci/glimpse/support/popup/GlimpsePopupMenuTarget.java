package com.metsci.glimpse.support.popup;

import javax.swing.JPopupMenu;

import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.util.var.Notifier;

/**
 * If a {@link GlimpseTarget} that implements this interface is added to a {@link NewtSwingEDTGlimpseCanvas},
 * then right-clicking on that target will show the popup-menu supplied by {@link #getPopupMenu()}.
 */
public interface GlimpsePopupMenuTarget
{

    JPopupMenu getPopupMenu( );

    /**
     * A notifier that will fire just before the popup menu becomes visible.
     */
    Notifier<GlimpseMouseEvent> getPopupMenuNotifier( );

}
