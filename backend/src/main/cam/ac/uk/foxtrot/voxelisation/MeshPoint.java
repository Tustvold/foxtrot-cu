package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Milos on 06/02/2016.
 */
public class MeshPoint
{
    private HashSet<MeshTriangle> triangles; // list of triangles which contain this
    private Vector3f coord; // coordinates of the point
    private int index; // index into the point array

    public Vector3f getTriplet()
    {
        return coord;
    }

    public int getIdx()
    {
        return index;
    }

    public float getX()
    {
        return coord.x;
    }

    public float getY()
    {
        return coord.y;
    }

    public float getZ()
    {
        return coord.z;
    }

    public void addTriangle(MeshTriangle triangle)
    {
        triangles.add(triangle);
    }

    public void removeTriangle(MeshTriangle triangle)
    {
        triangles.remove(triangle);
    }

    // returns the next triangle from the set of triangles which
    // contain the given point or returns null if none are left
    public MeshTriangle getNextTriangle()
    {
        Iterator<MeshTriangle> it = triangles.iterator();
        if (it.hasNext())
            return it.next();
        else
            return null;
    }

    public MeshPoint(int idx, Vector3f coordinates, MeshTriangle triangle)
    {
        index = idx;
        coord = coordinates;
        triangles = new HashSet<>();
        triangles.add(triangle);
    }

    public MeshPoint(int idx, float x, float y, float z, MeshTriangle triangle)
    {
        index = idx;
        coord.x = x;
        coord.y = y;
        coord.z = z;
        triangles = new HashSet<>();
        triangles.add(triangle);
    }
}
