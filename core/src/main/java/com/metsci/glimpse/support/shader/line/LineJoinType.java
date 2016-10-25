package com.metsci.glimpse.support.shader.line;

public enum LineJoinType
{

    JOIN_NONE( 0 ),
    JOIN_BEVEL( 1 ),
    JOIN_MITER( 2 );


    public final int value;


    private LineJoinType( int value )
    {
        this.value = value;
    }

}
