package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.Collections;

public class CustomPartMouldGenerator {

    //TODO questions: have two custom parts? can hole extrusion go on top of part intrusion? duplicate vertices?

    // codes used to tell what projection our custom part should use
    public enum ProjectionFace { XY0, XY1, ZY0, ZY1, ZX0, ZX1 }


    // sides of unit cube in the first octant of 3D space specified clockwise
    private final Point3f[] FACE_XY0 =
            {
                new Point3f(0, 0, 0),
                new Point3f(1, 0, 0),
                new Point3f(1, 1, 0),
                new Point3f(0, 1, 0)
            };

    private final Point3f[] FACE_XY1 =
            {
                new Point3f(0, 0, 1),
                new Point3f(1, 0, 1),
                new Point3f(1, 1, 1),
                new Point3f(0, 1, 1)

            };

    private final Point3f[] FACE_ZY0 =
            {
                new Point3f(0, 0, 0),
                new Point3f(0, 0, 1),
                new Point3f(0, 1, 1),
                new Point3f(0, 1, 0)
            };

    private final Point3f[] FACE_ZY1 =
            {
                new Point3f(1, 0, 0),
                new Point3f(1, 0, 1),
                new Point3f(1, 1, 1),
                new Point3f(1, 1, 0)
            };

    private final Point3f[] FACE_ZX0 =
            {
                new Point3f(0, 0, 0),
                new Point3f(1, 0, 0),
                new Point3f(1, 0, 1),
                new Point3f(0, 0, 1)
            };

    private final Point3f[] FACE_ZX1 =
            {
                new Point3f(0, 1, 0),
                new Point3f(1, 1, 0),
                new Point3f(1, 1, 1),
                new Point3f(0, 1, 1)
            };


    private final float PROJECTION_DEPTH = .25f; // distance from face projection will go
    private Point3f[] mesh;

    // takes an array of Point3f that represents a mesh of triangles (each 3 points is a triangle)
    public CustomPartMouldGenerator(Point3f[] inMesh) {
        if (inMesh.length % 3 != 0) {
            throw new IllegalArgumentException("CustomPartMouldGenerator: the number of points in the mesh argument" +
                    "must be a multiple of 3");
        }

        for (Point3f pt : inMesh) {
            if (pt.x < 0 || pt.x > 1 || pt.y < 0 || pt.y > 1 || pt.z < 0 || pt.z > 1) {
                throw new IllegalArgumentException("CustomPartMouldGenerator: coordinates for all points in the mesh" +
                        "must be between 0 and 1");
            }
        }

        mesh = inMesh;
    }

    // make a projected copy of the mesh coordinates onto the inputted plane
    private Point3f[] getProjectionCoords(ProjectionFace face) {
        Point3f[] projectionCoords = new Point3f[mesh.length];

        for (int i = 0; i < projectionCoords.length; i++) {
            Point3f point = new Point3f(mesh[i]);

            switch (face) {
                case XY0: point.z = 0; break;
                case XY1: point.z = 1; break;
                case ZY0: point.x = 0; break;
                case ZY1: point.x = 1; break;
                case ZX0: point.y = 0; break;
                case ZX1: point.y = 1; break;
            }

            projectionCoords[i] = point;
        }

        return projectionCoords;
    }

    // add the cube face with holes for the intrusions
    private void addIntrudedFace(ArrayList<Point3f> coordinates, ArrayList<Integer> stripCounts,
                                 ArrayList<Integer> contourCounts, ProjectionFace face,
                                 Point3f[][] projectionPolygons) {
        Point3f[] faceCoords = null;

        switch (face) {
            case XY0: faceCoords = FACE_XY0; break;
            case XY1: faceCoords = FACE_XY1; break;
            case ZY0: faceCoords = FACE_ZY0; break;
            case ZY1: faceCoords = FACE_ZY1; break;
            case ZX0: faceCoords = FACE_ZX0; break;
            case ZX1: faceCoords = FACE_ZX1; break;
        }

        // add coordinates of square as one strip
        Collections.addAll(coordinates, faceCoords);
        stripCounts.add(faceCoords.length);

        // add coordinates of each projection polygon as a strip
        for (Point3f[] projectionPolygon : projectionPolygons) {
            Collections.addAll(coordinates, projectionPolygon);
            stripCounts.add(projectionPolygon.length);
        }

        // each strip is a contour, meaning the first will be the polygon
        // and the rest will be holes in it
        contourCounts.add(stripCounts.size());
    }

