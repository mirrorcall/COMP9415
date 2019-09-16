# Week 3

## Recap

* View transform:
    * Word coordinate is rendered as it appears in the camera's local coordiante frame.
    * The view transform converts the world coordinate frame into the camera's local coordinate frame
* Inverse transformations:
    * Local-to-global transformation is $Q = M_TM_RM_S\ P$
    * On the contrary, global-to-local transformation is $P = M_S^{-1}M_R^{-1}M_T^{-1}\ Q$
    * translation: $M_T^{-1}(d_x,d_y) = M_T(-d_x, -d_y)$
    * roatation: $M_R^{-1}(\theta) = M_R(-\theta)$
    * scale: $M_S^{-1}(s_x,s_y) = M_S(1/s_x,1/s_y)$
    * shear: $M_H^{-1}(h) = M_H(-h)$; however, this operation we don't do much.
* Reparenting of the scene tree
    * rotation: $r_{Table} + r_{Bottle}$
    * scale: $s_{Table} \times s_{Bottle}$
    * **translation**: is different from the first two, it dooes need to perform the local-to-global transformation first.

> If it really comes to the shear, check dot product of the matrices is equal to zero

## Lerping (Linear interpolation)

This is now the time affine transformation (homogenous vector) working on the addtion of two points (orginally, not working for addtion for two points because of $1 + 1 = 2$). 

$$ \frac{1}{2}(p_1,p_2,1)^T + \frac{1}{2}(q_1,q_2,1)^T = (\frac{p_1+q_1}{2}, \frac{p_2,q_2}{2},1)^T $$

Linear interpolation
$$ lerp(P,Q,t) = P + t(Q-P) $$
$$ lerp(P,Q,t) = P(1-t) + tQ $$

#### Example

Using lienar interpolation, what is the midpoint between A=(4,9) and B=(3,7) <br>

As we are interested in midpoint, t is clearly $0,5$ here (half length of the line AB). By applying the formula mentioned above,
$$ lerp(A,B,0.5) = (4 + 0.5 * (3-4), 9 + 0.5 * (7-9)) $$
$$ midpoint = (3.5, 8) $$

#### Lines

* Parametric form:

$$ L(t) = P+tv $$
$$ v = Q - P $$
$$ L(t) = P + t(Q-P) $$


* Point-normal form in 2D:
$$ n \cdot (P - L) = 0 $$

#### Line intersection

* Two lines of parametric form
Two lines can be expressed by
$$ L_{AB}(t) = A + (B - A)t $$
$$ L_{CD}(u) = C + (D - C)u $$
Then their intersection can be calculated by
$$ L_{AB}(t) = L_{CD}(\textbf{u}) $$

* Two lines of parametric form and point-normal form

Two lines can be expressed by
$$ L_{AB} = A + \textbf{c}t $$
$$ L_{CD} = \textbf{n} \cdot (P-B) $$
$L_{CD}$ can then be converted into a form expressed in terms of x and y
$$ Ax + By + C = 0 $$
Finally, subustitute result of $L_{AB}$ into point-normal form, the resulting form is the intersection.

## Point in the polygon

It is of great importance to determine whether the point is in or out of the polygon when drawing the polygons.

* One simple of finding whether the point is side or outside the a simple polygon is to test how many times a ray, starting from the point and going in the any fixed direction, intersects the edge of polygon.
  * If the point is outside the polygon, the ray will intersects its edge an **even** number of time, while **odd** number of times implying it is inside the polygon
* For the difficult points which are crossing the actual vertex of the polygon, the detection now becomes only counting crossings at the lower vertex of an edge.

![example](https://cdncontribute.geeksforgeeks.org/wp-content/uploads/polygon1.png)

Reference: [Computational Geometry in C,P'Rourke](htts://cs.smith.edu/~orourke/books/compgeom.html)

## Shaders

![](https://i.stack.imgur.com/tLrbS.png)

Shaders are programs executed on the GPU for the purpose of rendering graphics written in GLSL (GL Shader Language).

Stages may or may not include but at least one of them:

* Vertex Shaders (execute as many times as we supply, three times for drawing a triangle)
* Tessellation Control and Evaluation Shaders
* Geometry Shaders
* Fragment Shaders
* Compute Shaders 

> OpenGL-Wiki: A shader is of purpose to execute one of the programming stages of the rendering pipeline. There are multiple stages each of which is specificed to its very own stage.

#### Fragment Shaders

The GPU will execute the fragment shader for every pixel it draws into the framebuffer <br>
Fragment shaders is useful when drawing a color gradient effect because it requires drawing pixel-by-pixel in different color.<br>
The colors along the line through those points can be calulated by linear interpolation (*lerp*).

Reference: [Vertex shaders vs Fragment shaders - stackoverflow](https://stackoverflow.com/questions/4421261/vertex-shader-vs-fragment-shader)

Reference: [Shader - opengl-wiki](https://www.khronos.org/opengl/wiki/Shader)


#### GLSL Syntax

* NO printf (so no characters and etc)
* NO recursion
* NO double precison (this is where shaders might be failed, by zooming in for a while - see more details of the graphics - gl would not be able to keep the precision.)
* YES to supporting of matrices (vec2/vec3/vec4)
* `in` and `out` implying input and output respectively
* `uniform` are inputs to the shader that are the same for every vertex

