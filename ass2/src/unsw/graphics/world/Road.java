package unsw.graphics.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import unsw.graphics.*;
import unsw.graphics.geometry.*;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    // Debug purpose see the shape of road
    private static final boolean DEBUG = false;

    // The more segments on the curve the more smooth it can be
    private static final int SEGMENTS = 100;

    // Constant normal (y-axis) for a flat road
    private static final Vector3 NORMAL = new Vector3(0, 1, 0);

    private List<Point2D> points;
    private float width;

    private float altitude;         // The altitude of the start point
                                    // Also the altitude of the whole Bezier curve
    private LineStrip3D inCurve;
    private LineStrip3D exCurve;

    private TriangleMesh model;
    private Texture modelTexture;

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine) {
        this.width = width;
        this.points = spine;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return width;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return points.size() / 3;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public Point2D controlPoint(int i) {
        return points.get(i);
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public Point2D point(float t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        

        float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
        float y = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();        
        
        return new Point2D(x, y);
    }

    /**
     * Calculate the Bezier coefficients
     *
     * @param i
     * @param t
     * @return
     */
    private float b(int i, float t) {

        switch(i) {

            case 0:
                return (1-t) * (1-t) * (1-t);

            case 1:
                return 3 * (1-t) * (1-t) * t;

            case 2:
                return 3 * (1-t) * t * t;

            case 3:
                return t * t * t;
        }

        // this should never happen
        throw new IllegalArgumentException("" + i);
    }

    /**
     * The tangent vector to the curve at parameter t is give by
     * m * \sum_{k=0}^{m-1}B_k^{m-1}(t)(P_{k+1} - P_k)
     * Another idea of generating the tangent vector is to approximate it
     * using two neighbours of the current point to form a vector
     *
     * @param t
     * @return
     */
    private Point2D tangent(float t) {
        float x = 0, y = 0f;
        int m = points.size();
        for (int k = 0; k < m-1; k++) {
            x += bPrime(k, t) * (points.get(k+1).getX() - points.get(k).getX());
            y += bPrime(k, t) * (points.get(k+1).getY() - points.get(k).getY());
        }

        // Don't forget to multiply the number of order (m)
        return new Point2D(m * x, m * y);
    }

    /**
     * Calculate the Bezier coefficients with (m-1) order
     * I.e., serving the derivative of the curve
     *
     * @param i
     * @param t
     * @return
     */
    private float bPrime(int i, float t) {

        switch (i) {

            case 0:
                return (1-t) * (1-t);

            case 1:
                return 2 * (1-t) * t;

            case 2:
                return t * t;

        }

        // this should never happen
        throw new IllegalArgumentException("" + i);
    }

    public void init(GL3 gl) {
        makeExtrusion();
        model.init(gl);
        // Shader properties are in World.java
        modelTexture = new Texture(gl, "res/textures/road.bmp", "bmp", false);
    }

    /**
     * Helper method
     * Making extrusion for two Bezier curves forming a road
     * with width
     */
    private void makeExtrusion() {
        // The shape of road is calculated based on the Bezier curve
        List<Point3D> vertices = new ArrayList<>();
        List<Vector3> normal = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();

        // Set to true in World.java to show the internal and external curve with LineStrip3D
        inCurve = new LineStrip3D();
        exCurve = new LineStrip3D();
        float dt = (points.size()/3f)/SEGMENTS;

        int index = 0;
        for(float t = 0f; t <= this.size(); t += dt) {
            Matrix4 frenetFrame = genFrenetFrame(t);

            // 3D transformation - moving the centre of a frenet frame
            // Moving backward along the i-axis in frenet frame to obtain the bigger curve
            Point3D inP = frenetFrame.multiply(new Point3D(-width/2, 0, 0).asHomogenous()).asPoint3D();
            // Moving forward along the i-axis in frenet frame to obtain the smaller curve
            Point3D exP = frenetFrame.multiply(new Point3D(width/2, 0, 0).asHomogenous()).asPoint3D();

            inCurve.add(inP);
            exCurve.add(exP);

            // To form the coherence, the TriangleMesh for road is constructed in the same way as terrain did
            // index+2 +-------+ index+3
            //         |      /|
            //         |   /   |
            //         |/      |
            // index+0 +-------+ index+1
            vertices.add(exP);
            vertices.add(inP);

            /*
             * Make extrusion
             */
            texCoords.add(new Point2D(exP.getX(), exP.getZ()));
            texCoords.add(new Point2D(inP.getX(), inP.getZ()));

            if (t < this.size()-dt) {
                indices.add(index + 3);
                normal.add(NORMAL);
                indices.add(index + 0);
                normal.add(NORMAL);
                indices.add(index + 2);
                normal.add(NORMAL);

                indices.add(index + 1);
                normal.add(NORMAL);
                indices.add(index + 0);
                normal.add(NORMAL);
                indices.add(index + 3);
                normal.add(NORMAL);
            }
            index += 2;
        }
        model = new TriangleMesh(vertices, normal, indices, texCoords);
    }

    /**
     * Draw method for drawable object
     * Specific drawing is in Terrain.draw()
     *
     * @param gl
     * @param frame
     */
    public void draw(GL3 gl, CoordFrame3D frame) {
        Shader.setPenColor(gl, Color.WHITE);
        Shader.setInt(gl, "tex", 0);

        gl.glActiveTexture(GL.GL_TEXTURE0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, modelTexture.getId());

        LineStrip3D curve = new LineStrip3D();

        if (DEBUG) {
            Shader.setPenColor(gl, Color.MAGENTA);
            inCurve.draw(gl, frame);
            exCurve.draw(gl, frame);
            Shader.setPenColor(gl, Color.WHITE);
        }

        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
        // Clearly the avatar needs to be pushed out a little bit
        gl.glPolygonOffset(-1, -1);
        model.draw(gl, frame);
        // Disable after drawing it
        gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
    }

    /**
     * Calculating the Frenet Frame of current point(t)
     * Lecture5B page 53
     * phi = C(t)
     * k = normalise(C'(t))
     * i = (-k2, k1, 0)
     * j = k cross product i
     *
     * @param t
     */
    private Matrix4 genFrenetFrame(float t) {
        Point3D phi = new Point3D(point(t).getX(), altitude, point(t).getY());
        // Align the k-axis with the NORMALISED tangent
        Vector3 k = new Vector3(tangent(t).getX(), 0, tangent(t).getY()).normalize();
        // X is still x-coordinate, Y is now z-coordinate
        Vector3 i = new Vector3(-k.getZ(), 0, k.getX());
        Vector3 j = k.cross(i);
        Matrix4 frenetFrame = new Matrix4(
                i.extend(),         // i
                j.extend(),         // j
                k.extend(),         // k
                phi.asHomogenous()  // phi
        );

        return frenetFrame;
    }

    public void destroy(GL3 gl) {
        model.destroy(gl);
        modelTexture.destroy(gl);
    }
}
