# Week 2

## Scene Trees

To draw (calculate) an compositing object containing lots of components (of this object), instead of drawing it piece by piece (by piece, it means vertex), we represent those complex scenes with a *scene tree*. Each node represents a component and each edge represents the transformation to get from the parent component's coordinate system to the child's

> Scene Tree
> It is a general data structure commonly used by vector-based graphics editing applications and modern computer games.
> A scene tree is a collection of nodes in a graph or tree structure. A tree node may have many children but only a single parent.
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
   /         /        |         \            \
LU Arm    LU Leg     Head     RU Leg       RU Arm
  |          |                   |            |
LL Arm    LL Leg              RL Leg       RL Arm         
  |          |                   |            |
L Hand    L Foot              R Foot       R Hand
```

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

TRS are short for translate(T), rotate(R) and scale(S) and generally abbreviated to *M*, and each object (i.e., component mentioned above) can be created in their own local coordinate system (i.e., their own frames) and passes that onto its children.<\br>
When a node in the graph is transformed, all **its children move with it**
