public class Quiz1
{
    public static void main(String[] args) {
        
        // question 1 starts here
        A q1 = new A();
        q1.f();

        // question 2 starts here
        B q2 = new B();
        q2.f();

        // question 3 starts here
        D q3 = new D();
        q3.f();
    }
}

class A {
    public void f() {
        int a = g(1);
        int b = g("foo");
        System.out.println(a + " " + b);
    }

    public int g(int n) {
        return n + 1;
    }

    public int g(String s) {
        return 1;
    }
}

class B {
    public void f() {
        C c1 = new C();
        C c2 = new C();
        c1.incX();
        c2.incY();
        System.out.println(c1.getX() + " " + c1.getY());
        System.out.println(c2.getX() + " " + c2.getY());
    }
}

class C {
    private int x;
    private static int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void incX() {
        x++;
    }

    public void incY() {
        y++;
    }
}

class D {
    public void f() {
        F f = new F();
        f.speak();
        E e = f;
        e.speak();
        e = new E();
        e.speak();
        f.speak();
    }
}

class E {
    public void speak() {
        System.out.println("moo");
    }
}

class F extends E {
    public void speak() {
        System.out.println("quack");
    }
}

/**
 * Question 4:
 *
 * Point2D p1 = new Point2D(0,0);
 * p1.draw(g1);
 * Point2D p2 = new Point2D(1,0);
 * p2.draw(g1);
 * Point2D p3 = new Point2D(2,2);
 * p3.draw(g1);
 *
 * Rewind this:
 * (-1, 1)----(0, 1)----(1, 1)
 *    |         |         |
 *    |         |         |
 * (-1, 0)----(0, 0)----(1, 0)
 *    |         |         |
 *    |         |         |
 * (-1,-1)----(0,-1)----(1, -1)
 */

/**
 * Question 5:
 *
 * What is the statement (or more importantly, what is the size) of creating
 * and initialising 8 vertices for the current GL_ARRAY_BUFFER?
 * Assuming vertices are of FLOAT format occupying 4 bytes each,
 * and each vertices is a 2D point (consisting x and y coordinate)
 * Hence, the result size is 2 * 4 * 8 = 64 bytes in total
 */

/**
 * Question 6:
 *
 * Properties of using GL_TRANGLE_FAN to draw a triangle (and other primitives)
 * Reference: https://www.khronos.org/opengl/wiki/Primitive
 *
 * GL_TRIANGLES: Vertices 0, 1 and 2 form a triangle
 * Vertives 3, 4 and 5 form a triangle and so on
 * E.g., {0 1 2}
 *             {3 4 5}
 * 
 * GL_TRIANGLE_STRIP: Every group of 3 adjacent vertives forms a triangle
 * The face direction of the strip is determined by the winding of the first
 * triagnle; A vertex stream of n length will generate n-2 triangles
 * E.g., {0 1 2}
 *         {1 2 3} starts with index 2 drawing 2-1-3
 *           {2 3 4} starts with index 3 drawing 3-2-4
 *
 * GL_TRIANGLE_FAN: The first vertex is always held fixed; From there on,
 * every group of 2 adjacent vertices form a triangle with the first
 * A vertex stream of n length will generate n-2 triangles
 * E.g., {0 1 2}
 *       {0} {2 3}
 *       {0}    {3 4}
 *       {0}      {4 5}
 */
