package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import java.util.ArrayList;

public class CustomPartGenerator {

    private final double PART_DEPTH = .25; // distance from top mould face projection will go as a fraction of unit cube

    private Point3d[] mesh; // array of points (each three is a triangle) representing the mesh to make a part for

    /**
     * Construct a new CustomPartGenerator
     *
     * @param inMesh array of points representing triangles as the faces of a mesh to operate on
     */
    public CustomPartGenerator(Point3d[] inMesh)
    {
        if (inMesh.length % 3 != 0)
        {
            throw new IllegalArgumentException("CustomPartGenerator: the number of points in the mesh argument" +
                    "must be a multiple of 3");
        }

        for (Point3d pt : inMesh)
        {
            if (pt.x < 0 || pt.x > 1 || pt.y < 0 || pt.y > 1 || pt.z < 0 || pt.z > 1)
            {
                if (!ProjectionUtils.correctPoint(pt))
                {
                    throw new IllegalArgumentException("CustomPartGenerator: coordinates for all points in the mesh" +
                            "must be between 0 and 1");
                }
            }
        }

        mesh = inMesh;
    }

    /**
     * Generate a custom part for mesh projected onto face
     *
     * @param face face on which to project the custom part
     * @return a CustomPart representing the custom part
     */
    public CustomPart generateCustomPart(ProjectionUtils.ProjectionFace face)
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
        ProjectionUtils.addPolygons(coordinates, stripCounts, contourCounts, face, combinedPolygons, 0);
        ProjectionUtils.addPolygons(coordinates, stripCounts, contourCounts, face, combinedPolygons, PART_DEPTH);

        // prepare and add side rectangles for each hole and non-hole
        ProjectionUtils.addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionPolygons, PART_DEPTH);
        ProjectionUtils.addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionHoles, PART_DEPTH);

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

        return new CustomPart(pts);
    }

    /**
     * Project the inputted mesh onto the inputted face of the unit cube
     *
     * @param face the face of the unit cube on which to project
     * @return the projected mesh
     */
    private Point3d[] getProjectionCoords(ProjectionUtils.ProjectionFace face)
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
}
