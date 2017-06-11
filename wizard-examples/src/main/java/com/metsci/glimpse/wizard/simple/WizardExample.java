package com.metsci.glimpse.wizard.simple;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardPageModel;
import com.metsci.glimpse.wizard.simple.pages.ChildFirstPage;
import com.metsci.glimpse.wizard.simple.pages.FirstPage;
import com.metsci.glimpse.wizard.simple.pages.SecondPage;
import com.metsci.glimpse.wizard.simple.pages.ThirdPage;

public class WizardExample
{
    public static void main( String[] args )
    {
        Wizard<Map<String, Object>> wizard = new Wizard<Map<String, Object>>( );
        
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
        wizard.addCancelledListener( ( )->
        {
           dialog.dispose( );
        });
        
        wizard.addFinishedListener( ( )->
        {
           dialog.dispose( );
        });
        
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
        });
    }
}
