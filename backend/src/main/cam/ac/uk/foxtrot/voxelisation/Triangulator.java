package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.Arrays;

public class Triangulator {
    private ArrayList<Point3d> triangulatedArrayList;

    public Triangulator(ArrayList<Point3d> originalCoordinates) {
        GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        geometryInfo.setCoordinates(originalCoordinates.toArray(new Point3d[originalCoordinates.size()]));
        int[] stripCount = {originalCoordinates.size()};
        geometryInfo.setStripCounts(stripCount);
        int[] contourCount = {1};
        geometryInfo.setContourCounts(contourCount);
        GeometryArray geometryArray = geometryInfo.getGeometryArray();
        int arrayLength = geometryArray.getVertexCount();
        Point3d[] triangulatedArray = new Point3d[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            triangulatedArray[i] = new Point3d();
        }
        geometryArray.getCoordinates(0,triangulatedArray);
        triangulatedArrayList = new ArrayList<>(Arrays.asList(triangulatedArray));
    }

    public ArrayList<Point3d> getTriangulatedArrayList() {
        return triangulatedArrayList;
    }
    
}
