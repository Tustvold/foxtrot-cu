package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Vector3f;
import java.util.List;

public class Mesh {
    // Anti-Clockwise winding order
    private List<Vector3f> vertices;

    /**
     *
     * Returns the mesh's list of vertices
     * These are triangle primitives with anti-clockwise winding order
     *
     * @return list of vertices
     */
    List<Vector3f> getVertices() {
        return vertices;
    }

}
