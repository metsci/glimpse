/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.wizard.error;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.metsci.glimpse.wizard.WizardError;

public class ErrorTableCellRenderer implements TableCellRenderer
{
    protected DefaultTableCellRenderer deleagate;
    protected JTextArea text;
    protected JLabel icon;
    protected JPanel background;
    
    public ErrorTableCellRenderer( )
    {
        this.deleagate = new DefaultTableCellRenderer( );
        this.background = new JPanel( );
        this.text = new JTextArea( );
        this.icon = new JLabel( );
        this.icon.setOpaque( true );
        
        this.background.setLayout( new BorderLayout( ) );
        
        this.text.setLineWrap(true);
        this.text.setWrapStyleWord(true);
    
        this.background.add( this.text, BorderLayout.CENTER );
        this.background.add( this.icon, BorderLayout.WEST );
    }
    
    public Component getTableCellRendererComponent( JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column )
    {
        this.deleagate.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

        this.text.setBackground( this.deleagate.getBackground( ) );
        this.text.setForeground( this.deleagate.getForeground( ) );
        this.text.setFont( this.deleagate.getFont( ) );
        
        this.icon.setBackground( this.deleagate.getBackground( ) );
        this.icon.setForeground( this.deleagate.getForeground( ) );

        this.background.setBorder( this.deleagate.getBorder( ) );
        
        // Column 0 contains the ErrorType, which should be displayed as an Icon
        if ( column == 0 )
        {
            WizardError error = ( WizardError ) value;

            this.icon.setIcon( error.getType( ).getSmallIcon( ) );
            this.text.setText( error.getDescription( ) );
        }
        
        this.background.setSize( table.getColumnModel( ).getColumn( column ).getWidth( ), this.text.getPreferredSize( ).height );
        if ( table.getRowHeight( row ) != this.text.getPreferredSize( ).height )
        {
            table.setRowHeight( row, this.text.getPreferredSize( ).height );
        }

        return this.background;
    }
}
