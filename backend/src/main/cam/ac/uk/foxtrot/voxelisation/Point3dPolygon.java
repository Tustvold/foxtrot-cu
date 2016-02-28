package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Point3d;

public class Point3dPolygon {
    private Point3d[] exterior;
    private Point3d[][] holes;

    /**
     * Creates a Java3d compatible representation of a polygon
     * @param ext Point3d[] representation of exterior of polygon
     * @param hls Point3d[][] representation of holes
     */
    public Point3dPolygon(Point3d[] ext, Point3d[][] hls) {
        exterior = ext;
        holes = hls;
        if (holes == null) {
            holes = new Point3d[0][];
        }
    }

    /**
     * Getter for representation of exterior of polygon
     * @return array representing the exterior of the polygon
     */
    public Point3d[] getExterior() {
        return exterior;
    }

    /**
     * Getter for representation of holes in polygon
     * @return array of arrays each representing a hole in the polygon
     */
    public Point3d[][] getHoles() {
        return holes;
    }

}
