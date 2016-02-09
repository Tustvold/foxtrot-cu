package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.Projection;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.Stripifier;
import org.junit.Assert;
import org.junit.Test;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;

import static org.junit.Assert.*;

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
        float EPSILON = .00000001f;
        int NUM_PTS = 9;
        float X_SET = 5.6f;
        float Y_SET = -6.2f;
        float Z_SET = 0.02f;
        Point3f[] points = new Point3f[NUM_PTS];
        float[] point = new float[3];
        for (int i = 0; i < NUM_PTS; i++) {
            point[0] = (float)((Math.random() - .5) * 10);
            point[1] = (float)((Math.random() - .5) * 10);
            point[2] = (float)((Math.random() - .5) * 10);
            points[i] = new Point3f(point);
        }

        TriangleArray ta = new TriangleArray(NUM_PTS, TriangleArray.COORDINATES);
        ta.setCoordinates(0, points);
        
        Projection p = new Projection(ta);
        Point3f[] xproj = p.setX(X_SET);
        Point3f[] yproj = p.setY(Y_SET);
        Point3f[] zproj = p.setZ(Z_SET);

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
        }

        GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        gi.setCoordinates(xproj);

        float[] pt = new float[3];
        GeometryArray result = gi.getGeometryArray();
        for (int i = 0; i <result.getVertexCount(); i++) {
            result.getCoordinate(i, pt);
            System.out.println("v " + pt[0] + " " + pt[1] + " " + pt[2]);
        }
        System.out.println("f 1 2 3");
        System.out.println("f 4 5 6");
        System.out.println("f 7 8 9");
        System.out.println("---- post strip---");


            Stripifier st = new Stripifier();
            st.stripify(gi);



        result = gi.getGeometryArray();

        for (int i = 0; i <result.getVertexCount(); i++) {
            result.getCoordinate(i, pt);
            System.out.println("v " + pt[0] + " " + pt[1] + " " + pt[2]);
        }
        System.out.println("f 1 2 3");
        System.out.println("f 4 5 6");
        System.out.println("f 7 8 9");
    }


}