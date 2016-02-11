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
        polygonArray = toPoint2dArray(union);
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
    private Point3f[][] toPoint2dArray(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        LinkedList<Point3f[]> polygonList = new LinkedList<>();
        LinkedList<Point3f> currentPolygon = new LinkedList<>();
        Point3f currentStart = null;
        int length = coordinates.length;
        if(length>0) {
            currentStart = toJ3DPoint3f(coordinates[0]);
            currentPolygon.add(currentStart);
        }
        for(int i = 1; i<length; i++) {
            Point3f point = toJ3DPoint3f(coordinates[i]);
            if (point.equals(currentStart)) {
                polygonList.add(currentPolygon.toArray(new Point3f[0]));
                currentPolygon = new LinkedList<>();
                if (i < length - 1) {
                    currentStart = toJ3DPoint3f(coordinates[++i]);
                    currentPolygon.add(currentStart);
                }
            }
            else {
                currentPolygon.add(point);
            }
        }
        return polygonList.toArray(new Point3f[0][]);
    }

    public Geometry getGeometry() {
        return union;
    }

    public Point3f[][] getPolygonArray() {
        return polygonArray;
    }

    public static void main(String[] args) {
        Point3f[] points = {new Point3f(0,0,0), new Point3f(2,0,0), new Point3f(1,2,0),
                            new Point3f(1,0,0), new Point3f(3,0,0), new Point3f(2,2,0),
                            new Point3f(0.5f,1.5f,0), new Point3f(2.5f,1.5f,0), new Point3f(1.5f,3.5f,0)};
        IntersectionRemover merged = new IntersectionRemover(points);
        System.out.println(Arrays.toString(merged.getGeometry().getCoordinates()));
        System.out.println(Arrays.deepToString(merged.getPolygonArray()));
    }

}
