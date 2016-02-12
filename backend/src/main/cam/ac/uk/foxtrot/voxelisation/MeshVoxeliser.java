package cam.ac.uk.foxtrot.voxelisation;

import javafx.util.Pair;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class MeshVoxeliser
{
    private Mesh mesh;                                      // the mesh which is voxelised
    private Point3i matrixDimensions;                       // dimesions of the block matrix
    private int[] dim;                                      // buffered version of dimensions in integers
    private Point3f meshOffset;                             // the offset of the mesh from the original origin
    private TriangleArray initialTriangles;                 // list of the initial triangles of the mesh
    public static final float float_tolerance = 0.0000001f; // global tolerance constant
    private Block[][][] blocks;                             // list of the meshes blocks

    private ArrayList<Point3f> initTrigs;                   // regular internal representation (needed for the ray tracing step)
    private ArrayList<ArrayList<ArrayList<Integer>>> sharesVertex;
    private ArrayList<ArrayList<ArrayList<Integer>>> sharesEdge;


    public MeshVoxeliser(Mesh mesh)
    {
        // instantiates the voxeliser and initialises voxelisation
        this.mesh = mesh;
        meshOffset = new Point3f(0, 0, 0);
        initialTriangles = mesh.getTriangles();
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
        initialTriangles = mesh.getTriangles();

        setMeshOffsetAndDetermineDimensions();
        shiftMeshByOffset();
        generateBlocks(mesh);
    }

    // calculates the vector which needs to be added to all the points
    // so that the mesh becomes centered in the block matrix
    private void setMeshOffsetAndDetermineDimensions()
    {
        System.out.println("Setting mesh offset and determining dimensions...");
        Point3f minBound = getMinimumInitialCoodrinateBounds();
        Point3f maxBound = getMaximumInitialCoodrinateBounds();
        float diffx = calculateSingleOffset(0, minBound.x, maxBound.x);
        float diffy = calculateSingleOffset(1, minBound.y, maxBound.y);
        float diffz = calculateSingleOffset(2, minBound.z, maxBound.z);
        //System.out.println("Mesh offsets: " + diffx + " " + diffy + " " + diffz);

        meshOffset = new Point3f(diffx, diffy, diffz);
        mesh.setOffset(meshOffset);
        System.out.println("Mesh offset set and dimensions determined...");
    }

    // determines the aximum values of the x,y, and z dimensions of the mesh separately
    private Point3f getMinimumInitialCoodrinateBounds()
    {
        Point3f min = null;
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = initialTriangles.getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            initialTriangles.getCoordinate(i, curr);
            if (min == null)
            {
                min = new Point3f(curr);
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
    private Point3f getMaximumInitialCoodrinateBounds()
    {
        Point3f max = null;
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = initialTriangles.getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            initialTriangles.getCoordinate(i, curr);
            if (max == null)
            {
                max = new Point3f(curr);
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
    private float calculateSingleOffset(int type, float minBound, float maxBound)
    {
        int dimension = (int) Math.ceil((float) Math.ceil(maxBound) - minBound);
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
        float ret = (float) Math.ceil(-minBound);
        return ret;
    }

    // moves mesh to the new position
    private void shiftMeshByOffset()
    {
        System.out.println("Shifting mesh by offset...");
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = mesh.getTriangles().getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            initialTriangles.getCoordinate(i, curr);
            curr.x += meshOffset.x;
            curr.y += meshOffset.y;
            curr.z += meshOffset.z;
            initialTriangles.setCoordinate(i, new Point3f(curr));
            mesh.getTriangles().setCoordinate(i, curr);
        }

        // TESTING METHOD!!!
        mesh.drawTriangles("../../testing/actualInitial.obj");
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
        Point3f fir = new Point3f(0, 0, 0);
        Point3f sec = new Point3f(0, 0, 0);
        Point3f trd = new Point3f(0, 0, 0);
        int cnt = mesh.getTriangles().getVertexCount();
        for (int i = 0; i < cnt; i += 3)
        {
            initialTriangles.getCoordinate(i, fir);
            initialTriangles.getCoordinate(i + 1, sec);
            initialTriangles.getCoordinate(i + 2, trd);
            ArrayList<Point3f> tmp = new ArrayList<>();
            tmp.add(new Point3f(fir));
            tmp.add(new Point3f(sec));
            tmp.add(new Point3f(trd));

            classifyPolygons(subdivideTriangle(tmp));
        }
        fillRemainingBlocks();

        // TESTING METHOD!!
        drawTrianglesFromBlocks("../../testing/out.obj");

        System.out.println("All blocks filled...");
    }

    // cuts the triangle given by 3 vertices into pieces and puts the pieces into appropriate bins
    private ArrayList<ArrayList<Point3f>> subdivideTriangle(ArrayList<Point3f> triangle)
    {
        // array which contains the intermediate results of triangle subdivision
        ArrayList<ArrayList<Point3f>> polygonList = new ArrayList<>();

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
                    ArrayList<Point3f> poly = new ArrayList<>(polygonList.get(curr));
                    int cnt = poly.size();
                    // now we perform the clipping and division
                    // since we can only have two intersections, we iterate through the points and check for new vertices that need to be added
                    // we prepare the new shapes if needed
                    ArrayList<ArrayList<Point3f>> polys = new ArrayList<>();
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
                        Point3f res = new Point3f(0, 0, 0);
                        if (intersect(poly.get(ver), poly.get((ver + 1) % cnt), line, ignore, res))
                        {
                            // there was a proper intersection between the two points
                            Point3f sec = new Point3f(poly.get((ver + 1) % cnt));
                            Point3f resNew = new Point3f(res);

                            polys.get(side).add(res);
                            side = (side + 1) % 2;
                            polys.get(side).add(resNew);
                            if (ver != cnt - 1)
                                polys.get(side).add(sec);
                        }
                        else
                        {
                            if (res.x < -0.5f)
                            {
                                // both points are on the line
                                // this means that we have no more intersections, and that the polygon
                                // remains intact, so we just copy it over as poly[0]
                                polys.set(0, poly);
                                onlyFirst = true;
                                polys.set(1, null);
                                break;
                            }
                            else if (res.x > 0.5f)
                            {
                                // only one point is on the line
                                if (res.y < -0.5f)
                                {
                                    // there was an intersection on a vertex, so no new vertices are introduced
                                    // the intersection was on the fir
                                    Point3f sec = new Point3f(poly.get((ver + 1) % cnt));

                                    // we add the first vertex but stay on the same side
                                    if (ver != cnt - 1)
                                        polys.get(side).add(sec);
                                }
                                else if (res.y > 0.5f)
                                {
                                    // there was an intersection on a vertex, so no new vertices are introduced
                                    // the intersection was on the sec
                                    Point3f sec = new Point3f(poly.get((ver + 1) % cnt));
                                    Point3f secNew = new Point3f(sec);

                                    polys.get(side).add(sec);
                                    side = (side + 1) % 2;
                                    if (ver != cnt - 1)
                                        polys.get(side).add(secNew);
                                }
                            }
                            else
                            {
                                // no intersection was observed, so we just add the second vertex
                                Point3f sec = new Point3f(poly.get((ver + 1) % cnt));
                                if (ver != cnt - 1)
                                    polys.get(side).add(sec);
                            }
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
    private boolean intersect(Point3f fir, Point3f sec, float line, int ignore, Point3f res)
    {
        float x1, y1, x2, y2;
        int idx = (ignore + 1) % 3;
        int idy = (ignore + 2) % 3;
        float[] coordfir = new float[3];
        float[] coordsec = new float[3];
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
                || Math.abs(x1 - line) < float_tolerance
                || Math.abs(x2 - line) < float_tolerance)
        {
            // the line between fir and sec is intersecting the line on one of the points or not intersecting it at all
            if (Math.abs(x1 - line) < float_tolerance)
            {
                // x1 is on the line
                if (Math.abs(x2 - line) < float_tolerance)
                {
                    // x2 is also on the line, so we do not have a propper intersection
                    res.set(-1f, 1f, 0f);
                    // we do not create a new division of the polygon
                    return false;
                }
                else
                {
                    // the intersecting point is the one with the lower x coordinate
                    if (coordfir[idx] < coordsec[idx])
                        res.set(1f, -1f, 0f); // intersection is on fir
                    else
                        res.set(1f, 1f, 0f); // intersection is on sec
                    // we create a new division, but do not create a new vertex
                    return false;
                }
            }
            else if (Math.abs(x2 - line) < float_tolerance)
            {
                // the intersecting point is the one with the greater x coordinate
                if (coordfir[idx] < coordsec[idx])
                    res.set(1f, 1f, 0f); // intersection is on sec
                else
                    res.set(1f, -1f, 0f); // intersection is on fir
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
        float xNew = line;
        float yNew = (y1 * (x2 - line) + y2 * (line - x1)) / (x2 - x1);
        float[] coordNew = new float[3];

        // determine the ignored coordinate from the line division formula
        double dtot = Math.sqrt((coordfir[idx] - coordsec[idx]) * (coordfir[idx] - coordsec[idx]) +
                (coordfir[idy] - coordsec[idy]) * (coordfir[idy] - coordsec[idy]));
        double dlef = Math.sqrt((coordfir[idx] - xNew) * (coordfir[idx] - xNew) +
                (coordfir[idy] - yNew) * (coordfir[idy] - yNew));
        double drig = dtot - dlef;

        coordNew[ignore] = (float) ((coordfir[ignore] * drig + coordsec[ignore] * dlef) / dtot);
        coordNew[idx] = xNew;
        coordNew[idy] = yNew;
        res.set(coordNew);
        return true;
    }

    // returns the minimum coordinates in the x, y and z plane which intersect the polygon
    private int[] getMinBounds(ArrayList<Point3f> polygon)
    {
        float[] min = null;
        int cnt = polygon.size();
        for (int i = 0; i < cnt; i++)
        {
            if (min == null)
            {
                min = new float[3];
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
    private int[] getMaxBounds(ArrayList<Point3f> polygon)
    {
        float[] max = null;
        int cnt = polygon.size();
        for (int i = 0; i < cnt; i++)
        {
            if (max == null)
            {
                max = new float[3];
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

    // clasififies the polygons obtained in the polygon list by adding them to their respective bins
    private void classifyPolygons(ArrayList<ArrayList<Point3f>> polygonList)
    {
        int cnt = polygonList.size();
        for (int curr = 0; curr < cnt; curr++)
        {
            ArrayList<Point3f> poly = polygonList.get(curr);

            // determine the block coordinates by rounding center of mass coordinates
            Point3f cm = getCenterOfMass(poly);
            int x, y, z;
            x = (int) cm.x;
            y = (int) cm.y;
            z = (int) cm.z;

            // create a new block if it does not exist already
            if (blocks[x][y][z] == null)
                blocks[x][y][z] = new Block(new Vector3f(x, y, z), true);

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

    private Point3f getCenterOfMass(ArrayList<Point3f> poly)
    {
        Point3f cm = new Point3f(0, 0, 0);
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

    // labels the remaining blocks within the mesh as full
    private void fillRemainingBlocks()
    {
        // first precompute all the helpful structures that will be useful to save time
        computeAssistiveStructures();
        System.out.println("Filling remaining blocks...");

        for (int x = 0; x < dim[0]; x++)
        {
            for (int y = 0; y < dim[1]; y++)
            {
                if (blocks[x][y][0] == null)
                {
                    if (numberOfIntersectionsOfVerticalRayWithMesh(x, y, 0) % 2 == 1)
                    {
                        // we create the block
                        blocks[x][y][0] = new Block(new Vector3f(x, y, 0), false);
                    }
                }
                for (int z = 1; z < dim[2]; z++)
                {
                    if (blocks[x][y][z] == null)
                    {
                        if (blocks[x][y][z - 1] == null)
                        {
                            // the previous block was empty, so this one must be as well
                            continue;
                        }
                        else
                        {
                            if (blocks[x][y][z - 1].isCustom())
                            {
                                if (numberOfIntersectionsOfVerticalRayWithMesh(x, y, z) % 2 == 1)
                                {
                                    // we create the block
                                    blocks[x][y][z] = new Block(new Vector3f(x, y, z), false);
                                }
                            }
                            else
                            {
                                // the previous was full, so this one must also be
                                blocks[x][y][z] = new Block(new Vector3f(x, y, z), false);
                            }
                        }
                    }
                }
            }
        }
        drawVoxelsOnly("../../testing/voxels.obj");
        System.out.println("Remaining blocks filled...");
    }

    private void computeAssistiveStructures()
    {
        System.out.println("Computing assistive sturctures...");
        // mesh parameters
        initTrigs = new ArrayList<>();
        int triangleCnt = initialTriangles.getVertexCount() / 3;
        // load the triangles from the initial array
        for (int i = 0; i < triangleCnt * 3; i++)
        {
            Point3f point = new Point3f();
            initialTriangles.getCoordinate(i, point);
            initTrigs.add(point);
        }

        // a list of vertices and their indexes, which will be used to determine the adjacency lists
        ArrayList<Pair<Point3f, Integer>> sortedVertices = new ArrayList<>();

        for (int i = 0; i < triangleCnt * 3; i++)
        {
            // we initialise the array which we will sort
            sortedVertices.add(new Pair<>(initTrigs.get(i), i));
        }

        // initialise the comparator and sort the vertices
        PointComparator comp = new PointComparator();
        sortedVertices.sort(comp);

        // prepare the vertex adjacency array
        sharesVertex = new ArrayList<>();
        for (int i = 0; i < triangleCnt; i++)
        {
            // an array list for each triangle
            ArrayList<ArrayList<Integer>> curr = new ArrayList<>();
            // which contains three array lists (for each vertex of the particular triangle)
            curr.add(new ArrayList<>());
            curr.add(new ArrayList<>());
            curr.add(new ArrayList<>());
            sharesVertex.add(curr);
        }

        // prepare the edge adjacency array
        sharesEdge = new ArrayList<>();
        for (int i = 0; i < triangleCnt; i++)
        {
            // an array list for each triangle
            ArrayList<ArrayList<Integer>> curr = new ArrayList<>();
            // which contains three array lists (for each edge of the particular triangle)
            curr.add(new ArrayList<>());
            curr.add(new ArrayList<>());
            curr.add(new ArrayList<>());
            sharesEdge.add(curr);
        }

        // fill the vertex adjacency array
        // we initialise a helper array which will hold the currently identical set of vertices
        int[] currentSet = new int[triangleCnt * 3];
        int setCnt = 1;
        currentSet[0] = sortedVertices.get(0).getValue();

        for (int curr = 1; curr < triangleCnt * 3; curr++)
        {
            if (areIdentical(sortedVertices.get(curr).getKey(), sortedVertices.get(curr - 1).getKey()))
            {
                // add the new vertex to the current set
                currentSet[setCnt] = sortedVertices.get(curr).getValue();
                setCnt++;
            }
            else
            {
                if (setCnt > 2)
                {
                    // if the vertex is shared by at least two triangles
                    for (int to = 0; to < setCnt; to++)
                    {
                        // we consider one of the touching triangles
                        for (int from = 0; from < setCnt; from++)
                        {
                            // and add all of the other ones to its adjacency list
                            if (from != to)
                                sharesVertex.get(currentSet[to] / 3).get(currentSet[to] % 3).add(currentSet[from] / 3);
                        }
                    }
                }

                // reset the current set
                currentSet[0] = sortedVertices.get(curr).getValue();
                setCnt = 1;
            }
        }

        // perform final check in case there is something left in the current set
        if (setCnt > 1)
        {
            // if the vertex is shared by at least two triangles
            for (int to = 0; to < setCnt; to++)
            {
                // we consider one of the touching triangles
                for (int from = 0; from < setCnt; from++)
                {
                    // and add all of the other ones to its adjacency list
                    if (from != to)
                        sharesVertex.get(currentSet[to] / 3).get(currentSet[to] % 3).add(currentSet[from] / 3);
                }
            }
        }

        // now we determine the edge adjacency array
        for (int curr = 0; curr < triangleCnt; curr++)
        {
            // we consider all the triangles, and observe their vertex adjacency lists
            for (int vIdx = 0; vIdx < 3; vIdx++)
            {
                int fromIdx = vIdx; // the point we are pulling the line from
                int toIdx = (vIdx + 1) % 3; // the point we are ending the line at
                int fromSize = sharesVertex.get(curr).get(fromIdx).size();
                int toSize = sharesVertex.get(curr).get(toIdx).size();
                boolean done = false;
                for (int from = 0; from < fromSize && !done; from++)
                {
                    // we consider a triangle in the first array and check if it can be found in the second
                    int firTriangle = sharesVertex.get(curr).get(fromIdx).get(from);
                    for (int to = 0; to < toSize; to++)
                    {
                        if (sharesVertex.get(curr).get(toIdx).get(to) == firTriangle)
                        {
                            // we found the triangle so we save and break
                            sharesEdge.get(curr).get(fromIdx).add(firTriangle);
                            done = true;
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("Assistive structures computed...");
    }

    // checks the original mesh for intersections with
    // the given ray (the ray is shot "upwards in the z direction")
    private int numberOfIntersectionsOfVerticalRayWithMesh(int x, int y, int z)
    {
        // prepare the ray
        Point3f R0 = new Point3f(x + 0.5f, y + 0.5f, z);
        Point3f R1 = new Point3f(x + 0.5f, y + 0.5f, z + 1.0f);

        // the number of intersections with the mesh
        int intersectionNo = 0;

        // mesh parameters
        int triangleCnt = initialTriangles.getVertexCount() / 3;

        // these labels will be used to determine if a triangle is to be checked for intersections
        // with the intersection ray
        boolean[] isConsidered = new boolean[triangleCnt];
        for (int i = 0; i < triangleCnt; i++)
            isConsidered[i] = true;

        // the numeric ray tracing calculations
        for (int curr = 0; curr < triangleCnt; curr++)
        {
            if (isConsidered[curr])
            {
                // pack up the current triangle
                ArrayList<Point3f> T = new ArrayList<>();
                T.add(initTrigs.get(curr * 3));
                T.add(initTrigs.get(curr * 3 + 1));
                T.add(initTrigs.get(curr * 3 + 2));
                // set up resulting point (if it exists)
                Point3f I = new Point3f(0.0f, 0.0f, 0.0f);
                // and check intersection
                int code = intersect3D_RayTriangle(R0, R1, T, I);
                if (code == 1)
                {
                    // there was a strict intersection
                    intersectionNo++;
                }
                else if (3 <= code && code <= 5)
                {
                    // there was a vertex intersection
                    // so we extract all the triangles which are touching at this one point
                    // and project them onto the xy plane. After this we check if all of the
                    // vector products of the appropriately oriented edges are either all
                    // positive or all negative. If this is not the case, then we do not add
                    // a new intersection point
                    code -= 3;
                    // we setup the initial positive and neagitve counters
                    int positiveCnt = 0, negativeCnt = 0;
                    if (vectorProd2DisPositive(T.get(code), T.get((code - 1) % 3), T.get((code + 1) % 3)))
                        positiveCnt++;
                    else
                        negativeCnt++;

                    int len = sharesVertex.get(curr).get(code).size();
                    for (int touching = 0; touching < len; touching++)
                    {
                        // load the adjacent triangle
                        ArrayList<Point3f> adj = new ArrayList<>();
                        adj.add(initTrigs.get(sharesEdge.get(curr).get(code).get(touching) * 3));
                        adj.add(initTrigs.get(sharesEdge.get(curr).get(code).get(touching) * 3 + 1));
                        adj.add(initTrigs.get(sharesEdge.get(curr).get(code).get(touching) * 3 + 2));

                        // remove the ability of the triangle to participate in the rest of the cutting
                        isConsidered[sharesEdge.get(curr).get(code).get(touching)] = false;

                        // find the point on the adjacent triangle which is not on the line in common
                        int other = 0;
                        for (other = 0; other < 3; other++)
                            if (areIdentical(adj.get(other), T.get(code)))
                                break;

                        if (vectorProd2DisPositive(adj.get(other), adj.get((other - 1) % 3), adj.get((other + 1) % 3)))
                            positiveCnt++;
                        else
                            negativeCnt++;
                    }
                    if (Math.abs(positiveCnt - negativeCnt) == len + 1)
                    {
                        // the tirangles are surrounding the punctuating ray so we increment the counter
                        intersectionNo++;
                    }
                }
                else if (6 <= code && code <= 8)
                {
                    // there was an edge intersection
                    // so we just check if the adjacent triangle cannot be projected
                    // onto the current one, and if this is the case, increment the
                    // counter, otherwise ignore it
                    code -= 6;
                    if (sharesEdge.get(curr).get(code).size() == 1)
                    {
                        // load the adjacent triangle
                        ArrayList<Point3f> adj = new ArrayList<>();
                        adj.add(initTrigs.get(sharesEdge.get(curr).get(code).get(0) * 3));
                        adj.add(initTrigs.get(sharesEdge.get(curr).get(code).get(0) * 3 + 1));
                        adj.add(initTrigs.get(sharesEdge.get(curr).get(code).get(0) * 3 + 2));

                        // remove the ability of the triangle to participate in the rest of the cutting
                        isConsidered[sharesEdge.get(curr).get(code).get(0)] = false;

                        // find the point on the adjacent triangle which is not on the line in common
                        int other = 0;
                        for (other = 0; other < 3; other++)
                            if (!areIdentical(adj.get(other), T.get(code)) || !areIdentical(adj.get(other), T.get((code + 1) % 3)))
                                break;

                        // then check if it has intersection with the model
                        int adjcode = intersect3D_RayTriangle(adj.get(other), vectorAdd(adj.get(other), new Point3f(0f, 0f, 1.0f)), T, I);
                        if (adjcode == 0)
                        {
                            // only if there is no intersection between the projection and the model do we increment the counter
                            intersectionNo++;
                        }
                    }
                    else
                    {
                        // THE MESH IS DISCONTINUOUS OR SELF INTERSECTING!!!!!!
                        // TODO handle this
                    }
                }
                isConsidered[curr] = false;
            }
        }
        return intersectionNo;
    }

    // returns true if the vector product ABxAC is positive
    private boolean vectorProd2DisPositive(Point3f A, Point3f B, Point3f C)
    {
        return ((B.x - A.x) * (C.y - A.y) - (B.y - A.y) * (C.x - A.x)) > 0;
    }

    // returns the vector product ABxAC
    private Point3f vectorProd(Point3f A, Point3f B, Point3f C)
    {
        Point3f result = new Point3f(0, 0, 0);
        result.x = (B.y - A.y) * (C.z - A.z) - (B.z - A.z) * (C.y - A.y);
        result.y = (B.z - A.z) * (C.x - A.x) - (B.x - A.x) * (C.z - A.z);
        result.z = (B.x - A.x) * (C.y - A.y) - (B.y - A.y) * (C.x - A.x);
        return result;
    }

    // returns A - B
    private Point3f vectorSub(Point3f A, Point3f B)
    {
        Point3f result = new Point3f(0, 0, 0);
        result.x = A.x - B.x;
        result.y = A.y - B.y;
        result.z = A.z - B.z;
        return result;
    }

    // returns A + B
    private Point3f vectorAdd(Point3f A, Point3f B)
    {
        Point3f result = new Point3f(0, 0, 0);
        result.x = A.x + B.x;
        result.y = A.y + B.y;
        result.z = A.z + B.z;
        return result;
    }

    // returns the dot product: A.B
    private float vectorDot(Point3f A, Point3f B)
    {
        return A.x * B.x + A.y * B.y + A.z * B.z;
    }

    // negates the given vector
    private Point3f vectorNeg(Point3f A)
    {
        Point3f result = new Point3f(0, 0, 0);
        result.x = -A.x;
        result.y = -A.y;
        result.z = -A.z;
        return result;
    }

    // multiploes the given vector by a scalar
    private Point3f vectorMul(float alpha, Point3f A)
    {
        Point3f result = new Point3f(0, 0, 0);
        result.x = alpha * A.x;
        result.y = alpha * A.y;
        result.z = alpha * A.z;
        return result;
    }

    // intersect3D_RayTriangle(): find the 3D intersection of a ray with a triangle
    //    Input:  a ray R0->R1, and a triangle T
    //    Output: I = intersection point (when it exists)
    //    Return: -1 = triangle is degenerate (a segment or point)
    //             0 = triangle and ray disjoint (no intersect)
    //             1 = intersect in unique point I1 (on the strict inside)
    //             2 = triangle and ray are in the same plane
    //             ----------------------------------------------------------
    //             3 = the ray intersects the triangle on the vertex T[0]
    //             4 = the ray intersects the triangle on the vertex T[1]
    //             5 = the ray intersects the triangle on the vertex T[2]
    //             ----------------------------------------------------------
    //             6 = the ray intersects the triangle on the edge T[0]->T[1]
    //             7 = the ray intersects the triangle on the edge T[1]->T[2]
    //             8 = the ray intersects the triangle on the edge T[2]->T[0]
    //             ----------------------------------------------------------
    int intersect3D_RayTriangle(Point3f R0, Point3f R1, ArrayList<Point3f> T, Point3f I)
    {
        Point3f u, v, n;    // triangle vectors
        Point3f dir, w0, w; // ray vectors
        float r, a, b;      // parameters to calculate ray-plane intersect

        // get triangle edge vectors and plane normal
        u = vectorSub(T.get(1), T.get(0));
        v = vectorSub(T.get(2), T.get(0));
        n = vectorProd(T.get(0), T.get(1), T.get(2)); // cross product

        dir = vectorSub(R1, R0); // ray direction vector
        w0 = vectorSub(R0, T.get(0));
        a = -vectorDot(n, w0);
        b = vectorDot(n, dir);

        if (Math.abs(b) < float_tolerance)
        {
            // ray is  parallel to triangle plane
            if (a == 0) return 2; // ray lies in triangle plane
            else return 0; // ray disjoint from plane
        }

        // get intersect point of ray with triangle plane
        r = a / b;
        if (r < 0.0)
        {
            // ray goes away from triangle
            return 0; // => no intersect
        }

        // intersect point of ray and plane
        I.set(R0.x + r * dir.x, R0.y + r * dir.y, R0.z + r * dir.z);

        // is I inside T?
        float uu, uv, vv, wu, wv, D;
        uu = vectorDot(u, u);
        uv = vectorDot(u, v);
        vv = vectorDot(v, v);
        w = vectorSub(I, T.get(0));
        wu = vectorDot(w, u);
        wv = vectorDot(w, v);
        D = uv * uv - uu * vv;

        // get and test parametric coordinates
        float s, t;
        s = (uv * wv - vv * wu) / D;
        if (s < 0.0 || s > 1.0)
        {
            // I is outside T
            return 0;
        }
        t = (uv * wu - uu * wv) / D;
        if (t < 0.0 || (s + t) > 1.0)
        {
            // I is outside T
            return 0;
        }

        boolean[] onEdge = new boolean[3];
        if (s < float_tolerance)
            onEdge[0] = true;
        if (Math.abs(s + t - 1) < float_tolerance)
            onEdge[1] = true;
        if (t < float_tolerance)
            onEdge[2] = true;

        if (onEdge[0] && onEdge[1]) return 3; // the ray intersects the triangle on the vertex T[0]
        if (onEdge[1] && onEdge[2]) return 4; // the ray intersects the triangle on the vertex T[1]
        if (onEdge[2] && onEdge[0]) return 5; // the ray intersects the triangle on the vertex T[2]
        if (onEdge[0]) return 6; // the ray intersects the triangle on the edge T[0]->T[1]
        if (onEdge[1]) return 7; // the ray intersects the triangle on the edge T[1]->T[2]
        if (onEdge[2]) return 8; // the ray intersects the triangle on the edge T[2]->T[0]
        return 1; // I is in T
    }

    // checks if two 3d points are identical
    private boolean areIdentical(Point3f ver1, Point3f ver2)
    {
        return Math.abs(ver1.x - ver2.x) < float_tolerance
                && Math.abs(ver1.y - ver2.y) < float_tolerance
                && Math.abs(ver1.z - ver2.z) < float_tolerance;
    }

    /**
     * TESTING method
     */
    public void drawTrianglesFromBlocks(String filename)
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
            for (int y = 0; y < matrixDimensions.y; y++)
            {
                for (int z = 0; z < matrixDimensions.z; z++)
                {
                    if (blocks[x][y][z] == null)
                        continue;
                    //blocks[x][y][z].drawBlock("../../testing/blocks/block " + x + " " + " " + y + " " + z + ".obj");
                    ArrayList<Point3f> triangles = new ArrayList<>(blocks[x][y][z].getTriangles());

                    totalTriangles += blocks[x][y][z].getTriangleCount();
                    for (int i = 0; i < blocks[x][y][z].getTriangleCount() * 3; i++)
                    {
                        try
                        {
                            /*
                            writer.write("v " + triangles.get(i).x + " "
                                    + triangles.get(i).y + " "
                                    + triangles.get(i).z + "\n");
                                    */
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

    private ArrayList<Point3f> makeUnitCube()
    {
        ArrayList<Point3f> cube = new ArrayList<>();
        //yz01
        cube.add(new Point3f(0, 1, 0));
        cube.add(new Point3f(0, 0, 0));
        cube.add(new Point3f(0, 1, 1));

        //yz02
        cube.add(new Point3f(0, 1, 1));
        cube.add(new Point3f(0, 0, 0));
        cube.add(new Point3f(0, 0, 1));

        //xz01
        cube.add(new Point3f(0, 0, 1));
        cube.add(new Point3f(0, 0, 0));
        cube.add(new Point3f(1, 0, 1));

        //xz02
        cube.add(new Point3f(1, 0, 1));
        cube.add(new Point3f(0, 0, 0));
        cube.add(new Point3f(1, 0, 0));

        //xy01
        cube.add(new Point3f(1, 0, 0));
        cube.add(new Point3f(0, 0, 0));
        cube.add(new Point3f(1, 1, 0));

        //xy02
        cube.add(new Point3f(1, 1, 0));
        cube.add(new Point3f(0, 0, 0));
        cube.add(new Point3f(0, 1, 0));

        //yz11
        cube.add(new Point3f(1, 0, 0));
        cube.add(new Point3f(1, 1, 0));
        cube.add(new Point3f(1, 1, 1));

        //yz12
        cube.add(new Point3f(1, 0, 0));
        cube.add(new Point3f(1, 1, 1));
        cube.add(new Point3f(1, 0, 1));

        //xz11
        cube.add(new Point3f(0, 1, 1));
        cube.add(new Point3f(1, 1, 1));
        cube.add(new Point3f(0, 1, 0));

        //xz12
        cube.add(new Point3f(0, 1, 0));
        cube.add(new Point3f(1, 1, 1));
        cube.add(new Point3f(1, 1, 0));

        //xy11
        cube.add(new Point3f(1, 0, 1));
        cube.add(new Point3f(1, 1, 1));
        cube.add(new Point3f(0, 0, 1));

        //xy12
        cube.add(new Point3f(0, 0, 1));
        cube.add(new Point3f(1, 1, 1));
        cube.add(new Point3f(0, 1, 1));

        return cube;
    }

    public void drawVoxelsOnly(String filename)
    {
        System.out.println("Preparing the voxel output...");
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
            for (int y = 0; y < matrixDimensions.y; y++)
            {
                for (int z = 0; z < matrixDimensions.z; z++)
                {
                    if (blocks[x][y][z] == null || blocks[x][y][z].isCustom())
                        continue;
                    ArrayList<Point3f> triangles = makeUnitCube();

                    totalTriangles += triangles.size()/3;
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
     * Generate custom parts for Blocks which need it
     */
    void generateCustomParts()
    {
        // TODO
    }

    /**
     * Write all the custom parts to the provided directory using the provided MeshIO
     *
     * @param directory the directory to write the files to
     * @param meshIO    the MeshIO object to use
     */
    void writeCustomPartsToDirectory(String directory, MeshIO meshIO) throws IOException
    {
        // TODO
        throw new RuntimeException("Not Implemented");
    }
}
