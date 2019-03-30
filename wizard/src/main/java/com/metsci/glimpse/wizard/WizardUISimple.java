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
package com.metsci.glimpse.wizard;

import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.wizard.error.ErrorPopupPanel;
import com.metsci.glimpse.wizard.listener.ErrorsUpdatedListener;
import com.metsci.glimpse.wizard.listener.PageEnteredListener;
import com.metsci.glimpse.wizard.listener.PageModelListener;

/**
 * A basic {@link WizardUI} typically used for {@link Wizard}'s
 * that requires data entry occur in a specific page order.
 * 
 * @author furlong
 *
 * @param <D>
 */
public class WizardUISimple<D> implements WizardUI<D>
{
    protected static final int NavigationListPreferredWidth = 200;
    protected static final int ErrorPopupHeight = 200;
    protected static final int ErrorPopupWidth = Integer.MAX_VALUE;

    protected JLabel title;
    protected JLabel errorButton;
    protected boolean displayErrorButton;

    protected JPanel templateContainer;
    protected JPanel pageContainer;
    protected CardLayout cardLayout;

    protected JPanel outerPanel;
    protected JPanel buttonPanel;
    protected JPanel extraButtonPanel;

    protected ErrorPopupPanel<D> errorPopup;

    protected Wizard<D> wizard;

    protected DefaultListModel<WizardPage<D>> model;

    protected PageModelListener<D> modelListener;
    protected PageEnteredListener<D> pageEnteredListener;
    protected ErrorsUpdatedListener errorsUpdatedListener;

    protected JButton prevBtn;
    protected JButton nextBtn;
    protected JButton finishBtn;
    protected JButton cancelBtn;

    public WizardUISimple( boolean displayErrorButton )
    {
        this.displayErrorButton = displayErrorButton;
    }

    public WizardUISimple( )
    {
        this( true );
    }

