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
package com.metsci.glimpse.wizard.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;

import com.metsci.glimpse.util.StringUtils;
import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.WizardErrorType;
import com.metsci.glimpse.wizard.WizardPage;
import com.metsci.glimpse.wizard.WizardPageModel;

import net.miginfocom.swing.MigLayout;

public class WizardPageListCellRenderer extends DefaultListCellRenderer
{
    private static final long serialVersionUID = 1L;

    protected Wizard<?> wizard;
    protected String indentString;

    public WizardPageListCellRenderer( Wizard<?> wizard )
    {
        this( wizard, "    " );
    }

    public WizardPageListCellRenderer( Wizard<?> wizard, String indentString )
    {
        this.wizard = wizard;
        this.indentString = indentString;
    }

    protected int getLevel( WizardPage<?> page )
    {
        WizardPageModel<?> model = this.wizard.getPageModel( );

        int level = 0;

        while ( page != null )
        {
            page = model.getPageById( page.getParentId( ) );
            level++;
        }

        return level;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus )
    {
        JLabel label = ( JLabel ) super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

        WizardPage<?> page = ( WizardPage<?> ) value;

        String text = page.getTitle( );
        // subtract 1 to make the children of the root node level 0
        String buffer = StringUtils.repeat( this.indentString, getLevel( page ) - 1 );
        setText( buffer + text );

        setPreferredSize( new Dimension( 0, 25 ) );

        JPanel background = new JPanel( );
        copySettings( label, background );
        background.setOpaque( true );

        background.setLayout( new MigLayout( "insets 0, gap 0" ) );
        background.add( label, "pushx, growx" );

        Collection<WizardError> errors = wizard.getErrors( page );

        if ( !errors.isEmpty( ) && page.showErrors( ) )
        {
            WizardErrorType type = WizardErrorType.getMaxSeverity( errors );
            ImageIcon errorIcon = type.getSmallIcon( );

            JLabel errorIconLabel = new JLabel( errorIcon );
            JLabel errorCountLabel = new JLabel( String.valueOf( errors.size( ) ) );

            copySettings( label, errorIconLabel );
            copySettings( label, errorCountLabel );

            background.add( errorIconLabel );
            background.add( errorCountLabel );
        }

        return background;
    }

    protected void copySettings( JComponent from, JComponent to )
    {
        to.setForeground( from.getForeground( ) );
        to.setBackground( from.getBackground( ) );
        to.setEnabled( from.isEnabled( ) );
        to.setFont( from.getFont( ) );
        to.setOpaque( from.isOpaque( ) );
    }

    protected int getLevel( TreeNode node )
    {
        int level = 0;

        while ( node != null )
        {
            node = node.getParent( );
            level++;
        }

        return level;
    }
}
