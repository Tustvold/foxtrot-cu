package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.*;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Mesh
{
    // Anti-Clockwise winding order
    private Point3f offset;          // position of the mesh in the grid
    private GeometryInfo info;       // the geometry info object
    private TriangleArray triangles; // the list of triangles representing the mesh

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

    public Mesh(Scene scene)
    {
        BranchGroup branch = scene.getSceneGroup();
        branch.setBoundsAutoCompute(true);

        // extract the triangle array
        Shape3D shape = (Shape3D) branch.getChild(0);
        info = new GeometryInfo((GeometryArray) shape.getGeometry());
        triangles = (TriangleArray) info.getGeometryArray();
/*
        System.out.println(triangles.getVertexCount()); // Prints around 30.000, sounds about right
        System.out.println(triangles.getVertexFormat()); // prints 387

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

}