    // add all cube faces besides the one passed in
    private void addSqareCubeFaces(ArrayList<Point3f> coordinates, ArrayList<Integer> stripCounts,
                                          ArrayList<Integer> contourCounts, ProjectionFace face) {
        if (face != ProjectionFace.XY0) {
            Collections.addAll(coordinates, FACE_XY0);
            stripCounts.add(FACE_XY0.length);
            contourCounts.add(1);
        }
        if (face != ProjectionFace.XY1) {
            Collections.addAll(coordinates, FACE_XY1);
            stripCounts.add(FACE_XY1.length);
            contourCounts.add(1);
        }
        if (face != ProjectionFace.ZY0) {
            Collections.addAll(coordinates, FACE_ZY0);
            stripCounts.add(FACE_ZY0.length);
            contourCounts.add(1);
        }
        if (face != ProjectionFace.ZY1) {
            Collections.addAll(coordinates, FACE_ZY1);
            stripCounts.add(FACE_ZY1.length);
            contourCounts.add(1);
        }
        if (face != ProjectionFace.ZX0) {
            Collections.addAll(coordinates, FACE_ZX0);
            stripCounts.add(FACE_ZX0.length);
            contourCounts.add(1);
        }
        if (face != ProjectionFace.ZX1) {
            Collections.addAll(coordinates, FACE_ZX1);
            stripCounts.add(FACE_ZX1.length);
            contourCounts.add(1);
        }
    }

