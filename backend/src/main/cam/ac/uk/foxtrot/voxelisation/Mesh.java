package cam.ac.uk.foxtrot.voxelisation;


import javax.media.j3d.BranchGroup;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashSet;

public class Mesh
{
    // Anti-Clockwise winding order
    private Vector3f origin;                 // the offset from (0,0,0) by which the mesh is skewed
    private ArrayList<MeshPoint> vertices;   // list of vertices
    private HashSet<MeshTriangle> triangles; // set of triangles
    private Mesh[][][] blocks;               // empty unless top level block (allows finer division if needed
    private float cubeSize = 0.5f;           // size of standard cube (TODO to be set by user)


    public Mesh(Vector3f origin, float cubeSize, BranchGroup objRoot)
    {
        // TODO
    }

}
