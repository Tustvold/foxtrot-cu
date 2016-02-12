package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.serializer.BlockJSONSerializer;
import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.Mesh;
import cam.ac.uk.foxtrot.voxelisation.MeshIO;
import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.google.gson.GsonBuilder;
import com.sun.j3d.loaders.Scene;
import com.google.gson.Gson;

import java.io.IOException;

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
        Scene scene;
        try
        {
            scene = meshIO.readFromFile(filePath);
        } catch (IOException error)
        {
            System.err.println("Loading fialied:" + error.getMessage());
            return;
        }
        Mesh m = new Mesh(scene);

        // voxelise it
        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();



        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().setPrettyPrinting().registerTypeAdapter(Block.class, new BlockJSONSerializer());
        Gson gsonParser = builder.create();
        System.out.print(gsonParser.toJson(blocks));


        //TODO: Generate Instructions

        //TODO: Generate Custom Parts Zip File

    }
}
