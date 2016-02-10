package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.Stripifier;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

public class Projection {

    private GeometryInfo gi;
    private Point3f[] coords;

    public Projection(TriangleArray ta) {
        gi = new GeometryInfo(ta);
        coords = gi.getCoordinates();
    }

    // project onto some yz plane
    public Point3f[] setX(float x) {
        Point3f[] ret = new Point3f[coords.length];
        for (int i = 0; i < coords.length; i++) {
            Point3f point = new Point3f(coords[i]);
            point.x = x;
            ret[i] = point;
        }
        return ret;
    }

    // project onto some xz plane
    public Point3f[] setY(float y) {
        Point3f[] ret = new Point3f[coords.length];
        for (int i = 0; i < coords.length; i++) {
            Point3f point = new Point3f(coords[i]);
            point.y = y;
            ret[i] = point;
        }
        return ret;
    }

    // project onto some xy plane
    public Point3f[] setZ(float z) {
        Point3f[] ret = new Point3f[coords.length];
        for (int i = 0; i < coords.length; i++) {
            Point3f point = new Point3f(coords[i]);
            point.z = z;
            ret[i] = point;
        }
        return ret;
    }

}
