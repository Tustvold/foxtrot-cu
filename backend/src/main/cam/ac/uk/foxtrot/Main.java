package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.deserializer.BlockJSONDeserializer;
import cam.ac.uk.foxtrot.serializer.BlockJSONSerializer;
import cam.ac.uk.foxtrot.sidefiller.SideFiller;
import cam.ac.uk.foxtrot.voxelisation.*;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.geometry.Side;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.vecmath.Point3d;
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

        // get the function reversing parameters (needed for yellow cube highlight on the frontend)
        // if the original transformation for all points is: P --> P' the reverse is then:
        // P = InitialCenterOfMass + (P' - MeshOffset)*blockSize
        Point3d InitialCenterOfMass = m.getInitialCM();
        Point3d MeshOffset = m.getOffset();

        // fill in the missing sides
        SideFiller filler = new SideFiller(blocks);
        blocks = filler.getBlocks();

        // add custom parts
        ArrayList<Block> sortedBlocks = new ArrayList<>();
        int[] dim = {blocks.length, blocks[0].length, blocks[0][0].length};
        // set make a selection of all custom parts
        int currdim = dim[1];
        int selectedProjection = 1;
        if(dim[0] < currdim)
        {
            selectedProjection = 0;
            currdim = dim[0];
        }
        if(dim[2] < currdim)
        {
            selectedProjection = 2;
        }
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

                    // add the block to the sorted array
                    sortedBlocks.add(block);

                    // generate all the custom parts for the blocks
                    CustomPartGenerator cp = new CustomPartGenerator(block);
                    cp.generateAllCustomParts();

                    // nullify the selection initially, so that we can later select the top n as custom
                    block.setIsCustom(false);
                    block.setSuggestedCustomPartIndex(selectedProjection);
                }
            }
        }

        sortedBlocks.sort((Block o1, Block o2) -> o2.getTriangleCount() - o1.getTriangleCount());
        for (int i = 0; i < sortedBlocks.size() && i < numCustomParts; i++)
        {
            sortedBlocks.get(i).setIsCustom(true);
        }

        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().registerTypeAdapter(Block.class, new BlockJSONSerializer());
        Gson gsonParser = builder.create();

        // write the gson to a file
        FunctionParameters parameters = new FunctionParameters(InitialCenterOfMass, MeshOffset, blocks);
        Writer writer;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonOut), "utf-8"));
            writer.write(gsonParser.toJson(parameters));
            writer.close();
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Given a JSON file representing a block list and a directory, output mould files from the block list into the
     * directory.
     *
     * @param jsonIn                path to JSON file representing a block list
     * @param mouldDirectoryPath    directory in which to write mould files
     * @param scale                 size of a block in mm
     */
    private static void mouldify(String jsonIn, String mouldDirectoryPath, float scale)
    {
        Block[][][] blocks;

        try {

            GsonBuilder b = new GsonBuilder();
            b.registerTypeAdapter(Block.class, new BlockJSONDeserializer());

            Gson gson = b.create();
            JsonReader reader = new JsonReader(new FileReader(jsonIn));
            blocks = gson.fromJson(reader, Block[][][].class);
            
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

                        // TODO HACK HERE
                        ProjectionUtils.ProjectionFace face;
                        switch (block.getCustomPartIndex()) {
                            case 0: face = ProjectionUtils.ProjectionFace.XY0;break;
                            case 1: face = ProjectionUtils.ProjectionFace.XY1;break;
                            case 2: face = ProjectionUtils.ProjectionFace.ZX0;break;
                            case 3: face = ProjectionUtils.ProjectionFace.ZX1;break;
                            case 4: face = ProjectionUtils.ProjectionFace.ZY0;break;
                            case 5: face = ProjectionUtils.ProjectionFace.ZY1;break;
                            default: throw new IllegalArgumentException("mouldify: block's custom part index must be between 0 and 5.");
                        }

                        Point3d[] al = block.getCustomPart()[block.getCustomPartIndex()].getTriangles();

                        if (al != null) {
                            CustomPartMouldGenerator m = new CustomPartMouldGenerator(al, scale);
                            File outFile = new File(mouldDirectoryPath, x + "-" + y + "-" + z + ".obj"); //todo better name?
                            m.generateMould(face, outFile);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * @param args arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        if (args.length < 4)
        {
            System.err.println("Error: Need at least 4 arguments.");
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
        else if (methodName.equals("mouldify")) {
            try
            {
                mouldify(args[1], args[2], Float.parseFloat(args[3]));
            } catch (NumberFormatException e)
            {
                System.err.println("Error: argument 3 should be a number for mouldification.");
            }
        }
        else
        {
            System.err.println("Error: invalid method name.");
        }
    }
}
