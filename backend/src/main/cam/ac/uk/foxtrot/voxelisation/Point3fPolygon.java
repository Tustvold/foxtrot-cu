package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import javax.vecmath.Point3f;

public class Point3fPolygon {
    private Point3f[] exterior;
    private Point3f[][] holes;
    private float z;

    public Point3fPolygon(Polygon jtsPolygon, float z) {
        this.z = z;
        exterior = toPointArray(jtsPolygon.getExteriorRing());
        int numHoles = jtsPolygon.getNumInteriorRing();
        holes = new Point3f[numHoles][];
        for (int i = 0; i < numHoles; i++) {
            holes[i] = toPointArray(jtsPolygon.getInteriorRingN(i));
        }
    }

    //convert LineString to array of Point3fs
    private Point3f[] toPointArray(LineString lineString) {
        Coordinate[] coordinates = lineString.getCoordinates();
        int numPoints = coordinates.length-1;
        Point3f[] points = new Point3f[numPoints];
        for(int i = 0; i < numPoints; i++) {
            points[i] = toJ3DPoint3f(coordinates[i]);
        }
        return points;
    }

    // convert JTS Coordinates to J3D Point3f
    private Point3f toJ3DPoint3f(Coordinate coordinate) {
        return new Point3f((float)coordinate.x,(float)coordinate.y,z);
    }

    public Point3f[] getExterior() {
        return exterior;
    }

    public Point3f[][] getHoles() {
        return holes;
    }

}
