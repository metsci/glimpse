package com.metsci.glimpse.wizard;


import java.util.Collection;

import javax.swing.ImageIcon;

import com.metsci.glimpse.docking.DockingUtils;

public enum WizardErrorType
{
    Error( DockingUtils.requireIcon( "icons/fugue-icon/exclamation-small-red.png" ), DockingUtils.requireIcon( "icons/fugue-icon/exclamation-red.png" ) ),
    Warning( DockingUtils.requireIcon( "icons/fugue-icon/exclamation-small.png" ), DockingUtils.requireIcon( "icons/fugue-icon/exclamation.png" ) ),
    Info( DockingUtils.requireIcon( "icons/fugue-icon/exclamation-small-white.png" ), DockingUtils.requireIcon( "icons/fugue-icon/exclamation-white.png" ) );
    
    private ImageIcon smallIcon;
    private ImageIcon largeIcon;
    
    private WizardErrorType( ImageIcon smallIcon, ImageIcon largeIcon )
    {
        this.smallIcon = smallIcon;
        this.largeIcon = largeIcon;
    }
    
    public ImageIcon getSmallIcon( )
    {
        return this.smallIcon;
    }
    
    public ImageIcon getLargeIcon( )
    {
        return this.largeIcon;
    }
    
    public static WizardErrorType getMaxSeverity( Collection<WizardError> errors )
    {
        WizardErrorType maxType = null;

        for ( WizardError error : errors )
        {
            if ( error.getType( ) == WizardErrorType.Error )
            {
                return WizardErrorType.Error;
            }
            else if ( error.getType( ) == WizardErrorType.Warning )
            {
                maxType = WizardErrorType.Warning;
            }
            else if ( maxType == null )
            {
                maxType = WizardErrorType.Info;
            }
        }

        return maxType;
    }
}