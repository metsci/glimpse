package com.metsci.glimpse.support.shader.line;

import static com.metsci.glimpse.support.shader.line.LineJoinType.*;

import java.util.Arrays;

import com.metsci.glimpse.support.color.GlimpseColor;

public class LineStyle
{

    /**
     * The thickness of the ideal bounds of the line. Feathering will encroach into
     * these bounds, by half of {@link #feather_PX}.
     */
    public float thickness_PX = 1.0f;

    /**
     * The thickness of the feather region, across which alpha fades to transparent.
     * Half the feather thickness (the more opaque half) lies inside the ideal bounds
     * of the line, and half (the more transparent half) lies outside.
     * <p>
     * For feathering to work, {@link javax.media.opengl.GL#GL_BLEND} must be enabled.
     * <p>
     * Line rendering is likely to be faster with feather set to zero.
     */
    public float feather_PX = 0.9f;

    /**
     * How to join connected line segments. Defaults to NONE, which gives appearance
     * and performance similar to familiar GL line drawing. Other join types may look
     * nicer, but are more computationally expensive to render.
     * <p>
     * Line rendering is likely to be faster with a join-type of NONE.
     */
    public LineJoinType joinType = JOIN_NONE;

    /**
     * To keep miters from growing out of control for very sharp angles, miter joins
     * are only used when:
     * <p>
     * {@code miterLength <= miterLimit * lineThickness}
     * <p>
     * where {@code miterLength} is the distance from the outer tip of the miter to
     * its inner corner.
     * <p>
     * Otherwise, a bevel join is used instead.
     * <p>
     * Has no effect unless {@link #joinType} is {@link LineJoinType#JOIN_MITER}.
     */
    public float miterLimit = 4;

    /**
     * The color used for the most opaque parts of the line. Due to feathering and/or
     * stippling, some parts of the line may have their alpha values scaled down, so
     * that they become more transparent.
     */
    public float[] rgba = GlimpseColor.getBlack( );

    /**
     * Line rendering is likely to be faster with stippling disabled.
     */
    public boolean stippleEnable = false;

    /**
     * The number of pixels, along the length of the line, covered by one bit
     * of {@link #stipplePattern}.
     */
    public float stippleScale = 1.0f;

    /**
     * Least significant bit is drawn at the start of the line. Despite the "int"
     * type, only the bottom 16 bits are used. 1 = opaque, 0 = transparent.
     */
    public int stipplePattern = 0b0101010101010101;

    public LineStyle( )
    {

    }

    public LineStyle( LineStyle source )
    {
        this.thickness_PX = source.thickness_PX;
        this.feather_PX = source.feather_PX;
        this.joinType = source.joinType;
        this.miterLimit = source.miterLimit;
        this.rgba = Arrays.copyOf( source.rgba, source.rgba.length );

        this.stippleEnable = source.stippleEnable;
        this.stippleScale = source.stippleScale;
        this.stipplePattern = source.stipplePattern;
    }

}
