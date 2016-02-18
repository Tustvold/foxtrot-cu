package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.vecmath.Point3f;

public class CustomPart {

    @SerializedName("triangle_array")
    private Point3f[] triangles;

    public CustomPart(Point3f[] inTriangles) {
        triangles = new Point3f[inTriangles.length];
        System.arraycopy(inTriangles, 0, triangles, 0, inTriangles.length);
    }
}
