package cam.ac.uk.foxtrot.voxelisation;


import javax.vecmath.Point3d;
import java.io.*;
import java.util.ArrayList;

public class Mesh
{
    // Anti-Clockwise winding order
    private Point3d offset;               // position of the mesh relative to the grid origin
    private ArrayList<Point3d> triangles; // the list of triangles representing the mesh
    private double blockSize;             // the size of a single block in mesh units
    private Point3d initialCM;            // the initial center of mass

    /**
     * Gwtter for the offset of the mesh from the origin.
     */
    public Point3d getOffset()
    {
        return offset;
    }

    /**
     * Setter for the offset of the mesh from the origin.
     */
    public void setOffset(Point3d offset)
    {
        this.offset = offset;
    }

    /**
     * Getter for the meshes triangle list.
     */
    public ArrayList<Point3d> getTriangles()
    {
        return triangles;
    }

    /**
     * Overrides the meshes triangle representation.
     */
    public void setTriangles(ArrayList<Point3d> trigs)
    {
        triangles = new ArrayList<>(trigs);
    }

    /**
     * Initialises, rescales and centers the Mesh.
     */
    public Mesh(ArrayList<Point3d> tri, double scale)
    {
        triangles = new ArrayList<>(tri);
        blockSize = scale;
        rescaleAndCenterMesh();

        System.out.println("Loaded: " + triangles.size() / 3 + " triangles");
    }

    /**
     * Returns the centre of mass of the entire mesh.
     */
    private Point3d getCentreOfMass()
    {
        Point3d mc = new Point3d(0, 0, 0);
        Point3d curr;
        int cnt = triangles.size();
        for (int i = 0; i < cnt; i++)
        {
            curr = triangles.get(i);
            mc.x += curr.x;
            mc.y += curr.y;
            mc.z += curr.z;
        }
        mc.x /= cnt;
        mc.y /= cnt;
        mc.z /= cnt;
        return mc;
    }

    /**
     * Scales the mesh so that a block is of unit size and then centers the mesh.
     */
    private void rescaleAndCenterMesh()
    {
        Point3d cm = getCentreOfMass();
        initialCM = cm;
        Point3d curr = new Point3d(0, 0, 0);
        int cnt = triangles.size();
        for (int i = 0; i < cnt; i++)
        {
            triangles.get(i).x = (triangles.get(i).x - cm.x) / blockSize;
            triangles.get(i).y = (triangles.get(i).y - cm.y) / blockSize;
            triangles.get(i).z = (triangles.get(i).z - cm.z) / blockSize;
        }
    }

    /**
     * Retruns the initial Center of Mass of the mesh;
     */
    public Point3d getInitialCM()
    {
        return initialCM;
    }

    /**
     * DEBUGGING and TESTING method!
     * Outputs a .obj file representing the input mesh.
     *
     * @param filename name of file to which to write output
     */
    public void drawTriangles(String filename)
    {
        System.out.println("Preparing mesh representation...");
        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            //System.err.println(ex.getMessage());
            return;
        }

        int totalTriangles = 0;

        for (int i = 0; i < triangles.size(); i++)
        {
            Point3d currPt;
            currPt = triangles.get(i);
            try
            {

                writer.write("v " + currPt.x + " " + currPt.y + " " + currPt.z + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write vertex: " + err.getMessage());
            }
        }

        for (int i = 1; i < triangles.size(); i += 3)
        {
            try
            {
                writer.write("f " + i + " " + (i + 1) + " " + (i + 2) + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write triangle: " + err.getMessage());
            }
        }
        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Mesh representation created...");
    }

}
