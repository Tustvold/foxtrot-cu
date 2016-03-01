package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.sidefiller.SideFiller;
import cam.ac.uk.foxtrot.voxelisation.*;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class CustomPartTest {

    @Test
    public void testSingleTriangle() {
        Block block = new Block(new Point3d(0,0,0), true);
        Point3d[] points = new Point3d[3];
        points[0] = new Point3d(.25f,.25f,.25f);
        points[1] = new Point3d(.5f,.5f,.25f);
        points[2] = new Point3d(.25f,.25f,.5f);
        block.addTriangle(points[0],points[1],points[2]);
        double[] dim = {0,0,0,1,1,1};
        block.modifInternalDim(dim);

        CustomPartGenerator pg = new CustomPartGenerator(block);
        CustomPart p = pg.generateCustomPart(0);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("testing/output/", "singleTriPart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35, dim);
        m.generateMould(0, new File("testing/output/","singleTriMould.obj"));
    }

    @Test
    public void testHole() {
        Block block = new Block(new Point3d(0,0,0), true);
        Point3d[] points = new Point3d[9];

        points[0] = new Point3d(0, 0, 0);
        points[1] = new Point3d(.66f, 0, 0);
        points[2] = new Point3d(.4f, .7f, 0);

        points[3] = new Point3d(.33f, 0, 0);
        points[4] = new Point3d(1, 0, 0);
        points[5] = new Point3d(.6f, .7f, 0);

        points[6] = new Point3d(.2f, .5f, 0);
        points[7] = new Point3d(.8f, .5f, 0);
        points[8] = new Point3d(.5f, 1, 0);
        block.addTriangle(points[0],points[1],points[2]);
        block.addTriangle(points[3],points[4],points[5]);
        block.addTriangle(points[6],points[7],points[8]);
        double[] dim = {0,0,0,1,1,1};
        block.modifInternalDim(dim);

        CustomPartGenerator pg = new CustomPartGenerator(block);
        CustomPart p = pg.generateCustomPart(2);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("testing/output/", "holePart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35, dim);
        m.generateMould(2, new File("testing/output/","holeMould.obj"));
    }

    @Test
    public void testOdd()
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
        filler.fillAllSides(); // fill the top and bottom side of z
        DrawingUtilities.drawBlocks(blocks,"testing/output/custom_part_test_odd_before.obj", false, 0.0, 0);
        block = blocks[0][0][0];
        block.setInternalDim();

        Point3d[] al = block.getTriangles().toArray(new Point3d[block.getTriangles().size()]);
        CustomPartGenerator pg = new CustomPartGenerator(block);
        CustomPart p0 = pg.generateCustomPart(0);
        CustomPart p1 = pg.generateCustomPart(1);
        CustomPart p2 = pg.generateCustomPart(2);
        ProjectionUtils.generateObjFile(p0.getTriangles(), new File("testing/output/", "custom_part_test_odd_after_part_0.obj"), 1);
        ProjectionUtils.generateObjFile(p1.getTriangles(), new File("testing/output/", "custom_part_test_odd_after_part_1.obj"), 1);
        ProjectionUtils.generateObjFile(p2.getTriangles(), new File("testing/output/", "custom_part_test_odd_after_part_2.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(al, 35, block.getInternalDim());
        m.generateMould(0, new File("testing/output/","custom_part_test_odd_after_mould_0.obj"));
        m.generateMould(1, new File("testing/output/","custom_part_test_odd_after_mould_1.obj"));
        m.generateMould(2, new File("testing/output/","custom_part_test_odd_after_mould_2.obj"));
    }

    @Test
    public void testFaces()
    {
        Block[][][] blocks = new Block[1][1][1];
        blocks[0][0][0] = new Block(new Point3d(0, 0, 0), true);
        Block block = blocks[0][0][0];
        int ignore = 1;
        int h = 1;
        boolean top = true;
        ArrayList<Point3d> points = new ArrayList<>();

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
        points.addAll(createRectangle(0.7, 0.5, 0.6, 0.5, ignore, h));
        points.addAll(createRectangle(0.6, 0.5, 0.7, 0.4, ignore, h));
        points.addAll(createRectangle(0.7, 0.4, 0.7, 0.5, ignore, h));

        points.addAll(createRectangle(0.8, 0.5, 0.8, 0.4, ignore, h));
        points.addAll(createRectangle(0.8, 0.4, 0.9, 0.4, ignore, h));
        points.addAll(createRectangle(0.9, 0.4, 0.9, 0.5, ignore, h));
        points.addAll(createRectangle(0.9, 0.5, 0.8, 0.5, ignore, h));

        block.addTriangles(points);
        SideFiller filler = new SideFiller(blocks);
        filler.fillAllSides(); // fill the top and bottom side of z
        DrawingUtilities.drawBlocks(blocks,"testing/output/custom_part_test_faces_before.obj", false, 0.0, 0);
        block = blocks[0][0][0];
        block.setInternalDim();

        Point3d[] al = block.getTriangles().toArray(new Point3d[block.getTriangles().size()]);
        CustomPartGenerator pg = new CustomPartGenerator(block);
        CustomPart p0 = pg.generateCustomPart(0);
        CustomPart p1 = pg.generateCustomPart(1);
        CustomPart p2 = pg.generateCustomPart(2);
        ProjectionUtils.generateObjFile(p0.getTriangles(), new File("testing/output/", "custom_part_test_faces_part_0.obj"), 1);
        ProjectionUtils.generateObjFile(p1.getTriangles(), new File("testing/output/", "custom_part_test_faces_part_1.obj"), 1);
        ProjectionUtils.generateObjFile(p2.getTriangles(), new File("testing/output/", "custom_part_test_faces_part_2.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(al, 35, block.getInternalDim());
        m.generateMould(0, new File("testing/output/","custom_part_test_faces_mould_0.obj"));
        m.generateMould(1, new File("testing/output/","custom_part_test_faces_mould_1.obj"));
        m.generateMould(2, new File("testing/output/","custom_part_test_faces_mould_2.obj"));
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