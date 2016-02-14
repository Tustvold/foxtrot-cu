package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Mesh
{
    // Anti-Clockwise winding order
    private Point3d offset;          // position of the mesh in the grid
    private ArrayList<Point3d> triangles; // the list of triangles representing the mesh
    private double blockSize = 1.0;

    // really fine scaling factors (limits of system)
    // 0.008 for teapot.obj
    // 0.04 for human.obj
    // 0.015 for sphere.obj
    // 0.15 for teddy.obj
    // 0.3 for pumpkin.obj
    // 0.7 for bunny.obj
    // the system is capable of producing up to about a 200x200x200 grid of voxels
    // in reasonable timeS

    public Point3d getOffset()
    {
        return offset;
    }

    public void setOffset(Point3d offset)
    {
        this.offset = offset;
    }

    public ArrayList<Point3d> getTriangles()
    {
        return triangles;
    }

    public void setTriangles(ArrayList<Point3d> trigs)
    {
        triangles = new ArrayList<>(trigs);
    }

    // creates, rescales and centers the mesh
    public Mesh(Scene scene)
    {
        BranchGroup branch = scene.getSceneGroup();
        branch.setBoundsAutoCompute(true);

        // TEMPORARY!!!
        // extract the triangle array
        Shape3D shape = (Shape3D) branch.getChild(0);
        GeometryInfo info = new GeometryInfo((GeometryArray) shape.getGeometry());
        TriangleArray ta = (TriangleArray) info.getGeometryArray();

        // load this into the triangle array
        triangles = new ArrayList<>();
        for(int i = 0; i < ta.getVertexCount(); i++)
        {
            Point3d curr = new Point3d();
            ta.getCoordinate(i, curr);
            triangles.add(curr);
        }

        // and finally rescale and ceter the mesh
        rescaleAndCenterMesh();

        System.out.println("Loaded: " + triangles.size() / 3 + " triangles");
        System.out.println("Mesh loaded...");
    }

    // returns the meshes centre of mass
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

    // scales the mesh in such a way so that a block is of unit size and centers it
    private void rescaleAndCenterMesh()
    {
        Point3d cm = getCentreOfMass();

        Point3d curr = new Point3d(0, 0, 0);
        int cnt = triangles.size();
        for (int i = 0; i < cnt; i++)
        {
            triangles.get(i).x = (triangles.get(i).x - cm.x) / blockSize;
            triangles.get(i).y = (triangles.get(i).y - cm.y) / blockSize;
            triangles.get(i).z = (triangles.get(i).z - cm.z) / blockSize;
        }
    }

    // TESTING METHOD!!
    public void drawTriangles(String filename)
    {
        System.out.println("Preparing mesh representation...");
        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
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
