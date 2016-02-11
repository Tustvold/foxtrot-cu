package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.function.ToIntFunction;

public class Projection {

    // codes used to tell what projection our custom part should use
    public enum ProjectionFace { XY0, XY1, ZY0, ZY1, ZX0, ZX1 };


    // sides of unit cube in the first octant of 3D space specified clockwise
    private Point3f[] face_xy0 =
            {
                new Point3f(0, 0, 0),
                new Point3f(1, 0, 0),
                new Point3f(1, 1, 0),
                new Point3f(0, 1, 0)
            };

    private Point3f[] face_xy1 =
            {
                new Point3f(0, 0, 1),
                new Point3f(1, 0, 1),
                new Point3f(1, 1, 1),
                new Point3f(0, 1, 1)

            };

    private Point3f[] face_zy0 =
            {
                new Point3f(0, 0, 0),
                new Point3f(0, 0, 1),
                new Point3f(0, 1, 1),
                new Point3f(0, 1, 0)
            };

    private Point3f[] face_zy1 =
            {
                new Point3f(1, 0, 0),
                new Point3f(1, 0, 1),
                new Point3f(1, 1, 1),
                new Point3f(1, 1, 0)
            };

    private Point3f[] face_zx0 =
            {
                new Point3f(0, 0, 0),
                new Point3f(1, 0, 0),
                new Point3f(1, 0, 1),
                new Point3f(0, 0, 1)
            };

    private Point3f[] face_zx1 =
            {
                new Point3f(0, 1, 0),
                new Point3f(1, 1, 0),
                new Point3f(1, 1, 1),
                new Point3f(0, 1, 1)
            };


    private final float PROJECTION_DEPTH = .25f; // distance from face projection will go
    private Point3f[] object_coords;

    public Projection(TriangleArray ta) {
        object_coords = new Point3f[ta.getVertexCount()];
        for (int i = 0; i < ta.getVertexCount(); i++) {
            object_coords[i] = new Point3f();
        }

        ta.getCoordinates(0, object_coords);
    }

    // return list of polygons making a cube with the appropriate projection
    public Point3f[][] getCube(ProjectionFace face) {
        Point3f[][] cube = new Point3f[6][];
        Point3f[][] squareFaces = getSqareCubeFaces(face);

        cube[5] = getProjectionCubeFace(face);
        for (int i = 0; i < 5; i++) {
            cube[i] = squareFaces[i];
        }

        return cube;
    }

    public Point3f[] getProjectionCubeFace(ProjectionFace face) { //TODO add projection depth
        Point3f[] projection_coords = new Point3f[object_coords.length];

        for (int i = 0; i < projection_coords.length; i++) {
            Point3f point = new Point3f(object_coords[i]);

            switch (face) {
                case XY0: point.z = 0; break;
                case XY1: point.z = 1; break;
                case ZY0: point.x = 0; break;
                case ZY1: point.x = 1; break;
                case ZX0: point.y = 0; break;
                case ZX1: point.y = 1; break;
            }

            projection_coords[i] = point;
        }

        // FEED PROJECTION_COORDS TO ISOBELS CODE - get back array of polygons and array of holes
        Point3f[][] projectionPolygons = new Point3f[][] {projection_coords};
        Point3f[][] projectionHoles = null;

        // make a unit square with all projection non-holes as holes in it
        ArrayList<Point3f> coordinates = new ArrayList<Point3f>();
        ArrayList<Integer> stripCounts = new ArrayList<Integer>();
        ArrayList<Integer> contourCounts = new ArrayList<Integer>();
        for (int i = 0; i < face_zx0.length; i++) { //todo make face parameter
            coordinates.add(face_zx0[i]);
        }

        stripCounts.add(4);

        for (int i = 0; i < projectionPolygons.length; i++) {
            for (int j = 0; j < projectionPolygons[i].length; j++) {
                coordinates.add(projectionPolygons[i][j]);
            }
            stripCounts.add(projectionPolygons[i].length);
        }

        contourCounts.add(stripCounts.size());

        // add cube faces
        Point3f[][] cubeFaces = getSqareCubeFaces(face);
        for (int i = 0; i < cubeFaces.length; i++) {
            for (int j = 0; j < cubeFaces[i].length; j++) {
                coordinates.add(cubeFaces[i][j]);
            }
            stripCounts.add(cubeFaces[i].length);
            contourCounts.add(1);
        }

        // prepare and add the projection polygons


        // triangulate the result
        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(coordinates.toArray(new Point3f[coordinates.size()]));
        //todo fix gross hacks
        gi.setStripCounts(stripCounts.stream().mapToInt(new ToIntFunction<Integer>() {
            @Override
            public int applyAsInt(Integer i) {
                return i;
            }
        }).toArray());
        gi.setContourCounts(contourCounts.stream().mapToInt(new ToIntFunction<Integer>() {
            @Override
            public int applyAsInt(Integer i) {
                return i;
            }
        }).toArray());

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


        return pts;
    }

    private Point3f[][] getSqareCubeFaces(ProjectionFace face) {
        Point3f[][] faces = new Point3f[5][];
        int i = 0;

        if (face != ProjectionFace.XY0) {
            faces[i++] = face_xy0;
        }
        if (face != ProjectionFace.XY1) {
            faces[i++] = face_xy1;
        }
        if (face != ProjectionFace.ZY0) {
            faces[i++] = face_zy0;
        }
        if (face != ProjectionFace.ZY1) {
            faces[i++] = face_zy1;
        }
        if (face != ProjectionFace.ZX0) {
            faces[i++] = face_zx0;
        }
        if (face != ProjectionFace.ZX1) {
            faces[i++] = face_zx1;
        }

        return faces;
    }

}
