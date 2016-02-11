package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.Mesh;
import cam.ac.uk.foxtrot.voxelisation.MeshIO;
import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.google.gson.GsonBuilder;
import com.sun.glass.ui.SystemClipboard;
import com.sun.j3d.loaders.Scene;
import com.google.gson.Gson;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
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
        if (args.length < 1)
        {
            System.err.println("Error: No file");
            return;
        }
        String filePath = args[0];

        System.out.println("Starting...");
        //TODO: Load Mesh from file
        MeshIO meshIO = new MeshIO();
        Scene scene;
        try
        {
            scene = meshIO.readFromFile(args[0]);
        } catch (IOException error)
        {
            System.err.println("Loading fialied:" + error.getMessage());
            return;
        }
        Mesh m = new Mesh(scene);

        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();


        Block[] b = new Block[5]; // The returned block array


        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gsonParser = builder.create();
        System.out.print(gsonParser.toJson(b));


        //TODO: Generate Instructions

        //TODO: Generate Custom Parts Zip File

    }
}
