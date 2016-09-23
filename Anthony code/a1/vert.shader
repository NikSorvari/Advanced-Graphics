#version 430

uniform float inx,iny,ins;

void main(void)
{ if (gl_VertexID == 0) gl_Position = vec4( 0.25+inx+ins,-0.25+iny-ins, 0.0, 1.0);
  else if (gl_VertexID == 1) gl_Position = vec4(-0.25+inx-ins,-0.25+iny-ins, 0.0, 1.0);
  else gl_Position = vec4( 0.25+inx+ins, 0.25+iny+ins, 0.0, 1.0);
}