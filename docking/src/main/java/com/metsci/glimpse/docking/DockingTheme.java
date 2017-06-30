package com.metsci.glimpse.docking;

import java.awt.Color;

import javax.swing.ImageIcon;

public class DockingTheme
{

    public final int dividerSize;

    public final int landingIndicatorThickness;
    public final Color landingIndicatorColor;

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

    public final ImageIcon maximizeIcon;
    public final ImageIcon unmaximizeIcon;
    public final ImageIcon optionsIcon;

    public final ImageIcon closeViewIcon;
    public final ImageIcon closeViewHoveredIcon;
    public final ImageIcon closeViewPressedIcon;


    public DockingTheme( int dividerSize,

                         int landingIndicatorThickness,
                         Color landingIndicatorColor,

                         int lineThickness, // Even lineThickness values do NOT work well
                         int cornerRadius,
                         int cardPadding,
                         int labelPadding,

                         Color lineColor,
                         Color highlightColor,
                         Color selectedTextColor,
                         Color unselectedTextColor,

                         ImageIcon maximizeIcon,
                         ImageIcon unmaximizeIcon,
                         ImageIcon optionsIcon,

                         ImageIcon closeViewIcon,
                         ImageIcon closeViewHoveredIcon,
                         ImageIcon closeViewPressedIcon )
    {
        this.dividerSize = dividerSize;

        this.landingIndicatorThickness = landingIndicatorThickness;
        this.landingIndicatorColor = landingIndicatorColor;

        this.lineThickness = lineThickness;
        this.cornerRadius = cornerRadius;
        this.cardPadding = cardPadding;
        this.labelPadding = labelPadding;

        this.lineColor = lineColor;
        this.highlightColor = highlightColor;
        this.selectedTextColor = selectedTextColor;
        this.unselectedTextColor = unselectedTextColor;

        this.maximizeIcon = maximizeIcon;
        this.unmaximizeIcon = unmaximizeIcon;
        this.optionsIcon = optionsIcon;

        this.closeViewIcon = closeViewIcon;
        this.closeViewHoveredIcon = closeViewHoveredIcon;
        this.closeViewPressedIcon = closeViewPressedIcon;
    }

}
