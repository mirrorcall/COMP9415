# Week 4 - B

## Recap

* Transoformation pipeline
  * Transform a point in 3D: $$ P = (p_x,p_y,p_z)^T
  * Extend to homogeneous coordinates: $$ P = (p_x,p_y,p_z,1)^T
  * World matrix can be obtained by model matrix: $$ P_w = M_{model}P $$
  * Camera matrix can be obtained by camera matrix: $$ P_c = M_{view}P_w $$
  * CVV coordinates can be obtained by project matrix: $$ P_{cvv} = M_PP_c $$
  * Clip to remove points outside CVV
  * Perspective division to eliminate fourth component: $$ P_n = \frac{1}{p_w}P_{cvv} $$
  * Viewport transformation to window coordinates: $$ P_v = M_{viewport}P_n $$
  * `gl_Position` is the point int CVV coordinates
* Pseudodepth
  * Not linear (i.e. A curve rather than a straight line according to the ratio)
  * More precision for objects close to the near plane (the gradient tends to be smaller and smaller - recall the graph) - rounding errors get worse towards far plane
  
## Computing pseudodepth for fragment

Bilinear interpolation is lerping in 2 dimensions. It works for any polygon.

![Polygon](polygon.md)
