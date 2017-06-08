package com.metsci.glimpse.wizard.error;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.metsci.glimpse.wizard.WizardError;

public class ErrorTableCellRenderer extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = 1L;

    public Component getTableCellRendererComponent( JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

        // Column 0 contains the ErrorType, which should be displayed as an Icon
        if ( column == 0 )
        {
            WizardError error = ( WizardError ) value;

            this.setIcon( error.getType( ).getSmallIcon( ) );
            this.setText( error.getDescription( ) );
        }

        return this;
    }
}
