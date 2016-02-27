package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class MeshVoxeliser
{
    private Mesh mesh;                                                             // the mesh which is voxelised
    private Point3i matrixDimensions;                                              // dimensions of the block matrix
    private int[] dim;                                                             // buffered version of dimensions in integers
    private Point3d meshOffset;                                                    // the offset of the mesh from the original origin
    private Block[][][] blocks;                                                    // list of the meshes blocks
    private Random r = new Random();                                               // used in the probability functions
    public static final double double_tolerance = 0.000000000001;                  // global tolerance constant
    public static final double probability_tolerance = 0.000000001;                // global probability tolerance constant
    public static final int reverse_tolerance = (int) (1 / probability_tolerance); // used in the probability constants
    private ArrayList<Point3d> initTrigs;                                          // a buffered list of initial triangles
    int telemetry;                                                                 // counts the total number of retries

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

    /**
     * main voxelisation function (called at voxeliser creation)
     */
    private void voxeliseMesh()
    {
        System.out.println("Beginning voxelisation...");
        matrixDimensions = new Point3i(0, 0, 0);
        setMeshOffsetAndDetermineDimensions();
        shiftMeshByOffset();

        telemetry = 0;
        generateBlocks();

        drawTrianglesFromBlocks("testing/output/mesh_subdivided.obj", true);
        //drawVoxelsOnly("testing/output/mesh_internal_voxels.obj", false);

        System.out.println("Number of retries: " + telemetry);
    }

    /**
     * Calculates the vector which needs to be added to all the points
     * of the mesh, so that it becomes centered in the first octant of
     * the block matrix.
     */
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

    /**
     * Separately determines the maximum values of the x,y and z dimensions of the mesh.
     */
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

    /**
     * Separately determines the minimum values of the x,y and z dimensions of the mesh.
     */
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

    /**
     * Determines the offset of one coordinate and sets the dimensions value.
     */
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

    /**
     * Translates the mesh to the new position.
     */
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

    /**
     * Generates the Block matrix for the mesh present in the voxeliser.
     * <p>
     * The Blocks which contain fragments of the mesh will be labeled as custom parts,
     * the blocks which are completely outside the mesh will be null, while the blocks
     * which are completely encompassed by the mesh will be created and labeled as not
     * custom.
     */
    private void generateBlocks()
    {
        System.out.println("Filling blocks...");
        blocks = new Block[matrixDimensions.x][matrixDimensions.y][matrixDimensions.z];
        dim = new int[3];
        matrixDimensions.get(dim);

        // iterate over all the triangles in the original mesh
        int cnt = initTrigs.size();
        for (int i = 0; i < cnt; i += 3)
        {
            ArrayList<Point3d> curr = new ArrayList<Point3d>();
            curr.add(new Point3d(initTrigs.get(i)));
            curr.add(new Point3d(initTrigs.get(i + 1)));
            curr.add(new Point3d(initTrigs.get(i + 2)));

            // subdivide each triangle and classify the
            // pieces into their respective new custom blocks
            classifyPolygons(subdivideTriangle(curr));
        }

        // fill the remaining internal blocks
        fillAllChunks();

        System.out.println("All blocks filled...");
    }

    /**
     * Subdivides the provided triangle defined by its list of vertices.
     * <p>
     * This is done by projecting the current subdivision onto a projection plane
     * (e.g. xy plane) and then iteratively slicing all the polygons in it with the
     * vertical grid lines of the current projection plane (e.g. lines parallel to
     * y axis). The obtained subdivision is the returned to 3d space, and reprojected
     * in one of the remaining projection planes (e.g. yz, zx), and the subdivision
     * process is continued until all three projection planes have been exhausted.
     *
     * @param triangle list of points representing the triangle
     * @return the array of distinct convex polygons, each of which is only
     * contained in a single Block
     */
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

    /**
     * Intersects the line between fir and sec with the vertical line, while
     * ignoring the ignore-th coordinate.
     *
     * @param fir    first point of the line
     * @param sec    second point of the line
     * @param line   grid-line 'x' coordinate
     * @param ignore coordinate to be ignored (the other two are then treated as 'x' and 'y')
     * @param res    the point in which the result will be returned
     * @return The following return values are possible:
     * 1. true  - the intersection was strictly between fir and sec
     * and the point of intersection is returned in res
     * 2. false - there was either no intersection or an
     * case was observed, res will contain:
     * 2.1. res is unchanged  - no intersection with the line
     * 2.2. res = (-1,  1, 0) - fir and sec are on the line
     * 2.3. res = ( 1, -1, 0) - fir is on the line (but sec is not)
     * 2.4. res = ( 1,  1, 0) - sec is on the line (but fir is not)
     */
    public static boolean intersect(Point3d fir, Point3d sec, double line, int ignore, Point3d res)
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

    /**
     * Returns the minimum coordinate of the x, y and z planes which intersect the polygon.
     */
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

    /**
     * Returns the maximum coordinate of the x, y and z planes which intersect the polygon.
     */
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

    /**
     * Triangulates the polygons provided and classifies the fragments obtained
     * by adding them to their respective Blocks in the Blocks matrix.
     *
     * @param polygonList list of polygons to classify
     */
    private void classifyPolygons(ArrayList<ArrayList<Point3d>> polygonList)
    {
        int cnt = polygonList.size();
        for (int curr = 0; curr < cnt; curr++)
        {
            ArrayList<Point3d> poly = polygonList.get(curr);

            // determine the block coordinates
            Point3i blockCoords = determineBlockCoordinates(poly);
            int x, y, z;
            x = blockCoords.x;
            y = blockCoords.y;
            z = blockCoords.z;

            if (x > dim[0] - 1)
                x = dim[0] - 1;
            if (y > dim[1] - 1)
                y = dim[1] - 1;
            if (z > dim[2] - 1)
                z = dim[2] - 1;

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

    /**
     * Returns the center of mass of the given polygon.
     */
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


    /**
     * Determines the coordinates of the block to sort the polygon in, depending on the
     * location of its centre of mass and its surface normal.
     */
    private Point3i determineBlockCoordinates(ArrayList<Point3d> poly)
    {
        Point3i res = new Point3i(0, 0, 0);

        Point3d cm = getCenterOfMass(poly);
        res.x = (int) cm.x;
        res.y = (int) cm.y;
        res.z = (int) cm.z;

        // works because polygons are convex
        Point3d A = poly.get(0);
        Point3d B = poly.get(1);
        Point3d C = poly.get(2);

        // IMPORTANT NOTE:
        // All surface normals point outside of the mesh!
        // Assuming the points of a polygon are given in a CCW order in a plane,
        // the surface normal will point towards the observer (vertically upward
        // from the paper, following the right hand rule).

        Point3d n = vectorProd(A, B, C);
        if (n.x > 0 && areIdentical(res.x, A.x) && areIdentical(res.x, B.x) && areIdentical(res.x, C.x))
        {
            // the polygon is in the 0yz plane in the selected blocks coordinate system
            // and the surface normal is pointing to the inside of the currently selected
            // block, so we relocate the polygon to the cube 'below' in x
            res.x--;
        }
        else if (n.y > 0 && areIdentical(res.y, A.y) && areIdentical(res.y, B.y) && areIdentical(res.y, C.y))
        {
            // the polygon is in the x0z plane in the selected blocks coordinate system
            // and the surface normal is pointing to the inside of the currently selected
            // block, so we relocate the polygon to the cube 'below' in y
            res.y--;
        }
        else if (n.z > 0 && areIdentical(res.z, A.z) && areIdentical(res.z, B.z) && areIdentical(res.z, C.z))
        {
            // the polygon is in the xy0 plane in the selected blocks coordinate system
            // and the surface normal is pointing to the inside of the currently selected
            // block, so we relocate the polygon to the cube 'below' in z
            res.z--;
        }
        return res;
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

    /**
     * Fills the entire block matrix with appropriate full/empty blocks.
     * <p>
     * (The block matrix must be initialized with mesh pieces before
     * calling fillAllChunks!)
     */
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

    /**
     * Fills a single xy chunk of blocks.
     * <p>
     * This is done by iterating through the blocks in the chunk in a top-down manner and
     * shooting the same vertical ray through each of them and adding the number of
     * intersection the ray has with the current block to a global total. When an empty
     * block is encountered it is either filled or left empty depending on the current
     * total number of intersections the ray has with the mesh. If an edge case intersection
     * of the ray with any of the triangles is encountered the filling of the chunk is halted,
     * and restarted with a fresh ray randomly offset from the previous position. This procedure
     * is performed until the entire chunk is successfully processed.
     *
     * @param x x coordinate of the chunk in the matrix
     * @param y y coordinate of the chunk in the matrix
     */
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

    /**
     * Probability distribution function for x.
     */
    private double punchX()
    {
        return 0.34 + probability_tolerance * (r.nextInt(reverse_tolerance) - (double) reverse_tolerance / 2) / 10;
    }

    /**
     * Probability distribution function for y.
     */
    private double punchY()
    {
        return 0.71 + probability_tolerance * (r.nextInt(reverse_tolerance) - (double) reverse_tolerance / 2) / 10;
    }

    /**
     * Shoots a vertical ray from R0 in the positive z direction, and returns a code for the type
     * of intersection the ray has with the Triangle T. It is assumed that T is in another Block
     * from R0!
     *
     * @param R0 ray origin
     * @param T  triangle to test intersection with
     * @return Returns one of the following codes depending on type of intersection:
     * 0 - no intersection
     * 1 - ray intersects triangles strict inside
     * 2 - ray passes through vertex T[0]
     * 3 - ray passes through vertex T[1]
     * 4 - ray passes through vertex T[2]
     * 5 - ray intersects edge T[0]->T[1]
     * 6 - ray intersects edge T[1]->T[2]
     * 7 - ray intersects edge T[2]->T[0]
     */
    public static int cutVertically(Point3d R0, ArrayList<Point3d> T)
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

    /**
     * Returns true if B is between A and C (all xy projections).
     */
    public static boolean isOnLineXY(Point3d A, Point3d B, Point3d C)
    {
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < double_tolerance;
    }

    /**
     * Checks if a given three dimensional point is inside the given triangle (checking xy coordinates)
     */
    public static boolean ptInTriangleXY(Point3d I, ArrayList<Point3d> T)
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

    /**
     * Returns true if the projections of the two points in the xy plane coincide.
     */
    public static boolean areIdenticalInXY(Point3d ver1, Point3d ver2)
    {
        return Math.abs(ver1.x - ver2.x) < double_tolerance
                && Math.abs(ver1.y - ver2.y) < double_tolerance;
    }

    /**
     * Checks if two 3d points are identical within the global tolerance.
     */
    public static boolean areIdentical(Point3d ver1, Point3d ver2)
    {
        return areIdentical(ver1.x, ver2.x)
                && areIdentical(ver1.y, ver2.y)
                && areIdentical(ver1.z, ver2.z);
    }

    /**
     * Checks if two double precision floating point values are identical within the global tolerance.
     */
    public static boolean areIdentical(double d1, double d2)
    {
        return Math.abs(d2 - d1) < double_tolerance;
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

    /**
     * DEBUGGING and TESTING method!
     * Creates a unit cube at (x,y,z) only creating visible sides.
     *
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     * @return list of appropriate triangles
     */
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

    /**
     * DEBUGGING and TESTING method!
     * Outputs a .obj file representing the current full Blocks in the block matrix.
     *
     * @param filename    name of file to which to write output
     * @param includeGrid if the x0z grid should be included
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
}
