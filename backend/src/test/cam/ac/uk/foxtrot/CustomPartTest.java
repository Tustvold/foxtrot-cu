package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.CustomPart;
import cam.ac.uk.foxtrot.voxelisation.CustomPartGenerator;
import cam.ac.uk.foxtrot.voxelisation.CustomPartMouldGenerator;
import cam.ac.uk.foxtrot.voxelisation.ProjectionUtils;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.io.File;
import java.util.Random;

public class CustomPartTest {
    /*
    @Test
    public void testSingleTriangle() {
        Point3d[] points = new Point3d[3];
        points[0] = new Point3d(.25f,.25f,.25f);
        points[1] = new Point3d(.5f,.5f,.25f);
        points[2] = new Point3d(.25f,.25f,.5f);

        CustomPartGenerator pg = new CustomPartGenerator(points);
        CustomPart p = pg.generateCustomPart(ProjectionUtils.ProjectionFace.ZX1);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("/tmp", "singleTriPart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35);
        m.generateMould(ProjectionUtils.ProjectionFace.ZX1, new File("/tmp","singleTriMould.obj"));
    }

    @Test
    public void testHole() {
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

        CustomPartGenerator pg = new CustomPartGenerator(points);
        CustomPart p = pg.generateCustomPart(ProjectionUtils.ProjectionFace.XY1);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("/tmp", "holePart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35);
        m.generateMould(ProjectionUtils.ProjectionFace.XY1, new File("/tmp","holeMould.obj"));
    }

    @Test
    public void testRandom() {
        int NUM_PTS = 9;
        Point3d[] points = new Point3d[NUM_PTS];
        double[] point = new double[3];
        for (int i = 0; i < NUM_PTS; i++) {
            point[0] = Math.random();
            point[1] = Math.random();
            point[2] = Math.random();
            points[i] = new Point3d(point);
        }

        ProjectionUtils.ProjectionFace face = ProjectionUtils.ProjectionFace.values()[new Random().nextInt(ProjectionUtils.ProjectionFace.values().length)];
        CustomPartGenerator pg = new CustomPartGenerator(points);
        CustomPart p = pg.generateCustomPart(face);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("/tmp", "randPart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35);
        m.generateMould(face, new File("/tmp","randMould.obj"));
    }

    @Test
    public void testEmpty() {
        Point3d[] points = new Point3d[0];

        ProjectionUtils.ProjectionFace face = ProjectionUtils.ProjectionFace.values()[new Random().nextInt(ProjectionUtils.ProjectionFace.values().length)];
        CustomPartGenerator pg = new CustomPartGenerator(points);
        CustomPart p = pg.generateCustomPart(face);
        ProjectionUtils.generateObjFile(p.getTriangles(), new File("/tmp", "emptyPart.obj"), 1);
        CustomPartMouldGenerator m = new CustomPartMouldGenerator(points, 35);
        m.generateMould(face, new File("/tmp","emptyMould.obj"));
    }
    */
}