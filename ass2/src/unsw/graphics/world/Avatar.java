package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Avatar implements KeyListener {

    private static final List<Point3D> LEFT_LEG = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(0, 6, 0),
            new Point3D(-2, 6, 0),
            new Point3D(-2 ,0, 0)
    );
    private static final List<Point3D> RIGHT_LEG = Arrays.asList(
            new Point3D(2, 0, 0),
            new Point3D(2, 6, 0),
            new Point3D(0, 6, 0),
            new Point3D(0, 0, 0)
    );
    private static final List<Point3D> TORSO = Arrays.asList(
            new Point3D(2 ,6, 0),
            new Point3D(2, 12, 0),
            new Point3D(-2, 12, 0),
            new Point3D(-2, 6, 0)
    );
    private static final List<Point3D> LEFT_ARM = Arrays.asList(
            new Point3D(-2, 6, 0),
            new Point3D(-2, 12, 0),
            new Point3D(-4, 12, 0),
            new Point3D(-4, 6, 0)
    );
    private static final List<Point3D> RIGHT_ARM = Arrays.asList(
            new Point3D(4, 6, 0),
            new Point3D(4, 12, 0),
            new Point3D(2, 12, 0),
            new Point3D(2, 6, 0)
    );
    private static final List<Point3D> HEAD = Arrays.asList(
            new Point3D(2, 12, 0),
            new Point3D(2, 16, 0),
            new Point3D(-2, 16, 0),
            new Point3D(-2, 12, 0)
    );

    private static final Point3D LEG_FRONT_TRAN = new Point3D(0f, 0.5f, -1.9f);
    private static final Point3D ARM_FRONT_TRAN = new Point3D(0, 0, -4f);
    private static final float FRONT_ROT = 20f;
    private static final Point3D LEG_BACK_TRAN = new Point3D(0f, 0.5f, 1.9f);
    private static final Point3D ARM_BACK_TRAN = new Point3D(0, 0, 4f);
    private static final float BACK_ROT = -20f;

    private static final float AVATAR_SCALE = 0.035f;
    private static final float AVATAR_ROT_OFFSET = 180f;    // as avatar faces backwards initially

    // Relative to the camera settings
    private Point3D position;
    private float rotateY;

    // Animation properties
    private boolean isLeft = false;     // shall avatar step left foot

    private float rotateYByMouse;

    private List<TriangleMesh> meshes = new ArrayList<>();

    // The model consists of multiple parts - left leg/right leg/torso/left arm/right arm/head
    private List<List<TriangleMesh>> model = new ArrayList<>();

