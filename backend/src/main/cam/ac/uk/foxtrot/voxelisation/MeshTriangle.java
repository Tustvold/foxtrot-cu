package cam.ac.uk.foxtrot.voxelisation;

/**
 * Created by Milos on 06/02/2016.
 */
public class MeshTriangle
{
    private int[] pts; // indices into the point array given in Counter Clock Wise winding order
    private Mesh mesh; // pointer to the mesh the object belongs to

    // removes the triangle from all the relevant lists
    public void remove()
    {
        // TODO
    }

    public int getNextPointIdx(int ptidx)
    {
        int pos = 0;
        for(; pos < 3 && pts[pos] != ptidx; pos++);

        if(pos < 3)
            return pts[(pos+1)%3];
        else
            return -1; // exception case (TODO)
    }

    public int getNextPointIdx(MeshPoint point)
    {
        return getNextPointIdx(point.getIdx());
    }

    public MeshTriangle(int pt0, int pt1, int pt2)
    {
        pts = new int[3];
        pts[0] = pt0;
        pts[1] = pt1;
        pts[2] = pt2;
    }

}
