package cam.ac.uk.foxtrot.voxelisation;


import java.io.IOException;

public abstract class MeshIO {
    /**
     *
     * Read the mesh from the file at path filename
     *
     * Will throw an exception if the file doesn't exist
     *
     * @param filename the path to the file without its file-type suffix
     * @return the loaded mesh
     * @throws IOException
     */
    public abstract Mesh readFromFile(String filename) throws IOException;

    /**
     *
     * Write the provided mesh to a file at the provided filename
     *
     * Will throw an exception if a file already exists at that location
     *
     * @param filename the path to the file without its file-type suffix
     * @param mesh the mesh to write
     * @throws IOException
     */
    public abstract void writeToFile(String filename, Mesh mesh) throws IOException;
}
