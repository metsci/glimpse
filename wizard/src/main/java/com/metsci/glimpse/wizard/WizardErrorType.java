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
package com.metsci.glimpse.wizard;

import java.util.Collection;

import javax.swing.ImageIcon;

import com.metsci.glimpse.docking.DockingUtils;

public enum WizardErrorType
{
    Good( "icons/fugue-icon/tick-small-circle.png", "icons/fugue-icon/tick-circle.png" ),
    Info( "icons/fugue-icon/exclamation-small-white.png", "icons/fugue-icon/exclamation-white.png" ),
    Warning( "icons/fugue-icon/exclamation-small.png", "icons/fugue-icon/exclamation.png" ),
    Error( "icons/fugue-icon/exclamation-small-red.png", "icons/fugue-icon/exclamation-red.png" );

    private final String smallIconPath;
    private final String largeIconPath;

    private volatile ImageIcon smallIcon;
    private volatile ImageIcon largeIcon;

    private WizardErrorType( String smallIconPath, String largeIconPath )
    {
        this.smallIconPath = smallIconPath;
        this.largeIconPath = largeIconPath;
    }

    public ImageIcon getSmallIcon( )
    {
        // lazily load icons
        if ( this.smallIcon == null )
        {
            this.smallIcon = DockingUtils.requireIcon( this.smallIconPath );
        }

        return this.smallIcon;
    }

    public ImageIcon getLargeIcon( )
    {
        // lazily load icons
        if ( this.largeIcon == null )
        {
            this.largeIcon = DockingUtils.requireIcon( this.largeIconPath );
        }

        return this.largeIcon;
    }

    public boolean isEqualOrWorse( WizardErrorType error )
    {
        return this.ordinal( ) >= error.ordinal( );
    }

    public static WizardErrorType getMaxSeverity( Collection<WizardError> errors )
    {
        WizardErrorType maxType = Good;

        for ( WizardError error : errors )
        {
            if ( error.getType( ).ordinal( ) > maxType.ordinal( ) )
            {
                maxType = error.getType( );
            }
        }

        return maxType;
    }
}