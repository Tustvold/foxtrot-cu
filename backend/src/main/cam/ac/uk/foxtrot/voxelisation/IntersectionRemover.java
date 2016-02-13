package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;

public class IntersectionRemover {
    Geometry union;
    Point3f[][] polygonArray;
    Point3f[][] holeArray;
    Point3fPolygon[] combinedArray;
    float z;

    public IntersectionRemover(Point3f[] originalCoordinates){ // in x-y plane
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

    // convert original J3D Point3f to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3f point) {
        return new Coordinate(point.x,point.y,point.z);
    }

    // convert Geometry to array of arrays of Point3fs for original plane
    private void generateArrays() {
        int length = union.getNumGeometries();
        polygonArray = new Point3f[length][];
        combinedArray = new Point3fPolygon[length];
        ArrayList<Point3f[]> holeList = new ArrayList<>();
        for(int i = 0; i < length; i++) {
            Point3fPolygon polygon = new Point3fPolygon(((Polygon)union.getGeometryN(i)),z);
            combinedArray[i] = polygon;
            polygonArray[i] = polygon.getExterior();
            Point3f[][] holes = polygon.getHoles();
            int numHoles = holes.length;
            for(int j = 0; j < numHoles; j++) {
                holeList.add(polygon.getHoles()[j]);
            }
        }
        holeArray = holeList.toArray(new Point3f[0][]);
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

    public Point3fPolygon[] getCombinedArray() {
        return combinedArray;
    }

}
