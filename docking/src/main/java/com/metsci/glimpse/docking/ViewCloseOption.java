package com.metsci.glimpse.docking;

public enum ViewCloseOption
{

    /**
     * Do not provide UI elements (e.g. an "x" button) which would allow the user to
     * request that the view be closed.
     */
    VIEW_NOT_CLOSEABLE,

    /**
     * Provide UI elements (e.g. an "x" button) which allow the user to request that
     * the view be closed, and automatically call {@link DockingGroup#closeView(View)}
     * in response to such requests.
     */
    VIEW_AUTO_CLOSEABLE,

    /**
     * Provide UI elements (e.g. an "x" button) which allow the user to request that
     * the view be closed -- but don't automatically call {@link DockingGroup#closeView(View)}.
     * <p>
     * This allows applications to provide custom view-close behavior, by implementing
     * {@link DockingGroupListener#userRequestingCloseView(DockingGroup, View)}, and
     * calling {@link DockingGroup#closeView(View)} as appropriate.
     */
    VIEW_CUSTOM_CLOSEABLE;

}
