package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class CustomPartMouldGenerator
{

    private final double MOULD_DEPTH = .25; // distance from top mould face projection will go as a fraction of scale
    private final double MOULD_PADDING = .1; // extra space for depth of the mould as a fraction of scale
    private final double EXTRA_WH = .1; // extra space for width and height of the mould as a fraction of scale

    // polygons representing faces of the mould
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
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, 0),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING)
            };

    private final Point3d[] FACE_ZY1 =
            {
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, 0),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING)
            };

    private final Point3d[] FACE_ZX0 =
            {
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, 0),
                    new Point3d(0 - EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 0 - EXTRA_WH, 0)
            };

    private final Point3d[] FACE_ZX1 =
            {
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, 0),
                    new Point3d(0 - EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING),
                    new Point3d(1 + EXTRA_WH, 1 + EXTRA_WH, 0)
            };

    private Point3d[] mesh; // array of points (each three is a triangle) representing the mesh to make a mould for
    private double scale; // mould scale in mm

    /**
     * Construct a new CustomPartMouldGenerator
     *
     * @param inMesh  array of points representing triangles as the faces of a mesh to operate on
     * @param inScale mould scale in mm
     */
    public CustomPartMouldGenerator(Point3d[] inMesh, double inScale)
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
                throw new IllegalArgumentException("CustomPartMouldGenerator: coordinates for all points in the mesh" +
                        "must be between 0 and 1");
            }
        }

        mesh = inMesh;
        scale = inScale;
    }

    /**
     * Project mesh onto the given face and transform the face so that it ends up as XY1
     *
     * @param face the face to project mesh onto
     * @return an array of points representing mesh projected onto face and transformed
     */
    private Point3d[] getMouldProjectionCoords(ProjectionUtils.ProjectionFace face)
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

        if (face == ProjectionUtils.ProjectionFace.ZY0 || face == ProjectionUtils.ProjectionFace.ZY1)
        {
            projectionCoords = ProjectionUtils.convertZyXy(projectionCoords);
        }
        if (face == ProjectionUtils.ProjectionFace.ZX0 || face == ProjectionUtils.ProjectionFace.ZX1)
        {
            projectionCoords = ProjectionUtils.convertZxXy(projectionCoords);
        }

        return projectionCoords;
    }

    /**
     * Add the top mould face with holes where intrusions should be added
     *
     * @param coordinates        list of coordinates of the mould to add to
     * @param stripCounts        list of strips of the mould to add to
     * @param contourCounts      list of contours of the mould to add to
     * @param projectionPolygons polygons that should be represented as holes on the top mould face
     */
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

    /**
     * Add the flat faces of the mould
     *
     * @param coordinates   list of coordinates of the mould to add to
     * @param stripCounts   list of strips of the mould to add to
     * @param contourCounts list of contours of the mould to add to
     */
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

    /**
     * Generate a mould for mesh projection onto the given face, and write it as an .obj file to file
     *
     * @param face the face on which mesh should be projected
     * @param file the file to which the output .obj file should be written
     */
    public void generateMould(ProjectionUtils.ProjectionFace face, File file)
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
        IntersectionRemover ir = new IntersectionRemover(projectionCoords, ProjectionUtils.ProjectionFace.XY1);
        Point3dPolygon[] combinedPolygons = ir.getCombinedArray();
        Point3d[][] projectionPolygons = ir.getPolygonArray();
        Point3d[][] projectionHoles = ir.getHoleArray();

        // add the faces of the mould
        addIntrudedMouldFace(coordinates, stripCounts, contourCounts, projectionPolygons);
        addFlatMouldFaces(coordinates, stripCounts, contourCounts);

        // prepare and add the polygons (with holes omitted) at depth
        ProjectionUtils.addPolygons(coordinates, stripCounts, contourCounts, ProjectionUtils.ProjectionFace.XY1, combinedPolygons, MOULD_DEPTH);

        // add the polygons representing holes to the top of the mould
        for (Point3d[] hole : projectionHoles)
        {
            Collections.addAll(coordinates, hole);
            stripCounts.add(hole.length);
            contourCounts.add(1);
        }

        // prepare and add side rectangles for each hole and non-hole
        ProjectionUtils.addSideRectangles(coordinates, stripCounts, contourCounts, ProjectionUtils.ProjectionFace.XY1, projectionPolygons, MOULD_DEPTH);
        ProjectionUtils.addSideRectangles(coordinates, stripCounts, contourCounts, ProjectionUtils.ProjectionFace.XY1, projectionHoles, MOULD_DEPTH);

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

        ProjectionUtils.generateObjFile(pts, file, scale);
    }


}
