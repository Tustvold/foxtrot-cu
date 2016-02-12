package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.serializer.BlockJSONSerializer;
import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.Mesh;
import cam.ac.uk.foxtrot.voxelisation.MeshIO;
import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.google.gson.GsonBuilder;
import com.sun.j3d.loaders.Scene;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class Main
{


    /**
     * Expects the following arguments
     * <p>
     * 1. Input Mesh File Location
     * 2. Output Directory
     *
     *
     * @param args arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        if (args.length != 2) {
            System.err.println("Expected Usage: <objfile> <output_dir>");
            return;
        }
        String filePath = args[0];
        String output_dir = args[1] + "/";

        // input the mesh
        MeshIO meshIO = new MeshIO();
        Scene scene;
        try
        {
            scene = meshIO.readFromFile(filePath);
        } catch (IOException error)
        {
            System.err.println("Loading failed:" + error.getMessage());
            return;
        }
        Mesh m = new Mesh(scene);

        // voxelise it
        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();



        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls().registerTypeAdapter(Block.class, new BlockJSONSerializer());
        Gson gsonParser = builder.create();

        BufferedWriter writer = new BufferedWriter( new FileWriter(output_dir + "output.json"));
        writer.write(gsonParser.toJson(blocks));
        writer.close();

        //TODO: Generate Instructions

        //TODO: Generate Custom Parts Zip File

    }
}
