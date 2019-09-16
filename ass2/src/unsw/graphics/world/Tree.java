package unsw.graphics.world;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.*;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import java.awt.*;
import java.io.IOException;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private static final float INIT_SCALE = 0.1f;
    private static final float INIT_Y_OFFSET = 0.5f;

    private Point3D position;

    private TriangleMesh model;
    private Texture modelTexture;

    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }
    
    public Point3D getPosition() {
        return position;
    }

    public void init(GL3 gl) {
        // Loading triangle mesh composition file for Tree
        try {
            model = new TriangleMesh("res/models/tree.ply", true, true);
        } catch (IOException e) {
            System.out.println("PLY for the tree model is loading unsuccessfully.");
            e.printStackTrace();
        }

        model.init(gl);

        // Shader properties are in Terrain.java

        // TODO: Adding textures in the future
        modelTexture = new Texture(gl, "res/textures/rock.bmp", "bmp", false);
    }

    public void reshape(GL3 gl, int width, int height) {
        Shader.setProjMatrix(gl, Matrix4.perspective(60, 1, 1, 100));
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        Shader.setPenColor(gl, Color.WHITE);
        Shader.setInt(gl, "tex", 0);

        gl.glActiveTexture(GL.GL_TEXTURE0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, modelTexture.getId());

        CoordFrame3D modelFrame = frame.translate(position)
                .translate(0, INIT_Y_OFFSET, 0)
                .scale(INIT_SCALE, INIT_SCALE, INIT_SCALE);
        model.draw(gl, modelFrame);
    }

    public void destroy(GL3 gl) {
        model.destroy(gl);
        modelTexture.destroy(gl);
    }

}
