package com.metsci.glimpse.tinylaf;

import static com.metsci.glimpse.tinylaf.TinyLafUtils.getDesktopTextScaling;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.plaf.ComponentUI;

import net.sf.tinylaf.TinyTableUI;

public class TinyTableUI2 extends TinyTableUI
{
    JTable table;

    public TinyTableUI2( )
    {
        super( );
    }

    public TinyTableUI2( JComponent table )
    {
        super( );
        this.table = ( JTable ) table;
    }

    public static ComponentUI createUI( JComponent table )
    {
        return new TinyTableUI2( table );
    }

    @Override
    protected void installDefaults( )
    {
        super.installDefaults( );

        /*
         * Apparently the default TableUI does not support a property for the row height, so we need to set it directly.
         */
        int defaultHeight = 16;
        table.setRowHeight( ( int ) ( getDesktopTextScaling( ) * defaultHeight ) );
    }
}
