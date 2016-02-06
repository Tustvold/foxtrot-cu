package cam.ac.uk.foxtrot.voxelisation;

import java.io.IOException;

public class CustomPartMould {

    private Mesh mesh;
    private String name;

    /**
     *
     * Generates a custom mould for the provided Block
     *
     * @param block the block to generate the mesh from
     * @param name the name of the custom part
     */
    CustomPartMould(Block block, String name) {
        this.name = name;
        throw new RuntimeException("Not Implemented");
    }


    /**
     * @return the mould's mesh
     */
    public Mesh getMesh() {
        return mesh;
    }

    /**
     * @return the custom part's name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * Write the mould for this mesh to the provided directory using the provided MeshIO object
     *
     * @param directory the directory to write the file in
     * @param meshIO the meshIO object to use to write
     */
    /*
    public void writeToDirectory(String directory, MeshIO meshIO) throws IOException {
        meshIO.writeToFile(directory + name, mesh);
    }*/
}
