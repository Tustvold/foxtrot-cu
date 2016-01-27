package cam.ac.uk.foxtrot.voxelisation;

import java.io.IOException;

public abstract class MeshVoxeliser {
    /**
     *
     * Generate a grid of blocks given a particular mesh
     *
     * Elements in the grid with no mesh will be null
     *
     * @param mesh the mesh to voxelise
     * @return the grid of blocks
     */
    public Block[][][] voxeliseMesh(Mesh mesh) {
        Block[][][] blocks = generateBlocks(mesh);
        generateCustomParts(blocks);
        return blocks;
    }


    /**
     *
     * Generate blocks for the given mesh
     *
     * Elements in the grid with no mesh will be null
     *
     * @param mesh the mesh to generate blocks from
     * @return Grid of blocks in order x,y,z
     */
    abstract Block[][][] generateBlocks(Mesh mesh);


    /**
     *
     * Generate custom parts for Blocks which need it
     *
     * @param blocks Grid of blocks to generate custom parts for
     */
    abstract void generateCustomParts(Block[][][] blocks);

    /**
     *
     * Write all the custom parts to the provided directory using the provided MeshIO
     *
     * @param directory the directory to write the files to
     * @param meshIO the MeshIO object to use
     */
    void writeCustomPartsToDirectory(String directory, MeshIO meshIO) throws IOException {
        throw new RuntimeException("Not Implemented");
    }
}
