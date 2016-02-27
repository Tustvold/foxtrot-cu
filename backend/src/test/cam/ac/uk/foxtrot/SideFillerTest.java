package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.sidefiller.SideFiller;
import cam.ac.uk.foxtrot.voxelisation.*;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.io.File;
import java.util.Random;

/**
 * Created by Milos on 27/02/2016.
 */
public class SideFillerTest
{
    @Test
    public void testALL()
    {
        Block[][][] blocks = new Block[1][1][1];
        blocks[0][0][0] = new Block(new Point3d(0,0,0), true);
        Block block = blocks[0][0][0];


        SideFiller filler = new SideFiller(blocks);
        filler.drawTrianglesFromBlocks("testing/output/mesh_sides_filled_test.obj", false);
    }
}
