package cam.ac.uk.foxtrot.voxelisation;

import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class CustomPartMouldGenerator
{

    private double MOULD_DEPTH; // distance from top mould face projection will go as a fraction of scale
    private double MOULD_PADDING; // extra space for depth of the mould as a fraction of scale
    private double EXTRA_WH; // extra space for width and height of the mould as a fraction of scale
    private double MOULD_SIZE_IN_X; // the new dimension in x of the mould
    private double MOULD_SIZE_IN_Y; // the new dimension in y of the mould
    private double SUBTRACT_IN_X; // the amount we need to shift the projection back
    private double SUBTRACT_IN_Y; // the amount we need to shift the projection back

    // polygons representing faces of the mould
    private Point3d[] FACE_XY0;
    private Point3d[] FACE_XY1;
    private Point3d[] FACE_ZY0;
    private Point3d[] FACE_ZY1;
    private Point3d[] FACE_ZX0;
    private Point3d[] FACE_ZX1;

    private Point3d[] mesh; // array of points (each three is a triangle) representing the mesh to make a mould for
    private double scale; // mould scale in mm
    private double[] mesh_dimension; // the internal dimensions of the mesh

    /**
     * Determines all the relevant faces, once the mould depth is calculated.
     */
    private void determine_faces()
    {
        FACE_XY0 = new Point3d[4];
        FACE_XY0[0] = new Point3d(0               - EXTRA_WH, 0               - EXTRA_WH, 0);
        FACE_XY0[1] = new Point3d(0               - EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, 0);
        FACE_XY0[2] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, 0);
        FACE_XY0[3] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, 0               - EXTRA_WH, 0);

        FACE_XY1 = new Point3d[4];
        FACE_XY1[0] = new Point3d(0               - EXTRA_WH, 0               - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_XY1[1] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, 0               - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_XY1[2] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_XY1[3] = new Point3d(0               - EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);

        FACE_ZY0 = new Point3d[4];
        FACE_ZY0[0] = new Point3d(0 - EXTRA_WH, 0               - EXTRA_WH, 0);
        FACE_ZY0[1] = new Point3d(0 - EXTRA_WH, 0               - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_ZY0[2] = new Point3d(0 - EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_ZY0[3] = new Point3d(0 - EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, 0);

        FACE_ZY1 = new Point3d[4];
        FACE_ZY1[0] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, 0               - EXTRA_WH, 0);
        FACE_ZY1[1] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, 0);
        FACE_ZY1[2] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_ZY1[3] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, 0               - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);

        FACE_ZX0 = new Point3d[4];
        FACE_ZX0[0] = new Point3d(0               - EXTRA_WH, 0 - EXTRA_WH, 0);
        FACE_ZX0[1] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, 0 - EXTRA_WH, 0);
        FACE_ZX0[2] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_ZX0[3] = new Point3d(0               - EXTRA_WH, 0 - EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);

        FACE_ZX1 = new Point3d[4];
        FACE_ZX1[0] = new Point3d(0               - EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, 0);
        FACE_ZX1[1] = new Point3d(0               - EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_ZX1[2] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, MOULD_DEPTH + MOULD_PADDING);
        FACE_ZX1[3] = new Point3d(MOULD_SIZE_IN_X + EXTRA_WH, MOULD_SIZE_IN_Y + EXTRA_WH, 0);
    }

    /**
     * Subrtacts the calculated constants from all points in proj;
     */
    private void subtractCoordinatesOfProjection(Point3d[] proj)
    {
        for (int i = 0; i < proj.length; i++)
        {
            proj[i].x -= SUBTRACT_IN_X;
            proj[i].y -= SUBTRACT_IN_Y;
        }
    }

    /**
     * Construct a new CustomPartMouldGenerator
     *
     * @param inMesh  array of points representing triangles as the faces of a mesh to operate on
     * @param inScale mould scale in mm
     */
    public CustomPartMouldGenerator(Point3d[] inMesh, double inScale, double[] dimensions)
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

        mesh_dimension = dimensions;
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

            // determine the appropriate mould sizes in each of the new dimensions
            switch (face)
            {
                case XY0:
                case XY1:
                    MOULD_SIZE_IN_X = mesh_dimension[3] - mesh_dimension[0]; // X -> X
                    MOULD_SIZE_IN_Y = mesh_dimension[4] - mesh_dimension[1]; // Y -> Y
                    SUBTRACT_IN_X = mesh_dimension[0]; // X -> X
                    SUBTRACT_IN_Y = mesh_dimension[1]; // Y -> Y
                    point.z = MOULD_DEPTH + MOULD_PADDING;
                    break;
                case ZY0:
                case ZY1:
                    MOULD_SIZE_IN_X = mesh_dimension[5] - mesh_dimension[2]; // Z -> X
                    MOULD_SIZE_IN_Y = mesh_dimension[4] - mesh_dimension[1]; // Y -> Y
                    SUBTRACT_IN_X = mesh_dimension[2]; // Z -> X
                    SUBTRACT_IN_Y = mesh_dimension[1]; // Y -> Y
                    point.x = MOULD_DEPTH + MOULD_PADDING;
                    break;
                case ZX0:
                case ZX1:
                    MOULD_SIZE_IN_X = mesh_dimension[3] - mesh_dimension[0]; // X -> X
                    MOULD_SIZE_IN_Y = mesh_dimension[5] - mesh_dimension[2]; // Z -> Y
                    SUBTRACT_IN_X = mesh_dimension[0]; // X -> X
                    SUBTRACT_IN_Y = mesh_dimension[2]; // Z -> Y
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
     * Determines the projection face depending on its index.
     *
     * @param projectTo face index on which to project the custom part
     *                  cases: 0 -> ZY0
     *                  1 -> ZX0
     *                  2 -> XY0
     */
    private ProjectionUtils.ProjectionFace getProjectionFace(int projectTo)
    {
        switch (projectTo)
        {
            case 0:
                return ProjectionUtils.ProjectionFace.ZY0;
            case 1:
                return ProjectionUtils.ProjectionFace.ZX0;
            case 2:
                return ProjectionUtils.ProjectionFace.XY0;
        }
        return null;
    }

    /**
     * Determines the parts depth depending on the index of the projection face
     */
    private double determinePartDepth(int projectTo)
    {
        return mesh_dimension[3 + projectTo] - mesh_dimension[projectTo];
    }

    /**
     * Generate a mould for mesh projection onto the given face, and write it as an .obj file to file
     *
     * @param projectTo the face  label on which mesh should be projected
     * @param file      the file to which the output .obj file should be written
     */
    public void generateMould(int projectTo, File file)
    {
        ProjectionUtils.ProjectionFace face;
        face = getProjectionFace(projectTo);
        MOULD_DEPTH = determinePartDepth(projectTo);
        MOULD_PADDING = 0.1;
        EXTRA_WH = 0.1;
        if(EXTRA_WH < 2 / scale)
        {
            EXTRA_WH = 2 / scale; // the extra mould thickness in xy never going to be under 2 mm
        }
        if(MOULD_PADDING < 2 / scale)
        {
            MOULD_PADDING = 2 / scale; // the extra mould height is never going to be under 2 mm
        }

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
        // now we create the proper faces and then shift the coordinates
        determine_faces();
        subtractCoordinatesOfProjection(projectionCoords);

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
