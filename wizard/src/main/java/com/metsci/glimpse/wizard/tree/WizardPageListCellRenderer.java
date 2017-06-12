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

    public WizardPageListCellRenderer( Wizard<?> wizard )
    {
        this.wizard = wizard;
    }

    protected int getLevel( WizardPage<?> page )
    {
        WizardPageModel<?> model = this.wizard.getPageModel( );

        int level = 0;

        while ( page != null )
        {
            page = model.getPage( page.getParentId( ) );
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
        String buffer = StringUtils.repeat( "  ", getLevel( page ) - 1 );
        setText( buffer + text );

        setPreferredSize( new Dimension( 0, 25 ) );

        JPanel background = new JPanel( );
        copySettings( label, background );
        background.setOpaque( true );

        background.setLayout( new MigLayout( "insets 0, gap 0" ) );
        background.add( label, "pushx, growx" );

        Collection<WizardError> errors = wizard.getErrors( page );

        if ( !errors.isEmpty( ) )
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
