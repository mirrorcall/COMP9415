# Introduction

## Transformation (2D)

Transformation includes following operations:

1. `translate(x,y)`: Translation is the process of moving and object in space without changing any other parameters besides spacial positions (e.g., SHAPE/SCALE and etc won't change)

2. `rotate(degrees)`: Rotation is rotating objects around the origin (i.e., the object will only be rotated around a certain point - origin).

3. `scale(x,y)`:Scaling is to scale along both axes (or scale across only one axis), -1 would flip the original axis.

**All the operations, other than `translate`, will not modify the position of the origin**

> All the transformations can be done by designed compositions (transformed positions of each component (not preferred)
> Extrinsic: An object being transformed or altered within a fixed co-ordinate system
> Intrinsic: The co-ordinate system of the object being transformed (i.e., instead of the object itself, the co-ordinate system is rather being transformed - vector-like).

## Model transformation

* Model Transformation: describes how a local coordinate system maps to the world coordinate system.

* Each object in a scene has its own local coordinate system.

#### Coordinate frames

Coordinate system is defined by a coordinate frame: frame defining the direction and scale of the x and y-axes.

#### Identity frame

* an origin at (0,0)
* y-axis vertical and of length 1
* x-axis horizontal and of length 1


#### Order matters!!!

* translate then rotate != rotate then translate
* translate then scale != scale then translate
* rotate then scale != scale then rotate

The reason of that is mainly because that this is a intrinsic transformation. In other words, the object is indirectly transformed based on co-ordinate system


## More about transformations

#### More about Rotations

Non-uniform scaling then rotating: Scale by different amounts in the x direction to the y direction and then rotate will result in unexpected results.

Rotating about an arbitrary point: rotation can be operated around another coordinate other than the origin


## Revision on Vectors

#### Construct a vector by two points

Example:
Point A ------------------------> Point B
(v1, v2)------------------------> (u1, u2)

Vector AB = (u1-v1, u2-v2)

> Vector = Destination - Source

#### Get a point by a point and the vector between them

Example:
Point A ------------------------> Point B
                Vector v

Point A + v = Point B

> Destination = Source + Vector

#### Arithmetic Operations

1. By adding components: AB = (u1, u2); AC = (v1, v2)
</br>
BC = AB + AC
CB = AC + AB

> Vector (SD) = [S]ource_Vector + [D]estination_Vector

2. By subtracting components: AB = (u1, u2); AC = (v1, v2)
</br>
BC = AC - AB
CB = AB - AC

> Vector (SD) = [D]estination_Vector - [S]ource_Vector

3. Magnitude (length)

$$ \abs{v} = \sqrt{v_1^2 + v_2^2 + \cdots + v_n^2} $$

4. Normalisation (direction)

$$ \hat{v} = \frac{v}{\abs{v}} $$
$$ \abs{\hat{v}} = 1 $$

> Zero vector cannot be normalised.

5. Dot product

$$ u \cdot v = u_1v_1 + u_2v_2 + \cdots + u_nv_n $$

* $ u \cdot v = v \cdot u $
* $ (au) \cdot v = a(u \cdot v) $
* $u \cdot (v + w) = u \cdot v + u \cdot w$
* $u \cdot u = \abs{u}^2$

6. Angles

$$ u \cdot v = \abs{u} \abs{v} cos \theta $$
$$ cos \theta = \hat{u} \cdot \hat{v} $$
$$ u \cdot v > 0 => \theta < 90 $$
$$ u \cdot v = 0 => \theta = 90 $$
$$ u \cdot v < 0 => \theta > 90 $$

7. Normals in 2D

If two vectors are perpendicular, their dot product is 0. I.e., If n = (x,y) is a normal to p = (a,b), then p * n = a*x + b*y = 0. </br>
Unless there is a zero vector, either n = (-y,x) or n = (y,-x)

8. Cross product in 3D

CAN BE USED TO FIND NORMALS

$$ a \times b = -(b \times a) $$
$$ a \cdot (a \times b) = 0 $$
$$ b \cdot (a \times b) = 0 $$


