package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.internal.DoubleBufferWrapper;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

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
    public ArrayList<Point3d> getTriangles(String filename) throws IOException
    {
    	//All vertexes
    	ArrayList<Point3d> points = new ArrayList<>();
    	//Vertexes in triangle order T[0], T[1], T[2] represents Triangle 1
    	ArrayList<Point3d> triangles = new ArrayList<>();

    	try {
            File theOBJFILE = new File(filename);
            FileReader fr = new FileReader(theOBJFILE);
            BufferedReader bfr = new BufferedReader(fr);

            String l = "";

            while (true) {
                l = bfr.readLine();
                //System.out.println(l);
                if(l==null){
                    break;
                }
                if(l.startsWith("v ")) {
                    String[] sa = l.split("\\s");
                    //for(String S : sa){
                    //    System.out.println("a: "+S);
                    //}
                    double[] co = new double[3];
                    try
                    {
                        int fir = 1;
                        for(int curr = 0; curr < 3; curr++)
                        {
                            for(; fir < sa.length && (sa[fir].compareTo("\\s") == 0 || sa[fir].compareTo("") == 0); fir++);
                            if(fir == sa.length)
                                throw new IOException("Vertex has too many coordinates");
                            co[curr] = Double.parseDouble(sa[fir]);
                            fir++;
                        }
                        /*
                        co[0] = Double.parseDouble(sa[1]);
                        co[1] = Double.parseDouble(sa[2]);
                        co[2] = Double.parseDouble(sa[3]);
                        */
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
                        ArrayList<Point3d> polygon = new ArrayList<>();
                        for(int i = 1; i < sa.length; i++)
                        {
                            // parse the polygon
                            String[] sai = sa[i].split("/");
                            polygon.add(points.get(Integer.parseInt(sai[0]) - 1));
                        }
                        for(int i = 0; i < 3; i++)
                        {
                            // add the initial triangle
                            triangles.add(new Point3d(polygon.get((i))));
                        }
                        for(int i = 3; i < polygon.size(); i++)
                        {
                            // and ear clip the rest
                            triangles.add(new Point3d(polygon.get(0)));
                            triangles.add(new Point3d(polygon.get(i-1)));
                            triangles.add(new Point3d(polygon.get(i)));
                        }
                        /*
                        String[] sa1 = sa[1].split("/");
                        String[] sa2 = sa[2].split("/");
                        String[] sa3 = sa[3].split("/");
                        triangles.add(points.get(Integer.parseInt(sa1[0]) - 1));
                        triangles.add(points.get(Integer.parseInt(sa2[0]) - 1));
                        triangles.add(points.get(Integer.parseInt(sa3[0]) - 1));
                        */
                    }
                    catch(java.lang.NumberFormatException error){
                        throw new IOException("Polygon Format Error");
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
