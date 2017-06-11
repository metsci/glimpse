package com.metsci.glimpse.wizard.simple;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;

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
        
        WizardPageModel<Map<String, Object>> model = wizard.getPageModel( );
        
        model.addPage( new FirstPage( ) );
        model.addPage( new SecondPage( ) );
        model.addPage( new ThirdPage( ) );
        model.addPage( new ChildFirstPage( ) );

        JDialog dialog = new JDialog( null, "Wizard Example", APPLICATION_MODAL );
        dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

        dialog.setSize( 800, 700 );
        dialog.setLocationRelativeTo( null );

        dialog.add( wizard.getUI( ).getContainer( ) );
        dialog.setVisible( true );

        dialog.dispose( );
    }
}
