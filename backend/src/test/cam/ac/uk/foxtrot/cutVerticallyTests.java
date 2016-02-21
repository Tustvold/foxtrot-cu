package cam.ac.uk.foxtrot;

import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.ArrayList;

import static cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser.cutVertically;

/**
 * Created by robbowman on 21/02/2016.
 */
public class cutVerticallyTests {
    //int cutVertically(Point3d R0, ArrayList<Point3d> T)

    int A, B;
    Point3d R, X, Y, Z;
    ArrayList<Point3d> T;

    @Test
    public void Is_First_Vertex(){
        A = 2;
        R = new Point3d(0,0,0);
        X = new Point3d(0,0,0);
        Y = new Point3d(1,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Is_xy_First_Vertex(){
        A = 2;
        R = new Point3d(0,0,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(1,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Is_Second_Vertex(){
        A = 3;
        R = new Point3d(1,0,0);
        X = new Point3d(0,0,0);
        Y = new Point3d(1,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Is_xy_Second_Vertex(){
        A = 3;
        R = new Point3d(1,0,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(1,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Is_Third_Vertex(){
        A = 4;
        R = new Point3d(0,1,0);
        X = new Point3d(0,0,0);
        Y = new Point3d(1,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Is_xy_Third_Vertex(){
        A = 4;
        R = new Point3d(0,1,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(1,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Between_0and1_Not_Vertical(){
        A = 5;
        R = new Point3d(1,0,0);
        X = new Point3d(0,0,0);
        Y = new Point3d(2,0,0);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Between_0and1_Vertical(){
        A = 2;
        R = new Point3d(0,0,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(0,0,2);
        Z = new Point3d(0,1,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Between_1and2_Not_Vertical(){
        A = 6;
        R = new Point3d(1,0,0);
        X = new Point3d(0,1,0);
        Y = new Point3d(0,0,0);
        Z = new Point3d(2,0,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Between_1and2_Vertical(){
        A = 3;
        R = new Point3d(0,0,1);
        X = new Point3d(0,1,0);
        Y = new Point3d(0,0,0);
        Z = new Point3d(0,0,2);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Between_0and2_Not_Vertical(){
        A = 7;
        R = new Point3d(1,0,0);
        X = new Point3d(0,0,0);
        Y = new Point3d(0,1,0);
        Z = new Point3d(2,0,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Between_0and2_Vertical(){
        A = 2;
        R = new Point3d(0,0,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(0,1,0);
        Z = new Point3d(0,0,2);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void In_Triangle(){
        A = 1;
        R = new Point3d(1,1,0);
        X = new Point3d(0,0,0);
        Y = new Point3d(0,3,0);
        Z = new Point3d(3,0,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void In_Triangle_different_z(){
        A = 1;
        R = new Point3d(1,1,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(0,3,0);
        Z = new Point3d(3,0,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

    @Test
    public void Outside_Triangle(){
        A = 0;
        R = new Point3d(-1,-1,1);
        X = new Point3d(0,0,0);
        Y = new Point3d(0,2,0);
        Z = new Point3d(2,0,0);
        T = new ArrayList<>();
        T.add(X); T.add(Y); T.add(Z);
        B = cutVertically(R, T);
        Assert.assertEquals(A, B);
    }

}
