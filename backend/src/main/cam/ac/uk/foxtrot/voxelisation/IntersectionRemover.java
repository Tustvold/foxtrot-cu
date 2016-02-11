import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3f;

public class IntersectionRemover {
    Geometry union;
    Point3f[][] polygonArray;
    Point3f[][] holeArray;

    public IntersectionRemover(Point3f[] originalCoordinates){
        List<Geometry> geometryList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            Coordinate coordinate1 = toJTSCoordinate(originalCoordinates[i]);
            Coordinate coordinate2 = toJTSCoordinate(originalCoordinates[i+1]);
            Coordinate coordinate3 = toJTSCoordinate(originalCoordinates[i+2]);
            Coordinate[] coordinates = {coordinate1,coordinate2,coordinate3,coordinate1}; //coordinates of triangle
            geometryList.add(factory.createPolygon(factory.createLinearRing(coordinates),null)); //add triangle to list of geometries
        }
        union = UnaryUnionOp.union(geometryList); //merge geometries, overlapping triangles merged
        generatePolygonArray();
        generateHoleArray();
    }

    // convert original J3D Point3f to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3f point) {
        return new Coordinate(point.x,point.y,point.z);
    }

    // convert JTS Coordinates to J3D Point3f
    private Point3f toJ3DPoint3f(Coordinate coordinate) {
        return new Point3f((float)coordinate.x,(float)coordinate.y,0);
    }

    // convert Geometry to array of arrays of Point3fs
    private void generatePolygonArray() {
        int length = union.getNumGeometries();
        polygonArray = new Point3f[length][];
        for(int i = 0; i < length; i++) {
            polygonArray[i] = toPointArray(((Polygon)union.getGeometryN(i)).getExteriorRing());
        }
    }

    private void generateHoleArray() {
        int length = union.getNumGeometries();
        LinkedList<Point3f[]> holes = new LinkedList<>();
        for (int i = 0; i < length; i++) {
            Polygon polygon = (Polygon)union.getGeometryN(i);
            int numHoles = polygon.getNumInteriorRing();
            for (int j = 0; j < numHoles; j++) {
                holes.add(toPointArray(polygon.getInteriorRingN(j)));
            }
        }
        holeArray = holes.toArray(new Point3f[0][]);
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

    public Geometry getGeometry() {
        return union;
    }

    public Point3f[][] getPolygonArray() {
        return polygonArray;
    }

    public Point3f[][] getHoleArray() {
        return holeArray;
    }

}
