/**
 * Created by Issie on 09/02/2016.
 */

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

public class IntersectionRemover {
    MultiPolygon union;

    public IntersectionRemover(TriangleArray triangleArray){
        GeometryInfo geometryInfo = new GeometryInfo(triangleArray);
        Point3f[] originalCoordinates = geometryInfo.getCoordinates();
        List<Geometry> geometryList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            Coordinate coordinate1 = toJTSCoordinate(originalCoordinates[i]);
            Coordinate coordinate2 = toJTSCoordinate(originalCoordinates[i+1]);
            Coordinate coordinate3 = toJTSCoordinate(originalCoordinates[i+2]);
            Coordinate[] coordinates = {coordinate1,coordinate2,coordinate3,coordinate1}; //coordinates of triangle
            geometryList.add(factory.createPolygon(factory.createLinearRing(coordinates),null)); //add triangle to list of geometries
        }
        union = (MultiPolygon)UnaryUnionOp.union(geometryList); //merge geometries, overlapping triangles merged
    }

    // convert original J3D Point3f to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3f point) {
        float[] xyz = new float[3];
        point.get(xyz);
        return new Coordinate(xyz[0],xyz[1],xyz[2]);
    }

}
