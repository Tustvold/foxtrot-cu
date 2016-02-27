package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.*;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.io.File;
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
    public void testRandom() {
        Block block = new Block(new Point3d(0,0,0), true);
        int NUM_PTS = 9;
        Point3d[] points = new Point3d[NUM_PTS];
        double[] point = new double[3];
        for (int i = 0; i < NUM_PTS; i++) {
            point[0] = Math.random();
            point[1] = Math.random();
            point[2] = Math.random();
            points[i] = new Point3d(point);
            if(i % 3 == 2)
                block.addTriangle(points[i-2],points[i-1],points[i]);
        }
        double[] dim = {0,0,0,1,1,1};
        block.modifInternalDim(dim);

        Random r = new Random();
        int face = r.nextInt()%3;
        CustomPartGenerator pg = new CustomPartGenerator(block);
        CustomPart p = pg.generateCustomPart(face);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("testing/output/", "randPart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35, dim);
        m.generateMould(face, new File("testing/output/","randMould.obj"));
    }

    @Test
    public void testEmpty() {
        Block block = new Block(new Point3d(0,0,0), true);
        Point3d[] points = new Point3d[0];
        double[] dim = {0,0,0,1,1,1};
        block.modifInternalDim(dim);

        Random r = new Random();
        int face = r.nextInt()%3;
        CustomPartGenerator pg = new CustomPartGenerator(block);
        CustomPart p = pg.generateCustomPart(face);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("testing/output/", "emptyPart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35, dim);
        m.generateMould(face, new File("testing/output/","emptyMould.obj"));
    }

}