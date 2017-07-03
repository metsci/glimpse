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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.WizardPage;

public class ErrorTablePanel<K> extends JPanel
{
    private static final long serialVersionUID = 1L;

    protected JTable table;
    protected ErrorTableModel model;
    protected Wizard<?> controller;
    protected JScrollPane scroll;

    public ErrorTablePanel( Wizard<K> controller )
    {
        this( controller, true );
    }

    public ErrorTablePanel( Wizard<K> controller, boolean showPage )
    {
        this.controller = controller;
        this.model = new ErrorTableModel( controller, showPage );
        this.table = new JTable( this.model )
        {
            private static final long serialVersionUID = 1L;

            @Override
            public String getToolTipText( MouseEvent event )
            {
                int row = rowAtPoint( event.getPoint( ) );
                row = convertRowIndexToModel( row );
                WizardError error = model.getError( row );
            
                if ( error != null )
                {
                    return error.getDescription( );
                }
                else
                {
                    return null;
                }
            }
        };
        
        this.table.setDefaultRenderer( WizardError.class, new ErrorTableCellRenderer( ) );
        this.scroll = new JScrollPane( this.table );

        this.table.getColumnModel( ).getColumn( 0 ).setMinWidth( 300 );

        // set up sorting

        this.table.setAutoCreateRowSorter( true );

        TableRowSorter<TableModel> sorter = new TableRowSorter<>( table.getModel( ) );
        this.table.setRowSorter( sorter );
        List<RowSorter.SortKey> sortKeys = new ArrayList<>( );
        sortKeys.add( new RowSorter.SortKey( showPage ? 1 : 0, SortOrder.ASCENDING ) );
        sorter.setSortKeys( sortKeys );
        sorter.sort( );

        // add to panel

        this.setLayout( new BorderLayout( ) );
        this.add( this.scroll, BorderLayout.CENTER );

        // navigate to the page associated with the error on double click
        this.table.addMouseListener( new MouseAdapter( )
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                if ( e.getClickCount( ) == 2 )
                {
                    int row = table.rowAtPoint( e.getPoint( ) );

                    if ( row > -1 )
                    {
                        WizardError error = model.getError( table.convertRowIndexToModel( row ) );

                        Object pageId = error.getPageId( );
                        WizardPage<K> page = controller.getPageModel( ).getPageById( pageId );

                        if ( page != null )
                        {
                            controller.visitPage( page );
                        }
                    }
                }
            }
        } );
    }

    public void setErrors( Collection<WizardError> errors )
    {
        this.model.setErrors( errors );
    }
}