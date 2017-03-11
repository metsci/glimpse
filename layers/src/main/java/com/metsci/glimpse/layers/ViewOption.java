package com.metsci.glimpse.layers;

/**
 * An option whose presence can affect the behavior of a {@link View}. For example,
 * a view can be made uncloseable by passing the option {@link StandardViewOption#HIDE_CLOSE_BUTTON}.
 * <p>
 * The options in {@link StandardViewOption} are sufficient for many views, but it is also
 * possible to create new options by implementing this interface. When creating new options,
 * it is often simplest and cleanest to create an <em>enum</em> that implements this interface,
 * so that a new option can be added with a single line of code (complete with sensible toString,
 * hashCode, and equals methods).
 */
public interface ViewOption
{ }
