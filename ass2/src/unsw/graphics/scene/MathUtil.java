package unsw.graphics.scene;

import unsw.graphics.geometry.Point2D;

/**
 * A collection of useful math methods 
 *
 * @author malcolmr
 */
public class MathUtil {

    /**
     * Normalise an angle to the range [-180, 180)
     * 
     * @param angle 
     * @return
     */
    public static float normaliseAngle(float angle) {
        return ((angle + 180f) % 360f + 360f) % 360f - 180f;
    }

    /**
     * Clamp a value to the given range
     * 
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Bilinear interpolation formula
     *
     * @param p  x/y component for unknown point
     * @param p1 x/y component for first point
     * @param p2 x/y component for second point
     * @param f1 depth component for first point
     * @param f2 depth component for second point
     * @return   depth component for unknown point
     */
    public static float lerp(float p, float p1, float p2, float f1, float f2) {
        return ((p-p1)/(p2-p1)) * f2 + ((p2-p)/(p2-p1)) * f1;
    }

    /**
     * Calculate the optimal distance on a quarter circle given radius using Bezier curve
     * There is a formula for n control points
     * (4/3)*tan(pi/(2n)) - for n = 4 - tan(pi/8)
     *
     * @param radius
     * @return
     */
    public static float distance(float radius) {
        return (4/3) * (float) Math.tan(Math.toRadians(22.5));
    }
}
