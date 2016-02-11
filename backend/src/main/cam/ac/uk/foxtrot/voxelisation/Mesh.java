package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.*;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Mesh
{
    // Anti-Clockwise winding order
    private Point3f offset;          // position of the mesh in the grid
    private GeometryInfo info;       // the geometry info object
    private TriangleArray triangles; // the list of triangles representing the mesh
    private float blockSize = 3.5f;

    public Point3f getOffset()
    {
        return offset;
    }

    public void setOffset(Point3f offset)
    {
        this.offset = offset;
    }

    public TriangleArray getTriangles()
    {
        return triangles;
    }

    public GeometryInfo getGeometryInfo()
    {
        return info;
    }

    // creates, rescales and centers the mesh
    public Mesh(Scene scene)
    {
        BranchGroup branch = scene.getSceneGroup();
        branch.setBoundsAutoCompute(true);

        // extract the triangle array
        Shape3D shape = (Shape3D) branch.getChild(0);
        info = new GeometryInfo((GeometryArray) shape.getGeometry());
        triangles = (TriangleArray) info.getGeometryArray();
        rescaleAndCenterMesh();

        System.out.println("Loaded: " + triangles.getVertexCount() / 3 + " triangles"); // Prints around 30.000, sounds about right
        System.out.println("Vertex format is: " + triangles.getVertexFormat()); // prints 387
        System.out.println("Mesh loaded...");
/*
        Point3f x = new Point3f(0,0,0);
        triangles.getCoordinate(0, x);
        System.out.println(x.x + " " + x.y +" " + x.z);
        triangles.getCoordinate(1, x);
        System.out.println(x.x + " " + x.y +" " + x.z);
        triangles.getCoordinate(2, x);
        System.out.println(x.x + " " + x.y +" " + x.z);
        System.out.println();
*/
    }

    // returns the meshes centre of mass
    private Point3f getCentreOfMass()
    {
        Point3f mc = new Point3f(0, 0, 0);
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = triangles.getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            triangles.getCoordinate(i, curr);
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
        Point3f cm = getCentreOfMass();

        Point3f curr = new Point3f(0, 0, 0);
        int cnt = triangles.getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            triangles.getCoordinate(i, curr);
            curr.x = (curr.x - cm.x) / blockSize;
            curr.y = (curr.y - cm.y) / blockSize;
            curr.z = (curr.z - cm.z) / blockSize;
            triangles.setCoordinate(i, curr);
        }
        drawTriangles("../../testing/initial.obj");
    }

    public void drawTriangles(String filename)
    {
        System.out.println("Preparing initial output...");
        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }

        int totalTriangles = 0;

        for (int i = 0; i < triangles.getVertexCount(); i++)
        {
            Point3f currPt = new Point3f(0,0,0);
            triangles.getCoordinate(i, currPt);
            try
            {

                writer.write("v " + currPt.x + " " + currPt.y + " " + currPt.z + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write vertex: " + err.getMessage());
            }
        }

        for (int i = 1; i < triangles.getVertexCount(); i +=3)
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
        System.out.println("Initial output created...");
    }

}
