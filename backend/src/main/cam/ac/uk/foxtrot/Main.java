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
        ArrayList<Point3d> tri;
        try
        {
            tri = meshIO.getTriangles(filePath);
        } catch (IOException error)
        {
            System.err.println("Loading failed:" + error.getMessage());
            return;
        }
        if(tri == null || tri.size() == 0)
        {
            System.err.println("Loading failed: Input file is empty!");
            return;
        }
        if(tri.size() % 3 != 0)
        {
            System.err.println("Loading failed: Input file is malformed!");
            return;
        }
        Mesh m = new Mesh(tri);
        // voxelise it
        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();

        // TODO connect custom part processing

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

        //TODO: Generate Custom Parts Zip File

    }
}
