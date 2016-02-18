package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import javax.vecmath.Point3d;

public class Point3dPolygon {
    private Point3d[] exterior;
    private Point3d[][] holes;
    private double z;

    public Point3dPolygon(Polygon jtsPolygon, double z) {
        this.z = z;
        exterior = toPointArray(jtsPolygon.getExteriorRing());
        int numHoles = jtsPolygon.getNumInteriorRing();
        holes = new Point3d[numHoles][];
        for (int i = 0; i < numHoles; i++) {
            holes[i] = toPointArray(jtsPolygon.getInteriorRingN(i));
        }
    }

    //convert LineString to array of Point3ds
    private Point3d[] toPointArray(LineString lineString) {
        Coordinate[] coordinates = lineString.getCoordinates();
        int numPoints = coordinates.length-1;
        Point3d[] points = new Point3d[numPoints];
        for(int i = 0; i < numPoints; i++) {
            points[i] = toJ3DPoint3d(coordinates[i]);
        }
        return points;
    }

    // convert JTS Coordinates to J3D Point3d
    private Point3d toJ3DPoint3d(Coordinate coordinate) {
        return new Point3d((float)coordinate.x,(float)coordinate.y,z);
    }

    public Point3d[] getExterior() {
        return exterior;
    }

    public Point3d[][] getHoles() {
        return holes;
    }

}
