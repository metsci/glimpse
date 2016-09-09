#version 150

in float vS;

uniform sampler1D TEXTURE1D;

main( )
{
    gl_FragColor = texture1D( TEXTURE1D, vS );
}
