package com.metsci.glimpse.wizard.simple.pages;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.JLabel;

import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.page.DescriptionWizardPage;

public class ThirdPage extends DescriptionWizardPage<Map<String, Object>>
{
    public ThirdPage( )
    {
        super( null, "Third Page", "descriptions/Example1.html" );
        
        this.container.add( new JLabel( "Label" ) );
    }

    @Override
    public void setData( Map<String, Object> data, boolean force )
    {
        // do nothing
    }

    @Override
    public Map<String, Object> updateData( Map<String, Object> data )
    {
        return data;
    }

    @Override
    public Collection<WizardError> getErrors( )
    {
        return Collections.emptyList( );
    }
}