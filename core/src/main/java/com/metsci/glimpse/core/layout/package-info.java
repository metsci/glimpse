/**
 * Provides a MigLayout-based framework for partitioning a
 * {@link com.metsci.glimpse.core.canvas.GlimpseCanvas} into multiple
 * logical drawing areas. Each drawing area is represented by
 * a {@link com.metsci.glimpse.core.layout.GlimpseLayout} which can
 * paint {@link com.metsci.glimpse.core.painter.base.GlimpsePainter}s
 * and receive notification of
 * {@link com.metsci.glimpse.core.event.mouse.GlimpseMouseEvent}s
 * occurring inside of it. GlimpseLayouts can be nested and can have
 * multiple parents.
 */
package com.metsci.glimpse.core.layout;
