package com.metsci.glimpse.wizard.tree;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createEtchedBorder;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.WizardErrorType;
import com.metsci.glimpse.wizard.WizardPage;
import com.metsci.glimpse.wizard.WizardUI;
import com.metsci.glimpse.wizard.error.ErrorPopupPanel;

public class WizardUITree<D> implements WizardUI<D>
{
    protected static final int NavigationListWidth = 200;
    protected static final int ErrorPopupHeight = 200;
    protected static final int ErrorPopupWidth = Integer.MAX_VALUE;

    protected JLabel title;
    protected JLabel errorButton;

    protected JScrollPane sidebarScroll;
    protected JList<WizardPage<D>> sidebar;
    protected DefaultListModel<WizardPage<D>> model;

    protected JPanel templateContainer;
    protected JPanel pageContainer;
    protected CardLayout cardLayout;

    protected ErrorPopupPanel<D> errorPopup;

    protected Wizard<D> wizard;

    public WizardUITree( )
    {

    }

    @Override
    public void setWizard( Wizard<D> wizard )
    {
        this.wizard = wizard;

        this.errorButton = new JLabel( );

        this.errorPopup = new ErrorPopupPanel<>( this.wizard, Collections.singleton( this.errorButton ) );

        this.title = new JLabel( );
        this.title.setFont( this.title.getFont( ).deriveFont( 16.0f ) );
        this.title.setBorder( createEmptyBorder( 5, 5, 5, 5 ) );

        this.errorButton.setBorder( createEmptyBorder( 0, 5, 0, 5 ) );
        this.errorButton.setVisible( false );
        this.errorButton.setFocusable( false );
        this.errorButton.addMouseListener( new MouseAdapter( )
        {
            @Override
            public void mousePressed( MouseEvent e )
            {
                if ( !errorPopup.isOpen( ) )
                {
                    Dimension containerSize = pageContainer.getSize( );
                    int height = ( int ) Math.min( containerSize.getHeight( ), ErrorPopupHeight );
                    int width = ( int ) Math.min( containerSize.getWidth( ), ErrorPopupWidth );
                    Dimension popupSize = new Dimension( width, height );
                    Collection<WizardError> errors = wizard.getErrors( wizard.getCurrentPage( ) );
                    errorPopup.showErrorPopup( errorButton, popupSize, errors );
                }
                else
                {
                    errorPopup.hideErrorPopup( );
                }
            }
        } );

        this.model = new DefaultListModel<>( );
        this.sidebar = new JList<>( );
        this.sidebar.setModel( this.model );
        this.sidebar.setCellRenderer( new WizardPageListCellRenderer( this.wizard ) );
        this.sidebar.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        this.sidebar.setFocusable( false );

        this.sidebar.addMouseListener( new MouseAdapter( )
        {
            @Override
            public void mousePressed( MouseEvent e )
            {
                int index = sidebar.locationToIndex( e.getPoint( ) );
                if ( index >= 0 && index < model.getSize( ) )
                {
                    WizardPage<D> page = model.getElementAt( index );
                    wizard.visitPage( page );
                }
            }
        } );

        this.sidebarScroll = new JScrollPane( this.sidebar );
        this.sidebarScroll.setPreferredSize( new Dimension( NavigationListWidth, 0 ) );
        this.sidebarScroll.setBorder( createCompoundBorder( createEmptyBorder( 5, 5, 2, 5 ), createEtchedBorder( EtchedBorder.LOWERED ) ) );

        this.templateContainer = new JPanel( );
        this.templateContainer.setLayout( new BorderLayout( ) );

        this.pageContainer = new JPanel( );
        this.cardLayout = new CardLayout( );
        this.pageContainer.setLayout( this.cardLayout );

        JPanel titleCenterPanel = new JPanel( );
        titleCenterPanel.setLayout( new BorderLayout( ) );
        titleCenterPanel.add( this.title, BorderLayout.CENTER );
        titleCenterPanel.add( this.errorButton, BorderLayout.WEST );

        JPanel titlePanel = new JPanel( );
        titlePanel.setLayout( new BorderLayout( ) );
        titlePanel.add( titleCenterPanel, BorderLayout.CENTER );
        titlePanel.add( new JSeparator( SwingConstants.HORIZONTAL ), BorderLayout.SOUTH );

        JPanel contentPanel = new JPanel( );
        contentPanel.setLayout( new BorderLayout( ) );
        contentPanel.add( titlePanel, BorderLayout.NORTH );
        contentPanel.add( this.pageContainer, BorderLayout.CENTER );

        JPanel sidebarPanel = new JPanel( );
        sidebarPanel.setLayout( new BorderLayout( ) );
        sidebarPanel.add( this.sidebarScroll, BorderLayout.CENTER );

        JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, contentPanel );
        splitPane.setResizeWeight( 0 );
        splitPane.setDividerLocation( 200 );

        this.templateContainer.add( splitPane, BorderLayout.CENTER );
        this.templateContainer.add( new JSeparator( SwingConstants.HORIZONTAL ), BorderLayout.SOUTH );

        this.updatePageTree( );
    }

    @Override
    public Container getContainer( )
    {
        return this.templateContainer;
    }

    @Override
    public void show( WizardPage<D> page )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        this.cardLayout.show( this.pageContainer, page.getId( ).toString( ) );
        this.title.setText( this.getFullName( page ) );
        this.updateErrorButton( page );
        this.sidebar.setSelectedValue( page, true );
    }

    @Override
    public void dispose( )
    {
        // TODO Auto-generated method stub
    }

    protected void updatePageTree( )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        this.model.removeAllElements( );

        for ( WizardPage<D> page : this.wizard.getPageModel( ).getPages( ) )
        {
            this.model.addElement( page );
        }

        WizardPage<D> selectedPage = this.wizard.getCurrentPage( );
        if ( selectedPage != null )
        {
            this.sidebar.setSelectedValue( new WizardTreeNode<>( selectedPage ), true );
        }
        else
        {
            this.sidebar.clearSelection( );
        }

        this.getContainer( ).revalidate( );
        this.getContainer( ).repaint( );
    }

    protected void updateErrorButton( final WizardPage<D> page )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        WizardErrorType maxSeverity = null;

        if ( page != null )
        {
            Collection<WizardError> errors = this.wizard.getErrors( page );
            maxSeverity = WizardErrorType.getMaxSeverity( errors );
        }

        if ( maxSeverity != null )
        {
            this.errorButton.setIcon( maxSeverity.getLargeIcon( ) );
            this.errorButton.setVisible( true );
        }
        else
        {
            this.errorButton.setVisible( false );
        }
    }

    protected String getFullName( WizardPage<D> page )
    {
        StringBuilder b = new StringBuilder( );

        while ( page != null )
        {
            b.append( page.getTitle( ) );

            page = this.wizard.getPageModel( ).getPage( page.getParentId( ) );

            if ( page != null ) b.append( " | " );
        }

        return b.toString( );
    }
}