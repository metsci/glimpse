package com.metsci.glimpse.wizard.simple.pages;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.page.DescriptionWizardPage;

public class FirstPage extends DescriptionWizardPage<Map<String, Object>>
{
    public FirstPage( )
    {
        super( null, "First Page", "descriptions/Example1.html" );
    }

    @Override
    public void setData( Map<String, Object> data )
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