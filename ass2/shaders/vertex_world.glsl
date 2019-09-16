//
// Vertex shader for 3D world using directional light
// and Phong shading

// Incoming vertex position
in vec3 position;

// Incoming normal
in vec3 normal;

// Incoming texture coordinate
in vec2 texCoord;

uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 proj_matrix;

out vec4 viewPosition;
out vec3 m;

out vec2 texCoordFrag;
noperspective out vec2 lakeTexCoordFrag;

out float visibility;
const float density = 0.2;
const float gradient = 1.5;

void main()
{
    // The global position is in homogenous coordinates
    vec4 globalPosition = model_matrix * vec4(position, 1);

    // The position in camera coordinates
    viewPosition = view_matrix * globalPosition;

    // The position in CVV coordinates
    gl_Position = proj_matrix * viewPosition;

    // Compute the normal in view coordinates and normalized it
    m = normalize(view_matrix * model_matrix * vec4(normal,0)).xyz;

    lakeTexCoordFrag = texCoord;
    texCoordFrag = texCoord;

    float distance = length(viewPosition.xyz);
    visibility = exp(-pow((distance*density),gradient));
    visibility = clamp(visibility,0.0,1.0);
}
