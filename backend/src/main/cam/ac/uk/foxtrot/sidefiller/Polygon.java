package cam.ac.uk.foxtrot.sidefiller;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.HashSet;

public class Polygon
{
    private ArrayList<Point2d> points;  // the array of internal points
    private Point2d centerOfMass;       // the centre of mass of the polygon
    private boolean isFace;             // is true if the polygon is a face
    private double V;                   // the volume of this polygon
    private HashSet<Polygon> outsideOf; // the polygons which are directly below this in the hierarchy
    private Polygon insideOf;           // the polygon which is above this in the hierarchy
    private boolean visited;            // if the polygon was visited in the creation of the hierarchy

    public Polygon()
    {
        points = new ArrayList<>();
        centerOfMass = null;
        isFace = false;
        outsideOf = new HashSet<>();
        insideOf = null;
        visited = false;
        V = 0;
    }

    public void addPoint(Point p)
    {
        points.add(new Point2d(p.getA()));
    }

    public void addPoint(Point2d p)
    {
        points.add(p);
    }

    public Point2d getPoint(int i)
    {
        return points.get(i);
    }

    public int getSize()
    {
        return points.size();
    }

    public void setParameters(boolean clockwisePolygon)
    {
        calculateCenterOfMass();
        calctulateIsFace(clockwisePolygon);
        calculateVolume();
    }

    private void calculateCenterOfMass()
    {
        centerOfMass = new Point2d(0, 0);
        int cnt = points.size();
        for (int i = 0; i < cnt; i++)
        {
            centerOfMass.x += points.get(i).x;
            centerOfMass.y += points.get(i).y;
        }
        centerOfMass.x /= cnt;
        centerOfMass.y /= cnt;
    }

    public void calctulateIsFace(boolean clockwisePolygon)
    {
        int botlef = findBotLef();
        Point2d A = points.get(botlef);
        Point2d B = points.get((botlef + 1) % points.size());
        Point2d C = points.get((botlef + points.size() - 1) % points.size());
        double prod = vectorProd(A, B, C);
        isFace = (prod > 0) == clockwisePolygon;
    }

    private int findBotLef()
    {
        int idx = 0;
        Point2d botlef = points.get(0);
        int cnt = points.size();
        for (int i = 1; i < cnt; i++)
        {
            Point2d curr = points.get(i);
            if (curr.x < botlef.x && curr.y < botlef.y)
            {
                botlef = curr;
                idx = i;
            }
        }
        return idx;
    }

    public void calculateVolume()
    {
        V = 0;
        int cnt = points.size();
        Point2d origin = new Point2d(0, 0);
        for (int i = 1; i < cnt; i++)
        {
            V += signedAreaFromOrigin(points.get(i), points.get(i - 1));
        }
        V = Math.abs(V);
    }

    /**
     * Returns the z coordinate of the vector product ABxAC.
     */
    private double vectorProd(Point2d A, Point2d B, Point2d C)
    {
        return (B.x - A.x) * (C.y - A.y) - (B.y - A.y) * (C.x - A.x);
    }

    private double signedAreaFromOrigin(Point2d A, Point2d B)
    {
        return (A.x * B.y - A.y * B.x) / 2;
    }

    public double getVolume()
    {
        return V;
    }

    public void visit()
    {
        visited = true;
    }

    public void unvisit()
    {
        visited = false;
    }

    public boolean wasVisited()
    {
        return visited;
    }

    public HashSet<Polygon> getBelow()
    {
        return outsideOf;
    }

    public Polygon getAbove()
    {
        return insideOf;
    }

    public void isNowOutsideOf(Polygon inner)
    {
        outsideOf.add(inner);
    }

    public void isNowInsideOf(Polygon outer)
    {
        insideOf = outer;
    }

    public Point2d getCenterOfMass()
    {
        return centerOfMass;
    }

    public int getNumberOfVertices()
    {
        return points.size();
    }

    public boolean hasAbove()
    {
        return insideOf != null;
    }

    public boolean isAFace()
    {
        return isFace;
    }
}
