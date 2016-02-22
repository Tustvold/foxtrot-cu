package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.vecmath.Point3d;

public class CustomPart {

    @SerializedName("triangle_array")
    private Point3d[] triangles;

    public Point3d[] getTriangles() {
        return triangles;
    }

    public CustomPart(Point3d[] inTriangles) {
        triangles = new Point3d[inTriangles.length];
        System.arraycopy(inTriangles, 0, triangles, 0, triangles.length);
    }
}
