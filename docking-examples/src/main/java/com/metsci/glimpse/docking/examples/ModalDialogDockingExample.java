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
package com.metsci.glimpse.docking.examples;

import static com.metsci.glimpse.core.support.QuickUtils.initStandardGlimpseApp;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.setArrangementAndSaveOnDispose;
import static com.metsci.glimpse.docking.DockingWindowTitlers.createDefaultWindowTitler;
import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_NOT_CLOSEABLE;
import static com.metsci.glimpse.docking.examples.SimpleDockingExample.newSolidPanel;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;
import static java.awt.Color.blue;
import static java.awt.Color.cyan;
import static java.awt.Color.gray;
import static java.awt.Color.green;
import static java.awt.Color.magenta;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static java.awt.Color.yellow;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.dialog.DockingGroupDialog;

public class ModalDialogDockingExample
{

    public static void main( String[] args ) throws Exception
    {
        // Initialize the GUI on the Swing thread, to avoid graphics-driver coredumps on shutdown
        SwingUtilities.invokeLater( ( ) ->
        {
            initTinyLaf( );
            initStandardGlimpseApp( );


            // Create view components
            //

            JPanel aPanel = newSolidPanel( red );
            JPanel bPanel = newSolidPanel( green );
            JPanel cPanel = newSolidPanel( blue );
            JPanel dPanel = newSolidPanel( cyan );
            JPanel ePanel = newSolidPanel( magenta );
            JPanel fPanel = newSolidPanel( yellow );
            JPanel gPanel = newSolidPanel( gray );
            JPanel hPanel = newSolidPanel( white );


            // Create views
            //

            View[] views =
            {
                new View( "aView", aPanel, "View A", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewA.png" ) ) ),
                new View( "bView", bPanel, "View B", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewB.png" ) ) ),
                new View( "cView", cPanel, "View C", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewC.png" ) ) ),
                new View( "dView", dPanel, "View D", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewD.png" ) ) ),
                new View( "eView", ePanel, "View E", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewE.png" ) ) ),
                new View( "fView", fPanel, "View F", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewF.png" ) ) ),
                new View( "gView", gPanel, "View G", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewG.png" ) ) ),
                new View( "hView", hPanel, "View H", VIEW_NOT_CLOSEABLE, null, requireIcon( ModalDialogDockingExample.class.getResource( "icons/ViewH.png" ) ) ),
            };


            // Create and show the docking group
            //

            String appName = "modal-dialog-docking-example";
            DockingGroup dockingGroup = new DockingGroupDialog( null, APPLICATION_MODAL, DISPOSE_ALL_FRAMES );
            dockingGroup.addListener( createDefaultWindowTitler( "Docking Example" ) );
            setArrangementAndSaveOnDispose( dockingGroup, appName, ModalDialogDockingExample.class.getResource( "docking/simple-arrangement-default.xml" ) );

            dockingGroup.addViews( views );
            dockingGroup.setVisible( true );

        } );
    }

}
