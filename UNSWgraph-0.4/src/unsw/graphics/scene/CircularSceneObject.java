package unsw.graphics.scene;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame2D;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Polygon2D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * CircularSceneObject is a subclass of PolygonalSceneObject class
 * but be specified to present a circle.
 *
 * Circle will be approximated by computing 32 sided polygon.
 */

public class CircularSceneObject extends PolygonalSceneObject {

    private static final int POLYGON_SIDES = 32;

    private float myRadius;

    /**
     * Create a CircularSceneObject with Centre (0,0) and radius 1
     * <p>
     * The line and fill colors can possibly be null, in which case that part of the object
     * should not be drawn.
     *
     * @param parent    The parent in the scene tree
     * @param fillColor The fill color
     * @param lineColor The outline color
     */
    public CircularSceneObject(SceneObject parent, Color fillColor, Color lineColor) {

        super(parent, constrCircle(1), fillColor, lineColor);
        myRadius = 1;
    }

    // Create a CircularSceneObject with Centre (0,0) and a given radius
    public CircularSceneObject(SceneObject parent, float radius, Color fillColor, Color lineColor) {

        super(parent, constrCircle(radius), fillColor, lineColor);
        myRadius = radius;
    }

    /**
     * Helper method for circle-like Polygon2D object construction
     * @return Polygon2D
     */
    private static Polygon2D constrCircle(float radius) {
        List<Point2D> points = new ArrayList<>();
        for (int i = 0; i < POLYGON_SIDES; i++) {
            // Convert standard degree unit radian unit for angles
            float x = radius * (float) Math.cos(Math.toRadians(360f/32)*i);
            float y = radius * (float) Math.sin(Math.toRadians(360f/32)*i);
            points.add(new Point2D(x, y));
        }
        return new Polygon2D(points);
    }
}
