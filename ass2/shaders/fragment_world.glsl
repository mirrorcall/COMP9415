//
// Fragment shader for 3D world using directional light
// and Phong shading

out vec4 outputColor;

uniform vec4 input_color;

uniform mat4 view_matrix;

// Light properties
uniform vec3 lightDir;  // Instead of the position of the light
// Directional light requires the direction
uniform vec3 lightPos;  // Serving specular light
uniform vec3 lightIntensity;
uniform vec3 ambientIntensity;

// Material properties
// Using phong shading
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;

in vec4 viewPosition;
in vec3 m;

uniform int isLake;
in vec2 texCoordFrag;
noperspective in vec2 lakeTexCoordFrag;

in float visibility;
uniform vec3 skyColor;
uniform int fog;        // fog configs

void main()
{
    // Phong shading requires normalize normal everytime for lerping
    vec3 m_unit = normalize(m);

    // Compute the s, v and r vectors
    // s(ource) vector no longer needs to be calculated
    // by \vec{S} - \vec{P}
    // as all objects share the same direction vector

    // Direction is fragment towards light resource
    vec3 s = normalize((view_matrix*vec4(lightDir,0)).xyz);
    vec3 v = normalize(-viewPosition.xyz);
    vec3 r = normalize(reflect(-s, m_unit));

    vec3 ambient = ambientIntensity * ambientCoeff;
    vec3 diffuse = max(lightIntensity*diffuseCoeff*dot(m_unit,s), 0.0);
    vec3 specular;

    // Only show specular reflections for the front face
    if (dot(m,s) > 0)
        specular = max(lightIntensity*specularCoeff*pow(dot(r,v),phongExp), 0.0);
    else
        specular = vec3(0);

    vec4 ambientAndDiffuse = vec4(ambient+diffuse, 1);

    // Applying noperspective texCoords for lake but not other objects
    if (isLake == 1)
        outputColor = ambientAndDiffuse * input_color * texture(tex,lakeTexCoordFrag) + vec4(specular, 1);
    else
        outputColor = ambientAndDiffuse * input_color * texture(tex,texCoordFrag) + vec4(specular, 1);

    if (fog == 1) {
        outputColor = mix(vec4(skyColor,1.0), outputColor, visibility);
    }

}
