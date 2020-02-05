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
package com.metsci.glimpse.examples.wizard.simple;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.metsci.glimpse.examples.wizard.simple.pages.ChildFirstPage;
import com.metsci.glimpse.examples.wizard.simple.pages.FirstPage;
import com.metsci.glimpse.examples.wizard.simple.pages.SecondPage;
import com.metsci.glimpse.examples.wizard.simple.pages.ThirdPage;
import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardPageModel;
import com.metsci.glimpse.wizard.WizardPageModelSimple;
import com.metsci.glimpse.wizard.WizardUISimple;

public class WizardSimpleExample
{
    public static void main( String[] args )
    {
        Wizard<Map<String, Object>> wizard = new Wizard<Map<String, Object>>( null, new WizardPageModelSimple<>( ), new WizardUISimple<>( ) );

        // add some pages to the model
        WizardPageModel<Map<String, Object>> model = wizard.getPageModel( );
        model.addPage( new FirstPage( ) );
        model.addPage( new SecondPage( ) );
        model.addPage( new ThirdPage( ) );
        model.addPage( new ChildFirstPage( ) );

        // visit the first page
        wizard.visitNextPage( );

        // add the wizard to a dialog
        JDialog dialog = new JDialog( null, "Wizard Example", APPLICATION_MODAL );
        dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        dialog.setSize( 800, 700 );
        dialog.setLocationRelativeTo( null );
        dialog.add( wizard.getUI( ).getContainer( ) );

        // add listeners to close the dialog when the user hits cancel or finish
        wizard.addCancelledListener( ( ) -> {
            dialog.dispose( );
        } );

        wizard.addFinishedListener( ( ) -> {
            dialog.dispose( );
        } );

        // show the dialog
        dialog.setVisible( true );

        // dispose the wizard when the dialog closes
        dialog.addWindowListener( new WindowAdapter( )
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                wizard.dispose( );
            }
        } );
    }
}
