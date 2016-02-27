package cam.ac.uk.foxtrot.sidefiller;

import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import com.sun.prism.Mesh;
import javafx.util.Pair;

import javax.vecmath.Point3d;
import java.util.Comparator;

public class PointComparator implements Comparator<Point>
{
    public int compare(Point c1, Point c2)
    {
        if(MeshVoxeliser.areIdentical(c1.getX(), c2.getX()))
        {
            // x's are the same
            if(MeshVoxeliser.areIdentical(c1.getY(), c2.getY()))
            {
                // y's are the same
                return 0;
            }
            else if(c1.getY() < c2.getY())
            {
                return -1;
            }
        }
        else if(c1.getX() < c2.getX())
        {
            return -1;
        }
        return 1;
    }
}
