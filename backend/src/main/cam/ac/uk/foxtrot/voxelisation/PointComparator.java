package cam.ac.uk.foxtrot.voxelisation;

import javafx.util.Pair;

import javax.vecmath.Point3f;
import java.util.Comparator;

/**
 * Created by Milos on 11/02/2016.
 */
public class PointComparator implements Comparator<Pair<Point3f, Integer>>
{
    public int compare(Pair<Point3f, Integer> c1, Pair<Point3f, Integer> c2)
    {
        if(Math.abs(c1.getKey().x - c2.getKey().x) < MeshVoxeliser.float_tolerance)
        {
            // x's are the same
            if(Math.abs(c1.getKey().y - c2.getKey().y) < MeshVoxeliser.float_tolerance)
            {
                // y's are the same
                if(Math.abs(c1.getKey().z - c2.getKey().z) < MeshVoxeliser.float_tolerance)
                {
                    // z's are the same
                    return 0;
                }
                else if(c1.getKey().z < c2.getKey().z)
                {
                    return -1;
                }
            }
            else if(c1.getKey().y < c2.getKey().y)
            {
                return -1;
            }
        }
        else if(c1.getKey().x < c2.getKey().x)
        {
            return -1;
        }
        return 1;
    }
}
