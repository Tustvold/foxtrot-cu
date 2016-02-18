package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3f;

public class IntersectionRemover {
    Geometry union;
    Point3f[][] polygonArray;
    Point3f[][] holeArray;
    Point3fPolygon[] combinedArray;
    float z;
    CustomPartMouldGenerator.ProjectionFace projectionFace;

    public IntersectionRemover(Point3f[] originalCoordinates, CustomPartMouldGenerator.ProjectionFace face){ // in x-y plane
        projectionFace = face;
        convertBetweenPlanes(originalCoordinates);
        List<Geometry> geometryList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        if(originalCoordinates.length>1) {
            z = originalCoordinates[0].z;
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

    private void zy(Point3f[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3f point = coordinates[i];
            coordinates[i] = new Point3f(point.z, point.y, point.x);
        }
    }

    private void zx(Point3f[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3f point = coordinates[i];
            coordinates[i] = new Point3f(point.x,point.z,point.y);
        }
    }

    private void convertBetweenPlanes(Point3f[] coordinates) {
        switch (projectionFace) {
            case ZX0: zx(coordinates); break;
            case ZX1: zx(coordinates); break;
            case ZY0: zy(coordinates); break;
            case ZY1: zy(coordinates); break;
            default: break;
        }
    }

    // convert original J3D Point3f to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3f point) {
        return new Coordinate(point.x, point.y, point.z);
    }

    // convert Geometry to array of arrays of Point3fs for original plane
    private void generateArrays() {
        int length = union.getNumGeometries();
        polygonArray = new Point3f[length][];
        combinedArray = new Point3fPolygon[length];
        ArrayList<Point3f[]> holeList = new ArrayList<>();
        for(int i = 0; i < length; i++) {
            Point3fPolygon polygon = new Point3fPolygon(((Polygon)union.getGeometryN(i)),z);
            convertBetweenPlanes(polygon.getExterior());
            Point3f[][] holes = polygon.getHoles();
            polygonArray[i] = polygon.getExterior();
            int numHoles = holes.length;
            for(int j = 0; j < numHoles; j++) {
                convertBetweenPlanes(holes[j]);
                holeList.add(holes[j]);
            }
            combinedArray[i] = polygon;
        }
        holeArray = holeList.toArray(new Point3f[0][]);
    }

    public Point3f[][] getPolygonArray() {
        return polygonArray;
    }

    public Point3f[][] getHoleArray() {
        return holeArray;
    }

    public Point3fPolygon[] getCombinedArray() {
        return combinedArray;
    }

    public static void main(String[] args) {
        Point3f[] point3fs = {new Point3f(0,0,0), new Point3f(2,0,0), new Point3f(1,0,2)};
        IntersectionRemover merged = new IntersectionRemover(point3fs, CustomPartMouldGenerator.ProjectionFace.ZX0);
        System.out.println(Arrays.toString(merged.getCombinedArray()[0].getExterior()));
        System.out.println(Arrays.deepToString(merged.getCombinedArray()[0].getHoles()));
        System.out.println(Arrays.deepToString(merged.getPolygonArray()));
        System.out.println(Arrays.toString(merged.getHoleArray()));
    }

}
