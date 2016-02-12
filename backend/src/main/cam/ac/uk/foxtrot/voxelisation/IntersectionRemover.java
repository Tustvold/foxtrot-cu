package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3f;

public class IntersectionRemover {
    Geometry union;
    Point3f[][] polygonArray;
    Point3f[][] holeArray;
    float z;
    CustomPartMouldGenerator.ProjectionFace face;

    public IntersectionRemover(Point3f[] originalCoordinates, CustomPartMouldGenerator.ProjectionFace projectionFace){
        face = projectionFace;
        originalCoordinates = convertBetweenPlanes(originalCoordinates);
        List<Geometry> geometryList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        if(originalCoordinates.length>1) {
            z = originalCoordinates[0].getZ();
        }
        for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            Coordinate coordinate1 = toJTSCoordinate(originalCoordinates[i]);
            Coordinate coordinate2 = toJTSCoordinate(originalCoordinates[i+1]);
            Coordinate coordinate3 = toJTSCoordinate(originalCoordinates[i+2]);
            Coordinate[] coordinates = {coordinate1,coordinate2,coordinate3,coordinate1}; //coordinates of triangle
            geometryList.add(factory.createPolygon(factory.createLinearRing(coordinates),null)); //add triangle to list of geometries
        }
        union = UnaryUnionOp.union(geometryList); //merge geometries, overlapping triangles merged
        generateArrays();
    }

    // convert between z-y plane and x-y plane
    private Point3f[] zY(Point3f[] originalCoordinates) {
        int length = originalCoordinates.length;
        Point3f[] newCoordinates = new Point3f[length];
        for(int i = 0; i < length; i++) {
            Point3f point = originalCoordinates[i];
            newCoordinates[i] = new Point3f(point.getZ(),point.getY(),point.getX());
        }
        return newCoordinates;
    }

    // convert between z-x plane and x-y plane
    private Point3f[] zX(Point3f[] originalCoordinates) {
        int length = originalCoordinates.length;
        Point3f[] newCoordinates = new Point3f[length];
        for(int i = 0; i < length; i++) {
            Point3f point = originalCoordinates[i];
            newCoordinates[i] = new Point3f(point.getX(),point.getZ(),point.getY());
        }
        return newCoordinates;
    }

    private Point3f[] convertBetweenPlanes(Point3f[] originalCoordinates) {
        switch(face) {
            case ZX0: case ZX1: return zX(originalCoordinates);
            case ZY0: case ZY1: return zY(originalCoordinates);
            default: return originalCoordinates;
        }
    }

    // convert original J3D Point3f to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3f point) {
        return new Coordinate(point.getX(),point.getY(),point.getZ());
    }

    // convert JTS Coordinates to J3D Point3f
    private Point3f toJ3DPoint3f(Coordinate coordinate) {
        return new Point3f((float)coordinate.x,(float)coordinate.y,z);
    }

    // convert Geometry to array of arrays of Point3fs for original plane
    private void generateArrays() {
        int length = union.getNumGeometries();
        polygonArray = new Point3f[length][];
        LinkedList<Point3f[]> holes = new LinkedList<>();
        for(int i = 0; i < length; i++) {
            Polygon polygon = (Polygon)union.getGeometryN(i);
            polygonArray[i] = convertBetweenPlanes(toPointArray(polygon.getExteriorRing()));
            int numHoles = polygon.getNumInteriorRing();
            for(int j = 0; j < numHoles; j++) {
                holes.add(convertBetweenPlanes(toPointArray(polygon.getInteriorRingN(j))));
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
    } // in x-y plane

    public Point3f[][] getPolygonArray() {
        return polygonArray;
    }

    public Point3f[][] getHoleArray() {
        return holeArray;
    }

}
