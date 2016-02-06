package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

import javax.media.j3d.BranchGroup;
import java.io.IOException;
import java.util.Enumeration;

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
    public BranchGroup readFromFile(String filename) throws IOException
    {
        ObjectFile theOBJFile = new ObjectFile(ObjectFile.TRIANGULATE);
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

        BranchGroup objRoot = theScene.getSceneGroup();
        //Enumeration allChildren = objRoot.getAllChildren();

        return objRoot;
    }
/*
    d draw(theNode)
    {
        if (theNode instanceof Group)
        {
            for (each child of the Group)
            draw(childNode);
        }
        else if (theNode instanceof Leaf)
        {
            drawLeaf(theNode);
        }
    }
    void drawLeaf(theLeaf)
    {
        if (theLeaf instanceof Shape3D)
        {
            Geometry geo = theShape.getGeometry();
            Appearance app = theShape.getAppearance();
            Material mat = app.getMaterial();
            Get data (coords, normals, colors, and/or tex coords) from geo;
            Get color data out of material;
            glBegin (GL_TRIANGLES);
            glColor (materialColor);
            for (each vertex in geo)
            {
                glNormal(vertexNormal);
                glTexCoord(vertexTextureCoords);
                glVertex (x,y,z);
            }
            gl.glEnd();
        }
    }
*/
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
