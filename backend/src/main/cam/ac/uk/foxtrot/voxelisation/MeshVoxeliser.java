package cam.ac.uk.foxtrot.voxelisation;

import javax.media.j3d.Material;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

public class MeshVoxeliser
{
    private Mesh mesh;
    private Point3i matrixDimensions; // dimesions of the block matrix
    private Point3f meshOffset;
    private TriangleArray initialTriangles;
    private final float float_tolerance = 0.0000001f;
    Block[][][] blocks;

    public Block[][][] getBlocks()
    {
        return blocks;
    }

    /**
     * Generate a grid of blocks given a particular mesh
     * <p>
     * Elements in the grid with no mesh will be null
     *
     * @param mesh the mesh to voxelise
     */
    public MeshVoxeliser(Mesh mesh)
    {
        this.mesh = mesh;
        meshOffset = new Point3f(0, 0, 0);
        initialTriangles = mesh.getTriangles();
        voxeliseMesh();
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
        generateCustomParts(blocks);
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

    // determines the minimum values of the x,y, and z dimensions separately
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
            } else
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

    // determines the aximum values of the x,y, and z dimensions separately
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
            } else
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

    // determines the offset of one coordinate and sets the dimensions value
    private float calculateSingleOffset(int type, float minBound, float maxBound)
    {
        int dimension = (int) Math.ceil((float) Math.ceil(maxBound) - minBound);
        //System.out.println("dimen: " + dimension);
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
        //System.out.println("ret: " + ret);
        return ret;
    }

    // calculates the vector which need to be added to all the points, so that the mesh becomes centered
    // in the block matrix
    private void setMeshOffsetAndDetermineDimensions()
    {
        System.out.println("Setting mesh offset and determining dimensions...");
        Point3f minBound = getMinimumInitialCoodrinateBounds();
        Point3f maxBound = getMaximumInitialCoodrinateBounds();
        float diffx = calculateSingleOffset(0, minBound.x, maxBound.x);
        float diffy = calculateSingleOffset(1, minBound.y, maxBound.y);
        float diffz = calculateSingleOffset(2, minBound.z, maxBound.z);
        System.out.println("Mesh offsets: " + diffx + " " + diffy + " " + diffz);

        meshOffset = new Point3f(diffx, diffy, diffz);
        mesh.setOffset(meshOffset);
        System.out.println("Mesh offset set and dimensions determined...");
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
        mesh.drawTriangles("actualInitial.obj");
        System.out.println("Mesh shifted...");
    }

    // calculates ABxAC
    private Point3f vectorProd(Point3f A, Point3f B, Point3f C)
    {
        Point3f res = new Point3f(0, 0, 0);
        res.x = (B.y - A.y) * (C.z - A.z) - (C.y - A.y) * (B.z - A.z);
        res.y = (B.z - A.z) * (C.x - A.x) - (C.z - A.z) * (B.x - A.x);
        res.z = (B.x - A.x) * (C.y - A.y) - (C.x - A.x) * (B.y - A.y);
        return res;
    }

    // assuming one of the coordiates is zeroed, restores the point to the 3d space
    private Point3f restoreTo3D(Point3f point, Point3f abc, float d)
    {
        float[] coords = new float[3];
        float[] param = new float[3];
        point.get(coords);
        abc.get(param);

        for (int i = 0; i < 3; i++)
        {
            if (coords[i] < float_tolerance)
            {
                coords[i] = (d - param[(i + 1) % 3] * coords[(i + 1) % 3]
                        - param[(i + 2) % 3] * coords[(i + 2) % 3]) / param[i];
                break;
            }
        }
        return new Point3f(coords);
    }


    // intersects the line between fir and sec with the vertical line at line ignoring the ignoreth coordinate
    // and writes the return in res. Returns true if there is an intersection
    private boolean intersect(Point3f fir, Point3f sec, float line, int ignore, Point3f res)
    {
        float x1, y1, x2, y2;
        float[] coordfir = new float[3];
        float[] coordsec = new float[3];
        fir.get(coordfir);
        sec.get(coordsec);
        if (coordfir[(ignore + 1) % 3] < coordsec[(ignore + 1) % 3])
        {
            x1 = coordfir[(ignore + 1) % 3];
            y1 = coordfir[(ignore + 2) % 3];
            x2 = coordsec[(ignore + 1) % 3];
            y2 = coordsec[(ignore + 2) % 3];
        } else
        {
            x1 = coordsec[(ignore + 1) % 3];
            y1 = coordsec[(ignore + 2) % 3];
            x2 = coordfir[(ignore + 1) % 3];
            y2 = coordfir[(ignore + 2) % 3];
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
                } else
                {
                    // the intersecting point is the one with the lower x coordinate
                    if (coordfir[(ignore + 1) % 3] < coordsec[(ignore + 1) % 3])
                        res.set(1f, -1f, 0f); // intersection is on fir
                    else
                        res.set(1f, 1f, 0f); // intersection is on sec
                    // we create a new division, but do not create a new vertex
                    return false;
                }
            } else if (Math.abs(x2 - line) < float_tolerance)
            {
                // the intersecting point is the one with the greater x coordinate
                if (coordfir[(ignore + 1) % 3] < coordsec[(ignore + 1) % 3])
                    res.set(1f, 1f, 0f); // intersection is on sec
                else
                    res.set(1f, -1f, 0f); // intersection is on fir
                // we create a new division, but do not create a new vertex
                return false;
            } else
            {
                // the lines are simply not intersecting
                return false;
            }
        }

        float xNew = line;
        float yNew = (y1 * (x2 - line) + y2 * (line - x1)) / (x2 - x1);

        float[] coordNew = new float[3];
        coordNew[ignore] = 0.00000000f;
        coordNew[(ignore + 1) % 3] = xNew;
        coordNew[(ignore + 2) % 3] = yNew;
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
            } else
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
            } else
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

    // cuts the triangle given by 3 vertices into pieces and puts the pieces into appropriate bins
    private void subdivideAndClassifyTriangle(ArrayList<Point3f> triangle)
    {
        // array which contains the intermediate results of triangle subdivision
        ArrayList<ArrayList<Point3f>> polygonList = new ArrayList<>();

        // global parameters used in the plane equation of the triangles
        // ax + by + cz = d
        // where:
        // a = abc.x
        // b = abc.y
        // c = abc.z
        Point3f abc = vectorProd(triangle.get(0), triangle.get(1), triangle.get(2));
        float d = abc.x * triangle.get(0).x
                + abc.y * triangle.get(0).y
                + abc.z * triangle.get(0).z;


        // initially we start dividing the triangle
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
            int currLen = polygonList.size();

            // we iterate through all the possible lines which may be intersecting some of our polygons
            for (int line = currMinBound; line <= currMaxBound; line++)
            {
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
                        } else
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
                            } else if (res.x > 0.5f)
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
                                } else if (res.y > 0.5f)
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
                            } else
                            {
                                // no intersection was observed, so we just add the second vertex
                                Point3f sec = new Point3f(poly.get((ver + 1) % cnt));
                                if (ver != cnt - 1)
                                    polys.get(side).add(sec);
                            }
                        }
                    }

                    if (!onlyFirst && polys.get(1) != null && polys.get(1).size() > 2)
                    {
                        // this means we need to refactor the polygon list
                        polygonList.set(curr, new ArrayList<>(polys.get(0)));
                        polygonList.add(new ArrayList<>(polys.get(1)));
                    }
                }
            }

            // we now need to correct the position of the points in each polygon by fixing the zeroed values
            for (int currPoly = 0; currPoly < polygonList.size(); currPoly++)
            {
                for (int pt = 0; pt < polygonList.get(currPoly).size(); pt++)
                {
                    Point3f fixedPt = restoreTo3D(polygonList.get(currPoly).get(pt), abc, d);
                    polygonList.get(currPoly).set(pt, fixedPt);
                }
            }
        }


        // finally we need to classify the polygons into their respective bins
        classifyPolygons(polygonList);
    }

    // clasififies the polygons obtained in the polygon list by adding them to their respective bins
    private void classifyPolygons(ArrayList<ArrayList<Point3f>> polygonList)
    {
        int cnt = polygonList.size();
        for (int curr = 0; curr < cnt; curr++)
        {
            ArrayList<Point3f> poly = polygonList.get(curr);
            Point3f cm = getCenterOfMass(poly);
            int x = (int) cm.x;
            int y = (int) cm.y;
            int z = (int) cm.z;

            System.out.println("Current representative point: " + cm.x + " " + cm.y + " " + cm.z);

            if (blocks[x][y][z] == null)
            {
                blocks[x][y][z] = new Block(new Vector3f(x, y, z));
            }

            // add the first triangle
            blocks[x][y][z].addTriangle(poly.get(0), poly.get(1), poly.get(2));
            for (int point = 3; point < poly.size(); point++)
            {
                // triangulate the remaining shape if needed
                blocks[x][y][z].addTriangle(poly.get(0), poly.get(point - 1), poly.get(point));
            }
        }
    }

    // labels the remaining blocks within the mesh as full
    private void fillRemainingBlocks()
    {
        System.out.println("Filling remaining blocks...");
        System.out.println("TODO!!!...");
        //TODO!
        System.out.println("Remaining blocks filled...");
    }

    private void drawTrianglesFromBlocks(String filename)
    {
        System.out.println("Preparing output...");
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
                    blocks[x][y][z].drawBlock("blocks/block " + x + " " + " " + y + " " + z + ".obj");
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
        System.out.println("Output created...");
    }

    /**
     * Generate blocks for the given mesh
     * <p>
     * Elements in the grid with no mesh will be null
     *
     * @param mesh the mesh to generate blocks from
     * @return Grid of blocks in order x,y,z
     */
    private Block[][][] generateBlocks(Mesh mesh)
    {
        System.out.println("Filling blocks...");
        blocks = new Block[matrixDimensions.x][matrixDimensions.y][matrixDimensions.z];
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
            subdivideAndClassifyTriangle(tmp);
        }
        fillRemainingBlocks();
        drawTrianglesFromBlocks("out.obj");
        System.out.println("All blocks filled...");
        return blocks;
    }

    /**
     * Generate custom parts for Blocks which need it
     *
     * @param blocks Grid of blocks to generate custom parts for
     */
    void generateCustomParts(Block[][][] blocks)
    {

    }

    /**
     * Write all the custom parts to the provided directory using the provided MeshIO
     *
     * @param directory the directory to write the files to
     * @param meshIO    the MeshIO object to use
     */
    void writeCustomPartsToDirectory(String directory, MeshIO meshIO) throws IOException
    {
        throw new RuntimeException("Not Implemented");
    }
}
