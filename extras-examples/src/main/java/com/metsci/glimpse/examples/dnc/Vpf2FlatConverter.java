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
package com.metsci.glimpse.examples.dnc;

import static com.google.common.base.Charsets.UTF_8;
import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncDefaultUserFlatDir;
import static com.metsci.glimpse.dnc.convert.Vpf.vpfDatabaseFilesByName;
import static com.metsci.glimpse.dnc.convert.Vpf2Flat.readVpfDatabase;
import static com.metsci.glimpse.dnc.convert.Vpf2Flat.writeFlatDatabase;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.createNewDir;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.startThread;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.takeNewValue;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.examples.dnc.DncExampleUtils.addTextListener;
import static com.metsci.glimpse.examples.dnc.DncExampleUtils.initTinyLaf;
import static com.metsci.glimpse.examples.dnc.DncExampleUtils.setTreeEnabled;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.util.logging.LoggerUtils.initializeLogging;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metsci.glimpse.dnc.convert.Vpf2Flat.Database;
import com.metsci.glimpse.dnc.util.DncMiscUtils.ThrowingRunnable;
import com.metsci.glimpse.dnc.util.SingletonEvictingBlockingQueue;

import gov.nasa.worldwind.formats.vpf.VPFDatabase;
import net.miginfocom.swing.MigLayout;

public class Vpf2FlatConverter
{

