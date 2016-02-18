package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.Collections;

public class CustomPartMouldGenerator
{

    // codes used to tell what projection our custom part should use
    public enum ProjectionFace
    {
        XY0, XY1, ZY0, ZY1, ZX0, ZX1
    }

    private final float MOULD_DEPTH = .25f; // distance from face projection will go
    private final float MOULD_PADDING = .1f;
    private final float EXTRA_WH = .1f;
    private final float SCALE = 35;

    // sides of unit cube in the first octant of 3D space specified clockwise
    private final Point3d[] FACE_XY0 =
            {
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, 0),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, 0)
            };

    private final Point3d[] FACE_XY1 =
            {
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING)

            };

    private final Point3d[] FACE_ZY0 =
            {
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, 0)
            };

    private final Point3d[] FACE_ZY1 =
            {
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, 0)
            };

    private final Point3d[] FACE_ZX0 =
            {
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING)
            };

    private final Point3d[] FACE_ZX1 =
            {
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING)
            };

    private Point3d[] mesh;

    // takes an array of Point3d that represents a mesh of triangles (each 3 points is a triangle)
    public CustomPartMouldGenerator(Point3d[] inMesh)
    {
        if (inMesh.length % 3 != 0)
        {
            throw new IllegalArgumentException("CustomPartMouldGenerator: the number of points in the mesh argument" +
                    "must be a multiple of 3");
        }

        for (Point3d pt : inMesh)
        {
            if (pt.x < 0 || pt.x > 1 || pt.y < 0 || pt.y > 1 || pt.z < 0 || pt.z > 1)
            {
                if (!correctPoint(pt))
                {
                    throw new IllegalArgumentException("CustomPartMouldGenerator: coordinates for all points in the mesh" +
                            "must be between 0 and 1");
                }
            }
        }

        mesh = inMesh;
    }

    // corrects for various rounding errors and returns true if correcting was needed
    private boolean correctPoint(Point3d pt)
    {
        boolean correctionNeeded = false;
        double tolerance = MeshVoxeliser.double_tolerance;
        if (0 > pt.x && pt.x > -tolerance)
        {
            pt.x = 0;
            correctionNeeded = true;
        }
        else if (1 < pt.x && pt.x < 1 + tolerance)
        {
            pt.x = 1.0;
            correctionNeeded = true;
        }
        if (0 > pt.y && pt.y > -tolerance)
        {
            pt.y = 0;
            correctionNeeded = true;
        }
        else if (1 < pt.y && pt.y < 1 + tolerance)
        {
            pt.y = 1.0;
            correctionNeeded = true;
        }
        if (0 > pt.z && pt.z > -tolerance)
        {
            pt.z = 0;
            correctionNeeded = true;
        }
        else if (1 < pt.z && pt.z < 1 + tolerance)
        {
            pt.z = 1.0;
            correctionNeeded = true;
        }
        return correctionNeeded;
    }

    // project the inputted mesh onto the inputted face of the unit cube
    private Point3d[] getProjectionCoords(ProjectionFace face)
    {
        Point3d[] projectionCoords = new Point3d[mesh.length];

        for (int i = 0; i < projectionCoords.length; i++)
        {
            Point3d point = new Point3d(mesh[i]);

            switch (face)
            {
                case XY0:
                    point.z = 0;
                    break;
                case XY1:
                    point.z = 1;
                    break;
                case ZY0:
                    point.x = 0;
                    break;
                case ZY1:
                    point.x = 1;
                    break;
                case ZX0:
                    point.y = 0;
                    break;
                case ZX1:
                    point.y = 1;
                    break;
            }

            projectionCoords[i] = point;
        }

        return projectionCoords;
    }

    // convert between z-y plane and x-y plane
    private Point3d[] convertZyXy(Point3d[] originalCoordinates)
    {
        int length = originalCoordinates.length;
        Point3d[] newCoordinates = new Point3d[length];
        for (int i = 0; i < length; i++)
        {
            Point3d point = originalCoordinates[i];
            newCoordinates[i] = new Point3d(point.z, point.y, point.x);
        }
        return newCoordinates;
    }

    // convert between z-x plane and x-y plane
    private Point3d[] convertZxXy(Point3d[] originalCoordinates)
    {
        int length = originalCoordinates.length;
        Point3d[] newCoordinates = new Point3d[length];
        for (int i = 0; i < length; i++)
        {
            Point3d point = originalCoordinates[i];
            newCoordinates[i] = new Point3d(point.x, point.z, point.y);
        }
        return newCoordinates;
    }

    // project the mesh onto the face and rotate so that face ends up as XY1
    private Point3d[] getMouldProjectionCoords(ProjectionFace face)
    {

        Point3d[] projectionCoords = new Point3d[mesh.length];

        for (int i = 0; i < projectionCoords.length; i++)
        {
            Point3d point = new Point3d(mesh[i]);

            switch (face)
            {
                case XY0:
                case XY1:
                    point.z = MOULD_DEPTH + MOULD_PADDING;
                    break;
                case ZY0:
                case ZY1:
                    point.x = MOULD_DEPTH + MOULD_PADDING;
                    break;
                case ZX0:
                case ZX1:
                    point.y = MOULD_DEPTH + MOULD_PADDING;
                    break;
            }

            projectionCoords[i] = point;
        }

        if (face == ProjectionFace.ZY0 || face == ProjectionFace.ZY1)
        {
            projectionCoords = convertZyXy(projectionCoords);
        }
        if (face == ProjectionFace.ZX0 || face == ProjectionFace.ZX1)
        {
            projectionCoords = convertZxXy(projectionCoords);
        }

        return projectionCoords;
    }

    // add the top mould face face with holes for the intrusions
    private void addIntrudedMouldFace(ArrayList<Point3d> coordinates, ArrayList<Integer> stripCounts,
                                      ArrayList<Integer> contourCounts, Point3d[][] projectionPolygons)
    {
        // add coordinates of face as one strip
        Collections.addAll(coordinates, FACE_XY1);
        stripCounts.add(FACE_XY1.length);

        // add coordinates of each projection polygon as a strip
        for (Point3d[] projectionPolygon : projectionPolygons)
        {
            Collections.addAll(coordinates, projectionPolygon);
            stripCounts.add(projectionPolygon.length);
        }

        // each strip is a contour, meaning the first will be the polygon
        // and the rest will be holes in it
        contourCounts.add(stripCounts.size());
    }

    // add the flat faces of the mould
    private void addFlatMouldFaces(ArrayList<Point3d> coordinates, ArrayList<Integer> stripCounts,
                                   ArrayList<Integer> contourCounts)
    {
        Collections.addAll(coordinates, FACE_XY0);
        stripCounts.add(FACE_XY0.length);
        contourCounts.add(1);

        Collections.addAll(coordinates, FACE_ZY0);
        stripCounts.add(FACE_ZY0.length);
        contourCounts.add(1);

        Collections.addAll(coordinates, FACE_ZY1);
        stripCounts.add(FACE_ZY1.length);
        contourCounts.add(1);

        Collections.addAll(coordinates, FACE_ZX0);
        stripCounts.add(FACE_ZX0.length);
        contourCounts.add(1);

        Collections.addAll(coordinates, FACE_ZX1);
        stripCounts.add(FACE_ZX1.length);
        contourCounts.add(1);
    }


    // generate a custom part for mesh projected onto face
    public Point3d[] generateCustomPart(ProjectionFace face)
    {
        if (face == null)
        {
            throw new IllegalArgumentException("generateCustomPart: face cannot be null");
        }

        // data about the polygons that will make up the part
        ArrayList<Point3d> coordinates = new ArrayList<>();
        ArrayList<Integer> stripCounts = new ArrayList<>();
        ArrayList<Integer> contourCounts = new ArrayList<>();

        // project the mesh onto face of the unit cube
        Point3d[] projectionCoords = getProjectionCoords(face);

        // remove self-intersections in the projection
        IntersectionRemover ir = new IntersectionRemover(projectionCoords, face);
        Point3dPolygon[] combinedPolygons = ir.getCombinedArray();
        Point3d[][] projectionPolygons = ir.getPolygonArray();
        Point3d[][] projectionHoles = ir.getHoleArray();

        // add the polygons (with holes omitted) on the face and at depth
        addPolygons(coordinates, stripCounts, contourCounts, face, combinedPolygons, false);
        addPolygons(coordinates, stripCounts, contourCounts, face, combinedPolygons, true);

        // prepare and add side rectangles for each hole and non-hole
        addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionPolygons);
        addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionHoles);

        // triangulate the result
        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(coordinates.toArray(new Point3d[coordinates.size()]));
        gi.setStripCounts(stripCounts.stream().mapToInt(i -> i).toArray());
        gi.setContourCounts(contourCounts.stream().mapToInt(i -> i).toArray());
        GeometryArray ga = gi.getGeometryArray();
        Point3d[] pts = new Point3d[ga.getVertexCount()];
        for (int i = 0; i < pts.length; i++)
        {
            pts[i] = new Point3d();
        }
        ga.getCoordinates(0, pts);

        //generateObjFile(pts);
        return pts;
    }

    // generate a mould for mesh projected onto face
    public Point3d[] generateMould(ProjectionFace face)
    {
        if (face == null)
        {
            throw new IllegalArgumentException("generateMould: face cannot be null");
        }

        // data about the polygons that will make up the mould
        ArrayList<Point3d> coordinates = new ArrayList<>();
        ArrayList<Integer> stripCounts = new ArrayList<>();
        ArrayList<Integer> contourCounts = new ArrayList<>();

        // project the mesh onto the face and rotate so that face ends up as XY1
        Point3d[] projectionCoords = getMouldProjectionCoords(face);

        // remove self-intersections in the projection
        IntersectionRemover ir = new IntersectionRemover(projectionCoords, ProjectionFace.XY1);
        Point3dPolygon[] combinedPolygons = ir.getCombinedArray();
        Point3d[][] projectionPolygons = ir.getPolygonArray();
        Point3d[][] projectionHoles = ir.getHoleArray();

        // add the faces of the mould
        addIntrudedMouldFace(coordinates, stripCounts, contourCounts, projectionPolygons);
        addFlatMouldFaces(coordinates, stripCounts, contourCounts);

        // prepare and add the polygons (with holes omitted) at depth
        addPolygons(coordinates, stripCounts, contourCounts, ProjectionFace.XY1, combinedPolygons, true);

        // add the polygons representing holes to the top of the mould
        for (Point3d[] hole : projectionHoles)
        {
            Collections.addAll(coordinates, hole);
            stripCounts.add(hole.length);
            contourCounts.add(1);
        }

        // prepare and add side rectangles for each hole and non-hole
        addSideRectangles(coordinates, stripCounts, contourCounts, ProjectionFace.XY1, projectionPolygons);
        addSideRectangles(coordinates, stripCounts, contourCounts, ProjectionFace.XY1, projectionHoles);

        // triangulate the result
        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        gi.setCoordinates(coordinates.toArray(new Point3d[coordinates.size()]));
        gi.setStripCounts(stripCounts.stream().mapToInt(i -> i).toArray());
        gi.setContourCounts(contourCounts.stream().mapToInt(i -> i).toArray());
        GeometryArray ga = gi.getGeometryArray();
        Point3d[] pts = new Point3d[ga.getVertexCount()];
        for (int i = 0; i < pts.length; i++)
        {
            pts[i] = new Point3d();
        }
        ga.getCoordinates(0, pts);

        //generateObjFile(pts);
        return pts;
    }

    // add polygons obtained from the projection at the appropriate depth
    private void addPolygons(ArrayList<Point3d> coordinates, ArrayList<Integer> stripCounts,
                             ArrayList<Integer> contourCounts, ProjectionFace face,
                             Point3dPolygon[] projectionPolygons, boolean intrude)
    {
        for (Point3dPolygon projectionPolygon : projectionPolygons)
        {

            // add polygon
            for (Point3d point : projectionPolygon.getExterior())
            {
                if (intrude)
                {
                    point = new Point3d(point);
                    switch (face)
                    {
                        case XY0:
                            point.z += MOULD_DEPTH;
                            break;
                        case XY1:
                            point.z -= MOULD_DEPTH;
                            break;
                        case ZY0:
                            point.x += MOULD_DEPTH;
                            break;
                        case ZY1:
                            point.x -= MOULD_DEPTH;
                            break;
                        case ZX0:
                            point.y += MOULD_DEPTH;
                            break;
                        case ZX1:
                            point.y -= MOULD_DEPTH;
                            break;
                    }
                }

                coordinates.add(point);
            }
            stripCounts.add(projectionPolygon.getExterior().length);

            // add holes
            for (Point3d[] hole : projectionPolygon.getHoles())
            {
                for (Point3d point : hole)
                {
                    if (intrude)
                    {
                        point = new Point3d(point);
                        switch (face)
                        {
                            case XY0:
                                point.z += MOULD_DEPTH;
                                break;
                            case XY1:
                                point.z -= MOULD_DEPTH;
                                break;
                            case ZY0:
                                point.x += MOULD_DEPTH;
                                break;
                            case ZY1:
                                point.x -= MOULD_DEPTH;
                                break;
                            case ZX0:
                                point.y += MOULD_DEPTH;
                                break;
                            case ZX1:
                                point.y -= MOULD_DEPTH;
                                break;
                        }
                    }

                    coordinates.add(point);
                }
                stripCounts.add(hole.length);
            }

            // one contour from polygon, the rest from holes
            contourCounts.add(1 + projectionPolygon.getHoles().length);
        }
    }

    // given a mesh of Point3ds representing triangles (each 3 is a triangle), print an obj
    // file representing this mesh to stdout
    private void generateObjFile(Point3d[] pts)
    {
        if (pts.length % 3 != 0)
        {
            throw new IllegalArgumentException("generateObjFile: number of vertices not a multiple of 3");
        }

        // print vertices
        for (Point3d pt : pts)
        {
            System.out.println("v " + pt.x * SCALE + " " + pt.y * SCALE + " " + pt.z * SCALE);
        }

        // print faces
        for (int i = 1; i < pts.length + 1; i += 3)
        {
            System.out.println("f " + i + " " + (i + 1) + " " + (i + 2));
        }
    }

    // given a list of polygons projected onto a face of a cube, add rectangles that connect
    // each side of each polygon to the side of that polygon projected to its appropriate depth
    private void addSideRectangles(ArrayList<Point3d> coordinates, ArrayList<Integer> stripCounts,
                                   ArrayList<Integer> contourCounts, ProjectionFace face,
                                   Point3d[][] polygons)
    {
        for (Point3d[] polygon : polygons)
        {
            Point3d[][] sideRectangles = new Point3d[polygon.length][];
            for (int i = 0; i < polygon.length - 1; i++)
            {
                sideRectangles[i] = new Point3d[4];

                Point3d projectionFaceCoord1 = new Point3d(polygon[i]);
                Point3d projectionFaceCoord2 = new Point3d(polygon[i + 1]);

                intrudeCoordinate(projectionFaceCoord1, face);
                intrudeCoordinate(projectionFaceCoord2, face);

                sideRectangles[i][0] = polygon[i];
                sideRectangles[i][1] = projectionFaceCoord1;
                sideRectangles[i][2] = projectionFaceCoord2;
                sideRectangles[i][3] = polygon[i + 1];
            }

            // create the last rectangle which contains the edge between the first and last points
            sideRectangles[sideRectangles.length - 1] = new Point3d[4];

            Point3d projectionFaceCoord1 = new Point3d(polygon[polygon.length - 1]);
            Point3d projectionFaceCoord2 = new Point3d(polygon[0]);

            intrudeCoordinate(projectionFaceCoord1, face);
            intrudeCoordinate(projectionFaceCoord2, face);

            sideRectangles[sideRectangles.length - 1][0] = polygon[polygon.length - 1];
            sideRectangles[sideRectangles.length - 1][1] = projectionFaceCoord1;
            sideRectangles[sideRectangles.length - 1][2] = projectionFaceCoord2;
            sideRectangles[sideRectangles.length - 1][3] = polygon[0];

            // add all of the created polygons to our mesh
            for (Point3d[] rectangle : sideRectangles)
            {
                Collections.addAll(coordinates, rectangle);
                stripCounts.add(rectangle.length);
                contourCounts.add(1);
            }
        }
    }

    // given a projected coordinate and what face it is on, intrude it to the appropriate depth
    private void intrudeCoordinate(Point3d coord, ProjectionFace face)
    {
        switch (face)
        {
            case XY0:
                coord.z += MOULD_DEPTH;
                break;
            case XY1:
                coord.z -= MOULD_DEPTH;
                break;
            case ZY0:
                coord.x += MOULD_DEPTH;
                break;
            case ZY1:
                coord.x -= MOULD_DEPTH;
                break;
            case ZX0:
                coord.y += MOULD_DEPTH;
                break;
            case ZX1:
                coord.y -= MOULD_DEPTH;
                break;
        }
    }


}
