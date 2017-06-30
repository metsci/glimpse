package com.metsci.glimpse.wizard;

import java.awt.Container;

/**
 * Interface handling the look and feel of a {@link Wizard} dialog. The
 * WizardUI is responsible for displaying the current page as well as any
 * surrounding decorations or buttons.
 * 
 * @author ulman
 */
public interface WizardUI<D>
{
    /**
     * Set the wizard associated with the UI.
     * <p>
     * This method will be called once prior to Swing components associated
     * with this WizardUI being added to a Frame.
     */
    public void setWizard( Wizard<D> wizard );
    
    /**
     * Display the provided page as the current wizard page.
     */
    public void show( WizardPage<D> page );
    
    /**
     * The container holding the Swing components to display the wizard.
     */
    public Container getContainer( );
    
    /**
     * Dispose resources associated with this WizardUI.
     */
    public void dispose( );
}
