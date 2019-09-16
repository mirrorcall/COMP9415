package unsw.graphics.world;



import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.scene.MathUtil;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private List<Lake> lakes;
    private Vector3 sunlight;

    private TriangleMesh terrainMesh;   // for efficiency
    private Texture terrainTexture;

    // Lighting settings
    private static final Color LIGHT_INTENSITY = Color.WHITE;
    private static final Color AMBIENT_INTENSITY = new Color(0.2f, 0.2f, 0.2f);
    private static final Color AMBIENT_COEFF = Color.WHITE;
    private static final Color DIFFUSE_COEFF = new Color(0.5f, 0.5f, 0.5f);
    private static final Color SPECULAR_COEFF = new Color(0.8f, 0.8f, 0.8f);
    private static final float PHONG_EXP = 16f;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight) {
        this.width = width;
        this.depth = depth;
        altitudes = new float[width][depth];
        trees = new ArrayList<Tree>();
        roads = new ArrayList<Road>();
        lakes = new ArrayList<>();
        this.sunlight = sunlight;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public List<Tree> trees() {
        return trees;
    }

    public List<Road> roads() {
        return roads;
    }

    public Vector3 getSunlight() {
        return sunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        sunlight = new Vector3(dx, dy, dz);      
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return altitudes[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, float h) {
        altitudes[x][z] = h;
    }

    /**
     * Calculating the altitude at an arbitrary with given Point2D version
     *
     * @param p
     * @return
     */
    public float altitude(Point2D p) {
        return altitude(p.getX(), p.getY());
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * @param x
     * @param z
     * @return
     */
    public float altitude(float x, float z) {
        float altitude = 0;

        // TODO: Implement this
        if (x < 0 || z < 0 || x > width || z > depth) {
            return altitude;
        }

        if ((int)x == x && (int)z == z) {
            // Both x and z are integers
            return altitudes[(int)x][(int)z];
        }

        // From assignment specification
        // (x0,z0) +-------+ (x1,z0)
        //         |      /|
        //         |   /   |
        //       q |/    * | r
        // (x0,z1) +-------+ (x1,z1)
        int x0 = (int) Math.floor(x), z0 = (int) Math.floor(z);
        // Round up component cannot be applied by ceil()
        // as x,z might be integer - causing floor and ceil to be the same
        int x1 = (int)x + 1 >= width ? width-1 : (int)x+1;
        int z1 = (int)z + 1 >= depth ? depth-1 : (int)z+1;

        // Diagonal can be calculated
        float gradient = (float) (z1-z0) / (x0-x1);
        float bias = z0 - x1 * gradient;

        if (gradient*x+bias > z) {
            // Inside the left-hand-side triangle
            float rx = (z-bias) / gradient;
            /*
             * Turns out only drawing one diagonal (x1,z0)-(x0,z1)
             * Hence (x0,z0) and (x1,z1) are disconnected
             */
            float rh = MathUtil.lerp(z, z0, z1, altitudes[x1][z0], altitudes[x0][z1]);
            float qh = MathUtil.lerp(z, z0, z1, altitudes[x0][z0], altitudes[x0][z1]);
            altitude = MathUtil.lerp(x, x0, rx, qh, rh);
        } else if (gradient*x+bias < z) {
            // Inside the right-hand-side triangle
            float qx = (z-bias) / gradient;
            float qh = MathUtil.lerp(z, z0, z1, altitudes[x1][z0], altitudes[x0][z1]);
            float rh = MathUtil.lerp(z, z0, z1, altitudes[x1][z0], altitudes[x1][z1]);
            altitude = MathUtil.lerp(x, qx, x1, qh, rh);
        } else {
            // Right on the diagonal
            altitude = MathUtil.lerp(x, x0, x1, altitudes[x0][z1], altitudes[x1][z0]);
        }

        return altitude;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        trees.add(tree);
    }


    /**
     * Add a road
     *
     * @param width
     * @param spine
     */
    public void addRoad(float width, List<Point2D> spine) {
        Road road = new Road(width, spine);
        roads.add(road);
    }

    /**
     * Add a lake
     *
     * @param spine
     */
    public void addLake(List<Point2D> spine) {
        Lake lake = new Lake(spine);
        lakes.add(lake);
    }

    public void init(GL3 gl) {
        // create triangle meshes for the terrain in init() method
        // for efficiency purpose preventing from constant CPU-GPU
        // buffer transfer
        List<Point3D> vertices = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // draw terrain using triangle meshes
        // note the indices should follow the pattern in altitude()
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                vertices.add(new Point3D(x, altitudes[x][z], z));
                texCoords.add(new Point2D(x, z));

                // neglect the last column which cannot be combined for a triangle
                if (x == width-1 || z == depth-1) continue;

                /*
                 * One more thing to be noted here
                 * Everything is supposed to be viewable from the sky
                 * Nothing shall be visible from the bottom
                */

                // indices for the (x0,z0)-(x0,z1)-(x1,z0)
                indices.add(z * width + x);
                indices.add(z * width + x + depth);
                indices.add(z * width + x + 1);

                // indices for the (x1,z0)-(x0,z1)-(x1,z1)
                indices.add(z * width + x + depth);
                indices.add(z * width + x + depth + 1);
                indices.add(z * width + x + 1);
            }
        }

        terrainMesh = new TriangleMesh(vertices, indices, true, texCoords);
        terrainMesh.init(gl);

        // Set the texture properties
        terrainTexture = new Texture(gl, "res/textures/grass.bmp", "bmp", false);

        // Set shader - in the World.java main class

        // Set lighting properties
        Shader.setPoint3D(gl, "lightDir", sunlight.asPoint3D());
        Shader.setPoint3D(gl, "lightPos", sunlight.asPoint3D());
        Shader.setColor(gl, "lightIntensity", LIGHT_INTENSITY);
        Shader.setColor(gl, "ambientIntensity", AMBIENT_INTENSITY);

        // Set material properties
        Shader.setColor(gl, "ambientCoeff", AMBIENT_COEFF);
        Shader.setColor(gl, "diffuseCoeff", DIFFUSE_COEFF);
        Shader.setColor(gl, "specularCoeff", SPECULAR_COEFF);
        Shader.setFloat(gl, "phongExp", PHONG_EXP);

        /*
         * Initialization for other objects
         */
        for (Tree t : trees)
            t.init(gl);
        for (Road r : roads) {
            // The altitude of the whole road is depending on
            // the first control point
            r.setAltitude(altitude(r.controlPoint(0)));
            r.init(gl);
        }
        for (Lake l : lakes)
            l.init(gl);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        Shader.setPenColor(gl, Color.WHITE);
        Shader.setInt(gl, "tex", 0);

        gl.glActiveTexture(GL.GL_TEXTURE0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, terrainTexture.getId());
        terrainMesh.draw(gl, frame);

        for (Tree t : trees)
            t.draw(gl, frame);
        for (Road r : roads)
            r.draw(gl, frame);
        for (Lake l : lakes)
            l.draw(gl, frame);

        // Disable textures previously used
//        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    public void destroy(GL3 gl) {
        terrainMesh.destroy(gl);
        terrainTexture.destroy(gl);

        for (Tree t : trees)
            t.destroy(gl);

        for (Road r : roads)
            r.destroy(gl);
    }
}
