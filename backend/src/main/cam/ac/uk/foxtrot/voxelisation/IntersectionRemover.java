package cam.ac.uk.foxtrot.voxelisation;

import cam.ac.uk.foxtrot.sidefiller.Point;
import cam.ac.uk.foxtrot.sidefiller.Polygon;
import cam.ac.uk.foxtrot.sidefiller.SideFiller;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class IntersectionRemover
{

    private Point3dPolygon[] combinedArray = new Point3dPolygon[0];     //array of non-intersecting polygons with their holes
    private Point3d[][] polygonArray = new Point3d[0][];                //array of non-intersecting polygons without their holes
    private Point3d[][] holeArray = new Point3d[0][];                   //array of all holes in non-intersecting representation
    private double z;                                                   //value of z if projected to x-y plane, y if x-z plane, x if y-z plane
    private ProjectionUtils.ProjectionFace projectionFace;              //which face mesh was projected onto

    /**
     * Getter for combined array
     */
    public Point3dPolygon[] getCombinedArray()
    {
        return combinedArray;
    }

    /**
     * Getter for polygon array
     */
    public Point3d[][] getPolygonArray()
    {
        return polygonArray;
    }

    /**
     * Getter for hole array
     */
    public Point3d[][] getHoleArray()
    {
        return holeArray;
    }

    public IntersectionRemover(Point3d[] originalCoordinates, ProjectionUtils.ProjectionFace projectionFace)
    {
        this.projectionFace = projectionFace;
        ArrayList<Area> triangleList = new ArrayList<>();
        convertBetweenPlanes(originalCoordinates);
        if (originalCoordinates.length > 0)
        {
            z = originalCoordinates[0].z;
        }
        for (int i = 0; i < originalCoordinates.length; i += 3)
        { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            Point3d[] points = {originalCoordinates[i], originalCoordinates[i + 1], originalCoordinates[i + 2]};
            Point3dPolygon triangle = new Point3dPolygon(points, null);
         } for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            Point3d[] points = {originalCoordinates[i],originalCoordinates[i+1],originalCoordinates[i+2]};
            triangleList.add(toArea(points));
        }
        ArrayList<Point3d[]> polygons = generatePolygons(merge(triangleList));
        combinedArray = determinePolygonsAndHoles(polygons).toArray(new Point3dPolygon[0]);
        generateArrays();
    }

    private Area merge(List<Area> areas) {
        Area result = new Area();
        int length = areas.size();
        if (length == 1) {
            result = areas.get(0);
        } if (length > 1) {
            int i = length/2;
            result = merge(areas.subList(0,i));
            result.add(merge(areas.subList(i,length)));
        }
        return result;
    }

    /**
     * Use the list of merged polgyons to create the necessary polygon and hole arrays
     */
    private void generateArrays()
    {
        polygonArray = new Point3d[combinedArray.length][];
        ArrayList<Point3d[]> holeList = new ArrayList<>();
        for (int i = 0; i < combinedArray.length; i++)
        {
            Point3dPolygon polygon = combinedArray[i];
            convertBetweenPlanes(polygon.getExterior());
            polygonArray[i] = polygon.getExterior();
            Point3d[][] holes = polygon.getHoles();
            for (Point3d[] hole : holes)
            {
                convertBetweenPlanes(hole);
                holeList.add(hole);
            }
        }
        holeArray = holeList.toArray(new Point3d[0][]);
    }

    /**
     * Convert points to corresponding Area
     *
     * @param points Points representing a polygon without holes
     * @return Area corresponding to path represented by points
     */
    private Area toArea(Point3d[] points)
    {
        Path2D.Double result = new Path2D.Double();
        int length = points.length;
        if (length > 0)
        {
            Point3d point = points[0];
            result.moveTo(point.x, point.y);
        }
        for (int i = 1; i < length; i++)
        {
            Point3d point = points[i];
            result.lineTo(point.x, point.y);
        }
        if (length > 0)
        {
            Point3d point = points[0];
            result.lineTo(point.x, point.y);
        }
        return new Area(result);
    }

    /**
     * Creates ArrayList of polygons from an Area
     *
     * @param area Area consisting of polygon with holes
     * @return List with exterior of polygon as first element and the holes as the remaining elements
     */
    private ArrayList<Point3d[]> generatePolygons(Area area)
    {
        ArrayList<Point3d[]> result = new ArrayList<>();
        ArrayList<Point3d> polygon = new ArrayList<>();
        PathIterator iterator = area.getPathIterator(null);
        double[] doubles = new double[6];
        while (!iterator.isDone())
        {
            int type = iterator.currentSegment(doubles);
            Point3d currentPoint = new Point3d(doubles[0], doubles[1], z);
            if (type != PathIterator.SEG_CLOSE)
            { // reached end of current polygon
                polygon.add(currentPoint);
            }
            else
            {
                if (polygon.get(0).equals(polygon.get(polygon.size() - 1)))
                {
                    polygon.remove(polygon.size() - 1);
                }
                result.add(polygon.toArray(new Point3d[0]));
                polygon = new ArrayList<>();
            }
            iterator.next();
        }
        return result;
    }

    /**
     * Convert coordinates between x-y and original plane
     *
     * @param coordinates Coordinates to be converted
     */
    private void convertBetweenPlanes(Point3d[] coordinates)
    {
        switch (projectionFace)
        {
            case ZX0:
                zx(coordinates);
                break;
            case ZX1:
                zx(coordinates);
                break;
            case ZY0:
                zy(coordinates);
                break;
            case ZY1:
                zy(coordinates);
                break;
            default:
                break;
        }
    }

    /**
     * Rotate between x-y and x-z plane
     *
     * @param coordinates Coordinates that should be rotated
     */
    private void zx(Point3d[] coordinates)
    {
        int length = coordinates.length;
        for (int i = 0; i < length; i++)
        {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.x, point.z, point.y);
        }
    }

    /**
     * Rotate between x-y and y-z plane
     *
     * @param coordinates Coordinates that should be rotated
     */
    private void zy(Point3d[] coordinates)
    {
        int length = coordinates.length;
        for (int i = 0; i < length; i++)
        {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.z, point.y, point.x);
        }
    }

    private ArrayList<Point3dPolygon> determinePolygonsAndHoles(ArrayList<Point3d[]> unsortedPolygons)
    {
        // transform between spaces // TODO remove this and modify Point3dPolygon
        ArrayList<Point3dPolygon> result = new ArrayList<>();
        double h = unsortedPolygons.get(0)[0].z;
        ArrayList<Polygon> polygons = new ArrayList<>();
        for(int i = 0; i < unsortedPolygons.size(); i++)
        {
            Polygon poly = new Polygon();
            for(int j = 0; j < unsortedPolygons.get(i).length; j++)
                poly.addPoint(new Point2d(unsortedPolygons.get(i)[j].x,unsortedPolygons.get(i)[j].y));
            polygons.add(poly);
        }

        // set the needed intial parameters
        int polyCnt = polygons.size();
        if (polyCnt <= 0)
        {
            // no polygons need to be added
            return new ArrayList<>();
        }
        for (int i = 0; i < polyCnt; i++)
        {
            polygons.get(i).calculateVolume();
            polygons.get(i).calctulateIsFace(true);
        }

        // sort all the polygons in ascending order by volume
        Collections.sort(polygons, (Polygon p1, Polygon p2) -> p1.getVolume() < p2.getVolume() ? -1 : 1);

        // create the tree
        for (int i = 0; i < polyCnt; i++)
        {
            Polygon prev = polygons.get(i);
            if (!prev.wasVisited())
            {
                prev.visit();
                Polygon curr;
                for (int j = i + 1; j < polyCnt; j++)
                {
                    curr = polygons.get(j);
                    // go through all the polygons bigger than main
                    if (SideFiller.pointIsInsidePolygon(prev.getPoint(0), curr))
                    {
                        // if one of the points of the smaller polygon
                        // is within the bigger one, the entire first
                        // polygon is within the second one
                        prev.isNowInsideOf(curr);
                        curr.isNowOutsideOf(prev);
                        if (curr.wasVisited())
                            break;
                        curr.visit();
                        prev = curr;
                    }
                }
            }
        }
        LinkedList<Polygon> queue = new LinkedList<>();

        // do a depth first search and prepare the arrays
        while (!queue.isEmpty())
        {
            Polygon curr = queue.pop();
            Point3d[] ext = transformToPoint3dArray(curr, 2, h, curr.isAFace());

            Point3d[][] hls = new Point3d[curr.getBelow().size()][];
            int pos = 0;
            // add all the second level polygons to the queue
            Iterator<Polygon> itHole = curr.getBelow().iterator();
            while (itHole.hasNext())
            {
                Polygon hole = itHole.next();
                hls[pos] = transformToPoint3dArray(hole, 2, h, !hole.isAFace());
                pos++;

                Iterator<Polygon> itPoly = hole.getBelow().iterator();
                while (itPoly.hasNext())
                {
                    queue.add(itPoly.next());
                }
            }
            result.add(new Point3dPolygon(ext, hls));
        }

        return result;
    }

    public static Point3d[] transformToPoint3dArray(Polygon p, int ignore, double h, boolean doNotReverse)
    {
        int cnt = p.getSize();
        Point3d[] res = new Point3d[cnt];
        for (int i = 0; i < cnt; i++)
        {
            double[] coord = new double[3];
            coord[ignore] = h;
            coord[(ignore + 1) % 3] = p.getPoint(i).x;
            coord[(ignore + 2) % 3] = p.getPoint(i).y;
            Point3d curr = new Point3d(coord);
            if(doNotReverse)
                res[i] = curr;
            else
                res[cnt-1-i] = curr;
        }
        return res;
    }

    public static void main(String[] args) {
        Point3d[] coordinates = new Point3d[]{new Point3d(0,0,0),new Point3d(2,0,0),new Point3d(1,2,0),
                                              new Point3d(3,0,0),new Point3d(5,0,0),new Point3d(4,2,0),
                                              new Point3d(1,0,0),new Point3d(4,0,0),new Point3d(2.5,2,0),
                                              new Point3d(0,1.5,0),new Point3d(5,1.5,0),new Point3d(2.5,5,0)};
        IntersectionRemover intersectionRemover = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        System.out.println(Arrays.deepToString(intersectionRemover.getPolygonArray()));
        System.out.println(Arrays.deepToString(intersectionRemover.getHoleArray()));
    }
}