    // generate a mould for mesh projected onto face
    public void generateMould(ProjectionFace face) {
        if (face == null) {
            throw new IllegalArgumentException("generateMould: face cannot be null");
        }

        // data about the polygons that will make up the mould
        ArrayList<Point3f> coordinates = new ArrayList<>();
        ArrayList<Integer> stripCounts = new ArrayList<>();
        ArrayList<Integer> contourCounts = new ArrayList<>();

        // project the mesh onto face and remove self-intersections
        Point3f[] projectionCoords = getProjectionCoords(face);
        //todo use this
        IntersectionRemover ir = new IntersectionRemover(projectionCoords);
        Point3f[][] test = ir.getPolygonArray();
        Point3f[][] projectionPolygons = new Point3f[][] {projectionCoords};
        Point3f[][] projectionHoles = new Point3f[][] {new Point3f[] {new Point3f(.3f,1f,.3f),new Point3f(.4f,1f,.3f),new Point3f(.3f,1f,.4f)}};
        //todo fail gracefully if these have less than 2 unique points (what behavior do we want?)

        // add the faces of the mould
        addIntrudedFace(coordinates, stripCounts, contourCounts, face, projectionPolygons);
        addSqareCubeFaces(coordinates, stripCounts, contourCounts, face);

        // prepare and add the projection polygons at their appropriate depth
        addProjectionPolygons(coordinates, stripCounts, contourCounts, face, projectionPolygons);

        // add the polygons representing holes on the face of the cube
        //todo test
        for (Point3f[] hole : projectionHoles) {
            Collections.addAll(coordinates, hole);
            stripCounts.add(hole.length);
            contourCounts.add(1);
        }

        // prepare and add side rectangles for each hole and non-hole
        addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionPolygons);
        addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionHoles);

        // triangulate the result
        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(coordinates.toArray(new Point3f[coordinates.size()]));
        gi.setStripCounts(stripCounts.stream().mapToInt(i -> i).toArray());
        gi.setContourCounts(contourCounts.stream().mapToInt(i -> i).toArray());
        GeometryArray ga = gi.getGeometryArray();
        Point3f[] pts = new Point3f[ga.getVertexCount()];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = new Point3f();
        }
        ga.getCoordinates(0, pts);

        generateObjFile(pts);
    }

    // add polygons obtained from the projection at the appropriate depth inside the cube
    private void addProjectionPolygons(ArrayList<Point3f> coordinates, ArrayList<Integer> stripCounts,
                                       ArrayList<Integer> contourCounts, ProjectionFace face,
                                       Point3f[][] projectionPolygons) {
        for (Point3f[] projectionPolygon : projectionPolygons) {
            for (Point3f ppPoint : projectionPolygon) {
                Point3f point = new Point3f(ppPoint);

                // shift to projection depth
                switch (face) {
                    case XY0: point.z += PROJECTION_DEPTH; break;
                    case XY1: point.z -= PROJECTION_DEPTH; break;
                    case ZY0: point.x += PROJECTION_DEPTH; break;
                    case ZY1: point.x -= PROJECTION_DEPTH; break;
                    case ZX0: point.y += PROJECTION_DEPTH; break;
                    case ZX1: point.y -= PROJECTION_DEPTH; break;
                }

                coordinates.add(point);
            }
            stripCounts.add(projectionPolygon.length);
            contourCounts.add(1);
        }
    }

    // given a mesh of Point3fs representing triangles (each 3 is a triangle), print an obj
    // file representing this mesh to stdout
    private void generateObjFile(Point3f[] pts) {
        if (pts.length % 3 != 0) {
            throw new IllegalArgumentException("generateObjFile: number of vertices not a multiple of 3");
        }

        // print vertices
        for (Point3f pt : pts) {
            System.out.println("v " + pt.x + " " + pt.y + " " + pt.z);
        }

        // print faces
        for (int i = 1; i < pts.length + 1; i += 3) {
            System.out.println("f " + i + " " + (i + 1) + " " + (i + 2));
        }
    }

    // given a list of polygons projected onto a face of a cube, add rectangles that connect
    // each side of each polygon to the side of that polygon projected to its appropriate depth
    private void addSideRectangles(ArrayList<Point3f> coordinates, ArrayList<Integer> stripCounts,
                                   ArrayList<Integer> contourCounts, ProjectionFace face,
                                   Point3f[][] polygons) {
        for (Point3f[] polygon : polygons) {
            Point3f[][] sideRectangles = new Point3f[polygon.length][];
            for (int i = 0; i < polygon.length - 1; i++) {
                sideRectangles[i] = new Point3f[4];

                Point3f projectionFaceCoord1 = new Point3f(polygon[i]);
                Point3f projectionFaceCoord2 = new Point3f(polygon[i+1]);

                intrudeCoordinate(projectionFaceCoord1, face);
                intrudeCoordinate(projectionFaceCoord2, face);

                sideRectangles[i][0] = polygon[i];
                sideRectangles[i][1] = projectionFaceCoord1;
                sideRectangles[i][2] = projectionFaceCoord2;
                sideRectangles[i][3] = polygon[i+1];
            }

            // create the last rectangle which contains the edge between the first and last points
            sideRectangles[sideRectangles.length - 1] = new Point3f[4];

            Point3f projectionFaceCoord1 = new Point3f(polygon[polygon.length - 1]);
            Point3f projectionFaceCoord2 = new Point3f(polygon[0]);

            intrudeCoordinate(projectionFaceCoord1, face);
            intrudeCoordinate(projectionFaceCoord2, face);

            sideRectangles[sideRectangles.length - 1][0] = polygon[polygon.length - 1];
            sideRectangles[sideRectangles.length - 1][1] = projectionFaceCoord1;
            sideRectangles[sideRectangles.length - 1][2] = projectionFaceCoord2;
            sideRectangles[sideRectangles.length - 1][3] = polygon[0];

            // add all of the created polygons to our mesh
            for (Point3f[] rectangle : sideRectangles) {
                Collections.addAll(coordinates, rectangle);
                stripCounts.add(rectangle.length);
                contourCounts.add(1);
            }
        }
    }

    // given a projected coordinate and what face it is on, intrude it to the appropriate depth
    private void intrudeCoordinate(Point3f coord, ProjectionFace face) {
        switch (face) {
            case XY0: coord.z += PROJECTION_DEPTH; break;
            case XY1: coord.z -= PROJECTION_DEPTH; break;
            case ZY0: coord.x += PROJECTION_DEPTH; break;
            case ZY1: coord.x -= PROJECTION_DEPTH; break;
            case ZX0: coord.y += PROJECTION_DEPTH; break;
            case ZX1: coord.y -= PROJECTION_DEPTH; break;
        }
    }



}
