package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.Mesh;
import cam.ac.uk.foxtrot.voxelisation.MeshIO;
import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.sun.j3d.loaders.Scene;

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
        System.out.println("Starting...");
        //TODO: Load Mesh from file
        MeshIO meshIO = new MeshIO();
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

        //TODO: Voxelise Mesh
        MeshVoxeliser voxeliser = new MeshVoxeliser(m);
        Block[][][] blocks = voxeliser.getBlocks();


        //TODO: Generate Instructions

        //TODO: Generate Custom Parts Zip File
    }
}
