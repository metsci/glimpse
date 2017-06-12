package com.metsci.glimpse.wizard.tree;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createEtchedBorder;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import com.metsci.glimpse.wizard.WizardPageModel;
import com.metsci.glimpse.wizard.WizardUI;
import com.metsci.glimpse.wizard.error.ErrorPopupPanel;
import com.metsci.glimpse.wizard.listener.ErrorsUpdatedListener;
import com.metsci.glimpse.wizard.listener.PageEnteredListener;
import com.metsci.glimpse.wizard.listener.PageModelListener;

public class WizardUITree<D> implements WizardUI<D>
{
    protected static final int NavigationListPreferredWidth = 200;
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

    protected JPanel outerPanel;
    protected JPanel buttonPanel;
    protected JPanel extraButtonPanel;

    protected ErrorPopupPanel<D> errorPopup;

    protected Wizard<D> wizard;

    protected PageModelListener<D> modelListener;
    protected PageEnteredListener<D> pageEnteredListener;
    protected ErrorsUpdatedListener errorsUpdatedListener;

    private final AbstractAction prevAction = new AbstractAction( "Back" )
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed( ActionEvent e )
        {
            WizardUITree.this.wizard.visitPreviousPage( );
        }
    };

    private final AbstractAction nextAction = new AbstractAction( "Next" )
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed( ActionEvent e )
        {
            WizardUITree.this.wizard.visitNextPage( );
        }
    };

    private final AbstractAction finishAction = new AbstractAction( "Finish" )
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed( ActionEvent e )
        {
            WizardUITree.this.wizard.finish( );
        }
    };

    private final AbstractAction cancelAction = new AbstractAction( "Cancel" )
    {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed( ActionEvent e )
        {
            WizardUITree.this.wizard.cancel( );
        }
    };

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
                if ( !WizardUITree.this.errorPopup.isOpen( ) )
                {
                    Dimension containerSize = WizardUITree.this.pageContainer.getSize( );
                    int height = ( int ) Math.min( containerSize.getHeight( ), ErrorPopupHeight );
                    int width = ( int ) Math.min( containerSize.getWidth( ), ErrorPopupWidth );
                    Dimension popupSize = new Dimension( width, height );
                    Collection<WizardError> errors = wizard.getErrors( wizard.getCurrentPage( ) );
                    WizardUITree.this.errorPopup.showErrorPopup( WizardUITree.this.errorButton, popupSize, errors );
                }
                else
                {
                    WizardUITree.this.errorPopup.hideErrorPopup( );
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
                int index = WizardUITree.this.sidebar.locationToIndex( e.getPoint( ) );
                if ( index >= 0 && index < WizardUITree.this.model.getSize( ) )
                {
                    WizardPage<D> page = WizardUITree.this.model.getElementAt( index );
                    wizard.visitPage( page );
                }
            }
        } );

        this.sidebarScroll = new JScrollPane( this.sidebar );
        this.sidebarScroll.setPreferredSize( new Dimension( NavigationListPreferredWidth, 0 ) );
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

        final JButton prevBtn = new JButton( this.prevAction );
        final JButton nextBtn = new JButton( this.nextAction );
        final JButton finishBtn = new JButton( this.finishAction );
        final JButton cancelBtn = new JButton( this.cancelAction );

        this.extraButtonPanel = new JPanel( );
        this.extraButtonPanel.setLayout( new BoxLayout( this.extraButtonPanel, BoxLayout.LINE_AXIS ) );

        this.buttonPanel = new JPanel( );
        this.buttonPanel.setLayout( new BoxLayout( this.buttonPanel, BoxLayout.LINE_AXIS ) );
        this.buttonPanel.add( this.extraButtonPanel );
        this.buttonPanel.add( Box.createHorizontalGlue( ) );
        this.buttonPanel.add( prevBtn );
        this.buttonPanel.add( Box.createHorizontalStrut( 5 ) );
        this.buttonPanel.add( nextBtn );
        this.buttonPanel.add( Box.createHorizontalStrut( 10 ) );
        this.buttonPanel.add( finishBtn );
        this.buttonPanel.add( Box.createHorizontalStrut( 10 ) );
        this.buttonPanel.add( cancelBtn );

        this.buttonPanel.setBorder( BorderFactory.createEmptyBorder( 5, 15, 5, 15 ) );

        this.outerPanel = new JPanel( );
        this.outerPanel.setLayout( new BorderLayout( ) );

        this.outerPanel.add( this.templateContainer, BorderLayout.CENTER );
        this.outerPanel.add( this.buttonPanel, BorderLayout.SOUTH );

        this.updatePageTree( );

        // update the UI when new pages are added
        this.modelListener = new PageModelListener<D>( )
        {
            @Override
            public void onPagesRemoved( Collection<WizardPage<D>> removedPages )
            {
                SwingUtilities.invokeLater( ( ) ->
                {
                    for ( WizardPage<D> page : removedPages )
                    {
                        pageContainer.remove( page.getContainter( ) );
                    }

                    updatePageTree( );
                } );
            }

            @Override
            public void onPagesAdded( Collection<WizardPage<D>> addedPages )
            {
                SwingUtilities.invokeLater( ( ) ->
                {
                    for ( WizardPage<D> page : addedPages )
                    {
                        pageContainer.add( page.getContainter( ), page.getId( ).toString( ) );
                    }

                    updatePageTree( );
                } );
            }
        };

        this.wizard.getPageModel( ).addListener( this.modelListener );

        this.pageEnteredListener = new PageEnteredListener<D>( )
        {
            @Override
            public void onPageEntered( WizardPage<D> page )
            {
                LinkedList<WizardPage<D>> history = wizard.getPageHistory( );

                // if there is no history, disable the ability to move backward
                prevAction.setEnabled( history.size( ) > 1 );
                // if there is a valid next page, enable the next action
                WizardPageModel<D> pageModel = wizard.getPageModel( );
                D data = wizard.getData( );
                nextAction.setEnabled( pageModel.getNextPage( history, data ) != null );

                getContainer( ).revalidate( );
                getContainer( ).repaint( );
            }
        };

        this.wizard.addPageEnteredListener( this.pageEnteredListener );

        this.errorsUpdatedListener = new ErrorsUpdatedListener( )
        {
            @Override
            public void onErrorsUpdated( )
            {
                getContainer( ).revalidate( );
                getContainer( ).repaint( );

                updateErrorButton( wizard.getCurrentPage( ) );
            }
        };

        this.wizard.addErrorsUpdatedListener( this.errorsUpdatedListener );
    }

    @Override
    public Container getContainer( )
    {
        return this.outerPanel;
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
        this.wizard.getPageModel( ).removeListener( this.modelListener );
        this.wizard.removePageEnteredListener( this.pageEnteredListener );
        this.wizard.removeErrorsUpdatedListener( this.errorsUpdatedListener );
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
            this.sidebar.setSelectedValue( selectedPage, true );
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

            page = this.wizard.getPageModel( ).getPageById( page.getParentId( ) );

            if ( page != null ) b.append( " | " );
        }

        return b.toString( );
    }
}