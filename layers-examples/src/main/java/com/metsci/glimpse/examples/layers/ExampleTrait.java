package com.metsci.glimpse.examples.layers;

import java.util.function.DoubleUnaryOperator;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.layers.LayeredGui;
import com.metsci.glimpse.layers.Trait;
import com.metsci.glimpse.layers.View;

public class ExampleTrait extends Trait
{

    public static final String exampleTraitKey = ExampleTrait.class.getName( );

    public static void addExampleLinkage( LayeredGui gui, ExampleTrait master )
    {
        gui.addLinkage( exampleTraitKey, master );
    }

    public static void setExampleTrait( View view, ExampleTrait exampleTrait )
    {
        view.setTrait( exampleTraitKey, exampleTrait );
    }

    public static ExampleTrait requireExampleTrait( View view )
    {
        return view.requireTrait( exampleTraitKey, ExampleTrait.class );
    }


    public final Axis1D zAxis;


    public ExampleTrait( boolean isLinkage )
    {
        super( isLinkage );

        this.zAxis = new Axis1D( );

        this.parent.addListener( true, ( ) ->
        {
            ExampleTrait newParent = ( ExampleTrait ) this.parent.v( );
            this.zAxis.setParent( newParent == null ? null : newParent.zAxis );
        } );
    }

    @Override
    protected boolean isValidParent( Trait linkage )
    {
        return ( linkage instanceof ExampleTrait );
    }

    @Override
    public ExampleTrait copy( boolean isLinkage )
    {
        ExampleTrait copy = new ExampleTrait( isLinkage );

        // Copy axis settings
        copy.zAxis.setParent( this.zAxis );
        copy.zAxis.setParent( null );

        return copy;
    }

    public void setZBounds( DoubleUnaryOperator unitsToSu, double zMin_UNITS, double zMax_UNITS )
    {
        this.zAxis.setMin( unitsToSu.applyAsDouble( zMin_UNITS ) );
        this.zAxis.setMax( unitsToSu.applyAsDouble( zMax_UNITS ) );
        this.zAxis.validate( );
    }

}
