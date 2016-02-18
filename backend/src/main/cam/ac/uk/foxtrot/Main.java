package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.serializer.BlockJSONSerializer;
import cam.ac.uk.foxtrot.voxelisation.*;
import com.google.gson.GsonBuilder;
import com.sun.j3d.loaders.Scene;
import com.google.gson.Gson;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import java.io.*;
import java.util.ArrayList;

public class Main
{

    /**
     * Voxelise a given object file and write the output to a temporary JSON file representing a block list.
     *
     * @param objPath        path to object to voxelise
     * @param jsonOut        temporary filename to write output JSON to
     * @param numCustomParts number of custom parts to suggest by default
     * @param scale          size of a block in mm
     */
    private static void voxelise(String objPath, String jsonOut, int numCustomParts, float scale)
    {
        // input the mesh
        MeshIO meshIO = new MeshIO();
        ArrayList<Point3d> tri;
        try
        {
            tri = meshIO.getTriangles(objPath);
        } catch (IOException error)
        {
            System.err.println("Loading failed:" + error.getMessage());
            return;
        }
        if (tri == null || tri.size() == 0)
        {
            System.err.println("Loading failed: Input file is empty!");
            return;
        }
        if (tri.size() % 3 != 0)
        {
            System.err.println("Loading failed: Input file is malformed!");
            return;
        }
        Mesh m = new Mesh(tri, scale);
        // voxelise it
        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();


        // add custom parts
        ArrayList<Block> sortedBlocks = new ArrayList<>();
        int[] dim = {blocks.length, blocks[0].length, blocks[0][0].length};
        for (int x = 0; x < dim[0]; x++)
        {
            for (int y = 0; y < dim[1]; y++)
            {
                for (int z = 0; z < dim[2]; z++)
                {
                    Block block = blocks[x][y][z];
                    if (block == null || !block.isCustom())
                    {
                        // we ignore all non custom blocks
                        continue;
                    }

                    sortedBlocks.add(block);
                    ArrayList<Point3d> al = block.getTriangles();

                    CustomPartMouldGenerator cp = new CustomPartMouldGenerator(al.toArray(new Point3f[al.size()]));
                    CustomPart p0 = new CustomPart(cp.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.XY0));
                    block.setCustomPart(0, p0);
                    CustomPart p1 = new CustomPart(cp.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.XY1));
                    block.setCustomPart(1, p1);
                    CustomPart p2 = new CustomPart(cp.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.ZX0));
                    block.setCustomPart(2, p2);
                    CustomPart p3 = new CustomPart(cp.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.ZX1));
                    block.setCustomPart(3, p3);
                    CustomPart p4 = new CustomPart(cp.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.ZY0));
                    block.setCustomPart(4, p4);
                    CustomPart p5 = new CustomPart(cp.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.ZY1));
                    block.setCustomPart(5, p5);

                    //todo accept scale for custom parts

                    // set suggested custom part
                    // nullify the selection initially, so that we can later select the top n as custom
                    block.setIsCustom(false);
                    if (x + 1 < dim[0] && blocks[x + 1][y][z] != null)
                    {
                        block.setSuggestedCustomPartIndex(5);
                    }
                    else if (x - 1 > 0 && blocks[x - 1][y][z] != null)
                    {
                        block.setSuggestedCustomPartIndex(4);
                    }
                    else if (z + 1 < dim[2] && blocks[x][y][z + 1] != null)
                    {
                        block.setSuggestedCustomPartIndex(1);
                    }
                    else if (z - 1 > 0 && blocks[x][y][z - 1] != null)
                    {
                        block.setSuggestedCustomPartIndex(0);
                    }
                    else if (y - 1 > 0 && blocks[x][y - 1][z] != null)
                    {
                        block.setSuggestedCustomPartIndex(2);
                    }
                    else if (y + 1 < dim[1] && blocks[x][y + 1][z] != null)
                    {
                        block.setSuggestedCustomPartIndex(3);
                    }
                }
            }
        }

        sortedBlocks.sort((Block o1, Block o2) -> o2.getTriangleCount() - o1.getTriangleCount());
        for (int i = 0; i < numCustomParts; i++)
        {
            sortedBlocks.get(i).setIsCustom(true);
        }

        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().setPrettyPrinting().registerTypeAdapter(Block.class, new BlockJSONSerializer());
        Gson gsonParser = builder.create();

        // write the gson to a file
        Writer writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonOut), "utf-8"));
            writer.write(gsonParser.toJson(blocks));
            writer.close();
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
            return;
        }
    }

    /**
     * Given a JSON file representing a block list and a directory, output mould files from the block list into the
     * directory.
     *
     * @param jsonIn
     * @param mouldDirectoryPath
     */
    private static void mouldify(String jsonIn, String mouldDirectoryPath)
    {

    }


    /**
     * @param args arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        if (args.length < 3)
        {
            System.err.println("Error: Need at least 3 arguments.");
            return;
        }
        String methodName = args[0];
        if (methodName.equals("voxelise"))
        {
            if (args.length < 5)
            {
                System.err.println("Error: Need at least 5 arguments for voxelisation.");
                return;
            }
            try
            {
                voxelise(args[1], args[2], Integer.parseInt(args[3]), Float.parseFloat(args[4]));
            } catch (NumberFormatException e)
            {
                System.err.println("Error: arguments 4 and 5 should be numbers for voxelisation.");
            }
        }
        else if (methodName.equals("mouldify"))
        {
            mouldify(args[1], args[2]);
        }
        else
        {
            System.err.println("Error: invalid method name.");
        }


        String filePath = args[0];


    }
}
