package com.metsci.glimpse.docking;

import java.awt.Color;

public class CustomDockingTheme
{

    public final int dividerSize;

    /**
     * Even lineThickness values do NOT work well
     */
    public final int lineThickness;
    public final int cornerRadius;
    public final int cardPadding;
    public final int labelPadding;

    public final Color lineColor;
    public final Color highlightColor;
    public final Color selectedTextColor;
    public final Color unselectedTextColor;


    public CustomDockingTheme( int dividerSize,

                               int lineThickness, // Even lineThickness values do NOT work well
                               int cornerRadius,
                               int cardPadding,
                               int labelPadding,

                               Color lineColor,
                               Color highlightColor,
                               Color selectedTextColor,
                               Color unselectedTextColor )
    {
        this.dividerSize = dividerSize;

        this.lineThickness = lineThickness;
        this.cornerRadius = cornerRadius;
        this.cardPadding = cardPadding;
        this.labelPadding = labelPadding;

        this.lineColor = lineColor;
        this.highlightColor = highlightColor;
        this.selectedTextColor = selectedTextColor;
        this.unselectedTextColor = unselectedTextColor;
    }

}