    @Override
    public void setWizard( Wizard<D> wizard )
    {
        this.wizard = wizard;

        this.errorButton = new JLabel( );

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
                if ( errorPopup == null || !errorPopup.isVisible( ) )
                {
                    errorPopup = new ErrorPopupPanel<>(
                            SwingUtilities.getWindowAncestor( wizard.getCurrentPage( ).getContainter( ) ),
                            wizard,
                            Collections.singletonList( errorButton ) );

                    Dimension containerSize = pageContainer.getSize( );
                    int height = ( int ) Math.min( containerSize.getHeight( ), ErrorPopupHeight );
                    int width = ( int ) Math.min( containerSize.getWidth( ), ErrorPopupWidth );
                    Dimension popupSize = new Dimension( width, height );
                    Collection<WizardError> errors = wizard.getErrors( wizard.getCurrentPage( ) );
                    if ( errors.isEmpty( ) )
                    {
                        errors = Collections.unmodifiableList( Arrays.asList( new WizardError( WizardErrorType.Good, "No Errors Detected." ) ) );
                    }

                    errorPopup.showErrorPopup( errorButton, popupSize, errors );
                }
                else
                {
                    errorPopup.hideErrorPopup( );
                    errorPopup = null;
                }
            }
        } );

        this.model = new DefaultListModel<>( );

        this.templateContainer = new JPanel( );
        this.templateContainer.setLayout( new BorderLayout( ) );

        this.pageContainer = new JPanel( );
        this.cardLayout = new CardLayout( );
        this.pageContainer.setLayout( this.cardLayout );

        this.templateContainer.add( this.createContentComponent( ), BorderLayout.CENTER );
        this.templateContainer.add( new JSeparator( SwingConstants.HORIZONTAL ), BorderLayout.SOUTH );

        this.prevBtn = new JButton( "Back" );
        this.prevBtn.addActionListener( ( e ) -> this.wizard.visitPreviousPage( ) );
        this.nextBtn = new JButton( "Next" );
        this.nextBtn.addActionListener( ( e ) -> this.wizard.visitNextPage( ) );
        this.finishBtn = new JButton( "Finish" );
        this.finishBtn.addActionListener( ( e ) -> this.wizard.finish( ) );
        this.cancelBtn = new JButton( "Cancel" );
        this.cancelBtn.addActionListener( ( e ) -> this.wizard.cancel( ) );

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

        this.udpatePageModel( );

        // update the UI when new pages are added
        this.modelListener = new PageModelListener<D>( )
        {
            @Override
            public void onPagesRemoved( Collection<WizardPage<D>> removedPages )
            {
                removePages( removedPages );
            }

            @Override
            public void onPagesAdded( Collection<WizardPage<D>> addedPages )
            {
                addPages( addedPages );
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
                prevBtn.setEnabled( history.size( ) > 1 );
                // if there is a valid next page, enable the next action
                WizardPageModel<D> pageModel = wizard.getPageModel( );
                D data = wizard.getData( );
                nextBtn.setEnabled( pageModel.getNextPage( history, data ) != null );

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
        assert ( SwingUtilities.isEventDispatchThread( ) );

        return this.outerPanel;
    }

    @Override
    public void show( WizardPage<D> page )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        this.cardLayout.show( this.pageContainer, page.getId( ).toString( ) );
        this.title.setText( this.getFullName( page ) );
        this.updateErrorButton( page );
    }

    @Override
    public void dispose( )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        this.wizard.getPageModel( ).removeListener( this.modelListener );
        this.wizard.removePageEnteredListener( this.pageEnteredListener );
        this.wizard.removeErrorsUpdatedListener( this.errorsUpdatedListener );

        if ( this.errorPopup != null ) this.errorPopup.hideErrorPopup( );
    }

    public void removePages( Collection<WizardPage<D>> removedPages )
    {
        for ( WizardPage<D> page : removedPages )
        {
            pageContainer.remove( page.getContainter( ) );
        }

        this.udpatePageModel( );
    }

    public void addPages( Collection<WizardPage<D>> addedPages )
    {
        for ( WizardPage<D> page : addedPages )
        {
            pageContainer.add( page.getContainter( ), page.getId( ).toString( ) );
        }

        this.udpatePageModel( );
    }

    protected void udpatePageModel( )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        this.model.removeAllElements( );

        for ( WizardPage<D> page : this.wizard.getPageModel( ).getPages( ) )
        {
            this.model.addElement( page );
        }

        this.getContainer( ).revalidate( );
        this.getContainer( ).repaint( );
    }

    protected Component createContentComponent( )
    {
        JPanel titleCenterPanel = new JPanel( );
        titleCenterPanel.setLayout( new BorderLayout( ) );
        titleCenterPanel.add( this.title, BorderLayout.CENTER );
        titleCenterPanel.add( this.errorButton, BorderLayout.WEST );

        JPanel titlePanel = new JPanel( );
        titlePanel.setLayout( new BorderLayout( ) );
        titlePanel.add( titleCenterPanel, BorderLayout.CENTER );
        titlePanel.add( new JSeparator( SwingConstants.HORIZONTAL ), BorderLayout.SOUTH );

        JPanel contentContainer = new JPanel( );
        contentContainer.setLayout( new BorderLayout( ) );
        contentContainer.add( titlePanel, BorderLayout.NORTH );
        contentContainer.add( this.pageContainer, BorderLayout.CENTER );

        return contentContainer;
    }

    protected void updateErrorButton( final WizardPage<D> page )
    {
        assert ( SwingUtilities.isEventDispatchThread( ) );

        if ( page != null && this.displayErrorButton && page.showErrors( ) )
        {
            Collection<WizardError> errors = this.wizard.getErrors( page );
            WizardErrorType maxSeverity = WizardErrorType.getMaxSeverity( errors );
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
        assert ( SwingUtilities.isEventDispatchThread( ) );

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