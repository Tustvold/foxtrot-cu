package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

public class MeshIO
{

    /**
     * Read the mesh from the file at path filename
     * <p/>
     * Will throw an exception if the file doesn't exist
     *
     * @param filename the path to the file without its file-type suffix
     * @return the loaded mesh
     * @throws IOException
     */
    public Scene readFromFile(String filename) throws IOException
    {
        ObjectFile theOBJFile = new ObjectFile();
        theOBJFile.setFlags(ObjectFile.TRIANGULATE);
        Scene theScene;
        try
        {
            theScene = theOBJFile.load(filename);
        }
        catch(java.io.FileNotFoundException error)
        {
            throw new IOException("File was not found!");
        }
        catch (IncorrectFormatException error)
        {
            throw new IOException("Incorrect file format!");
        }
        catch (ParsingErrorException error)
        {
            throw new IOException("Parsing failed!");
        }

        System.out.println("Loading mesh...");
        return theScene;
    }

    /**
     * Write the provided mesh to a file at the provided filename
     * <p/>
     * Will throw an exception if a file already exists at that location
     *
     * @param filename the path to the file without its file-type suffix
     * @param mesh     the mesh to write
     * @throws IOException
     */
    public void writeToFile(String filename, BranchGroup mesh) throws IOException
    {
    }
}
