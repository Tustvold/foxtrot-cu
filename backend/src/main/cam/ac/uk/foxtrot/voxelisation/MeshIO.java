package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.internal.DoubleBufferWrapper;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import java.io.*;
import java.util.ArrayList;

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
    public ArrayList<Point3d> readFromFile(String filename) throws IOException
    {
        // TODO @rob implement and test
        ArrayList<Point3d> list = new ArrayList<>();
        // OLD LOADER STARTS HERE

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
        BranchGroup branch = theScene.getSceneGroup();
        branch.setBoundsAutoCompute(true);

        // extract the triangle array
        Shape3D shape = (Shape3D) branch.getChild(0);
        GeometryInfo info = new GeometryInfo((GeometryArray) shape.getGeometry());
        TriangleArray triangles = (TriangleArray) info.getGeometryArray();

        for(int i = 0; i < triangles.getVertexCount(); i++)
        {
            Point3d tmp = new Point3d();
            triangles.getCoordinate(i,tmp);
            list.add(tmp);
        }

        // OLD LOADER ENDS HERE
        return list;
    }
    
    public ArrayList<Point3d> getTriangles(String filename) throws IOException
    {
    	//All vertexes
    	ArrayList<Point3d> points = new ArrayList<Point3d>();
    	//Vertexes in triangle order T[0], T[1], T[2] represents Triangle 1
    	ArrayList<Point3d> triangles = new ArrayList<Point3d>();
    
    	try {
            File theOBJFILE = new File(filename);
            FileReader fr = new FileReader(theOBJFILE);
            BufferedReader bfr = new BufferedReader(fr);

            String l = "";

            while (true) {
                l = bfr.readLine();
                System.out.println(l);
                if(l==null){
                    break;
                }
                if(l.startsWith("v ")) {
                    String[] sa = l.split("\\s");
                    //for(String S : sa){
                    //    System.out.println("a: "+S);
                    //}
                    double[] co = new double[3];
                    try {
                        co[0] = Double.parseDouble(sa[1]);
                        co[1] = Double.parseDouble(sa[2]);
                        co[2] = Double.parseDouble(sa[3]);
                    }
                    catch(java.lang.NumberFormatException error){
                        throw new IOException("Vertex Format Error");
                    }
                    Point3d v = new Point3d(co);
                    points.add(v);
                }
                if (l.startsWith("f")) {
                    String[] sa = l.split("\\s");
                    //for(String S : sa){
                    //    System.out.println("a: "+S);
                    //}
                    try {
                        String[] sa1 = sa[1].split("/");
                        String[] sa2 = sa[2].split("/");
                        String[] sa3 = sa[3].split("/");
                        triangles.add(points.get(Integer.parseInt(sa1[0]) - 1));
                        triangles.add(points.get(Integer.parseInt(sa2[0]) - 1));
                        triangles.add(points.get(Integer.parseInt(sa3[0]) - 1));
                    }
                    catch(java.lang.NumberFormatException error){
                        throw new IOException("Triangle Format Error");
                    }
                }
            }
        }
        catch (java.io.FileNotFoundException error) {
            throw new IOException("File was not found!");
        }
        return triangles;
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
