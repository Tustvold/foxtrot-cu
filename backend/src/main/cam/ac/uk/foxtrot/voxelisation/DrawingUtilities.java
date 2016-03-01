package cam.ac.uk.foxtrot.voxelisation;

import cam.ac.uk.foxtrot.sidefiller.Point;

import javax.vecmath.Point3d;
import java.io.*;
import java.util.ArrayList;

public class DrawingUtilities
{
    /**
     * Outputs a .obj file representing the block subdivision within the block matrix.
     *
     * @param block    the block to draw
     * @param filename name of file to which to write output
     */
    public static void drawBlock(Block block, String filename)
    {
        System.out.println("Drawing single block...");
        Writer writer = null;
        int triangleCnt = block.getTriangleCount();
        ArrayList<Point3d> triangles = block.getTriangles();
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }

        int totalTriangles = 0;

        for (int i = 0; i < triangleCnt * 3; i++)
        {
            try
            {
                Point3d currPt = triangles.get(i);
                writer.write("v " + currPt.x + " " + currPt.y + " " + currPt.z + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write vertex: " + err.getMessage());
            }
        }

        for (int i = 1; i < triangleCnt * 3; i += 3)
        {
            try
            {
                writer.write("f " + i + " " + (i + 1) + " " + (i + 2) + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write triangle: " + err.getMessage());
            }
        }
        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Done!");
    }

    /**
     * Outputs a .obj file representing the provided block matrix spaced equally.
     *
     * @param filename    name of file to which to write output
     * @param includeGrid if the x0z grid should be included
     * @param spacing     the spacing between the blocks
     * @param mode        0 -> just the block with mesh pieces
     *                    1 -> just the internal voxels
     *                    2 -> both
     */
    public static void drawBlocks(Block[][][] blocks, String filename, boolean includeGrid, double spacing, int mode)
    {
        System.out.println("Drawing blocks...");
        Writer writer = null;

        if (blocks == null)
            return;

        int[] dim = new int[3];
        dim[0] = blocks.length;
        dim[1] = blocks[0].length;
        dim[2] = blocks[0][0].length;

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
                            writer.write("v " + (triangles.get(i).x + x + x * spacing) + " "
                                    + (triangles.get(i).y) + " "
                                    + (triangles.get(i).z + z + z * spacing) + "\n");

                        } catch (IOException err)
                        {
                            System.err.println("Could not write blocks: " + err.getMessage());
                        }
                    }
                }
                for (int y = 0; y < dim[1]; y++)
                {
                    Block curr = blocks[x][y][z];
                    if (curr == null)
                        continue;
                    if (mode % 3 == 0 && !curr.isCustom())
                        continue;
                    if (mode % 3 == 1 && curr.isCustom())
                        continue;

                    ArrayList<Point3d> triangles;

                    if (curr.isCustom())
                        triangles = new ArrayList<>(curr.getTriangles());
                    else if (spacing > 0)
                        triangles = makeUnitCube();
                    else
                        triangles = makeUnitCube(blocks, x, y, z);

                    totalTriangles += triangles.size() / 3;
                    for (int i = 0; i < triangles.size(); i++)
                    {
                        try
                        {
                            writer.write("v " + (triangles.get(i).x + curr.getPosition().x * (1 + spacing)) + " "
                                    + (triangles.get(i).y + curr.getPosition().y * (1 + spacing)) + " "
                                    + (triangles.get(i).z + curr.getPosition().z * (1 + spacing)) + "\n");

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
        System.out.println("Done!");
    }


    /**
     * Creates a horizontal unit square in the xz plane.
     *
     * @return list of appropriate triangles
     */
    private static ArrayList<Point3d> makeHorizontalSquare()
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
     * Creates a unit cube at (x,y,z) only creating visible sides.
     *
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     * @return list of appropriate triangles
     */
    public static ArrayList<Point3d> makeUnitCube(Block[][][] blocks, int x, int y, int z)
    {
        if (blocks == null)
            return null;

        int[] dim = new int[3];
        dim[0] = blocks.length;
        dim[1] = blocks[0].length;
        dim[2] = blocks[0][0].length;

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
     * Creates a unit cube with all sides.
     */
    public static ArrayList<Point3d> makeUnitCube()
    {
        ArrayList<Point3d> cube = new ArrayList<>();

        //yz01
        cube.add(new Point3d(0, 1, 0));
        cube.add(new Point3d(0, 0, 0));
        cube.add(new Point3d(0, 1, 1));

        //yz02
        cube.add(new Point3d(0, 1, 1));
        cube.add(new Point3d(0, 0, 0));
        cube.add(new Point3d(0, 0, 1));

        //xz01
        cube.add(new Point3d(0, 0, 1));
        cube.add(new Point3d(0, 0, 0));
        cube.add(new Point3d(1, 0, 1));

        //xz02
        cube.add(new Point3d(1, 0, 1));
        cube.add(new Point3d(0, 0, 0));
        cube.add(new Point3d(1, 0, 0));

        //xy01
        cube.add(new Point3d(1, 0, 0));
        cube.add(new Point3d(0, 0, 0));
        cube.add(new Point3d(1, 1, 0));

        //xy02
        cube.add(new Point3d(1, 1, 0));
        cube.add(new Point3d(0, 0, 0));
        cube.add(new Point3d(0, 1, 0));

        //yz11
        cube.add(new Point3d(1, 0, 0));
        cube.add(new Point3d(1, 1, 0));
        cube.add(new Point3d(1, 1, 1));

        //yz12
        cube.add(new Point3d(1, 0, 0));
        cube.add(new Point3d(1, 1, 1));
        cube.add(new Point3d(1, 0, 1));

        //xz11
        cube.add(new Point3d(0, 1, 1));
        cube.add(new Point3d(1, 1, 1));
        cube.add(new Point3d(0, 1, 0));

        //xz12
        cube.add(new Point3d(0, 1, 0));
        cube.add(new Point3d(1, 1, 1));
        cube.add(new Point3d(1, 1, 0));

        //xy11
        cube.add(new Point3d(1, 0, 1));
        cube.add(new Point3d(1, 1, 1));
        cube.add(new Point3d(0, 0, 1));

        //xy12
        cube.add(new Point3d(0, 0, 1));
        cube.add(new Point3d(1, 1, 1));
        cube.add(new Point3d(0, 1, 1));
        return cube;
    }

    /**
     * Outputs a .obj file representing the triangles passed.
     *
     * @param triangles    the triangle array to draw
     * @param filename name of file to which to write output
     */
    public static void drawTriangles(ArrayList<Point3d> triangles, String filename)
    {
        System.out.println("Drawing single block...");
        Writer writer = null;
        int triangleCnt = triangles.size()/3;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }

        int totalTriangles = 0;

        for (int i = 0; i < triangleCnt * 3; i++)
        {
            try
            {
                Point3d currPt = triangles.get(i);
                writer.write("v " + currPt.x + " " + currPt.y + " " + currPt.z + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write vertex: " + err.getMessage());
            }
        }

        for (int i = 1; i < triangleCnt * 3; i += 3)
        {
            try
            {
                writer.write("f " + i + " " + (i + 1) + " " + (i + 2) + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write triangle: " + err.getMessage());
            }
        }
        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Done!");
    }


    public static void drawPolygons(Point3dPolygon[] poly, String filename)
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

        try
        {
            for (int p = 0; p < poly.length; p++)
            {
                Point3d[][] holes = poly[p].getHoles();
                Point3d[] ext = poly[p].getExterior();
                for (int i = 0; i < ext.length; i++)
                {
                    writer.write("v " + ext[i].x + " "
                            + ext[i].y + " "
                            + ext[i].z + "\n");
                }
                for (int i = 0; i < holes.length; i++)
                {
                    for (int j = 0; j < holes[i].length; j++)
                    {
                        writer.write("v " + holes[i][j].x + " "
                                + holes[i][j].y + " "
                                + holes[i][j].z + "\n");
                    }
                }
            }
            int cumulative = 1;
            for (int p = 0; p < poly.length; p++)
            {
                Point3d[][] holes = poly[p].getHoles();
                Point3d[] ext = poly[p].getExterior();
                writer.write("f");
                for (int i = 0; i < ext.length; i++)
                    writer.write(" " + (i + cumulative));
                writer.write("\n");
                cumulative += ext.length;
                for (int i = 0; i < holes.length; i++)
                {
                    writer.write("f");
                    for (int j = 0; j < holes[i].length; j++)
                        writer.write(" " + (j + cumulative));
                    writer.write("\n");
                    cumulative += holes[i].length;
                }
            }
        } catch (IOException err)
        {
            System.err.println("Could not write triangles: " + err.getMessage());
        }

        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Triangles drawn...");
    }
}
