package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.sidefiller.SideFiller;
import cam.ac.uk.foxtrot.voxelisation.Block;
import org.junit.Test;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.util.ArrayList;


public class SideFillerTest
{
    @Test
    public void testHoleAndRectangle()
    {
        Block[][][] blocks = new Block[1][1][1];
        Block block = new Block(new Point3d(0, 0, 0), true);
        int ignore = 2;
        int h = 1;
        boolean top = true;
        ArrayList<Point3d> points = new ArrayList<>();

        // add the triangles
        points.addAll(createTriangle(
                0.2, 0.6,
                0.4, 0.4,
                0.4, 0.6, ignore, h));
        points.addAll(createTriangle(
                0.2, 0.6,
                0.2, 0.4,
                0.4, 0.4, ignore, h));

        // and add the rectangles
        points.addAll(createRectangle(0.2, 0.6, 0.4, 0.6, ignore, h));
        points.addAll(createRectangle(0.4, 0.6, 0.8, 0.6, ignore, h));
        points.addAll(createRectangle(0.8, 0.6, 0.8, 0.4, ignore, h));
        points.addAll(createRectangle(0.8, 0.4, 0.4, 0.4, ignore, h));
        points.addAll(createRectangle(0.4, 0.4, 0.2, 0.4, ignore, h));
        points.addAll(createRectangle(0.2, 0.4, 0.2, 0.6, ignore, h));

        block.addTriangles(points);
        blocks[0][0][0] = block;
        SideFiller filler = new SideFiller(blocks);
        filler.drawTrianglesFromBlocks("testing/output/filler_test_hole_and_rectangle_before.obj", false);
        filler.fillAllSides(); // fill the top and bottom side of z
        filler.drawTrianglesFromBlocks("testing/output/filler_test_hole_and_rectangle_after.obj", false);
    }

    @Test
    public void testALL()
    {
        Block[][][] blocks = new Block[1][1][1];
        blocks[0][0][0] = new Block(new Point3d(0, 0, 0), true);
        Block block = blocks[0][0][0];
        int ignore = 2;
        int h = 1;
        boolean top = true;
        ArrayList<Point3d> points = new ArrayList<>();

        // add the side square
        points.addAll(createRectangle(0.0, 0.0, 0.0, 1.0, ignore, h));

        // add left line
        points.addAll(createRectangle(0.2, 1.0, 0.1, 0.9, ignore, h));
        points.addAll(createRectangle(0.1, 0.9, 0.2, 0.8, ignore, h));
        points.addAll(createRectangle(0.2, 0.8, 0.1, 0.2, ignore, h));
        points.addAll(createRectangle(0.1, 0.2, 0.1, 0.1, ignore, h));
        points.addAll(createRectangle(0.1, 0.1, 0.2, 0.0, ignore, h));

        // add bottom line
        points.addAll(createRectangle(0.2, 0.0, 0.3, 0.1, ignore, h));
        points.addAll(createRectangle(0.3, 0.1, 0.7, 0.0, ignore, h));

        // add two triangles
        points.addAll(createTriangle(0.2, 0.0, 0.3, 0.0, 0.3, 0.1, ignore, h));
        points.addAll(createTriangle(0.3, 0.1, 0.3, 0.0, 0.7, 0.0, ignore, h));

        // add top line
        points.addAll(createRectangle(1.0, 0.9, 0.8, 0.8, ignore, h));
        points.addAll(createRectangle(0.8, 0.8, 0.7, 0.9, ignore, h));
        points.addAll(createRectangle(0.7, 0.9, 0.5, 0.9, ignore, h));
        points.addAll(createRectangle(0.5, 0.9, 0.4, 1.0, ignore, h));

        // add outermost ring
        points.addAll(createRectangle(1.0, 0.2, 0.6, 0.1, ignore, h));
        points.addAll(createRectangle(0.6, 0.1, 0.2, 0.3, ignore, h));
        points.addAll(createRectangle(0.2, 0.3, 0.3, 0.9, ignore, h));
        points.addAll(createRectangle(0.3, 0.9, 0.4, 0.8, ignore, h));
        points.addAll(createRectangle(0.4, 0.8, 1.0, 0.7, ignore, h));

        // add first inner ring
        points.addAll(createRectangle(1.0, 0.6, 0.3, 0.7, ignore, h));
        points.addAll(createRectangle(0.3, 0.7, 0.3, 0.4, ignore, h));
        points.addAll(createRectangle(0.3, 0.4, 0.6, 0.2, ignore, h));
        points.addAll(createRectangle(0.6, 0.2, 1.0, 0.3, ignore, h));

        // add inner triangle and its inner line
        points.addAll(createTriangle(0.4, 0.6, 0.4, 0.5, 0.5, 0.3, ignore, h));
        points.addAll(createRectangle(0.4, 0.6, 0.5, 0.3, ignore, h));
        points.addAll(createRectangle(0.5, 0.3, 0.4, 0.5, ignore, h));
        points.addAll(createRectangle(0.4, 0.5, 0.4, 0.6, ignore, h));

        // add second nested polygon
        points.addAll(createRectangle(1.0, 0.4, 0.6, 0.3, ignore, h));
        points.addAll(createRectangle(0.6, 0.3, 0.5, 0.4, ignore, h));
        points.addAll(createRectangle(0.5, 0.4, 0.5, 0.6, ignore, h));
        points.addAll(createRectangle(0.5, 0.6, 0.9, 0.6, ignore, h));
        points.addAll(createRectangle(0.9, 0.6, 1.0, 0.4, ignore, h));

        // add the two double nested holes
        points.addAll(createRectangle(0.7, 0.5, 0.7, 0.4, ignore, h));
        points.addAll(createRectangle(0.7, 0.4, 0.6, 0.5, ignore, h));
        points.addAll(createRectangle(0.6, 0.5, 0.7, 0.5, ignore, h));

        points.addAll(createRectangle(0.8, 0.5, 0.9, 0.5, ignore, h));
        points.addAll(createRectangle(0.9, 0.5, 0.9, 0.4, ignore, h));
        points.addAll(createRectangle(0.9, 0.4, 0.8, 0.4, ignore, h));
        points.addAll(createRectangle(0.8, 0.4, 0.8, 0.5, ignore, h));

        block.addTriangles(points);
        SideFiller filler = new SideFiller(blocks);
        filler.drawTrianglesFromBlocks("testing/output/filler_test_all_before.obj", false);
        filler.fillAllSides(); // fill the top and bottom side of z
        filler.drawTrianglesFromBlocks("testing/output/filler_test_all_after.obj", false);
    }