//    private TriangleMesh model;
    private Texture modelTexture;
    private Texture eyeTexture;

    public Avatar(float x, float y, float z, float rotateY) {
        this.position = new Point3D(x, y, z);
        this.rotateY = rotateY;
    }

    public Point3D getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }

    public void opposite() {
        isLeft = !isLeft;
    }

    public void setRotateY(float rotateY) {
        this.rotateY = rotateY;
    }

    public void init(GL3 gl) {
        modelTexture = new Texture(gl, "res/textures/EndermanBody.png", "png", false);
        eyeTexture = new Texture(gl, "res/textures/EndermanEye.jpg", "jpg", false);
        makeModel(gl);

    }

    private void makeModel(GL3 gl) {
        // Create the model with a bit hack using extrusion
        List<List<Point3D>> shapes = Arrays.asList(
                LEFT_LEG,
                RIGHT_LEG,
                TORSO,
                LEFT_ARM,
                RIGHT_ARM,
                HEAD
        );

        int counter = 0;
        for (List<Point3D> shape : shapes) {
            List<Integer> shapeIndices = Arrays.asList(0, 1, 2, 2, 3, 0);
            List<Point2D> frontTexCoords = new ArrayList<>();
            List<Point2D> backTexCoords = new ArrayList<>();

            for (Point3D p : shape) {
                if (counter == 5) {     // avoid repeating texture for the face
                    frontTexCoords.add(new Point2D(0, 0));
                    frontTexCoords.add(new Point2D(0, 1));
                    frontTexCoords.add(new Point2D(1,1));
                    frontTexCoords.add(new Point2D(1,0));
                } else
                    frontTexCoords.add(new Point2D(p.getX(), p.getY()));
            }

            TriangleMesh frontMesh = new TriangleMesh(shape, shapeIndices, true, frontTexCoords);

            List<Point3D> shapeExt = new ArrayList<>();
            for (Point3D p : shape)
                shapeExt.add(p.translate(0, 0, -2f));

            List<Integer> shapeExtIndices = new ArrayList<>(shapeIndices);
            Collections.reverse(shapeExtIndices);

            for (Point3D p : shapeExt)
                backTexCoords.add(new Point2D(p.getX(), p.getY()));

            TriangleMesh backMesh = new TriangleMesh(shapeExt, shapeExtIndices, true, backTexCoords);
            TriangleMesh sideMesh = makeExtrusion(shape, shapeExt);

            frontMesh.init(gl);
            backMesh.init(gl);
            sideMesh.init(gl);

            model.add(Arrays.asList(frontMesh, backMesh, sideMesh));
            counter++;
        }
    }

    /**
     * Make extrusion for the given front and back faces
     *
     * @param shape     Vertices of the front mesh
     * @param shapeExt  Vertices of the back mesh
     * @return          All side meshes
     */
    private static TriangleMesh makeExtrusion(List<Point3D> shape, List<Point3D> shapeExt) {
        List<Point3D> sides = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Point3D bl = shape.get(i);
            Point3D br = shapeExt.get(i);
            Point3D tl = shape.get((i + 1) % 4);
            Point3D tr = shapeExt.get((i + 1) % 4);

            // Side meshes should be visible in any direction
            // tr +--------+ tl
            //    |        |
            //    |        |
            // br +--------+ bl

            sides.add(bl);
            sides.add(br);
            sides.add(tl);

            sides.add(tl);
            sides.add(br);
            sides.add(tr);

            texCoords.add(new Point2D(bl.getX(), bl.getZ()));
            texCoords.add(new Point2D(br.getX(), br.getZ()));
            texCoords.add(new Point2D(tl.getX(), tl.getZ()));
            texCoords.add(new Point2D(tr.getX(), tr.getZ()));
        }

        return new TriangleMesh(sides, true, texCoords);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        Shader.setPenColor(gl, Color.WHITE);
        Shader.setInt(gl, "tex", 0);

        gl.glActiveTexture(GL.GL_TEXTURE0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, modelTexture.getId());

        // Moving relative to the camera
        CoordFrame3D modelFrame = frame.translate(position)
                .rotateY(rotateY)
                .rotateY(rotateYByMouse)        // rotate avatar itself
                .scale(AVATAR_SCALE, AVATAR_SCALE, AVATAR_SCALE);

        // Fix up the z-fighting problem
        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        // Clearly the avatar needs to be pushed out a little bit
        gl.glPolygonOffset(-1, -1);
        for (int i = 0; i < model.size(); i++) {
            /*
             * 0 - left leg
             * 1 - right leg
             * 2 - torso
             * 3 - left arm
             * 4 - right arm
             * 5 - head
             */
            if (i == 0) {           // left leg
                animate(gl, modelFrame, i, LEG_FRONT_TRAN, FRONT_ROT, LEG_BACK_TRAN, BACK_ROT);
            } else if (i == 1) {    // right leg
                animate(gl, modelFrame, i, LEG_BACK_TRAN, BACK_ROT, LEG_FRONT_TRAN, FRONT_ROT);
            } else if (i == 3) {    // left arm
                animate(gl, modelFrame, i, ARM_BACK_TRAN, BACK_ROT, ARM_FRONT_TRAN, FRONT_ROT);
            } else if (i == 4) {    // right arm
                animate(gl, modelFrame, i, ARM_FRONT_TRAN, FRONT_ROT, ARM_BACK_TRAN, BACK_ROT);
            } else if (i == 5) {    // preparing the texture for the eye
                // Switching to the eye texture for the face
                gl.glBindTexture(GL.GL_TEXTURE_2D, eyeTexture.getId());
                model.get(i).get(0).draw(gl, modelFrame);
                // Switching back to the body texture
                gl.glBindTexture(GL.GL_TEXTURE_2D, modelTexture.getId());
                model.get(i).get(1).draw(gl, modelFrame);
                model.get(i).get(2).draw(gl, modelFrame);
            } else {                // rest of the components of the avatar
                model.get(i).get(0).draw(gl, modelFrame);
                model.get(i).get(1).draw(gl, modelFrame);
                model.get(i).get(2).draw(gl, modelFrame);
            }
        }
        // Disable after drawing it
        gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
    }

    /**
     * Simulating walking animation by drawing legs with rotating different degrees
     *
     * @param gl
     * @param modelFrame
     * @param i
     * @param frontTran
     * @param frontRot
     * @param backTran
     * @param backRot
     */
    private void animate(GL3 gl, CoordFrame3D modelFrame, int i, Point3D frontTran, float frontRot,
                            Point3D backTran, float backRot) {
        if (isLeft) {
            model.get(i).get(0).draw(gl, modelFrame.translate(frontTran).rotateX(frontRot));
            model.get(i).get(1).draw(gl, modelFrame.translate(frontTran).rotateX(frontRot));
            model.get(i).get(2).draw(gl, modelFrame.translate(frontTran).rotateX(frontRot));
        } else {
            model.get(i).get(0).draw(gl, modelFrame.translate(backTran).rotateX(backRot));
            model.get(i).get(1).draw(gl, modelFrame.translate(backTran).rotateX(backRot));
            model.get(i).get(2).draw(gl, modelFrame.translate(backTran).rotateX(backRot));
        }
    }

    public void destroy(GL3 gl) {
        for (List<TriangleMesh> meshes : model)
            for (TriangleMesh mesh : meshes)
                mesh.destroy(gl);
        modelTexture.destroy(gl);
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_A:
                rotateYByMouse += 10f;
                break;
            case  KeyEvent.VK_D:
                rotateYByMouse -= 10f;
                break;
            case KeyEvent.VK_R:
                rotateYByMouse = 0f;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
