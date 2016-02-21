package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3d;

public class IntersectionRemover {
    Geometry union;
    Point3d[][] polygonArray;
    Point3d[][] holeArray;
    Point3dPolygon[] combinedArray;
    private static final double approximate_tolerance = 0.00001;
    double z;
    ProjectionUtils.ProjectionFace projectionFace;

    // checks if the three points can be approximated as on the same line
    public boolean approximatesToLine(Point3d A, Point3d B, Point3d C)
    {
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < approximate_tolerance;
    }

    public IntersectionRemover(Point3d[] originalCoordinates, ProjectionUtils.ProjectionFace face){ // in x-y plane
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

    private void zy(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.z, point.y, point.x);
        }
    }

    private void zx(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.x,point.z,point.y);
        }
    }

    private void convertBetweenPlanes(Point3d[] coordinates) {
        switch (projectionFace) {
            case ZX0: zx(coordinates); break;
            case ZX1: zx(coordinates); break;
            case ZY0: zy(coordinates); break;
            case ZY1: zy(coordinates); break;
            default: break;
        }
    }

    // convert original J3D Point3d to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3d point) {
        return new Coordinate(point.x, point.y, point.z);
    }

    // convert Geometry to array of arrays of Point3ds for original plane
    private void generateArrays() {
        int length = union.getNumGeometries();
        polygonArray = new Point3d[length][];
        combinedArray = new Point3dPolygon[length];
        ArrayList<Point3d[]> holeList = new ArrayList<>();
        for(int i = 0; i < length; i++) {
            Point3dPolygon polygon = new Point3dPolygon(((Polygon)union.getGeometryN(i)),z);
            convertBetweenPlanes(polygon.getExterior());
            Point3d[][] holes = polygon.getHoles();
            polygonArray[i] = polygon.getExterior();
            int numHoles = holes.length;
            for(int j = 0; j < numHoles; j++) {
                convertBetweenPlanes(holes[j]);
                holeList.add(holes[j]);
            }
            combinedArray[i] = polygon;
        }
        holeArray = holeList.toArray(new Point3d[0][]);
    }

    public Point3d[][] getPolygonArray() {
        return polygonArray;
    }

    public Point3d[][] getHoleArray() {
        return holeArray;
    }

    public Point3dPolygon[] getCombinedArray() {
        return combinedArray;
    }

    public static void main(String[] args) {
        Point3d[] Point3ds = {new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,0,2)};
        IntersectionRemover merged = new IntersectionRemover(Point3ds, ProjectionUtils.ProjectionFace.ZX0);
        System.out.println(Arrays.toString(merged.getCombinedArray()[0].getExterior()));
        System.out.println(Arrays.deepToString(merged.getCombinedArray()[0].getHoles()));
        System.out.println(Arrays.deepToString(merged.getPolygonArray()));
        System.out.println(Arrays.toString(merged.getHoleArray()));
    }

}
