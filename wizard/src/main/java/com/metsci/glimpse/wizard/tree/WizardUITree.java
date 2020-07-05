/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.wizard.tree;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createEtchedBorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardPage;
import com.metsci.glimpse.wizard.WizardUISimple;

public class WizardUITree<D> extends WizardUISimple<D>
{
    protected JScrollPane sidebarScroll;
    protected JList<WizardPage<D>> sidebar;

    public WizardUITree( boolean displayErrorButton )
    {
        super( displayErrorButton );
    }

    public WizardUITree( )
    {
        this( true );
    }

    @Override
    public void setWizard( Wizard<D> wizard )
    {
        super.setWizard( wizard );

        this.updatePageTree( );
    }

    @Override
    public void show( WizardPage<D> page )
    {
        super.show( page );
        this.sidebar.setSelectedValue( page, true );
    }

    public void removePages( Collection<WizardPage<D>> removedPages )
    {
        super.removePages( removedPages );

        this.updatePageTree( );
    }

    public void addPages( Collection<WizardPage<D>> addedPages )
    {
        super.addPages( addedPages );

        this.updatePageTree( );
    }

    protected void updatePageTree( )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        WizardPage<D> selectedPage = this.wizard.getCurrentPage( );
        if ( selectedPage != null )
        {
            this.sidebar.setSelectedValue( selectedPage, true );
        }
        else
        {
            this.sidebar.clearSelection( );
        }

        this.getContainer( ).revalidate( );
        this.getContainer( ).repaint( );
    }

    protected Component createContentComponent( )
    {
        Component parentContainer = super.createContentComponent( );

        this.sidebar = new JList<>( );
        this.sidebar.setModel( this.model );
        this.sidebar.setCellRenderer( new WizardPageListCellRenderer( this.wizard, this.getChildIndentString( ) ) );
        this.sidebar.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        this.sidebar.setFocusable( false );

        this.sidebar.addListSelectionListener( new ListSelectionListener( )
        {
            @Override
            public void valueChanged( ListSelectionEvent lsEvent )
            {
                if ( !lsEvent.getValueIsAdjusting( ) )
                {
                    int index = WizardUITree.this.sidebar.getSelectedIndex( );
                    if ( index >= 0 && index < WizardUITree.this.model.getSize( ) )
                    {
                        WizardPage<D> page = WizardUITree.this.model.getElementAt( index );
                        wizard.visitPage( page );
                    }
                }
            }
        } );

        this.sidebarScroll = new JScrollPane( this.sidebar );
        this.sidebarScroll.setPreferredSize( new Dimension( NavigationListPreferredWidth, 0 ) );
        this.sidebarScroll.setBorder( createCompoundBorder( createEmptyBorder( 5, 5, 2, 5 ), createEtchedBorder( EtchedBorder.LOWERED ) ) );

        JPanel sidebarPanel = new JPanel( );
        sidebarPanel.setLayout( new BorderLayout( ) );
        sidebarPanel.add( this.sidebarScroll, BorderLayout.CENTER );

        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, parentContainer );
        splitPane.setResizeWeight( 0 );
        splitPane.setDividerLocation( 200 );

        return splitPane;
    }

    // the string to use to indent child tree elements
    protected String getChildIndentString( )
    {
        return "    ";
    }
}