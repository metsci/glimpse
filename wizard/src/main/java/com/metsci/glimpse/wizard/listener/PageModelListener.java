package com.metsci.glimpse.wizard.listener;

import java.util.Collection;

public interface PageModelListener
{
    public void onPagesAdded( Collection<Object> addedPageIds );
    public void onPagesRemoved( Collection<Object> removedPageIds );

}
