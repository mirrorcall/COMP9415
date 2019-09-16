package unsw.graphics.world;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.*;
import unsw.graphics.geometry.Point3D;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class World extends Application3D implements KeyListener {

    /*
     * A bunch of predefined constants to be applied for the World
     */
    // Debugging set to true to see the structure of the world with line strip
    private static final boolean DEBUGGING = false;

    // Constant properties
    private static final float ROTATION_DEG = 10f;
    private static final float ROTATION_SCALE = 1f;
    private static final float TRANSLATION_SCALE = 0.3f;    // how fast the camera moving

    // Initial Avatar properties
    private static final Point3D STARTING_POSITION = new Point3D(0, 0, 7);
    private static final float STARTING_ROTATION = 180f;

    private static final Point3D THIRD_PERSON_CAMERA_OFFSET = new Point3D(0, 0.0f, 1.5f);
    private static final Point3D FIRST_PERSON_CAMERA_OFFSET = new Point3D(0, 0.0f, 1f);

    // Spec initial camera movement for test8.json
    private static final Point3D INIT_POS = new Point3D(0, 0.5f, 9f);
    private static final float INIT_ROT = 0f;

    // Offsets to be pre-considered
    private static final float ALTITUDE_OFFSET = 0.8f;  // Applied as camera initially at altitude of 0.5f
    private static final float ALTITUDE_BIAS = 0.7f;   // Applied for updating camera without losing quality

    // Initial transform for drawing objects
    private static final Point3D OBJ_TRANS = new Point3D(0, 0, 0);
    private static final float OBJ_ROTS = 0f;
    private static final float OBJ_SCALE = 1f;

    // Shader properties
    private Shader shader;

    // Drawn objects
    private Terrain terrain;
    private Avatar avatar;

    // Camera properties
    private Point3D position;       // Position of camera in 3D world
    private CoordFrame3D view;	    // Camera is just a form of coordinate frame
    private Point3D	movement;	    // How camera is translated in 3D world
    private float rotateY;		    // How camera is rotated in 3D world
    private boolean firstPerson;    // 1st person or 3rd person view

    private int fog;
    private int fogtrig;

    private boolean isNight;
    private long curtime;

    // Sunlight configs
    private Point3D sunmove;
    private int wid;
    private int dep;

    public World(Terrain terrain) {
        super("Assignment 2", 800, 600);
        this.terrain = terrain;
        avatar = new Avatar(0, 0, 7, STARTING_ROTATION);
        // Initialising camera
        this.view = CoordFrame3D.identity();
        this.position = new Point3D(0, 0, 0);
        this.movement = INIT_POS;
        this.rotateY = INIT_ROT;
        this.firstPerson = true;

        //night time
        this.isNight = false;
        this.sunmove = new Point3D(-1,0,(float)dep/2);
        this.wid = terrain.getWidth();
        this.dep = terrain.getDepth();
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        World world = new World(terrain);
        world.start();
    }

    @Override
    public void display(GL3 gl) {
        super.display(gl);

        // Updating altitude every frame
        // As altitude is postprocessing, it have to be set here instead of keyPressed()
        updateSelf();
        update(gl);
        float cameraAltitude = getCameraAltitude() == 0 ? ALTITUDE_BIAS : getCameraAltitude();
        movement.setXYZ(movement.getX(), cameraAltitude, movement.getZ());
        // Altitude for avatar itself needs to be subtracted by the ALTITUDE_BIAS which is meant for the camera altitude
        avatar.setPosition(movement.getX(), cameraAltitude-ALTITUDE_BIAS, movement.getZ());
        System.out.println(getCameraAltitude());

        // Applying camera
        setCamera(gl);

        // Draw other objects
        // Terrain will be at the negative z axis relative to the camera
        CoordFrame3D objFrame = CoordFrame3D.identity()
                .translate(OBJ_TRANS)
                .rotateY(OBJ_ROTS)
                .scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);

        /*
            All the magic of drawing happening here
         */
        terrain.draw(gl, objFrame);
        if (!firstPerson)
            avatar.draw(gl, objFrame);
    }

    public void setCamera(GL3 gl) {
        // Camera operations are inverse relative to world coordinate

        if (firstPerson) { // Disable avatar with first person view
            view = CoordFrame3D.identity()
                    .translate(FIRST_PERSON_CAMERA_OFFSET.negate())
                    .rotateY(-rotateY)
                    .translate(movement.negate());
        } else { // Enable avatar with third person view
            view = CoordFrame3D.identity()
                    .translate(THIRD_PERSON_CAMERA_OFFSET.negate())
                    .rotateY(-rotateY)
                    .translate(movement.negate());
        }

        Shader.setViewMatrix(gl, view.getMatrix());
    }

    @Override
    public void destroy(GL3 gl) {
        super.destroy(gl);

        avatar.destroy(gl);
        terrain.destroy(gl);
        shader.destroy(gl);
    }

    @Override
    public void init(GL3 gl) {
        super.init(gl);

        getWindow().addKeyListener(this);
        getWindow().addKeyListener(avatar);

        // debugging purpose - see the structure of terrain
        if (DEBUGGING) gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE);

        // Global Shader settings
        shader = new Shader(gl, "shaders/vertex_world.glsl",
                "shaders/fragment_world.glsl");
        shader.use(gl);

        terrain.init(gl);
        avatar.init(gl);

        fog = 0;
        fogtrig = 0;
        Shader.setInt(gl, "fog", fog);
        Color temp = new Color(0.35f,0.35f,0.35f);
        Shader.setColor(gl, "skyColor", temp);
    }

    @Override
    public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        // Near plane 0.2f works well thanks for the help.
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 0.2f, 100));
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        float x, y, z;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_UP:	// Moves forwards
                // Terrain is at the negative z axis relative to the camera
                x = movement.getX() - (float) Math.sin(Math.toRadians(rotateY)) * TRANSLATION_SCALE;
                z = movement.getZ() - (float) Math.cos(Math.toRadians(rotateY)) * TRANSLATION_SCALE;
                // Set altitude in the display() method hence 0 here
                movement.setXYZ(x, 0, z);
                avatar.opposite();  // movement animation
                break;

            case KeyEvent.VK_DOWN:	// Moves backwards
                // Terrain is at the negative z axis relative to the camera
                x = movement.getX() + (float) Math.sin(Math.toRadians(rotateY)) * TRANSLATION_SCALE;
                z = movement.getZ() + (float) Math.cos(Math.toRadians(rotateY)) * TRANSLATION_SCALE;
                // Set altitude in the display() method hence 0 here
                movement.setXYZ(x, 0, z);
                avatar.opposite();  // movement animation
                break;

            case KeyEvent.VK_LEFT:	// Turns left
                // Left-hand rule rotate z towards x
                // turning left by addition (CCW)
                rotateY += ROTATION_DEG * ROTATION_SCALE;
                avatar.setRotateY(rotateY);
                break;

            case KeyEvent.VK_RIGHT:	// Turns right
                // Left-hand rule rotate z towards x
                // turning right by subtraction (CW)
                rotateY -= ROTATION_DEG * ROTATION_SCALE;
                avatar.setRotateY(rotateY);
                break;

            case KeyEvent.VK_SPACE: // Debugging purpose
                movement = movement.translate(0, 0.1f, 0);
                break;

            case KeyEvent.VK_B:
                movement = movement.translate(0, -0.1f, 0);
                break;

            case KeyEvent.VK_C:     // Change view
                // Negate the truth value for view option
                firstPerson = !firstPerson;
                break;

            case KeyEvent.VK_L:
                this.isNight = !(this.isNight);
                System.out.println("isNight: " + this.isNight);
                break;

            case KeyEvent.VK_F:
                fogtrig = 1;
                fog = Math.abs(fog-1);

            default: break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }

    /**
     * Updating position of camera every movement
     */
    private void updateSelf() {
        // View matrix is the inverse of camera model matrix
        Matrix4 viewMat = Matrix4.scale(-1/OBJ_SCALE, -1/OBJ_SCALE, -1/OBJ_SCALE)
                .multiply(Matrix4.rotationY(-OBJ_ROTS))
                .multiply(Matrix4.translation(OBJ_TRANS.negate()));

        // Camera position is the multiplication of view matrix and world position
        // Remember to take negation of the movement (it is intended to be positive)
        position = viewMat.multiply(movement.negate().asHomogenous()).asPoint3D();
    }

    private void update(GL3 gl) {
        // compute the time since the last frame
        curtime = System.currentTimeMillis();
        curtime %= 24000;

        Color temp = new Color(0,0,0);
        if (fogtrig == 1) {
            fogtrig = 0;
            if (fog == 1) {
                Shader.setInt(gl, "fog", fog);
                temp = new Color(0.35f,0.35f,0.35f);
                setBackground(temp);
            }
            if (fog == 0) {
                Shader.setInt(gl, "fog", fog);
                temp = new Color(1f,1f,1f);
                setBackground(temp);
            }
        }
        if(isNight) {
            if (curtime <  5000) { //until 5am
                temp = new Color(0,0,0);
                sunmove.setXYZ(-1,0,(float)dep/2);
                Shader.setPoint3D(gl,  "lightDir", sunmove);
            } else if (curtime < 10000) { //5am - 10am
                float intensity = (10000 - curtime) / 5000f;
                sunmove.setXYZ(-1,(1-intensity)*10,(float)dep/2);
                Shader.setPoint3D(gl,  "lightDir", sunmove);
                temp = new Color(1-intensity,(1-intensity)*0.90f,(1-intensity)*0.66f);//1.0, 0.90, 0.66
            } else if(curtime < 18000) {//10am - 5pm
                float intensity = (18000 - curtime) / 8000f;
                sunmove.setXYZ((1-intensity)*(wid+2),10,(float)dep/2);
                Shader.setPoint3D(gl,  "lightDir", sunmove);
                float clr_g = 255;
                float clr_b = 255;

                if(curtime <= 12500) {
                    clr_b = (float) (0.34f*(curtime - 10000)/2500f);
                    clr_g = (float) (0.1f *(curtime - 10000)/2500f);
                }

                if (curtime >= 15500) {
                    clr_b = (float) (0.34f*(1-((curtime - 15500)/2500f)));
                    clr_g = (float) (0.1f *(1-((curtime - 15500)/2500f)));
                    //System.out.println((0.9f+clr_g) + "   " + (0.66f+clr_b));
                    //System.out.println(curtime + "   " + (curtime - 13500) + "    " + (curtime - 13500)/3500);
                }

                temp = new Color(1f,Math.min(0.9f+clr_g,1f),Math.min(0.66f+clr_b,1f));
                //temp = new Color(255,255,255);
            } else if (curtime < 23000) {//6pm - 11pm
                float intensity = (23000f - curtime) / 5000f;
                sunmove.setXYZ(wid+2,intensity*10,(float)dep/2);
                Shader.setPoint3D(gl,  "lightDir", sunmove);
                temp = new Color(intensity,intensity*0.90f,intensity*0.66f);
            } else { //until midnight
                temp = new Color(0,0,0);
            }
            Shader.setColor(gl, "lightIntensity", temp);
            setBackground(temp);
            //System.out.println(curtime);
        } else {
            Point3D sunlight = new Point3D(terrain.getSunlight().getX(),terrain.getSunlight().getY(),terrain.getSunlight().getZ());//-1 1 0
            //sunlight = new Point3D(10,10,10);
            if (fog == 1) {
                temp = new Color(0.35f,0.35f,0.35f);
            }
            if (fog == 0) {
                temp = new Color(1f,1f,1f);
            }
            setBackground(temp);
            Shader.setPoint3D(gl, "lightDir", sunlight);
            Shader.setColor(gl, "lightIntensity", Color.WHITE);
        }
    }

    private float getCameraAltitude() {
        float altitude = 0;
        if (isInTerrain()) {
            altitude = terrain.altitude(position.getX(), position.getZ()) + ALTITUDE_BIAS;
        }
//        System.out.println(position.getX() + "," + (altitude-ALTITUDE_BIAS) + "," + position.getZ());

//        return altitude < ALTITUDE_OFFSET ? ALTITUDE_OFFSET : altitude;
        return altitude;
    }

    private boolean isInTerrain() {
        boolean on = false;
        Point3D originOfTerrain = new Point3D(0, 0, 0);

        if (position.getX() >= originOfTerrain.getX() && position.getX() <= originOfTerrain.getX()+terrain.getWidth()-1
                && position.getZ() >= originOfTerrain.getZ() && position.getZ() <= originOfTerrain.getZ()+terrain.getDepth()-1) {
            on = true;
        }
        return on;
    }
}
