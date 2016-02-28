package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Point3d;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ProjectionUtils {

    // codes used to tell what projection our custom part should use
    public enum ProjectionFace
    {
        XY0, XY1, ZY0, ZY1, ZX0, ZX1
    }

    /**
     * Convert an array of points between being in the z-y and x-y plane
     *
     * @param originalCoordinates the array of points to convert
     * @return the converted points
     */
    public static Point3d[] convertZyXy(Point3d[] originalCoordinates)
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

    /**
     * Convert an array of points between being in the z-x and x-y plane
     *
     * @param originalCoordinates the array of points to convert
     * @return the converted points
     */
    public static Point3d[] convertZxXy(Point3d[] originalCoordinates)
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


    /**
     * Add polygons obtained from the projection at the appropriate depth
     *
     * @param coordinates list of coordinates of the mould to add to
     * @param stripCounts list of strips of the mould to add to
     * @param contourCounts list of contours of the mould to add to
     * @param face the face from which to project
     * @param projectionPolygons the polygons to project
     * @param depth projection depth
     */
    public static void addPolygons(ArrayList<Point3d> coordinates, ArrayList<Integer> stripCounts,
                             ArrayList<Integer> contourCounts, ProjectionFace face,
                             Point3dPolygon[] projectionPolygons, double depth)
    {
        for (Point3dPolygon projectionPolygon : projectionPolygons)
        {

            // add polygon
            for (Point3d point : projectionPolygon.getExterior())
            {
                point = new Point3d(point);
                switch (face)
                {
                    case XY0:
                        point.z += depth;
                        break;
                    case XY1:
                        point.z -= depth;
                        break;
                    case ZY0:
                        point.x += depth;
                        break;
                    case ZY1:
                        point.x -= depth;
                        break;
                    case ZX0:
                        point.y += depth;
                        break;
                    case ZX1:
                        point.y -= depth;
                        break;
                }

                coordinates.add(point);
            }
            stripCounts.add(projectionPolygon.getExterior().length);

            // add holes
            for (Point3d[] hole : projectionPolygon.getHoles())
            {
                for (Point3d point : hole)
                {
                    point = new Point3d(point);
                    switch (face)
                    {
                        case XY0:
                            point.z += depth;
                            break;
                        case XY1:
                            point.z -= depth;
                            break;
                        case ZY0:
                            point.x += depth;
                            break;
                        case ZY1:
                            point.x -= depth;
                            break;
                        case ZX0:
                            point.y += depth;
                            break;
                        case ZX1:
                            point.y -= depth;
                            break;
                    }
                    

                    coordinates.add(point);
                }
                stripCounts.add(hole.length);
            }

            // one contour from polygon, the rest from holes
            contourCounts.add(1 + projectionPolygon.getHoles().length);
        }
    }

    /**
     * Given a list of polygons projected onto a face of a cube, add rectangles that connect
     * each side of each polygon to the side of that polygon projected to its appropriate depth
     *
     * @param coordinates list of coordinates of the mould to add to
     * @param stripCounts list of strips of the mould to add to
     * @param contourCounts list of contours of the mould to add to
     * @param face the face on which polygons are projected
     * @param polygons the projected polygons
     * @param depth the depth that the side rectangles should reach
     */
    public static void addSideRectangles(ArrayList<Point3d> coordinates, ArrayList<Integer> stripCounts,
                                   ArrayList<Integer> contourCounts, ProjectionFace face,
                                   Point3d[][] polygons, double depth)
    {
        for (Point3d[] polygon : polygons)
        {
            Point3d[][] sideRectangles = new Point3d[polygon.length][];
            for (int i = 0; i < polygon.length - 1; i++)
            {
                sideRectangles[i] = new Point3d[4];

                Point3d projectionFaceCoord1 = new Point3d(polygon[i]);
                Point3d projectionFaceCoord2 = new Point3d(polygon[i + 1]);

                intrudeCoordinate(projectionFaceCoord1, face, depth);
                intrudeCoordinate(projectionFaceCoord2, face, depth);

                // reverse winding order for non- XY0 or XY1 projection
                if (face == ProjectionFace.XY0 || face == ProjectionFace.XY1)
                {
                    sideRectangles[i][0] = polygon[i];
                    sideRectangles[i][1] = projectionFaceCoord1;
                    sideRectangles[i][2] = projectionFaceCoord2;
                    sideRectangles[i][3] = polygon[i + 1];
                }
                else
                {
                    sideRectangles[i][3] = polygon[i];
                    sideRectangles[i][2] = projectionFaceCoord1;
                    sideRectangles[i][1] = projectionFaceCoord2;
                    sideRectangles[i][0] = polygon[i + 1];
                }
            }

            // create the last rectangle which contains the edge between the first and last points
            sideRectangles[sideRectangles.length - 1] = new Point3d[4];

            Point3d projectionFaceCoord1 = new Point3d(polygon[polygon.length - 1]);
            Point3d projectionFaceCoord2 = new Point3d(polygon[0]);

            intrudeCoordinate(projectionFaceCoord1, face, depth);
            intrudeCoordinate(projectionFaceCoord2, face, depth);

            // reverse winding order for non- XY0 or XY1 projection
            if (face == ProjectionFace.XY0 || face == ProjectionFace.XY1)
            {
                sideRectangles[sideRectangles.length - 1][0] = polygon[polygon.length - 1];
                sideRectangles[sideRectangles.length - 1][1] = projectionFaceCoord1;
                sideRectangles[sideRectangles.length - 1][2] = projectionFaceCoord2;
                sideRectangles[sideRectangles.length - 1][3] = polygon[0];
            }
            else
            {
                sideRectangles[sideRectangles.length - 1][3] = polygon[polygon.length - 1];
                sideRectangles[sideRectangles.length - 1][2] = projectionFaceCoord1;
                sideRectangles[sideRectangles.length - 1][1] = projectionFaceCoord2;
                sideRectangles[sideRectangles.length - 1][0] = polygon[0];
            }

            // add all of the created polygons to our mesh
            for (Point3d[] rectangle : sideRectangles)
            {
                Collections.addAll(coordinates, rectangle);
                stripCounts.add(rectangle.length);
                contourCounts.add(1);
            }
        }
    }

    /**
     * Given a projected coordinate and what face it is on, intrude it to depth
     *
     * @param coord the projected coordinate
     * @param face the face on which coord lies
     * @param depth the depth of intrusion
     */
    public static void intrudeCoordinate(Point3d coord, ProjectionFace face, double depth)
    {
        switch (face)
        {
            case XY0:
                coord.z += depth;
                break;
            case XY1:
                coord.z -= depth;
                break;
            case ZY0:
                coord.x += depth;
                break;
            case ZY1:
                coord.x -= depth;
                break;
            case ZX0:
                coord.y += depth;
                break;
            case ZX1:
                coord.y -= depth;
                break;
        }
    }

    /**
     * Given a mesh of Point3ds representing triangles, print an obj file representing this mesh
     *
     * @param pts the mesh
     * @param file the file to print output to
     * @param scale factor by which to scale points
     */
    public static void generateObjFile(Point3d[] pts, File file, double scale)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            if (pts.length % 3 != 0)
            {
                throw new IllegalArgumentException("generateObjFile: number of vertices not a multiple of 3");
            }

            // print vertices
            for (Point3d pt : pts)
            {
                bw.write("v " + pt.x * scale + " " + pt.y * scale + " " + pt.z * scale);
                bw.newLine();
            }

            // print faces
            for (int i = 1; i < pts.length + 1; i += 3)
            {
                bw.write("f " + i + " " + (i + 1) + " " + (i + 2));
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.err.println("generateObjFile: couldn't write to file");
            e.printStackTrace();
        }
    }

    /**
     * Given an array of Point3dPolygons, return a new array with the winding orders of the polygons reversed.
     *
     * @param combinedPolygons - an array of Point3dPolygons
     * @return an array of Point3dPolygons with the winding orders of the polygons reversed
     */
    public static Point3dPolygon[] reverseWindingOrder(Point3dPolygon[] combinedPolygons) {
        Point3dPolygon[] ret = new Point3dPolygon[combinedPolygons.length];
        for (int i = 0; i < combinedPolygons.length; i++) {
            Point3dPolygon combinedPolygon = combinedPolygons[i];
            Point3d[] polygon = combinedPolygon.getExterior();
            Point3d[] reverseWind = new Point3d[polygon.length];
            for (int j = 0; j < polygon.length; j++) {
                reverseWind[j] = polygon[polygon.length - (j + 1)];
            }

            Point3d[][] holes = combinedPolygon.getHoles();
            Point3d[][] reverseWindHoles = new Point3d[holes.length][];
            for (int j = 0; j < holes.length; j++) {
                Point3d[] hole = holes[j];
                Point3d[] reverseWindHole = new Point3d[hole.length];
                for (int k = 0; k < hole.length; k++) {
                    reverseWindHole[k] = hole[hole.length - (k + 1)];
                }
                reverseWindHoles[j] = reverseWindHole;
            }
            ret[i] = new Point3dPolygon(reverseWind, combinedPolygon.getHoles());//reverseWindHoles);
        }

        return ret;
    }
}
