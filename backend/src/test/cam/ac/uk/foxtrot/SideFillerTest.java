package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.sidefiller.Point;
import cam.ac.uk.foxtrot.sidefiller.SideFiller;
import cam.ac.uk.foxtrot.voxelisation.*;
import org.junit.Test;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Milos on 27/02/2016.
 */
public class SideFillerTest
{
    public void testHoleAndRectangle()
    {
        Block[][][] blocks = new Block[1][1][1];
        blocks[0][0][0] = new Block(new Point3d(0,0,0), true);
        Block block = blocks[0][0][0];



        SideFiller filler = new SideFiller(blocks);
        filler.fillSingleSide(0,0,0,2, true); // fill the top side of z
        filler.drawTrianglesFromBlocks("testing/output/filler_test_hole_and_rectangle.obj", false);
    }
    @Test
    public void testALL()
    {
        Block[][][] blocks = new Block[1][1][1];
        blocks[0][0][0] = new Block(new Point3d(0,0,0), true);
        Block block = blocks[0][0][0];




        SideFiller filler = new SideFiller(blocks);
        filler.drawTrianglesFromBlocks("testing/output/filler_test_all.obj", false);
    }

    private ArrayList<Point3d> createRectangle(Point2d A, Point2d B, int ignore, int h)
    {
        ArrayList<Point3d> rectangle = new ArrayList<>();

        return rectangle;
    }
}
