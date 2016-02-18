package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.CustomPartMouldGenerator;
import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Point3d;

public class MainTest {

    @Test
    public void testMain() throws Exception {
        Assert.assertTrue(true);
    }

    private boolean floatCompare(float a, float b, float epsilon) {
        return Math.abs(a-b) <= epsilon;
    }

    @Test
    public void testProjection() throws Exception {
        /*float EPSILON = .00000001f;

        float X_SET = 5.6f;
        float Y_SET = -6.2f;
        float Z_SET = 0.02f;

        
        CustomPartMouldGenerator p = new CustomPartMouldGenerator(ta);
        Point3d[] xproj = p.setX(X_SET);
        Point3d[] yproj = p.setY(Y_SET);
        Point3d[] zproj = p.setZ(Z_SET);

        for (int i = 0; i < NUM_PTS; i++) {
            //System.out.println("     x       y      z ");
            //System.out.println("xs:  " + xproj[i].x + " " + yproj[i].x + " " + zproj[i].x);
            //System.out.println("ys:  " + xproj[i].y + " " + yproj[i].y + " " + zproj[i].y);
            //System.out.println("zs:  " + xproj[i].z + " " + yproj[i].z + " " + zproj[i].z);
            Assert.assertTrue(floatCompare(xproj[i].x, X_SET, EPSILON));
            Assert.assertTrue(floatCompare(yproj[i].y, Y_SET, EPSILON));
            Assert.assertTrue(floatCompare(zproj[i].z, Z_SET, EPSILON));
            Assert.assertTrue(floatCompare(xproj[i].y, zproj[i].y, EPSILON));
            Assert.assertTrue(floatCompare(xproj[i].z, yproj[i].z, EPSILON));
            Assert.assertTrue(floatCompare(yproj[i].x, zproj[i].x, EPSILON));
        }*/

        int NUM_PTS = 9;
        Point3d[] points = new Point3d[NUM_PTS];
       /* float[] point = new float[3];
        for (int i = 0; i < NUM_PTS; i++) {
            point[0] = (float)(Math.random());
            point[1] = (float)(Math.random());
            point[2] = (float)(Math.random());
            points[i] = new Point3d(point);
        }*/

        //points[0] = new Point3d(.25f,.25f,.25f);
        //points[1] = new Point3d(.5f,.5f,.25f);
        //points[2] = new Point3d(.25f,.25f,.5f);

        points[0] = new Point3d(0, 0, 0);
        points[1] = new Point3d(.66f, 0, 0);
        points[2] = new Point3d(.4f, .7f, 0);

        points[3] = new Point3d(.33f, 0, 0);
        points[4] = new Point3d(1, 0, 0);
        points[5] = new Point3d(.6f, .7f, 0);

        points[6] = new Point3d(.2f, .5f, 0);
        points[7] = new Point3d(.8f, .5f, 0);
        points[8] = new Point3d(.5f, 1, 0);

        CustomPartMouldGenerator p = new CustomPartMouldGenerator(points);
        p.generateMould(CustomPartMouldGenerator.ProjectionFace.XY1);
        System.out.println("\n\n-----\n\n");
        p.generateCustomPart(CustomPartMouldGenerator.ProjectionFace.XY1);






    }


}