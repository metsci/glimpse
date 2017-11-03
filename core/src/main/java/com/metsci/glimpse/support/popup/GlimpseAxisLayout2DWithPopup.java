package com.metsci.glimpse.support.popup;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.util.var.Notifier;

public class GlimpseAxisLayout2DWithPopup extends GlimpseAxisLayout2D implements GlimpsePopupMenuTarget
{

    protected JPopupMenu popupMenu;
    protected Notifier<GlimpseMouseEvent> popupMenuNotifier;


    public GlimpseAxisLayout2DWithPopup( GlimpseLayout parent, String name, Axis2D axis )
    {
        super( parent, name, axis );

        this.popupMenu = new JPopupMenu( )
        {
            @Override
            protected void addImpl( Component c, Object constraints, int index )
            {
                if ( c instanceof JComponent )
                {
                    // If menu items have different values for alignmentX, the menu ends up
                    // ridiculously wide ... for now just left-align everything; maybe make
                    // this alignment configurable in the future
                    ( ( JComponent ) c ).setAlignmentX( 0f );
                }

                super.addImpl( c, constraints, index );
            }
        };

        this.popupMenuNotifier = new Notifier<>( );
    }

    @Override
    public JPopupMenu getPopupMenu( )
    {
        return this.popupMenu;
    }

    @Override
    public Notifier<GlimpseMouseEvent> getPopupMenuNotifier( )
    {
        return this.popupMenuNotifier;
    }

}
