# Week 2

## Scene Trees

To draw (calculate) an compositing object containing lots of components (of this object), instead of drawing it piece by piece (by piece, it means vertex), we represent those complex scenes with a *scene tree*. Each node represents a component and each edge represents the transformation to get from the parent component's coordinate system to the child's

> Scene Tree <br>
> It is a general data structure commonly used by vector-based graphics editing applications and modern computer games. <br>
> A scene tree is a collection of nodes in a graph or tree structure. A tree node may have many children but only a single parent. <br>
> A common feature is the ability to group related shapes and objects into a compound object that can then be moved, transformed, etc, as easily as a single object.

#### Scene Graph

* Directed acyclic multi-graph
* Each node can have multiple parents
* Multiple edges can go from parent to child
* Shared noes are drawn multiple times

#### Example

![](https://webcms3.cse.unsw.edu.au/static/uploads/coursepic/COMP3421/19T2/2a71d26a94db94904965390b89b3021cc7ba17d697b5698f643bed1bdbb6f758/Screen_Shot_2019-06-07_at_10.44.20_pm.png)

Supposing we have to draw a object demonstrated above, in an abstract way, we can split the whole figure into subcomponents, including head, arms, legs and arms. But there is still one missing part connecting all of those subcomponents - a torso. In terms of the torso, the components can rotate, scale and transform with regarding to it, which makes the torso the root of our scene tree. Other than that, arms can also be split into upper arm, lower arms and hands.

```
                    Torso
   /         /        |         \         \
LU Arm    LU Leg     Head     RU Leg    RU Arm
  |          |                   |         |
LL Arm    LL Leg              RL Leg    RL Arm         
  |          |                   |         |
L Hand    L Foot              R Foot    R Hand
```

##### Scene Graph with multiple parents

Bicycle

```
            Frame
     /        |        \
   Seat     Handle      \
              |          \
             Fork ----- Wheel
```

##### More Complicated Example

![](http://what-when-how.com/wp-content/uploads/2012/07/tmpc2f9104_thumb.png)

#### Implementation

```java
drawTree(frame) {
    compute new_frame by transforming frame (TRS):
        translation
        rotation
        scale

    draw the object

    for all children:
        child.drawTree(new_frame)
}
```

## Instance Transformation (TRS)

TRS are short for translate(T), rotate(R) and scale(S) and generally abbreviated to *M*, and each object (i.e., component mentioned above) can be created in their own local coordinate system (i.e., their own frames) and passes that onto its children.<br>
When a node in the graph is transformed, all **its children move with it**

## Camera

The situation talked about above is speicically assuming that the camera is positioned at the world coordinate (0,0). In actual, camera is also considered to be an object with its own position, rotation, and scale. <br>
The world is rendered as it appears in the camera's local coordinate frame. <br>
<br>
Aspect ratio should also be taken into consideration: the ratio of the width to the height.

## View Transform

**View transform** converts the world coordinate frmae in to camera's local coordinate frame by a series of *inverse* of the transformations that would convert the camera's local coordinate frame into world coordinates. <br>
Now, consider the camera as the reference frame, rather than that the camera moves, it is the whole world moves (or transforms).

* Moving the camera left = moving the whole world right
* Rotating the camera clockwise = rotating the world anticlockwise
* Growing the camera's view = shrinking the world

#### Transformation pipeline

Transform performed in 2 stages:

$$ P_{camera} \xleftarrow{\text{view}} P_{world} \xleftarrow{\text{model}} P_{local} $$
> Firstly, model transform transforms points in the *local coordinate system* to the *world coordinate system* <br>
> Then, view transform transforms points in the *world coordinate systems* to the *camera's coordinate system*

Example:
$$ if\ P_{world} = Trans(Rot(Scale(P_{camera}))) $$
$$ then\ P_{camera} = Scale^{-1}(Rot^{-1}(Tran^{-1}(P_{world}))) $$

Doubly linked list might do:
```java
for op : OPs {
    LinkedList.add(op);
}

for op : LinkedList {
    reverseOp(LinkedList.pop();
}
```

#### Implementing a camera in UNSWgraph

```java
CoordFrame2D viewFrame = CoordFrame2D.identity()
        /**
         * start with the last operation in world coordinate
         * and get the inverse of each parameter in operations (i.e., what -1 does)
         */
        .scale(1/myScale, 1/myScale)
        .rotate(-myAngle)
        .translate(-myPos.getX(), -myPos.getY());
```

#### Performing camera in side the Scene Tree

The relationship between the camera and the scene tree is actually that whenever the camera is transformed, we actually transform the world to adapt the camera. Hence, there should be a trace from the camera back to the root of scene tree, and of course a trace from camera to to be rendered object as well. More specifically, we need to compute the camera's transformations in world coordinates (and then get the inverse) in order to compute the view transform.


## Transformations in the coordinate frame

#### Points

A point can be described as a displacement from the origin:
$$ P = p_{x}x + p_{y}y + \phi $$
where $p_x$ is the modulus of the vector along x-aixs and $\phi$ is the origin

#### Transformation

To convert **P** to a different coordinate frame:

1. Move the coordiante frame
    1. Convert x-axis: $x = p_xx^{\prime} + p_yy^{\prime}$
    2. Convert y-axis: $y = p_xx^{\prime} + p_yy^{\prime}$
    3. Convert origin: $\phi = p_xx^{\prime} + p_yy^{\prime}$
2. Convert the point based on the previous displacement but in the new coordinate frame

where $x^{\prime}$ is the previous x-aixs


##### Homogenous coordinates

Point: 
$$P = \begin{pmatrix}
p_1 \\
p_2 \\
\textbf{1} \end{pmatrix} $$

Vector:
$$v = \begin{pmatrix}
v_1 \\
v_2 \\
\textbf{0} \end{pmatrix} $$

In general, first two elements are coming from two axes repectively, while the thrid showing whether it is a point (1 for point, 0 for vector) or not. <br>
Because of this unique property (0/1 binary relationship), vectors can be summed altogether to get a new vector while a point can be the result of sum of one point and one vector. But two points cannot be performed addition in this case (resulting in 2 at the end).

## Affine transformation

1. Points

Transformations between coordintate frames can be represented as matrices:
$$ Q = \textbf{M}P $$
$$ \begin{pmatrix}
q1 \\ q2 \\ 1 \end{pmatrix} =
\begin{pmatrix}
x_1 & y_1 & \phi_1 \\
x_2 & y_2 & \phi_2 \\
0 & 0 & 1 \end{pmatrix}
\begin{pmatrix}
p_1 \\ p_2 \\ 1 \end{pmatrix} $$

> Affine Transformation is a linear mapping method that preserves points, straight lines, and planes. Sets of parallel lines remain parallel after an affine transformation. <br>
> However, an affine transformation does not necessarily preserve angles between lines or distances between lines or distinces between points.

2. Vectors

$$ v = \textbf{M} u $$
$$ \begin{pmatrix}
v_1 \\ v_2 \\ 0 \end{pmatrix} = 
\begin{pmatrix}
x_1 & y_1 & \phi_1 \\
x_2 & y_2 & \phi_2 \\
0 & 0 & 1 \end{pmatrix}
\begin{pmatrix}
u_1 \\ u_2 \\ 0 \end{pmatrix} $$

#### Basic transformations

* Translation
* Rotation
* Scale
* Shear (happens whenever non-uniform scaling and then rotating happens)

#### Properties

* Collinearity: Affine transformation preserves the straight lines
    * $\textbf{M} (A + t\textbf{v}) = \textbf{M} A + t\textbf{Mv}$ It is still a vector.
* Parallelism: They maintain the parallel lines
    * Performing affine transformation on twp parallel lines inside the coordinate system is essentially performing affine transformation on two parallel lines altogether.
* Ratio of lengths: They maintain the relative distances
* They don't necessarily preserve the angle or area
    * Non-uniform scaling
* (*) Convexity
* (*) Barycentres

#### 2-D Transformation

* Translation
$$ \begin{pmatrix}
q_1 \\ q_2 \\ 1 \end{pmatrix} = 
\begin{pmatrix}
1 & 0 & \phi_1 \\
0 & 1 & \phi_2 \\
0 & 0 & 1 \end{pmatrix}
\begin{pmatrix}
p_1 \\ p_2 \\ 1 \end{pmatrix} $$

* Rotation about the origin (counterclockwise)
$$ \begin{pmatrix}
q_1 \\ q_2 \\ 1 \end{pmatrix} =
\begin{pmatrix}
\cos(\theta) & -\sin(\theta) & 0 \\
\sin(\theta) & \cos(\theta) & 0 \\
0 & 0 & 1 \end{pmatrix}
\begin{pmatrix}
p_1 \\ p_2 \\ 1 \end{pmatrix} $$

Applying homogeneous coordinate method for vectors (replace 1 with 0)

* Scale by factor $(s_x, s_y)$ about the origin
$$ \begin{pmatrix}
s_x p_1 \\ s_y p_2 \\ 1 \end{pmatrix} =
\begin{pmatrix}
s_x & 0 & 0 \\
0 & s_y & 0 \\
0 & 0 & 1 \end{pmatrix}
\begin{pmatrix}
p_1 \\ p_2 \\ 1 \end{pmatrix} $$

Likewise to vectors

* Shear
    * It can occur by scaling axes non-uniformly and then rotate
    * It does not preserve the angles
    * It can be avoided by uniorming scaling
    * Horizontal - y-axis
    * Vertical - x-axis

$$ \begin{pmatrix}
q_1 \\ q_2 \\ 1 \end{pmatrix} =
\begin{pmatrix}
1 & h(\text{if h else 0}) & 0 \\
v(\text{if v else 0}) & 1 & 0 \\
0 & 0 & 1 \end{pmatrix}
\begin{pmatrix}
p_1 \\ p_2 \\ 0 \end{pmatrix} $$

#### Matrix

> OpenGL reads matrix in a transpose way.
> ```java
> float[] values = new float[] {
>       1, 0, 0, // x
>       0, 1, 0, // y
>       0, 0, 1  // phi
> };
> ```

## Composing/Decomposing Transformations

#### Composing Transformation

We can combine series of transformations by post-mulitplying their matrices.

#### Decomposing Transformation

Every 2D affine transformation can be decomposed as:
$$ M = M_{T} M_{R} M_{S} M_{SHEAR} $$

> If scaling is always uniform in both axes, the shear can be eliminated.

##### Example

Consider, we now have a matrix form to be decomposed
$$ \begin{pmatrix}
i_1 & j_1 & \phi_1 \\
i_2 & j_2 & \phi_2 \\
0 & 0 & 1 \end{pmatrix} $$

It is important to make the assumption that **uniform scaling and hence no shear**

$$ translation = (\phi_1, \phi_2, 1)^T $$
$$ rotation = atan2(i_2, i_1) $$
$$ scale = |i| $$

A sloid example:
Given $\begin{pmatrix} 0 & -2 & 1 \\ 2 & 0 & 2 \\ 0 & 0 & 1 \end{pmatrix}$

Translation: $(1, 2, 1)^T$ <br>
Rotation: $atan2(2, 0) = 90\degree$ <br>
Scale: $|i| = |j| = 2$