    public static void main( String[] args )
    {
        initializeLogging( "dnc-examples/logging.properties" );
        fixPlatformQuirks( );
        initTinyLaf( );


        SwingUtilities.invokeLater( ( ) ->
        {

            // Shared state
            //

            class State
            {
                public File browseDir = new File( System.getProperty( "user.home" ) );
                public Map<String,File> dhtFiles = new LinkedHashMap<>( );
                public Set<String> dbNames = new LinkedHashSet<>( );
            }
            State state = new State( );


            // Components
            //

            JTextField vpfParentField = new JTextField( 32 );
            vpfParentField.setText( "" );

            JButton vpfParentButton = new JButton( requireIcon( "icons/fugue/folder-horizontal-open.png" ) );
            vpfParentButton.setToolTipText( "Browse" );

            JPanel vpfCheckboxesPanel = new JPanel( new MigLayout( "insets 2 4 2 2, gapy 0, wrap 1" ) );

            JScrollPane vpfCheckboxesScroller = new JScrollPane( vpfCheckboxesPanel, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER );
            int vpfCheckboxHeight = ( new JCheckBox( "X" ) ).getPreferredSize( ).height;
            vpfCheckboxesScroller.getVerticalScrollBar( ).setUnitIncrement( vpfCheckboxHeight );
            vpfCheckboxesScroller.setPreferredSize( new Dimension( 100, 5*vpfCheckboxHeight + 4 ) );

            JButton vpfCheckAllButton = new JButton( requireIcon( "icons/fugue/ui-check-box.png" ) );
            vpfCheckAllButton.setToolTipText( "Select All" );

            JButton vpfCheckNoneButton = new JButton( requireIcon( "icons/fugue/ui-check-box-uncheck.png" ) );
            vpfCheckNoneButton.setToolTipText( "Select None" );

            JTextField flatParentField = new JTextField( 32 );
            flatParentField.setText( glimpseDncDefaultUserFlatDir.getAbsolutePath( ) );

            JButton flatParentButton = new JButton( requireIcon( "icons/fugue/folder-horizontal-open.png" ) );
            flatParentButton.setToolTipText( "Browse" );

            JButton convertButton = new JButton( "Convert" );


            // Layout
            //

            JPanel contentPane = new JPanel( new MigLayout( ) );

            contentPane.add( new JLabel( "From:" ), "wrap" );
            contentPane.add( new JLabel( "VPF Parent:" ), "gapleft 8" );
            contentPane.add( vpfParentField, "growx, pushx" );
            contentPane.add( vpfParentButton, "wrap" );

            contentPane.add( new JLabel( "Databases:" ), "gapleft 8" );
            contentPane.add( vpfCheckboxesScroller, "grow, span 1 3" );
            contentPane.add( vpfCheckAllButton, "wrap" );
            contentPane.add( vpfCheckNoneButton, "skip 1, wrap" );
            contentPane.add( new JPanel( ), "pushy, wrap" );

            contentPane.add( new JLabel( "To:" ), "gapy 12, wrap" );
            contentPane.add( new JLabel( "FLAT Parent:" ), "gapleft 8" );
            contentPane.add( flatParentField, "growx, pushx" );
            contentPane.add( flatParentButton, "wrap" );

            contentPane.add( convertButton, "gapy 12, span, split, alignx right, wrap" );

            JFrame frame = new JFrame( "DNC Converter" );
            frame.setContentPane( contentPane );


            // Browse to VPF dir
            //

            vpfParentButton.addActionListener( ( ev ) ->
            {
                JFileChooser chooser = new JFileChooser( );
                chooser.setApproveButtonText( "Select" );
                chooser.setCurrentDirectory( state.browseDir );
                chooser.setDialogTitle( "Select VPF Parent" );
                chooser.setFileSelectionMode( DIRECTORIES_ONLY );
                chooser.setMultiSelectionEnabled( false );
                chooser.setAcceptAllFileFilterUsed( false );

                if ( chooser.showOpenDialog( contentPane ) == APPROVE_OPTION )
                {
                    File dir = chooser.getSelectedFile( );
                    vpfParentField.setText( dir.getPath( ) );

                    state.browseDir = dir;
                }
            } );


            // Load VPF databases list
            //

            BlockingQueue<String> vpfParentPaths = new SingletonEvictingBlockingQueue<>( );

            addTextListener( vpfParentField, ( ) ->
            {
                vpfParentPaths.add( vpfParentField.getText( ) );
            } );

            startThread( "VPF Database Lister", true, new ThrowingRunnable( )
            {
                String oldPath = null;
                public void runThrows( ) throws Exception
                {
                    while ( true )
                    {
                        String newPath = takeNewValue( vpfParentPaths, oldPath );

                        SwingUtilities.invokeAndWait( ( ) ->
                        {
                            vpfCheckboxesPanel.removeAll( );
                            vpfCheckboxesScroller.validate( );
                        } );

                        Map<String,File> dhtFiles = vpfDatabaseFilesByName( new File( newPath ) );

                        SwingUtilities.invokeAndWait( ( ) ->
                        {
                            state.dhtFiles = dhtFiles;
                            state.dbNames = new LinkedHashSet<>( dhtFiles.keySet( ) );

                            dhtFiles.forEach( ( dbName, dhtFile ) ->
                            {
                                JCheckBox checkbox = new JCheckBox( dbName );
                                checkbox.addItemListener( ( ev ) ->
                                {
                                    if ( checkbox.isSelected( ) )
                                    {
                                        state.dbNames.add( dbName );
                                    }
                                    else
                                    {
                                        state.dbNames.remove( dbName );
                                    }
                                } );
                                checkbox.setSelected( true );
                                vpfCheckboxesPanel.add( checkbox );
                            } );
                            vpfCheckboxesScroller.validate( );
                        } );

                        oldPath = newPath;
                    }
                }
            } );


            // Select all checkboxes
            //

            vpfCheckAllButton.addActionListener( ( ev ) ->
            {
                for ( Component c : vpfCheckboxesPanel.getComponents( ) )
                {
                    if ( c instanceof JCheckBox )
                    {
                        ( ( JCheckBox ) c ).setSelected( true );
                    }
                }
            } );


            // De-select all checkboxes
            //

            vpfCheckNoneButton.addActionListener( ( ev ) ->
            {
                for ( Component c : vpfCheckboxesPanel.getComponents( ) )
                {
                    if ( c instanceof JCheckBox )
                    {
                        ( ( JCheckBox ) c ).setSelected( false );
                    }
                }
            } );


            // Browse to FLAT dir
            //

            flatParentButton.addActionListener( ( ev ) ->
            {
                JFileChooser chooser = new JFileChooser( );
                chooser.setApproveButtonText( "Select" );
                chooser.setCurrentDirectory( state.browseDir );
                chooser.setDialogTitle( "Select FLAT Parent" );
                chooser.setFileSelectionMode( DIRECTORIES_ONLY );
                chooser.setMultiSelectionEnabled( false );
                chooser.setAcceptAllFileFilterUsed( false );

                if ( chooser.showSaveDialog( contentPane ) == APPROVE_OPTION )
                {
                    File dir = chooser.getSelectedFile( );
                    flatParentField.setText( dir.getPath( ) );

                    state.browseDir = dir;
                }
            } );


            // Convert button
            //

            convertButton.addActionListener( ( ev ) ->
            {
                File flatParentDir = new File( flatParentField.getText( ) );
                Map<String,File> dhtFiles = ImmutableMap.copyOf( state.dhtFiles );
                Set<String> dbNames = ImmutableSet.copyOf( state.dbNames );

                setTreeEnabled( contentPane, false );

                JFrame progressFrame = new JFrame( "DNC Conversion" );
                ProgressPane progressPane = new ProgressPane( "Converting" );
                progressFrame.setContentPane( progressPane );
                progressFrame.setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
                progressFrame.pack( );
                progressFrame.setResizable( false );
                progressFrame.setLocationRelativeTo( frame );
                progressFrame.setVisible( true );

                // Worldwind's VPF reader doesn't handle interrupts well, so this is the best we can do
                AtomicBoolean cancelled = new AtomicBoolean( false );
                class CancellationException extends RuntimeException { }
                Runnable checkForCancellation = ( ) ->
                {
                    if ( cancelled.get( ) )
                    {
                        throw new CancellationException( );
                    }
                };

                startThread( "Converter", false, ( ) ->
                {
                    try
                    {
                        if ( dbNames.isEmpty( ) )
                        {
                            SwingUtilities.invokeLater( ( ) ->
                            {
                                progressFrame.dispose( );
                                showMessageDialog( frame, "No VPF databases to convert", "Error", ERROR_MESSAGE );
                                setTreeEnabled( contentPane, true );
                            } );
                        }
                        else
                        {
                            int stepsComplete = 0;
                            flatParentDir.mkdirs( );
                            for ( String dbName : dbNames )
                            {
                                File dhtFile = dhtFiles.get( dbName );
                                checkForCancellation.run( );
                                VPFDatabase vpfDatabase = VPFDatabase.fromFile( dhtFile.getPath( ) );
                                checkForCancellation.run( );
                                Database database = readVpfDatabase( vpfDatabase );
                                checkForCancellation.run( );

                                stepsComplete++;
                                double fracCompleteA = stepsComplete / ( 2.0*dbNames.size( ) );
                                SwingUtilities.invokeAndWait( ( ) -> progressPane.setProgress( fracCompleteA ) );

                                String dirname = vpfDatabase.getName( ).toLowerCase( ).replace( "dnc", "dncflat" );
                                checkForCancellation.run( );
                                File flatDir = createNewDir( flatParentDir, dirname );
                                checkForCancellation.run( );
                                writeFlatDatabase( database, flatDir, UTF_8 );
                                checkForCancellation.run( );

                                stepsComplete++;
                                double fracCompleteB = stepsComplete / ( 2.0*dbNames.size( ) );
                                SwingUtilities.invokeAndWait( ( ) -> progressPane.setProgress( fracCompleteB ) );
                            }

                            SwingUtilities.invokeLater( ( ) ->
                            {
                                progressFrame.dispose( );
                                showMessageDialog( frame, "Conversion succeeded", "Success", INFORMATION_MESSAGE );
                                setTreeEnabled( contentPane, true );
                            } );
                        }
                    }
                    catch ( CancellationException e )
                    {
                        SwingUtilities.invokeLater( ( ) ->
                        {
                            progressFrame.dispose( );
                            showMessageDialog( frame, "Conversion cancelled", "Cancelled", INFORMATION_MESSAGE );
                            setTreeEnabled( contentPane, true );
                        } );
                    }
                    catch ( Exception e )
                    {
                        SwingUtilities.invokeLater( ( ) ->
                        {
                            progressFrame.dispose( );

                            JLabel summaryLabel = new JLabel( "Conversion failed" );

                            JTextArea detailArea = new JTextArea( );
                            StringWriter detailText = new StringWriter( );
                            e.printStackTrace( new PrintWriter( detailText ) );
                            detailArea.setText( detailText.toString( ) );
                            detailArea.setCaretPosition( 0 );

                            JScrollPane detailScroller = new JScrollPane( detailArea, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED );

                            JPanel messagePanel = new JPanel( new MigLayout( ) );
                            messagePanel.add( summaryLabel, "wrap" );
                            messagePanel.add( detailScroller, "height ::250" );

                            showMessageDialog( frame, messagePanel, "Error", ERROR_MESSAGE );
                            setTreeEnabled( contentPane, true );
                        } );
                    }
                } );

                progressPane.addCancelListener( ( ) ->
                {
                    cancelled.set( true );
                } );
            } );


            // Show frame
            //

            frame.setDefaultCloseOperation( DISPOSE_ON_CLOSE );
            frame.pack( );
            frame.setMinimumSize( frame.getPreferredSize( ) );
            frame.setLocationRelativeTo( null );
            frame.setVisible( true );

        } );
    }

}
