package cam.ac.uk.foxtrot.sidefiller;

import cam.ac.uk.foxtrot.voxelisation.Block;

/**
 * Created by Milos on 25/02/2016.
 */
public class SideFiller
{
    Block[][][] blocks; // the block matrix whose blocks need side filling
    int dim[];          // dimensions of the input block matrix

    /**
     * @return Returns
     */
    public Block[][][] getBlocks()
    {
        return blocks;
    }

    public SideFiller(Block[][][] blocks)
    {
        this.blocks = blocks;
        dim = new int[3];
        dim[0] = blocks.length;
        dim[1] = blocks[0].length;
        dim[2] = blocks[0][0].length;
    }
}