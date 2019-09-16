package unsw.graphics.geometry;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Point3DBuffer;
import unsw.graphics.Shader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LineStrip3D {
    private List<Point3D> points;

    public LineStrip3D() {
        this.points = new ArrayList<>();
    }

    public LineStrip3D(List<Point3D> points) {
        this.points = new ArrayList<>(points);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        Point3DBuffer buffer = new Point3DBuffer(points);

        int[] names = new int[1];
        gl.glGenBuffers(1, names, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, names[0]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, points.size() * 3 * Float.BYTES,
                buffer.getBuffer(), GL.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(Shader.POSITION, 3, GL.GL_FLOAT, false, 0, 0);
        Shader.setModelMatrix(gl, frame.getMatrix());
        gl.glDrawArrays(GL.GL_LINE_STRIP, 0, points.size());

        gl.glDeleteBuffers(1, names, 0);
    }

    public void add(Point2D p, float y) {
        add(new Point3D(p.getX(), y, p.getY()));
    }

    public void add(Point3D p) {
        points.add(p);
    }
}
