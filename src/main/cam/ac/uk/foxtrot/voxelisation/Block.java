package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Matrix4f;

public class Block {

    private Matrix4f transform;

    private Mesh mesh;

    private CustomPartMould custom_mould;

    /**
     *
     * Constructs a new Block with the provided mesh and transform
     *
     * @param mesh the exact mesh
     * @param transform the transform of this Block
     */
    public Block(Mesh mesh, Matrix4f transform) {
        this.mesh = mesh;
        this.transform = transform;
    }

    /**
     * @return True if Block has an associated custom mould
     */
    public Boolean usesCustomPart() {
        return custom_mould != null;
    }

    /**
     * @return the mesh of this block (NB this is the exact mesh not the approximated one)
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * @return the custom_mould associated with this part if any. Returns null if no custom mould
     */
    public CustomPartMould getCustomMould() {
        return custom_mould;
    }

    /**
     * Generates a custom mould for this Block
     */
    public void generateCustomMould(String name) {
        custom_mould = new CustomPartMould(this, name);
    }

    /**
     * @return the transform for this block
     */
    public Matrix4f getTransform() {
        return transform;
    }

}
