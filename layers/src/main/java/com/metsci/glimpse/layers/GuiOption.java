package com.metsci.glimpse.layers;

/**
 * An option whose presence can affect the behavior of a {@link LayeredGui}. For example,
 * the layer cards panel can be hidden by passing the option {@link StandardGuiOption#HIDE_LAYERS_PANEL}.
 * <p>
 * The options in {@link StandardGuiOption} are sufficient in most cases, but it is also
 * possible to create new options by implementing this interface. When creating new options,
 * it is often simplest and cleanest to create an <em>enum</em> that implements this interface,
 * so that a new option can be added with a single line of code (complete with sensible toString,
 * hashCode, and equals methods).
 */
public interface GuiOption
{ }
