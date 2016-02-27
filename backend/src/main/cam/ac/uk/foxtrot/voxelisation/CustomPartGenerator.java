package cam.ac.uk.foxtrot.voxelisation;


import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.GeometryArray;
import javax.vecmath.Point3d;
import java.util.ArrayList;

public class CustomPartGenerator
{

    private double PART_DEPTH; // distance from top mould face projection will go as a fraction of unit cube

    private Point3d[] mesh; // array of points (each three is a triangle) representing the mesh to make a part for
    private Block block; // the block the array was extracted from

    /**
     * Construct a new CustomPartGenerator
     *
     * @param block the block for which the sides will be created
     */
    public CustomPartGenerator(Block block)
    {
        this.block = block;
        ArrayList<Point3d> al = block.getTriangles();
        mesh = al.toArray(new Point3d[al.size()]);

        if (mesh.length % 3 != 0)
        {
            throw new IllegalArgumentException("CustomPartGenerator: the number of points in the mesh argument" +
                    "must be a multiple of 3");
        }

        for (Point3d pt : mesh)
        {
            if (pt.x < 0 || pt.x > 1 || pt.y < 0 || pt.y > 1 || pt.z < 0 || pt.z > 1)
            {
                throw new IllegalArgumentException("CustomPartGenerator: coordinates for all points in the mesh" +
                        "must be between 0 and 1");
            }
        }
    }

    /**
     * Generate all custom parts for the block  on the Custom Part Generator
     */
    public void generateAllCustomParts()
    {
        for(int i = 0; i < 3; i++)
        {
            block.setCustomPart(i, generateCustomPart(i));
        }
    }

    /**
     * Offsets the entire mesh appropriately and returns it as a custom part.
     */
    private CustomPart offsetAndReturn(Point3d[] pts, int projectTo)
    {
        double offset = block.getInternalDim()[projectTo];
        for (int i = 0; i < pts.length; i++)
        {
            switch (projectTo)
            {
                case 0: pts[i].x += offset;
                    break;
                case 1: pts[i].y += offset;
                    break;
                case 2: pts[i].z += offset;
                    break;
            }
        }
        return new CustomPart(pts);
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
        double[] dim = block.getInternalDim();
        return dim[3 + projectTo] - dim[projectTo];
    }

    /**
     * Generate a custom part for mesh projected onto face
     *
     * @param projectTo face index on which to project the custom part
     *                  cases: 0 -> ZY0
     *                  1 -> ZX0
     *                  2 -> XY0
     * @return a CustomPart representing the custom part
     */
    public CustomPart generateCustomPart(int projectTo)
    {
        if (projectTo < 0 || projectTo > 2)
        {
            throw new IllegalArgumentException("generateCustomPart: face cannot be null");
        }
        // determine the face and the part depth
        ProjectionUtils.ProjectionFace face;
        face = getProjectionFace(projectTo);
        PART_DEPTH = determinePartDepth(projectTo);

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
        ProjectionUtils.addPolygons(coordinates, stripCounts, contourCounts, face, ProjectionUtils.reverseWindingOrder(combinedPolygons), PART_DEPTH);

        // prepare and add side rectangles for each hole and non-hole
        ProjectionUtils.addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionPolygons, PART_DEPTH);
        ProjectionUtils.addSideRectangles(coordinates, stripCounts, contourCounts, face, projectionHoles, PART_DEPTH);

        Point3d[] pts;
        // triangulate the result
        if (coordinates.size() > 0)
        {
            GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
            gi.setCoordinates(coordinates.toArray(new Point3d[coordinates.size()]));
            gi.setStripCounts(stripCounts.stream().mapToInt(i -> i).toArray());
            gi.setContourCounts(contourCounts.stream().mapToInt(i -> i).toArray());
            GeometryArray ga = gi.getGeometryArray();
            pts = new Point3d[ga.getVertexCount()];
            for (int i = 0; i < pts.length; i++)
            {
                pts[i] = new Point3d();
            }
            ga.getCoordinates(0, pts);

        }
        else
        {
            // if the projectino is empty, return an empty custom part
            pts = new Point3d[0];
        }

        return offsetAndReturn(pts, projectTo);
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