    private ArrayList<Point3d> createTriangle(double Ax, double Ay, double Bx, double By, double Cx, double Cy, int ignore, int h)
    {
        ArrayList<Point3d> rectangle = new ArrayList<>();
        double[] coordA = new double[3];
        coordA[ignore] = h;
        coordA[(ignore + 1) % 3] = Ax;
        coordA[(ignore + 2) % 3] = Ay;
        Point3d A3d = new Point3d(coordA);

        double[] coordB = new double[3];
        coordB[ignore] = h;
        coordB[(ignore + 1) % 3] = Bx;
        coordB[(ignore + 2) % 3] = By;
        Point3d B3d = new Point3d(coordB);

        double[] coordC = new double[3];
        coordC[ignore] = h;
        coordC[(ignore + 1) % 3] = Cx;
        coordC[(ignore + 2) % 3] = Cy;
        Point3d C3d = new Point3d(coordC);

        // create the triangle
        rectangle.add(A3d);
        rectangle.add(B3d);
        rectangle.add(C3d);

        return rectangle;
    }

    private ArrayList<Point3d> createRectangle(double Ax, double Ay, double Bx, double By, int ignore, int h)
    {
        ArrayList<Point3d> rectangle = new ArrayList<>();
        double[] coordA = new double[3];
        coordA[ignore] = h;
        coordA[(ignore + 1) % 3] = Ax;
        coordA[(ignore + 2) % 3] = Ay;
        Point3d A3d = new Point3d(coordA);

        double[] coordB = new double[3];
        coordB[ignore] = h;
        coordB[(ignore + 1) % 3] = Bx;
        coordB[(ignore + 2) % 3] = By;
        Point3d B3d = new Point3d(coordB);

        double[] coordaboveA = new double[3];
        coordaboveA[ignore] = 1 - h;
        coordaboveA[(ignore + 1) % 3] = Ax;
        coordaboveA[(ignore + 2) % 3] = Ay;
        Point3d aboveA = new Point3d(coordaboveA);

        double[] coordaboveB = new double[3];
        coordaboveB[ignore] = 1 - h;
        coordaboveB[(ignore + 1) % 3] = Bx;
        coordaboveB[(ignore + 2) % 3] = By;
        Point3d aboveB = new Point3d(coordaboveB);

        // triangle on the side
        rectangle.add(A3d);
        rectangle.add(B3d);
        rectangle.add(aboveB);
        // other triangle
        rectangle.add(aboveA);
        rectangle.add(A3d);
        rectangle.add(aboveB);

        return rectangle;
    }
}
