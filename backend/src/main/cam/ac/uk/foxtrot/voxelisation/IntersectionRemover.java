package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;

import java.util.ArrayList;

import javax.vecmath.Point3d;

public class IntersectionRemover {
    Geometry union;
    Point3d[][] polygonArray;
    Point3d[][] holeArray;
    Point3dPolygon[] combinedArray;
    private static final double approximate_tolerance = 0.00000001;
    double z;
    CustomPartMouldGenerator.ProjectionFace projectionFace;

    // checks if the three points can be approximated as on the same line
    public boolean approximatesToLine(Point3d A, Point3d B, Point3d C)
    {
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < approximate_tolerance;
    }

    public IntersectionRemover(Point3d[] originalCoordinates, CustomPartMouldGenerator.ProjectionFace face){ // in x-y plane
        projectionFace = face;
        convertBetweenPlanes(originalCoordinates);
        ArrayList<Polygon> triangleList= new ArrayList<>();
        ArrayList<Polygon> polygonList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        if(originalCoordinates.length>1) {
            z = originalCoordinates[0].z;
        }
        for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            if(!approximatesToLine(originalCoordinates[i],originalCoordinates[i+1],originalCoordinates[i+2])) {
                Coordinate coordinate1 = toJTSCoordinate(originalCoordinates[i]);
                Coordinate coordinate2 = toJTSCoordinate(originalCoordinates[i + 1]);
                Coordinate coordinate3 = toJTSCoordinate(originalCoordinates[i + 2]);
                Coordinate[] coordinates = {coordinate1, coordinate2, coordinate3, coordinate1}; //coordinates of triangle
                triangleList.add(factory.createPolygon(factory.createLinearRing(coordinates), null)); //add triangle to list of geometries
            }
        } for (Polygon triangle: triangleList){
            if (polygonList.size() == 0) {
                polygonList.add(triangle);
            } else {
                mergeIntersecting(polygonList,triangle);
            }
        }
        union = mergeNonIntersecting(polygonList);

        if (union != null) {
            generateArrays();
        } else {
            polygonArray = new Point3d[0][];
            holeArray = new Point3d[0][];
            combinedArray = new Point3dPolygon[0];
        }
    }

    private boolean mergeIntersecting(ArrayList<Polygon> list, Polygon polygon) {
        boolean success;
        int length = list.size();
        if (length == 0) {
            list.add(polygon);
            return true;
        } else {
            Polygon element = list.get(0);
            list.remove(0);

            try {
                Geometry temp = polygon.union(element); // merge element & polygon
                if (temp instanceof Polygon) { // forms one polygon
                    element = (Polygon)temp;
                    success = mergeIntersecting(list,element); // does recursively merging succeed
                } else { // forms multiple polygon
                    success = mergeIntersecting(list,polygon);
                    if(success) {
                        list.add(element);
                    }
                }
            } catch (TopologyException e) { // ignore this polygon
                return false;
            }

        }
        return success;
    }

    private Geometry mergeNonIntersecting(ArrayList<Polygon> list) {
        Geometry result = null;
        for (Polygon polygon : list) {
            if (result == null) {
                result = polygon;
            } else {
                result = result.union(polygon);
            }
        } return result;
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

}
