package cam.ac.uk.foxtrot.sidefiller;

import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.jmx.remote.internal.ArrayQueue;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class SideFiller
{
    private Block[][][] blocks; // the block matrix whose blocks need side filling
    private int dim[];          // dimensions of the input block matrix

    /**
     * @return Returns the blocks stored in the Side Filler.
     */
    public Block[][][] getBlocks()
    {
        return blocks;
    }

    public SideFiller(Block[][][] blocks)
    {
        this.blocks = blocks;
        dim = new int[3];
        dim[0] = blocks.length;
        dim[1] = blocks[0].length;
        dim[2] = blocks[0][0].length;
    }

    /**
     * Fills all the sides of all blocks in the array stored int the SideFiller.
     */
    public void fillAllSides()
    {
        for (int x = 0; x < dim[0]; x++)
        {
            for (int y = 0; y < dim[1]; y++)
            {
                for (int z = 0; z < dim[2]; z++)
                {
                    if (blocks[x][y][z] == null || !blocks[x][y][z].isCustom())
                    {
                        continue;
                    }
                    ArrayList<Point3d> newTriangles = new ArrayList<>();

                    for (int ignore = 0; ignore < 3; ignore++)
                    {
                        newTriangles.addAll(fillSingleSide(x, y, z, ignore, true));
                        newTriangles.addAll(fillSingleSide(x, y, z, ignore, false));
                    }
                    blocks[x][y][z].addTriangles(newTriangles); // add all the newly created triangles
                    blocks[x][y][z].setInternalDim(); // determine the internal dimensions of the block
                }
            }
        }
    }

    /**
     * Fills a single side of a single block of the block matrix.
     *
     * @param ignore the coordinate to ignore (0 -> x, 1 -> y, 2 -> z)
     * @param top true if the ignore coordinate is 1
     * @return
     */
    public ArrayList<Point3d> fillSingleSide(int x, int y, int z, int ignore, boolean top)
    {
        // checks for adjacency with full block
        // -------------------------------------------------------------------------------------------------------------
        int diff, h;
        if (top) diff = 1;
        else diff = -1;
        if (top) h = 1;
        else h = 0;
        Point3i adjacent = new Point3i(x, y, z);
        switch (ignore)
        {
            case 0:
                // ignoring x
                adjacent.x += diff;
                break;
            case 1:
                adjacent.y += diff;
                break;
            case 2:
                adjacent.z += diff;
                break;
        }
        if (isInGrid(adjacent)
                && blocks[adjacent.x][adjacent.y][adjacent.z] != null
                && !blocks[adjacent.x][adjacent.y][adjacent.z].isCustom())
        {
            // the side is adjacent to a full block, so we just return two triangles representing this side
            return makeSquare(ignore, h);
        }
        // -------------------------------------------------------------------------------------------------------------


        // create the appropriate polygons on the given side
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        Block block = blocks[x][y][z];
        ArrayList<Point3d> triangles = block.getTriangles();          // all of the triangles from the block
        ArrayList<Point> initPoints = new ArrayList<>();              // the initial set of points on the face
        ArrayList<Point> interPoints = new ArrayList<>();             // the new set of with no duplicate points
        ArrayList<Point> finalPoints = new ArrayList<>();             // the final points with triangles eroded
        ArrayList<ArrayList<Point>> edgingPoints = new ArrayList<>(); // the four element array of sides of the face
        ArrayList<Polygon> polygons = new ArrayList<>();              // the list of all extracted polygons
        int triangleCnt = triangles.size() / 3;                       // number of triangles in the current array
        double already_covered = 0;                                   // the area of the face initially covered by triangles
        boolean clockwisePolygon = top;                               // the orientation of initial holes on this face

        // initialisation step - loading the initPoints and sorting them
        // -------------------------------------------------------------------------------------------------------------
        double[] fir = new double[3];
        double[] sec = new double[3];
        for (int i = 0; i < triangleCnt; i++)
        {
            // iterate through all the edges of the given triangle
            int edges_added = 0;
            for (int j = 0; j < 3; j++)
            {
                triangles.get(3 * i + j).get(fir);
                triangles.get(3 * i + (j + 1) % 3).get(sec);

                if (MeshVoxeliser.areIdentical(fir[ignore], h) && MeshVoxeliser.areIdentical(sec[ignore], h))
                {
                    // both points are in the plane, so we add them to the point list
                    Point B = new Point(sec[(ignore + 1) % 3], sec[(ignore + 2) % 3]);
                    Point A = new Point(fir[(ignore + 1) % 3], fir[(ignore + 2) % 3], B);
                    initPoints.add(A);
                    initPoints.add(B);
                    edges_added++;
                }
            }
            if (edges_added == 3)
            {
                // we added all three edges, so we need to add the area to the total area
                double area = unsignedTriangleArea(triangles.get(3 * i), triangles.get(3 * i + 1), triangles.get(3 * i + 2));
                already_covered += area;
            }
        }
        if (initPoints.size() == 0)
        {
            // there are no polygons to return, so we just terminate
            return new ArrayList<>();
        }

        // sort our points
        initPoints.sort(new PointComparator());
        // -------------------------------------------------------------------------------------------------------------

        // filling up of the interPoints by iterating through the sorted sets of initPoints
        // -------------------------------------------------------------------------------------------------------------
        // initialise the first set
        int set = 0;
        initPoints.get(0).setInSet(0);

        // initial set labelling loop
        boolean notDone = true;
        int pos = 1;
        int pointCnt = initPoints.size();
        while (notDone)
        {
            for (; pos < pointCnt && initPoints.get(pos).equals(initPoints.get(pos - 1)); pos++)
            {
                // label the point
                initPoints.get(pos).setInSet(set);
            }

            // add the last point of the set as its representative
            interPoints.add(new Point(initPoints.get(pos - 1)));

            if (pos < pointCnt)
            {
                // we have more to go, so open new set and set its representative
                set++;
                initPoints.get(pos).setInSet(set);
            }
            else
            {
                // we are done
                notDone = false;
            }
            // lastly move the pos cursor, so that we include the last point in the calculation
            pos++;
        }
        // -------------------------------------------------------------------------------------------------------------


        // creating the finalPoints from interPoints - edge connection and triangle erosion
        // -------------------------------------------------------------------------------------------------------------
        pointCnt = initPoints.size();
        for (int i = 0; i < pointCnt; i++)
        {
            Point currFrom = initPoints.get(i);
            Iterator<Point> it = currFrom.getNeighbours().iterator();
            while (it.hasNext())
            {
                Point currTo = it.next();
                Point realFrom = interPoints.get(currFrom.getInSet());
                Point realTo = interPoints.get(currTo.getInSet());

                if(realFrom == realTo)
                    continue;

                if (realTo.hasNeighbour(realFrom))
                {
                    // we are about to introduce a double edge, so we
                    // delete the existing one and add nothing
                    realTo.removeNeighbour(realFrom);
                    realFrom.removeParent(realTo);
                }
                else
                {
                    // we add the edge since we are introducing no double edges
                    realFrom.addNeighbour(realTo);
                    realTo.addParent(realFrom);
                }
            }
        }

        // now we remove the edges which are going along the side of the square, but in the wrong orientation
        pointCnt = interPoints.size();
        for (int i = 0; i < pointCnt; i++)
        {
            Point currFrom = interPoints.get(i);
            Iterator<Point> it = currFrom.getNeighbours().iterator();
            while (it.hasNext())
            {
                Point currTo = it.next();
                if (areOnSideAndBadlyOriented(currFrom, currTo, clockwisePolygon))
                {
                    // we remove the appropriate edge
                    currFrom.removeNeighbour(currTo);
                    currTo.removeParent(currFrom);
                }
            }
        }

        // finally we just copy the connected points to the final array
        pointCnt = interPoints.size();
        for (int i = 0; i < pointCnt; i++)
        {
            Point curr = interPoints.get(i);
            if (!curr.isUnconnected())
            {
                finalPoints.add(curr);
            }
        }
        // -------------------------------------------------------------------------------------------------------------

        // connecting anything which is left unconnected on the sides of the face
        // -------------------------------------------------------------------------------------------------------------
        /*
            The side labels of the square.
            y      1
            ^    - - -
            |   |     |
            | 0 |     | 2
            |    - - -
            |      3
             - - - - - - - > x

            We connect points in a clockwise manor if we are on 'top' of the cube, because this
            equates to detecting holes on the face. The same is done if we are at z = 0, but
            the points are connected in a counter clockwise manor for same reasons.
        */
        // we prepare the edge bins for all four sides of the square
        for (int i = 0; i < 4; i++)
        {
            edgingPoints.add(new ArrayList<>());
        }
        pointCnt = finalPoints.size();
        int edgingCnt = 0; // number of points which are properly on the sides
        for (int i = 0; i < pointCnt; i++)
        {
            Point curr = finalPoints.get(i);
            curr.determineEdgingType();
            if (curr.getEdgingType() != 0)
            {
                // add the point to the appropriate side bins
                if (MeshVoxeliser.areIdentical(curr.getX(), 0)) edgingPoints.get(0).add(curr);
                if (MeshVoxeliser.areIdentical(curr.getY(), 1)) edgingPoints.get(1).add(curr);
                if (MeshVoxeliser.areIdentical(curr.getX(), 1)) edgingPoints.get(2).add(curr);
                if (MeshVoxeliser.areIdentical(curr.getY(), 0)) edgingPoints.get(3).add(curr);
                edgingCnt++;
            }
        }

        if (edgingCnt > 0)
        {
            // sort the respective bins depending on the orientation (CW/CCW)
            final boolean increasing = clockwisePolygon;
            Collections.sort(edgingPoints.get(0), (Point p1, Point p2) -> isLessThan(p1.getY(), p2.getY(), increasing) ? -1 : 1);
            Collections.sort(edgingPoints.get(1), (Point p1, Point p2) -> isLessThan(p1.getX(), p2.getX(), increasing) ? -1 : 1);
            Collections.sort(edgingPoints.get(2), (Point p1, Point p2) -> isLessThan(p1.getY(), p2.getY(), !increasing) ? -1 : 1);
            Collections.sort(edgingPoints.get(3), (Point p1, Point p2) -> isLessThan(p1.getX(), p2.getX(), !increasing) ? -1 : 1);

            // find the first non-empty side
            int firstNonEmpty = 0;
            for (; firstNonEmpty < 4 && edgingPoints.get(firstNonEmpty).isEmpty(); firstNonEmpty++) ;

            // iterate through all the sides, starting with the first non-empty one
            for (int i = 0; i < 4; i++)
            {
                int curr = 0;
                int side;
                // determine which size it actually is, depending on direction
                if (clockwisePolygon)
                    side = (firstNonEmpty + i) % 4;
                else
                    side = (firstNonEmpty + 4 - i) % 4;

                ArrayList<Point> currLine = edgingPoints.get(side);
                int cnt = currLine.size();

                if (cnt == 0)
                    continue;

                // connect first point
                Point currPt = currLine.get(0);
                if (currPt.getEdgingType() == 1)
                {
                    // if the first point is an ending point, we need to see if the left corner of the side should be included
                    Point2d corner = getLeftPoint(side, clockwisePolygon);
                    if (!currPt.equals(corner))
                    {
                        // the corner needs to be added
                        Point newPt = new Point(corner);
                        currPt.addParent(newPt);
                        newPt.addNeighbour(currPt);
                        currPt.determineEdgingType();
                        newPt.determineEdgingType();
                        // add the corner to the previous bin in line
                        edgingPoints.get(prevSide(side, clockwisePolygon)).add(newPt);
                        finalPoints.add(newPt);
                    }
                }
                curr++;

                // connect intermediate points
                Point prevPt;
                for (; curr < cnt; curr++)
                {
                    currPt = currLine.get(curr);
                    prevPt = currLine.get(curr - 1);
                    if (prevPt.getEdgingType() == -1 && currPt.getEdgingType() == 1)
                    {
                        prevPt.addNeighbour(currPt);
                        currPt.addParent(prevPt);
                        prevPt.determineEdgingType();
                        currPt.determineEdgingType();
                    }
                }

                // connect last point
                currPt = currLine.get(cnt - 1);
                if (currPt.getEdgingType() == -1)
                {
                    // if the last point is a starting point, we need to see if the right corner of the side should be included
                    Point2d corner = getRightPoint(side, clockwisePolygon);
                    if (!currPt.equals(corner))
                    {
                        // the corner needs to be added
                        Point newPt = new Point(corner);
                        currPt.addNeighbour(newPt);
                        newPt.addParent(currPt);
                        currPt.determineEdgingType();
                        newPt.determineEdgingType();
                        // add the corner to the next bin in line
                        addToFront(edgingPoints.get(nextSide(side, clockwisePolygon)), newPt);
                        finalPoints.add(newPt);
                    }
                }
            }
        }
        // -------------------------------------------------------------------------------------------------------------

        // extract polygons from finalPoints
        // -------------------------------------------------------------------------------------------------------------
        // first remove the point itself from the adjacency lists, if it happens
        pointCnt = finalPoints.size();
        for(int i = 0; i < pointCnt; i++)
        {
            Point curr = finalPoints.get(i);
            if(curr.getFirstParent() == curr)
            {
                curr.removeParent(curr);
            }
            if(curr.getFirstNeighbour() == curr)
            {
                curr.removeNeighbour(curr);
            }
        }

        // continue to the actual polygon extraction
        if (pointCnt < 3)
        {
            return new ArrayList<>();
        }
        for (int i = 0; i < pointCnt; i++)
        {
            Point first = finalPoints.get(i);
            if (!first.isLabeled())
            {
                first.label();
                Polygon poly = new Polygon();
                poly.addPoint(first);
                // start adding the points in reverse order, because we want to reorient all the polygons
                Point curr = first.getFirstParent();
                while (curr != first && !curr.isLabeled())
                {
                    poly.addPoint(curr);
                    curr.label();
                    curr = curr.getFirstParent();
                }
                if (poly.getSize() >= 3 && curr == first)
                {
                    // if the polygon actually exists we add it
                    polygons.add(poly);
                }
            }
        }
        // -------------------------------------------------------------------------------------------------------------
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        // creating a hierarchy of polygons
        // -------------------------------------------------------------------------------------------------------------
        // set the needed intial parameters
        int polyCnt = polygons.size();
        if (polyCnt <= 0)
        {
            // no polygons need to be added
            return new ArrayList<>();
        }
        for (int i = 0; i < polyCnt; i++)
        {
            polygons.get(i).setParameters(clockwisePolygon);
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
                    if (pointIsInsidePolygon(prev.getPoint(0), curr))
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

        // test if we need to introduce bounding square
        boolean topHolesExist = false;
        boolean topFacesExist = false;
        for (int i = 0; i < polyCnt; i++)
        {
            Polygon curr = polygons.get(i);
            if (!curr.hasAbove())
            {
                if (curr.isAFace())
                    topFacesExist = true;
                else
                    topHolesExist = true;
            }
        }
        if (topHolesExist && !topFacesExist)
        {
            // the square is needed, so we add it
            Polygon square = make2DSquare(clockwisePolygon);
            for (int i = 0; i < polyCnt; i++)
            {
                Polygon curr = polygons.get(i);
                if (!curr.hasAbove() && !curr.isAFace())
                {
                    square.isNowOutsideOf(curr);
                    curr.isNowInsideOf(square);
                }
            }
            polygons.add(square);
        }
        else if (topHolesExist && topFacesExist)
        {
            // something is wrong with the mesh return empty
            return new ArrayList<>();
        }

        // finally prepare grounds for the triangulation
        ArrayList<Point3d> coordinates = new ArrayList<>();
        ArrayList<Integer> stripCounts = new ArrayList<>();
        ArrayList<Integer> contourCounts = new ArrayList<>();
        LinkedList<Polygon> queue = new LinkedList<>();

        // reset the visitings
        polyCnt = polygons.size();
        for (int i = 0; i < polyCnt; i++)
        {
            Polygon curr = polygons.get(i);
            curr.unvisit();
            if (!curr.hasAbove())
            {
                // add all the tree-tops to the queue
                queue.push(curr);
            }
        }

        // do a depth first search and prepare the arrays
        while (!queue.isEmpty())
        {
            Polygon curr = queue.pop();
            coordinates.addAll(transformToPoint3d(curr, ignore, h));
            stripCounts.add(curr.getSize());
            int contourCnt = 1;

            // add all the second level polygons to the queue
            Iterator<Polygon> itHole = curr.getBelow().iterator();
            while (itHole.hasNext())
            {
                Polygon hole = itHole.next();
                coordinates.addAll(transformToPoint3d(hole, ignore, h));
                stripCounts.add(hole.getSize());
                contourCnt++;

                Iterator<Polygon> itPoly = hole.getBelow().iterator();
                while (itPoly.hasNext())
                {
                    queue.add(itPoly.next());
                }
            }
            contourCounts.add(contourCnt);
        }

        // triangulate the result
        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(coordinates.toArray(new Point3d[coordinates.size()]));
        gi.setStripCounts(stripCounts.stream().mapToInt(i -> i).toArray());
        gi.setContourCounts(contourCounts.stream().mapToInt(i -> i).toArray());
        GeometryArray ga = gi.getGeometryArray();
        Point3d[] pts = new Point3d[ga.getVertexCount()];
        for (int i = 0; i < pts.length; i++)
        {
            pts[i] = new Point3d();
        }
        ga.getCoordinates(0, pts);

        // transform the output into something more acceptible
        ArrayList<Point3d> returnPoints = new ArrayList<>();
        for (int i = 0; i < pts.length; i++)
        {
            returnPoints.add(new Point3d(pts[i]));
        }

        return returnPoints;
        // -------------------------------------------------------------------------------------------------------------
    }

    private ArrayList<Point3d> transformToPoint3d(Polygon p, int ignore, int h)
    {
        ArrayList<Point3d> res = new ArrayList<>();
        int cnt = p.getSize();
        for (int i = 0; i < cnt; i++)
        {
            double[] coord = new double[3];
            coord[ignore] = h;
            coord[(ignore + 1) % 3] = p.getPoint(i).x;
            coord[(ignore + 2) % 3] = p.getPoint(i).y;
            Point3d curr = new Point3d(coord);
            res.add(curr);
        }
        return res;
    }

    public static boolean pointIsInsidePolygon(Point2d test, Polygon poly)
    {
        int nvert = poly.getNumberOfVertices();
        int i, j;
        boolean c = false;
        for (i = 0, j = nvert - 1; i < nvert; j = i++)
        {
            Point2d currI = poly.getPoint(i);
            Point2d currJ = poly.getPoint(j);
            if (((currI.y > test.y) != (currJ.y > test.y)) &&
                    (test.x < (currJ.x - currI.x) * (test.y - currI.y) / (currJ.y - currI.y) + currI.x))
            {
                c = !c;
            }
        }
        return c;
    }

    private boolean areOnSideAndBadlyOriented(Point from, Point to, boolean clokcwise)
    {
        /*
         The side labels of the square.
         y      1
         ^    - - -
         |   |     |
         | 0 |     | 2
         |    - - -
         |      3
          - - - - - - - > x
         */

        if (MeshVoxeliser.areIdentical(from.getX(), to.getX()))
        {
            if (MeshVoxeliser.areIdentical(0, from.getX()))
            {
                // side 0
                if (clokcwise)
                {
                    return from.getY() > to.getY();
                }
                else
                {
                    return from.getY() < to.getY();
                }
            }
            else if (MeshVoxeliser.areIdentical(1, from.getX()))
            {
                // side 2
                if (clokcwise)
                {
                    return from.getY() < to.getY();
                }
                else
                {
                    return from.getY() > to.getY();
                }
            }
        }
        else if (MeshVoxeliser.areIdentical(from.getY(), to.getY()))
        {
            if (MeshVoxeliser.areIdentical(1, from.getY()))
            {
                // side 1
                if (clokcwise)
                {
                    return from.getX() > to.getX();
                }
                else
                {
                    return from.getX() < to.getX();
                }
            }
            else if (MeshVoxeliser.areIdentical(0, from.getY()))
            {
                // side 3
                if (clokcwise)
                {
                    return from.getX() < to.getX();
                }
                else
                {
                    return from.getX() > to.getX();
                }
            }
        }
        return false;
    }

    private int prevSide(int side, boolean clockwise)
    {
        if (clockwise)
            return (side + 3) % 4;
        else
            return (side + 1) % 4;
    }

    private int nextSide(int side, boolean clockwise)
    {
        if (clockwise)
            return (side + 1) % 4;
        else
            return (side + 3) % 4;
    }

    private boolean isLessThan(double a, double b, boolean increasing)
    {
        if (increasing)
            return a < b;
        else
            return a > b;
    }

    private Point2d getLeftPoint(int side, boolean clockwise)
    {
        if (clockwise)
            return new Point2d(side / 2, ((side + 1) % 4) / 2);
        else
            return new Point2d(((side + 1) % 4) / 2, 1 - side / 2);
    }

    private Point2d getRightPoint(int side, boolean clockwise)
    {
        if (clockwise)
            return new Point2d(((side + 1) % 4) / 2, 1 - side / 2);
        else
            return new Point2d(side / 2, ((side + 1) % 4) / 2);
    }

    private void addToFront(ArrayList<Point> list, Point p)
    {
        int size = list.size();
        list.add(null);
        for (int i = size; i >= 1; i--)
        {
            list.set(i, list.get(i - 1));
        }
        list.set(0, p);
    }

    private double unsignedTriangleArea(Point3d A, Point3d B, Point3d C)
    {
        return vectorNorm(vectorProd(A, B, C)) / 2;
    }

    private double vectorNorm(Point3d A)
    {
        return Math.sqrt(A.x * A.x + A.y * A.y + A.z * A.z);
    }

    /**
     * Returns the vector product ABxAC.
     */
    private Point3d vectorProd(Point3d A, Point3d B, Point3d C)
    {
        Point3d result = new Point3d(0, 0, 0);
        result.x = (B.y - A.y) * (C.z - A.z) - (B.z - A.z) * (C.y - A.y);
        result.y = (B.z - A.z) * (C.x - A.x) - (B.x - A.x) * (C.z - A.z);
        result.z = (B.x - A.x) * (C.y - A.y) - (B.y - A.y) * (C.x - A.x);
        return result;
    }

    private boolean isInGrid(Point3i pt)
    {
        return 0 <= pt.x && pt.x < dim[0] && 0 <= pt.y && pt.y < dim[1] && 0 <= pt.z && pt.z < dim[2];
    }

    /**
     * Returns a outside facing square on the given side
     *
     * @param ignore
     * @param h
     * @return
     */
    private ArrayList<Point3d> makeSquare(int ignore, double h)
    {
        ArrayList<Point3d> square = new ArrayList<>();
        //xz01
        double[] pt = new double[3];
        pt[ignore] = h;
        pt[(ignore + 1) % 3] = 0;
        pt[(ignore + 2) % 3] = 1;
        square.add(new Point3d(pt));
        pt = new double[3];
        pt[ignore] = h;
        pt[(ignore + 1) % 3] = 0;
        pt[(ignore + 2) % 3] = 0;
        square.add(new Point3d(pt));
        pt = new double[3];
        pt[ignore] = h;
        pt[(ignore + 1) % 3] = 1;
        pt[(ignore + 2) % 3] = 1;
        square.add(new Point3d(pt));
/*
        square.add(new Point3d(0, 0, 1));
        square.add(new Point3d(0, 0, 0));
        square.add(new Point3d(1, 0, 1));
*/
        //xz02
        pt = new double[3];
        pt[ignore] = h;
        pt[(ignore + 1) % 3] = 1;
        pt[(ignore + 2) % 3] = 1;
        square.add(new Point3d(pt));
        pt = new double[3];
        pt[ignore] = h;
        pt[(ignore + 1) % 3] = 0;
        pt[(ignore + 2) % 3] = 0;
        square.add(new Point3d(pt));
        pt = new double[3];
        pt[ignore] = h;
        pt[(ignore + 1) % 3] = 1;
        pt[(ignore + 2) % 3] = 0;
        square.add(new Point3d(pt));
/*
        square.add(new Point3d(1, 0, 1));
        square.add(new Point3d(0, 0, 0));
        square.add(new Point3d(1, 0, 0));
*/
        if (h == 0)
        {
        }
        return square;
    }

    private Polygon make2DSquare(boolean clockwisePolygon)
    {
        Polygon square = new Polygon();
        if (clockwisePolygon)
        {
            square.addPoint(new Point2d(0, 0));
            square.addPoint(new Point2d(1, 0));
            square.addPoint(new Point2d(1, 1));
            square.addPoint(new Point2d(0, 1));
        }
        else
        {
            square.addPoint(new Point2d(0, 0));
            square.addPoint(new Point2d(0, 1));
            square.addPoint(new Point2d(1, 1));
            square.addPoint(new Point2d(1, 0));
        }
        square.setParameters(clockwisePolygon);
        return square;
    }

    /**
     * DEBUGGING and TESTING method!
     * Outputs a .obj file representing the current mesh subdivision within the block matrix.
     *
     * @param filename    name of file to which to write output
     * @param includeGrid if the x0z grid should be included
     */
    public void drawTrianglesFromBlocks(String filename, boolean includeGrid)
    {
        System.out.println("Preparing the sliced output...");
        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            //System.err.println(ex.getMessage());
            return;
        }

        int totalTriangles = 0;

        for (int x = 0; x < dim[0]; x++)
        {
            for (int z = 0; z < dim[2]; z++)
            {
                if (includeGrid)
                {
                    ArrayList<Point3d> triangles = makeHorizontalSquare();
                    totalTriangles += triangles.size() / 3;
                    for (int i = 0; i < triangles.size(); i++)
                    {
                        try
                        {
                            writer.write("v " + (triangles.get(i).x + x) + " "
                                    + (triangles.get(i).y) + " "
                                    + (triangles.get(i).z + z) + "\n");

                        } catch (IOException err)
                        {
                            System.err.println("Could not write blocks: " + err.getMessage());
                        }
                    }
                }
                for (int y = 0; y < dim[1]; y++)
                {
                    if (blocks[x][y][z] == null || !blocks[x][y][z].isCustom())
                        continue;
                    ArrayList<Point3d> triangles = new ArrayList<>(blocks[x][y][z].getTriangles());
                    //blocks[x][y][z].drawBlock("testing/output/blocks/block_" + x + "_" + y + "_" + z + ".obj");

                    totalTriangles += blocks[x][y][z].getTriangleCount();
                    for (int i = 0; i < blocks[x][y][z].getTriangleCount() * 3; i++)
                    {
                        try
                        {
                            writer.write("v " + (triangles.get(i).x + blocks[x][y][z].getPosition().x) + " "
                                    + (triangles.get(i).y + blocks[x][y][z].getPosition().y) + " "
                                    + (triangles.get(i).z + blocks[x][y][z].getPosition().z) + "\n");

                        } catch (IOException err)
                        {
                            System.err.println("Could not write blocks: " + err.getMessage());
                        }
                    }
                }
            }
        }

        for (int i = 1; i <= totalTriangles * 3; i += 3)
        {
            try
            {
                writer.write("f " + i + " " + (i + 1) + " " + (i + 2) + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write blocks: " + err.getMessage());
            }
        }
        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Sliced output created...");
    }

    public void drawTriangles(ArrayList<Point3d> trig, String filename)
    {
        System.out.println("Drawing triangles...");
        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            //System.err.println(ex.getMessage());
            return;
        }

        int trianglecnt = trig.size() / 3;
        for (int i = 0; i < trianglecnt * 3; i++)
        {
            try
            {
                writer.write("v " + trig.get(i).x + " "
                        + trig.get(i).y + " "
                        + trig.get(i).z + "\n");

            } catch (IOException err)
            {
                System.err.println("Could not write triangles: " + err.getMessage());
            }
        }

        for (int i = 1; i <= trianglecnt * 3; i += 3)
        {
            try
            {
                writer.write("f " + i + " " + (i + 1) + " " + (i + 2) + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write triangles: " + err.getMessage());
            }
        }
        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Triangles drawn...");
    }

    /**
     * Creates a horizontal unit square in the xz plane.
     *
     * @return list of appropriate triangles
     */
    private ArrayList<Point3d> makeHorizontalSquare()
    {
        ArrayList<Point3d> square = new ArrayList<>();
        //xz01
        square.add(new Point3d(0, 0, 1));
        square.add(new Point3d(0, 0, 0));
        square.add(new Point3d(1, 0, 1));

        //xz02
        square.add(new Point3d(1, 0, 1));
        square.add(new Point3d(0, 0, 0));
        square.add(new Point3d(1, 0, 0));

        return square;
    }
}