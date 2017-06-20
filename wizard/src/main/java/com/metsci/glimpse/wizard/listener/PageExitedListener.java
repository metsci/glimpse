package com.metsci.glimpse.wizard.listener;

import com.metsci.glimpse.wizard.WizardPage;

@FunctionalInterface
public interface PageExitedListener<D>
{
    public void onPageExited( WizardPage<D> page );
}
