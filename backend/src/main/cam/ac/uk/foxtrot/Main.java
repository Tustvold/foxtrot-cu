package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.Mesh;
import cam.ac.uk.foxtrot.voxelisation.MeshIO;
import com.google.gson.GsonBuilder;
import com.sun.glass.ui.SystemClipboard;
import com.sun.j3d.loaders.Scene;
import com.google.gson.Gson;

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
        if (args.length < 1) {
            System.err.println("Error: No file");
        }
        String filePath = args[0];

        System.out.println("Starting...");
        //TODO: Load Mesh from file
        MeshIO meshIO = new MeshIO();
        System.out.println("Loading mesh...");
        Scene scene;
        try
        {
            scene = meshIO.readFromFile("pumpkin.obj");
        }
        catch (IOException error)
        {
            System.err.println("Loading fialied:" + error.getMessage());
            return;
        }
        Mesh m = new Mesh(scene);
        System.out.println("Mesh loaded...");

        //TODO: Voxelise Mesh

        Block[] b = new Block[100]; // The returned block array

        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gsonParser = builder.create();
        System.out.print(gsonParser.toJson(b));



        //TODO: Generate Instructions

        //TODO: Generate Custom Parts Zip File

    }
}
