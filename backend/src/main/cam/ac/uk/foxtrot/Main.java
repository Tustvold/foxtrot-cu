package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.serializer.BlockJSONSerializer;
import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.Mesh;
import cam.ac.uk.foxtrot.voxelisation.MeshIO;
import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.google.gson.GsonBuilder;
import com.sun.j3d.loaders.Scene;
import com.google.gson.Gson;

import javax.vecmath.Point3d;
import java.io.*;
import java.util.ArrayList;

public class Main
{


    /**
     * Expects the following arguments
     * <p>
     * 1. Output Directory
     * 2. Input Mesh File Location
     *
     * @param args arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        if (args.length < 1)
        {
            System.err.println("Error: No file");
            return;
        }
        String filePath = args[0];

        // input the mesh
        MeshIO meshIO = new MeshIO();
        ArrayList<Point3d> input;
        try
        {
            input = meshIO.readFromFile(filePath);
        } catch (IOException error)
        {
            System.err.println("Loading fialied:" + error.getMessage());
            return;
        }
        if(input == null || input.size() == 0)
        {
            System.err.println("Loading fialied: Input file is empty!");
            return;
        }
        if(input.size() % 3 != 0)
        {
            System.err.println("Loading fialied: Input file is malformed!");
            return;
        }
        Mesh m = new Mesh(input);

        // voxelise it
        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();



        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().setPrettyPrinting().registerTypeAdapter(Block.class, new BlockJSONSerializer());
        Gson gsonParser = builder.create();

        // write the gson to a file
        Writer writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testing/output/mesh.json"), "utf-8"));
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
            return;
        }
        writer.write(gsonParser.toJson(blocks));
        writer.close();


        //TODO: Generate Instructions

        //TODO: Generate Custom Parts Zip File

    }
}
