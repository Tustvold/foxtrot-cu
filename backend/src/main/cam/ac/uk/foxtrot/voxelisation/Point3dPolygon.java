package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import javax.vecmath.Point3d;

public class Point3dPolygon {
    private Point3d[] exterior;
    private Point3d[][] holes;
    private double z;

    /**
     * Creates a Java3D compatible representation of a polygon from the JTS representation
     * @param jtsPolygon JTS representation
     * @param z The z value for this JTS representation (JTS automatically gives z value of NaN)
     */
    public Point3dPolygon(Polygon jtsPolygon, double z) {
        this.z = z;
        exterior = toPointArray(jtsPolygon.getExteriorRing());
        int numHoles = jtsPolygon.getNumInteriorRing();
        holes = new Point3d[numHoles][];
        for (int i = 0; i < numHoles; i++) {
            holes[i] = toPointArray(jtsPolygon.getInteriorRingN(i));
        }
    }

    /**
     * Creates a Java3d compatible representation of a polygon
     * @param ext Point3d[] representation of exterior of polygon
     * @param hls Point3d[][] representation of holes
     */
    public Point3dPolygon(Point3d[] ext, Point3d[][] hls) {
        exterior = ext;
        holes = hls;
    }

    /**
     * Convert JTS representation of LineString to an array of Point3ds
     */
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

    /**
     * Convert from a Coordinate used in the JTS library to a Point3d from the Java3D library
     * @param coordinate Coordinate to be converted
     * @return Point3d equivalent to coordinate
     */
    private Point3d toJ3DPoint3d(Coordinate coordinate) {
        return new Point3d((float)coordinate.x,(float)coordinate.y,z);
    }

    /**
     * Getter for representation of exterior of polygon
     * @return array representing the exterior of the polygon
     */
    public Point3d[] getExterior() {
        return exterior;
    }

    /**
     * Getter for representation of holes in polygon
     * @return array of arrays each representing a hole in the polygon
     */
    public Point3d[][] getHoles() {
        return holes;
    }

}
