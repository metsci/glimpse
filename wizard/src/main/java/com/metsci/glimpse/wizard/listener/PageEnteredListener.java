package com.metsci.glimpse.wizard.listener;

import com.metsci.glimpse.wizard.WizardPage;

@FunctionalInterface
public interface PageEnteredListener<D>
{
    public void onPageEntered( WizardPage<D> page );
}
