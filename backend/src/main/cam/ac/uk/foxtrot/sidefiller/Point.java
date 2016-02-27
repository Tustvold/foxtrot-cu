package cam.ac.uk.foxtrot.sidefiller;

import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Milos on 25/02/2016.
 */
public class Point
{
    private Point2d A;                 // the 2d points coordinates
    private HashSet<Point> neighbours; // the set of points reachable from this with only one jump (should be 1 in the end)
    private HashSet<Point> parents;    // the set of points which directly point ot this (should be 0 or 1 in the end)
    private int inSet;                 // the set the point belongs to
    private int edging_type;           // determines if the point is an edge
    private boolean labeled;           // used for polygon creation

    public Point(double x, double y)
    {
        this.A = new Point2d(x, y);
        neighbours = new HashSet<>();
        parents = new HashSet<>();
        inSet = -1;
        labeled = false;
    }

    public Point(Point2d A)
    {
        this.A = new Point2d(A.x, A.y);
        neighbours = new HashSet<>();
        parents = new HashSet<>();
        inSet = -1;
        labeled = false;
    }

    public Point(double x, double y, Point B)
    {
        this.A = new Point2d(x, y);
        neighbours = new HashSet<>();
        parents = new HashSet<>();
        neighbours.add(B);
        inSet = -1;
        labeled = false;
    }

    /**
     * Copy constructor which ignores neighbours
     */
    public Point(Point X)
    {
        this.A = new Point2d(X.A);
        this.neighbours = new HashSet<>();
        this.parents = new HashSet<>();
        this.inSet = X.inSet;
        labeled = false;
    }

    public void label()
    {
        labeled = true;
    }
    public boolean isLabeled()
    {
        return labeled;
    }

    public Point2d getA()
    {
        return A;
    }

    public double getX()
    {
        return A.x;
    }

    public double getY()
    {
        return A.y;
    }

    /**
     * Returns true if the point is on the edge of the bounding square.
     */
    public boolean isOnSquareEdge()
    {
        return MeshVoxeliser.areIdentical(A.x, 1)
                || MeshVoxeliser.areIdentical(A.x, 0)
                || MeshVoxeliser.areIdentical(A.y, 1)
                || MeshVoxeliser.areIdentical(A.y, 0);
    }

    public void setInSet(int set)
    {
        inSet = set;
    }

    public int getInSet()
    {
        return inSet;
    }

    public boolean isUnconnected()
    {
        return neighbours.isEmpty() && parents.isEmpty();
    }

    public HashSet<Point> getNeighbours()
    {
        return neighbours;
    }

    /**
     * Adds the given neighbour.
     */
    public void addNeighbour(Point p)
    {
        neighbours.add(p);
    }

    /**
     * Removes the given neighbour.
     */
    public boolean removeNeighbour(Point p)
    {
        return neighbours.remove(p);
    }

    /**
     * Returns true if p is a directed neighbour of this.
     */
    public boolean hasNeighbour(Point p)
    {
        return neighbours.contains(p);
    }

    public Point getFirstNeighbour()
    {
        Iterator<Point> it = neighbours.iterator();
        return it.next();
    }

    /**
     * Adds the given neighbour.
     */
    public void addParent(Point p)
    {
        parents.add(p);
    }

    /**
     * Removes the given neighbour.
     */
    public boolean removeParent(Point p)
    {
        return parents.remove(p);
    }

    /**
     * Returns true if p is a directed neighbour of this.
     */
    public boolean hasParent(Point p)
    {
        return parents.contains(p);
    }

    public Point getFirstParent()
    {
        Iterator<Point> it = parents.iterator();
        if (it == null || !it.hasNext())
            return null;
        return it.next();
    }

    /**
     * Determines the edging type of the point:
     * edging_type = -1 - the point is a starting point
     *             =  1 - the point is an ending point
     *             =  0 - the point is not relevant
     */
    public void determineEdgingType()
    {
        edging_type = 0;
        if(isOnSquareEdge())
        {
            if(!parents.isEmpty() && neighbours.isEmpty())
            {
                // the point has no children, so it is a starting point
                edging_type = -1;
            }
            else if(parents.isEmpty() && !neighbours.isEmpty())
            {
                // the point has no parents, so it is a ending point
                edging_type = 1;
            }
        }
    }

    public int getEdgingType()
    {
        return edging_type;
    }

    public boolean equals(Point B)
    {
        return MeshVoxeliser.areIdentical(A.x, B.getX()) && MeshVoxeliser.areIdentical(A.y, B.getY());
    }

    public boolean equals(Point2d B)
    {
        return MeshVoxeliser.areIdentical(A.x, B.x) && MeshVoxeliser.areIdentical(A.y, B.y);
    }
}
