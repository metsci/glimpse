grammar GlslArg;

options
{
  language = Java;
  output = AST;
  ASTLabelType=CommonTree;
}

@lexer::header
{
package com.metsci.glimpse.gl.shader.grammar;
}

@parser::header
{
package com.metsci.glimpse.gl.shader.grammar;

import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderArgType;
import com.metsci.glimpse.gl.shader.ShaderArgInOut;
import com.metsci.glimpse.gl.shader.ShaderArgQualifier;

import java.util.List;
import java.util.ArrayList;
}

shader returns [List<ShaderArg> result]
@init
{
  List<ShaderArg> result = new ArrayList<ShaderArg>();
}
  : ( parameter { result.add( $parameter.result ); } )* VOID! 'main'! LPAREN! (VOID?)! RPAREN! LCURLY!
  {
    $result = result;
  }
  ;

parameter returns [ShaderArg result]
@init
{
  String name;
  ShaderArgType type;
  ShaderArgQualifier qual;
  ShaderArgInOut inout;
}
  : pqual=qualifier? { qual = $pqual.result; }
    pinout=inout? { inout = $pinout.result; }
    ptype=type { type = $ptype.result; }
    pname=identifier { name = $pname.text; }
    (LBRACKET RBRACKET)?
    SEMI!
  {
    $result = new ShaderArg( name, type, qual, inout );
  }
  ;

type returns [ShaderArgType result]
  : VOID
  | FLOAT        { $result = ShaderArgType.FLOAT; }
  | INT          { $result = ShaderArgType.INT; }
  | BOOL         { $result = ShaderArgType.BOOLEAN; }
  | VEC2         { $result = ShaderArgType.VEC2; }
  | VEC3         { $result = ShaderArgType.VEC3; }
  | VEC4         { $result = ShaderArgType.VEC4; }
  | BVEC2        { $result = ShaderArgType.BVEC2; }
  | BVEC3        { $result = ShaderArgType.BVEC3; }
  | BVEC4        { $result = ShaderArgType.BVEC4; }
  | IVEC2        { $result = ShaderArgType.IVEC2; }
  | IVEC3        { $result = ShaderArgType.IVEC3; }
  | IVEC4        { $result = ShaderArgType.IVEC4; }
  | MAT2         { $result = ShaderArgType.MAT2; }
  | MAT3         { $result = ShaderArgType.MAT3; }
  | MAT4         { $result = ShaderArgType.MAT4; }
  | SAMPLER1D    { $result = ShaderArgType.SAMPLER_1D; }
  | ISAMPLER1D   { $result = ShaderArgType.ISAMPLER_1D; }
  | USAMPLER1D   { $result = ShaderArgType.USAMPLER_1D; }
  | SAMPLER2D    { $result = ShaderArgType.SAMPLER_2D; }
  | ISAMPLER2D   { $result = ShaderArgType.ISAMPLER_2D; }
  | USAMPLER2D   { $result = ShaderArgType.USAMPLER_2D; }
  | SAMPLERCUBE  { $result = ShaderArgType.SAMPLER_CUBE; }
  | SAMPLER1DARRAY    { $result = ShaderArgType.SAMPLER_1D_ARRAY; }
  | SAMPLER2DARRAY    { $result = ShaderArgType.SAMPLER_2D_ARRAY; }
  ;

qualifier returns [ShaderArgQualifier result]
  : CONST                { $result = ShaderArgQualifier.CONST; }
  | ATTRIBUTE            { $result = ShaderArgQualifier.ATTRIBUTE; }
  | VARYING              { $result = ShaderArgQualifier.VARYING; }
  | INVARIANT VARYING    { $result = ShaderArgQualifier.INVARIANT_VARYING; }
  | UNIFORM              { $result = ShaderArgQualifier.UNIFORM; }
  ;

inout returns [ShaderArgInOut result]
  : IN    { $result = ShaderArgInOut.IN; }
  | OUT   { $result = ShaderArgInOut.OUT; }
  | INOUT { $result = ShaderArgInOut.INOUT; }
  ;

identifier returns [String result]
  : IDENTIFIER  { $result = $IDENTIFIER.text; }
  ;

LCURLY           : '{';
RCURLY           : '}';
LPAREN           : '(';
RPAREN           : ')';
LBRACKET         : '[';
RBRACKET         : ']';
SEMI             : ';';

ATTRIBUTE        : 'attribute';
BOOL             : 'bool';
BREAK            : 'break';
BVEC2            : 'bvec2';
BVEC3            : 'bvec3';
BVEC4            : 'bvec4';
CONST            : 'const';
CONTINUE         : 'continue';
DISCARD          : 'discard';
DO               : 'do';
ELSE             : 'else';
FALSE            : 'false';
FLOAT            : 'float';
FOR              : 'for';
HIGH_PRECISION   : 'highp';
IF               : 'if';
IN               : 'in';
INOUT            : 'inout';
INT              : 'int';
INVARIANT        : 'invariant';
IVEC2            : 'ivec2';
IVEC3            : 'ivec3';
IVEC4            : 'ivec4';
LOW_PRECISION    : 'lowp';
MAT2             : 'mat2';
MAT3             : 'mat3';
MAT4             : 'mat4';
MEDIUM_PRECISION : 'mediump';
OUT              : 'out';
PRECISION        : 'precision';
RETURN           : 'return';
SAMPLER2D        : 'sampler2D';
ISAMPLER2D       : 'isampler2D';
USAMPLER2D       : 'usampler2D';
SAMPLER1D        : 'sampler1D';
ISAMPLER1D       : 'isampler1D';
USAMPLER1D       : 'usampler1D';
SAMPLERCUBE      : 'samplerCube';
SAMPLER1DARRAY        : 'sampler1DArray';
SAMPLER2DARRAY        : 'sampler2DArray';
STRUCT           : 'struct';
TRUE             : 'true';
UNIFORM          : 'uniform';
VARYING          : 'varying';
VEC2             : 'vec2';
VEC3             : 'vec3';
VEC4             : 'vec4';
VOID             : 'void';
WHILE            : 'while';

IDENTIFIER
  : ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
  ;

// skipped
WHITESPACE
  : ( ' ' | '\t' | '\f' | '\r' | '\n' )
  { $channel = HIDDEN; }
  ;

COMMENT
  : '//' (~('\n'|'\r'))*
  { $channel = HIDDEN; }
  ;
  
DIRECTIVE
  : '#'  (~('\n'|'\r'))*
  { $channel = HIDDEN; }
  ;
  
MULTILINE_COMMENT
  : '/*' ( options {greedy=false;} : . )* '*/'
  { $channel = HIDDEN; }
  ;
