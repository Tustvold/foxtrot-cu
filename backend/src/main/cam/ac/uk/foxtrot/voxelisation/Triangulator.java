package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3f;
import java.util.ArrayList;

/**
 * Created by Brett on 2/10/16.
 */
public class Triangulator {

    // given polygons which may or may not be triangles (but don't have holes), triangulate all of them and make an obj file
    public static void triangulateAndPrintObjFile(Point3f[][] polygons) {
        // triangulate
        ArrayList<Point3f> coordinates = new ArrayList<Point3f>();
        int[] stripCounts = new int[polygons.length];
        int[] contourCounts = new int[polygons.length];

        for (int i = 0; i < polygons.length; i++) {
            for (int j = 0; j < polygons[i].length; j++) {
                coordinates.add(polygons[i][j]);
            }
            stripCounts[i] = polygons[i].length;
            contourCounts[i] = 1;
        }

        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(coordinates.toArray(new Point3f[coordinates.size()]));
        gi.setStripCounts(stripCounts);
        gi.setContourCounts(contourCounts);

        // print obj format to stdout
        GeometryArray ga = gi.getGeometryArray();
        Point3f[] pts = new Point3f[ga.getVertexCount()];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new Point3f();
        }
        ga.getCoordinates(0, pts);
        for (int i = 0; i < pts.length; i++) {
            System.out.println("v " + pts[i].x + " " + pts[i].y + " " + pts[i].z);
        }
        for (int i = 0; i < pts.length; i++) {
            if (i % 3 == 0) System.out.print("\nf ");
            System.out.print((i+1) + " ");
        }
    }
}
