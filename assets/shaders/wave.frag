varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform float u_time;
#define PI 3.14159265359
void main(){
    float Time = sin(PI * u_time);
    vec2 uv = v_texCoord;
    float waveOffsetY = sin(v_texCoord.x * 5 + Time * 5) * 0.08;
    uv.y += waveOffsetY;
    gl_FragColor = texture2D(u_texture, uv);
}