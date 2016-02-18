package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class MeshVoxeliser
{
    private Mesh mesh;                                                             // the mesh which is voxelised
    private Point3i matrixDimensions;                                              // dimesions of the block matrix
    private int[] dim;                                                             // buffered version of dimensions in integers
    private Point3d meshOffset;                                                    // the offset of the mesh from the original origin
    private Block[][][] blocks;                                                    // list of the meshes blocks
    private Random r = new Random();                                               // used in the propability functions
    public static final double double_tolerance = 0.000000000001;                  // global tolerance constant
    public static final double probability_tolerance = 0.000000001;                // global probability tolerance constant
    public static final int reverse_tolerance = (int) (1 / probability_tolerance); // used in the probability constants

    private ArrayList<Point3d> initTrigs;

    int telemetry;

    public MeshVoxeliser(Mesh mesh)
    {
        // instantiates the voxeliser and initialises voxelisation
        this.mesh = mesh;
        meshOffset = new Point3d(0, 0, 0);
        initTrigs = mesh.getTriangles();
        voxeliseMesh();
    }

    /**
     * Getter for the blocks list
     *
     * @return Returns the list of blocks for the mesh inside the voxeliser
     */
    public Block[][][] getBlocks()
    {
        return blocks;
    }

    // main voxelisation funcion (called at voxeliser creation)
    private void voxeliseMesh()
    {
        System.out.println("Beginning voxelisation...");
        matrixDimensions = new Point3i(0, 0, 0);

        setMeshOffsetAndDetermineDimensions();
        shiftMeshByOffset();
        mesh.drawTriangles("testing/output/mesh_positioned.obj");
        telemetry = 0;
        generateBlocks(mesh);
        System.out.println("Number of retries: " + telemetry);
    }

    // calculates the vector which needs to be added to all the points
    // so that the mesh becomes centered in the block matrix
    private void setMeshOffsetAndDetermineDimensions()
    {
        System.out.println("Setting mesh offset and determining dimensions...");
        Point3d minBound = getMinimumInitialCoordinateBounds();
        Point3d maxBound = getMaximumInitialCoordinateBounds();
        double diffx = calculateSingleOffset(0, minBound.x, maxBound.x);
        double diffy = calculateSingleOffset(1, minBound.y, maxBound.y);
        double diffz = calculateSingleOffset(2, minBound.z, maxBound.z);

        meshOffset = new Point3d(diffx, diffy, diffz);
        mesh.setOffset(meshOffset);
        System.out.println("Mesh offset set and dimensions determined...");
    }

    // determines the aximum values of the x,y, and z dimensions of the mesh separately
    private Point3d getMinimumInitialCoordinateBounds()
    {
        Point3d min = null;
        Point3d curr;
        int cnt = initTrigs.size();
        for (int i = 0; i < cnt; i++)
        {
            curr = initTrigs.get(i);
            if (min == null)
            {
                min = new Point3d(curr);
            }
            else
            {
                if (min.x > curr.x)
                    min.x = curr.x;
                if (min.y > curr.y)
                    min.y = curr.y;
                if (min.z > curr.z)
                    min.z = curr.z;
            }
        }
        System.out.println("Minimum bounds: " + min.x + " " + min.y + " " + min.z);
        return min;
    }

    // determines the minimum values of the x,y, and z dimensions of the mesh separately
    private Point3d getMaximumInitialCoordinateBounds()
    {
        Point3d max = null;
        Point3d curr;
        int cnt = initTrigs.size();
        for (int i = 0; i < cnt; i++)
        {
            curr = initTrigs.get(i);
            if (max == null)
            {
                max = new Point3d(curr);
            }
            else
            {
                if (max.x < curr.x)
                    max.x = curr.x;
                if (max.y < curr.y)
                    max.y = curr.y;
                if (max.z < curr.z)
                    max.z = curr.z;
            }
        }
        System.out.println("Maximum bounds: " + max.x + " " + max.y + " " + max.z);
        return max;
    }

    // determines the offset of one coordinate and sets the dimensions value
    private double calculateSingleOffset(int type, double minBound, double maxBound)
    {
        int dimension = (int) Math.ceil(maxBound - minBound);
        switch (type)
        {
            case 0:
                matrixDimensions.x = dimension;
                break;
            case 1:
                matrixDimensions.y = dimension;
                break;
            case 2:
                matrixDimensions.z = dimension;
                break;
        }
        double diff = (dimension - maxBound + minBound) / 2;
        double ret = diff - minBound;
        return ret;
    }

    // moves mesh to the new position
    private void shiftMeshByOffset()
    {
        System.out.println("Shifting mesh by offset...");
        int cnt = initTrigs.size();
        for (int i = 0; i < cnt; i++)
        {
            initTrigs.get(i).x += meshOffset.x;
            initTrigs.get(i).y += meshOffset.y;
            initTrigs.get(i).z += meshOffset.z;
        }
        mesh.setTriangles(initTrigs);

        // TESTING METHOD!!!
        // mesh.drawTriangles("testing/output/mesh_centered.obj");
        System.out.println("Mesh shifted...");
    }

    // generates blocks for the given mesh
    // elements in the grid with no mesh will be null
    private void generateBlocks(Mesh mesh)
    {
        System.out.println("Filling blocks...");
        blocks = new Block[matrixDimensions.x][matrixDimensions.y][matrixDimensions.z];
        dim = new int[3];
        matrixDimensions.get(dim);

        // iterate over all the triangles in the mesh
        int cnt = initTrigs.size();
        for (int i = 0; i < cnt; i += 3)
        {
            ArrayList<Point3d> tmp = new ArrayList<Point3d>();
            tmp.add(new Point3d(initTrigs.get(i)));
            tmp.add(new Point3d(initTrigs.get(i + 1)));
            tmp.add(new Point3d(initTrigs.get(i + 2)));

            classifyPolygons(subdivideTriangle(tmp));
        }

        fillAllChunks();

        // TESTING METHODS
        drawVoxelsOnly("testing/output/mesh_internal_voxels.obj", true);
        // drawTrianglesFromBlocks("testing/output/mesh_subdivided.obj", true);

        System.out.println("All blocks filled...");
    }

    // cuts the triangle given by 3 vertices into pieces and puts the pieces into appropriate bins
    private ArrayList<ArrayList<Point3d>> subdivideTriangle(ArrayList<Point3d> triangle)
    {
        // array which contains the intermediate results of triangle subdivision
        ArrayList<ArrayList<Point3d>> polygonList = new ArrayList<>();

        // initially we start dividing only the triangle
        polygonList.add(triangle);

        int[] minBounds = getMinBounds(triangle);
        int[] maxBounds = getMaxBounds(triangle);

        // iterate through all 3 projections:
        // ignore = 0 -> projecting onto yz plane
        // ignore = 1 -> projecting onto xz plane
        // ignore = 2 -> projecting onto xy plane
        for (int ignore = 0; ignore < 3; ignore++)
        {
            int currMinBound = minBounds[(ignore + 1) % 3];
            int currMaxBound = maxBounds[(ignore + 1) % 3];

            // we iterate through all the possible lines which may be intersecting some of our polygons
            for (int line = currMinBound; line <= currMaxBound; line++)
            {
                int currLen = polygonList.size();
                // we iterate through all polygons in our list for every line
                for (int curr = 0; curr < currLen; curr++)
                {
                    ArrayList<Point3d> poly = new ArrayList<>(polygonList.get(curr));
                    int cnt = poly.size();
                    // now we perform the clipping and division
                    // since we can only have two intersections, we iterate through the points and check for new vertices that need to be added
                    // we prepare the new shapes if needed
                    ArrayList<ArrayList<Point3d>> polys = new ArrayList<>();
                    polys.add(new ArrayList<>());
                    polys.add(new ArrayList<>());
                    int side = 0;

                    // setup the first polygon with a single point
                    polys.get(side).add(poly.get(0));

                    // perform an initial check on the vertices to check if there is a true intersection
                    int[] polyMinBounds = getMinBounds(poly);
                    int[] polyMaxBounds = getMaxBounds(poly);
                    if (polyMinBounds[(ignore + 1) % 3] > line ||
                            polyMaxBounds[(ignore + 1) % 3] < line)
                    {
                        // there is no intersection so we continue in our search
                        continue;
                    }

                    // iterate through the edges
                    boolean onlyFirst = false;
                    for (int ver = 0; ver < cnt; ver++)
                    {
                        Point3d res = new Point3d(0, 0, 0);
                        Point3d nextVer = polys.get(side).get(0);
                        if (intersect(poly.get(ver), poly.get((ver + 1) % cnt), line, ignore, res))
                        {
                            // there was a proper intersection between the two points
                            Point3d sec = new Point3d(poly.get((ver + 1) % cnt));
                            Point3d resNew = new Point3d(res);

                            polys.get(side).add(res);
                            side = (side + 1) % 2;
                            polys.get(side).add(resNew);
                            nextVer = sec;
                        }
                        else
                        {
                            if (res.x < -0.5)
                            {
                                // both points are on the line
                                // this means that we have no more intersections, and that the polygon
                                // remains intact, so we just copy it over as poly[0]
                                polys.set(0, poly);
                                onlyFirst = true;
                                polys.set(1, null);
                                break;
                            }
                            else if (res.x > 0.5)
                            {
                                // only one point is on the line
                                if (res.y < -0.5)
                                {
                                    // there was an intersection on a vertex, so no new vertices are introduced
                                    // the intersection was on the fir
                                    Point3d sec = new Point3d(poly.get((ver + 1) % cnt));

                                    // we add the first vertex but stay on the same side
                                    nextVer = sec;
                                }
                                else if (res.y > 0.5)
                                {
                                    // there was an intersection on a vertex, so no new vertices are introduced
                                    // the intersection was on the sec
                                    Point3d sec = new Point3d(poly.get((ver + 1) % cnt));
                                    Point3d secNew = new Point3d(sec);

                                    polys.get(side).add(sec);
                                    side = (side + 1) % 2;
                                    nextVer = secNew;
                                }
                            }
                            else
                            {
                                // no intersection was observed, so we just add the second vertex
                                Point3d sec = new Point3d(poly.get((ver + 1) % cnt));
                                nextVer = sec;
                            }
                        }
                        if (ver != cnt - 1 || (polys.get(side).size() > 0 && !areIdentical(polys.get(side).get(0), nextVer)))
                        {
                            // we only actually perform the new addition if the new vertex is not present at the start of
                            // the polygon list it is being added to (this is considered only if it is the last vertex
                            // to be considered)
                            polys.get(side).add(nextVer);
                        }
                    }

                    if (!onlyFirst && polys.get(1) != null && polys.get(0).size() > 2 && polys.get(1).size() > 2)
                    {
                        // this means we need to refactor the polygon list
                        polygonList.set(curr, new ArrayList<>(polys.get(0)));
                        polygonList.add(new ArrayList<>(polys.get(1)));
                    }
                }
            }
        }
        return polygonList;
    }

    // intersects the line between fir and sec with the vertical line at line ignoring the ignoreth coordinate
    // and writes the return in res. Returns true if there is an intersection
    private boolean intersect(Point3d fir, Point3d sec, double line, int ignore, Point3d res)
    {
        double x1, y1, x2, y2;
        int idx = (ignore + 1) % 3;
        int idy = (ignore + 2) % 3;
        double[] coordfir = new double[3];
        double[] coordsec = new double[3];
        fir.get(coordfir);
        sec.get(coordsec);
        if (coordfir[(ignore + 1) % 3] < coordsec[(ignore + 1) % 3])
        {
            x1 = coordfir[idx];
            y1 = coordfir[idy];
            x2 = coordsec[idx];
            y2 = coordsec[idy];
        }
        else
        {
            x1 = coordsec[idx];
            y1 = coordsec[idy];
            x2 = coordfir[idx];
            y2 = coordfir[idy];
        }

        if (x1 > line
                || x2 < line
                || Math.abs(x1 - line) < double_tolerance
                || Math.abs(x2 - line) < double_tolerance)
        {
            // the line between fir and sec is intersecting the line on one of the points or not intersecting it at all
            if (Math.abs(x1 - line) < double_tolerance)
            {
                // x1 is on the line
                if (Math.abs(x2 - line) < double_tolerance)
                {
                    // x2 is also on the line, so we do not have a proper intersection
                    res.set(-1, 1, 0);
                    // we do not create a new division of the polygon
                    return false;
                }
                else
                {
                    // the intersecting point is the one with the lower x coordinate
                    if (coordfir[idx] < coordsec[idx])
                        res.set(1, -1, 0); // intersection is on fir
                    else
                        res.set(1, 1, 0); // intersection is on sec
                    // we create a new division, but do not create a new vertex
                    return false;
                }
            }
            else if (Math.abs(x2 - line) < double_tolerance)
            {
                // the intersecting point is the one with the greater x coordinate
                if (coordfir[idx] < coordsec[idx])
                    res.set(1, 1, 0); // intersection is on sec
                else
                    res.set(1, -1, 0); // intersection is on fir
                // we create a new division, but do not create a new vertex
                return false;
            }
            else
            {
                // the lines are simply not intersecting
                return false;
            }
        }

        // prepare the grounds for a lot of numeric
        double xNew = line;
        double yNew = (y1 * (x2 - line) + y2 * (line - x1)) / (x2 - x1);
        double[] coordNew = new double[3];

        // determine the ignored coordinate from the line division formula
        double dtot = Math.sqrt((coordfir[idx] - coordsec[idx]) * (coordfir[idx] - coordsec[idx]) +
                (coordfir[idy] - coordsec[idy]) * (coordfir[idy] - coordsec[idy]));
        double dlef = Math.sqrt((coordfir[idx] - xNew) * (coordfir[idx] - xNew) +
                (coordfir[idy] - yNew) * (coordfir[idy] - yNew));
        double drig = dtot - dlef;

        coordNew[ignore] = ((coordfir[ignore] * drig + coordsec[ignore] * dlef) / dtot);
        coordNew[idx] = xNew;
        coordNew[idy] = yNew;
        res.set(coordNew);
        return true;
    }

    // returns the minimum coordinates in the x, y and z plane which intersect the polygon
    private int[] getMinBounds(ArrayList<Point3d> polygon)
    {
        double[] min = null;
        int cnt = polygon.size();
        for (int i = 0; i < cnt; i++)
        {
            if (min == null)
            {
                min = new double[3];
                polygon.get(i).get(min);
            }
            else
            {
                if (min[0] > polygon.get(i).x)
                    min[0] = polygon.get(i).x;
                if (min[1] > polygon.get(i).y)
                    min[1] = polygon.get(i).y;
                if (min[2] > polygon.get(i).z)
                    min[2] = polygon.get(i).z;
            }
        }
        int[] res = new int[3];
        for (int i = 0; i < 3; i++)
            res[i] = (int) Math.ceil(min[i]);
        return res;
    }

    // returns the maximum coordinates in the x, y and z plane which intersect the polygon
    private int[] getMaxBounds(ArrayList<Point3d> polygon)
    {
        double[] max = null;
        int cnt = polygon.size();
        for (int i = 0; i < cnt; i++)
        {
            if (max == null)
            {
                max = new double[3];
                polygon.get(i).get(max);
            }
            else
            {
                if (max[0] < polygon.get(i).x)
                    max[0] = polygon.get(i).x;
                if (max[1] < polygon.get(i).y)
                    max[1] = polygon.get(i).y;
                if (max[2] < polygon.get(i).z)
                    max[2] = polygon.get(i).z;
            }
        }
        int[] res = new int[3];
        for (int i = 0; i < 3; i++)
            res[i] = (int) Math.ceil(max[i]) - 1;
        return res;
    }

    // classifies the polygons obtained in the polygon list by adding them to their respective bins
    private void classifyPolygons(ArrayList<ArrayList<Point3d>> polygonList)
    {
        int cnt = polygonList.size();
        for (int curr = 0; curr < cnt; curr++)
        {
            ArrayList<Point3d> poly = polygonList.get(curr);

            // determine the block coordinates by rounding center of mass coordinates
            Point3d cm = getCenterOfMass(poly);
            int x, y, z;
            x = (int) cm.x;
            y = (int) cm.y;
            z = (int) cm.z;

            // create a new block if it does not exist already
            if (blocks[x][y][z] == null)
                blocks[x][y][z] = new Block(new Point3d(x, y, z), true);

            // triangulate the current polygon and add the resulting triangles to the new block
            switch (poly.size())
            {
                case 6:
                    blocks[x][y][z].addTriangle(poly.get(0), poly.get(1), poly.get(2));
                    blocks[x][y][z].addTriangle(poly.get(0), poly.get(2), poly.get(5));
                    blocks[x][y][z].addTriangle(poly.get(5), poly.get(2), poly.get(3));
                    blocks[x][y][z].addTriangle(poly.get(5), poly.get(3), poly.get(4));
                    break;
                default:
                    blocks[x][y][z].addTriangle(poly.get(0), poly.get(1), poly.get(2));
                    for (int point = 3; point < poly.size(); point++)
                    {
                        // triangulate the remaining shape if needed
                        blocks[x][y][z].addTriangle(poly.get(0), poly.get(point - 1), poly.get(point));
                    }
                    break;
            }
        }
    }

    // returns the center of mass of the given polygon
    private Point3d getCenterOfMass(ArrayList<Point3d> poly)
    {
        Point3d cm = new Point3d(0, 0, 0);
        int cnt = poly.size();
        for (int i = 0; i < cnt; i++)
        {
            cm.x += poly.get(i).x;
            cm.y += poly.get(i).y;
            cm.z += poly.get(i).z;
        }
        cm.x /= cnt;
        cm.y /= cnt;
        cm.z /= cnt;
        return cm;
    }

    // fills the entire block matrix with appropriate full/empty blocks
    private void fillAllChunks()
    {
        System.out.println("Filling chunks...");

        for (int x = 0; x < dim[0]; x++)
        {
            for (int y = 0; y < dim[1]; y++)
            {
                fillChunk(x, y);
            }
        }
        System.out.println("Chunks filled...");
    }

    // fills a single xy chunk of blocks
    private void fillChunk(int x, int y)
    {
        // prepare the initial ray
        double xOffset = punchX();
        double yOffset = punchY();
        ArrayList<Point3d> currTrigs;
        int pointCnt;
        Point3d R0 = new Point3d();

        // the number of current intersections with the mesh
        int intersectionNo;

        // the intersection filling loop
        boolean restart;
        do
        {
            // reset the parameters
            R0.set((xOffset), (yOffset), -1.0);
            intersectionNo = 0;
            restart = false;

            // loop over all the blocks within the chunk
            for (int a = dim[2] - 1; a >= 0 && !restart; a--)
            {
                if (blocks[x][y][a] == null)
                {
                    if (intersectionNo % 2 == 1)
                    {
                        // create the block
                        blocks[x][y][a] = new Block(new Point3d(x, y, a), false);
                    }
                    continue;
                }
                if (!blocks[x][y][a].isCustom())
                {
                    // ignore the block we have already filled
                    continue;
                }
                currTrigs = blocks[x][y][a].getTriangles();
                pointCnt = currTrigs.size();
                for (int curr = 0; curr < pointCnt && !restart; curr += 3)
                {
                    // pack up the current triangle
                    ArrayList<Point3d> T = new ArrayList<>();
                    T.add(currTrigs.get(curr));
                    T.add(currTrigs.get(curr + 1));
                    T.add(currTrigs.get(curr + 2));

                    // and check intersection
                    int code = cutVertically(R0, T);
                    if (code == 1)
                    {
                        // intersection is unique so we count it
                        intersectionNo++;
                    }
                    else if (2 <= code && code <= 7)
                    {
                        // we restart with another starting point
                        restart = true;
                        telemetry++;
                    }
                }
            }
            if (restart)
            {
                // we take another arbitrary point in the cube to shoot the ray from
                xOffset = punchX();
                yOffset = punchY();
            }
        } while (restart);
    }

    // probability function for x
    private double punchX()
    {
        return 0.34 + probability_tolerance * (r.nextInt(reverse_tolerance) - (double) reverse_tolerance / 2) / 10;
    }

    // probability function for y
    private double punchY()
    {
        return 0.71 + probability_tolerance * (r.nextInt(reverse_tolerance) - (double) reverse_tolerance / 2) / 10;
    }

    int cutVertically(Point3d R0, ArrayList<Point3d> T)
    {
        if (areIdenticalInXY(R0, T.get(0))) return 2;
        if (areIdenticalInXY(R0, T.get(1))) return 3;
        if (areIdenticalInXY(R0, T.get(2))) return 4;
        if (!areIdenticalInXY(T.get(0), T.get(1)) && isOnLineXY(T.get(0), R0, T.get(1))) return 5;
        if (!areIdenticalInXY(T.get(1), T.get(2)) && isOnLineXY(T.get(1), R0, T.get(2))) return 6;
        if (!areIdenticalInXY(T.get(2), T.get(0)) && isOnLineXY(T.get(2), R0, T.get(0))) return 7;
        if (!isOnLineXY(T.get(0), T.get(1), T.get(2)) && ptInTriangleXY(R0, T)) return 1;
        return 0;
    }

    // returns true if B is between A and C (all xy projections)
    boolean isOnLineXY(Point3d A, Point3d B, Point3d C)
    {
        double inter = Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y));
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < double_tolerance;
    }

    // checks if a given three dimensional point is inside the given triangle (checking xy coordinates)
    boolean ptInTriangleXY(Point3d I, ArrayList<Point3d> T)
    {
        double dX = I.x - T.get(2).x;
        double dY = I.y - T.get(2).y;
        double dX21 = T.get(2).x - T.get(1).x;
        double dY12 = T.get(1).y - T.get(2).y;
        double D = dY12 * (T.get(0).x - T.get(2).x) + dX21 * (T.get(0).y - T.get(2).y);
        double s = dY12 * dX + dX21 * dY;
        double t = (T.get(2).y - T.get(0).y) * dX + (T.get(0).x - T.get(2).x) * dY;
        if (D < 0) return s <= 0 && t <= 0 && s + t >= D;
        return s >= 0 && t >= 0 && s + t <= D;
    }

    // returns true if the projections of the two points on the xy plane coincide
    boolean areIdenticalInXY(Point3d ver1, Point3d ver2)
    {
        return Math.abs(ver1.x - ver2.x) < double_tolerance
                && Math.abs(ver1.y - ver2.y) < double_tolerance;
    }

    // checks if two 3d points are identical
    private boolean areIdentical(Point3d ver1, Point3d ver2)
    {
        return Math.abs(ver1.x - ver2.x) < double_tolerance
                && Math.abs(ver1.y - ver2.y) < double_tolerance
                && Math.abs(ver1.z - ver2.z) < double_tolerance;
    }

    /**
     * TESTING method
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
            System.err.println(ex.getMessage());
            return;
        }

        int totalTriangles = 0;

        for (int x = 0; x < matrixDimensions.x; x++)
        {
            for (int z = 0; z < matrixDimensions.z; z++)
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
                for (int y = 0; y < matrixDimensions.y; y++)
                {
                    if (blocks[x][y][z] == null)
                        continue;
                    ArrayList<Point3d> triangles = new ArrayList<>(blocks[x][y][z].getTriangles());

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

    private ArrayList<Point3d> makeUnitCube(int x, int y, int z)
    {
        ArrayList<Point3d> cube = new ArrayList<>();

        if (x < 1 || (blocks[x - 1][y][z] == null || blocks[x - 1][y][z].isCustom()))
        {
            //yz01
            cube.add(new Point3d(0, 1, 0));
            cube.add(new Point3d(0, 0, 0));
            cube.add(new Point3d(0, 1, 1));

            //yz02
            cube.add(new Point3d(0, 1, 1));
            cube.add(new Point3d(0, 0, 0));
            cube.add(new Point3d(0, 0, 1));
        }

        if (y < 1 || (blocks[x][y - 1][z] == null || blocks[x][y - 1][z].isCustom()))
        {
            //xz01
            cube.add(new Point3d(0, 0, 1));
            cube.add(new Point3d(0, 0, 0));
            cube.add(new Point3d(1, 0, 1));

            //xz02
            cube.add(new Point3d(1, 0, 1));
            cube.add(new Point3d(0, 0, 0));
            cube.add(new Point3d(1, 0, 0));
        }

        if (z < 1 || (blocks[x][y][z - 1] == null || blocks[x][y][z - 1].isCustom()))
        {
            //xy01
            cube.add(new Point3d(1, 0, 0));
            cube.add(new Point3d(0, 0, 0));
            cube.add(new Point3d(1, 1, 0));

            //xy02
            cube.add(new Point3d(1, 1, 0));
            cube.add(new Point3d(0, 0, 0));
            cube.add(new Point3d(0, 1, 0));
        }
        if (x >= dim[0] - 1 || (blocks[x + 1][y][z] == null || blocks[x + 1][y][z].isCustom()))
        {
            //yz11
            cube.add(new Point3d(1, 0, 0));
            cube.add(new Point3d(1, 1, 0));
            cube.add(new Point3d(1, 1, 1));

            //yz12
            cube.add(new Point3d(1, 0, 0));
            cube.add(new Point3d(1, 1, 1));
            cube.add(new Point3d(1, 0, 1));
        }

        if (y >= dim[1] - 1 || (blocks[x][y + 1][z] == null || blocks[x][y + 1][z].isCustom()))
        {
            //xz11
            cube.add(new Point3d(0, 1, 1));
            cube.add(new Point3d(1, 1, 1));
            cube.add(new Point3d(0, 1, 0));

            //xz12
            cube.add(new Point3d(0, 1, 0));
            cube.add(new Point3d(1, 1, 1));
            cube.add(new Point3d(1, 1, 0));
        }

        if (z >= dim[2] - 1 || (blocks[x][y][z + 1] == null || blocks[x][y][z + 1].isCustom()))
        {
            //xy11
            cube.add(new Point3d(1, 0, 1));
            cube.add(new Point3d(1, 1, 1));
            cube.add(new Point3d(0, 0, 1));

            //xy12
            cube.add(new Point3d(0, 0, 1));
            cube.add(new Point3d(1, 1, 1));
            cube.add(new Point3d(0, 1, 1));
        }
        return cube;
    }

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

    /**
     * TESTING method
     */
    public void drawVoxelsOnly(String filename, boolean includeGrid)
    {
        System.out.println("Preparing the voxel output...");
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

        for (int x = 0; x < matrixDimensions.x; x++)
        {
            for (int z = 0; z < matrixDimensions.z; z++)
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
                for (int y = 0; y < matrixDimensions.y; y++)
                {
                    if (blocks[x][y][z] == null || blocks[x][y][z].isCustom())
                        continue;
                    ArrayList<Point3d> triangles = makeUnitCube(x, y, z);

                    totalTriangles += triangles.size() / 3;
                    for (int i = 0; i < triangles.size(); i++)
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
        System.out.println("Voxel output created...");
    }

    /**
     * TESTING method
     */
    private void drawPolygonList(ArrayList<ArrayList<Point3d>> polys, String filename)
    {
        Writer writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            //System.err.println(ex.getMessage());
            return;
        }

        for (int i = 0; i < polys.size(); i++)
        {
            for (int j = 0; j < polys.get(i).size(); j++)
            {
                try
                {
                    writer.write("v " + (polys.get(i).get(j).x) + " "
                            + (polys.get(i).get(j).y) + " "
                            + (polys.get(i).get(j).z) + "\n");

                } catch (IOException err)
                {
                    System.err.println("Could not write blocks: " + err.getMessage());
                }
            }
        }
        int curr = 1;
        for (int poly = 0; poly < polys.size(); poly++)
        {
            String out = "f";
            for (int i = 0; i < polys.get(poly).size(); i++)
            {
                out += " " + curr;
                curr++;
            }
            try
            {
                writer.write(out + "\n");
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
    }

    /**
     * TESTING method
     */
    private void drawRayTest(ArrayList<Point3d> T, Point3d I)
    {
        ArrayList<ArrayList<Point3d>> poligoni = new ArrayList<>();
        ArrayList<Point3d> fir = new ArrayList<>();
        fir.add(new Point3d(T.get(0)));
        fir.add(new Point3d(T.get(1)));
        fir.add(new Point3d(I));
        ArrayList<Point3d> sec = new ArrayList<>();
        sec.add(new Point3d(T.get(1)));
        sec.add(new Point3d(T.get(2)));
        sec.add(new Point3d(I));
        ArrayList<Point3d> trd = new ArrayList<>();
        trd.add(new Point3d(T.get(2)));
        trd.add(new Point3d(T.get(0)));
        trd.add(new Point3d(I));
        poligoni.add(fir);
        poligoni.add(sec);
        poligoni.add(trd);
        drawPolygonList(poligoni, "testing/output/poly.obj");
    }
}
