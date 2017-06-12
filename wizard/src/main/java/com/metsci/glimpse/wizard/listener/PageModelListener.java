package com.metsci.glimpse.wizard.listener;

import java.util.Collection;

import com.metsci.glimpse.wizard.WizardPage;

public interface PageModelListener<D>
{
    public void onPagesAdded( Collection<WizardPage<D>> addedPages );
    public void onPagesRemoved( Collection<WizardPage<D>> removedPages );

}
