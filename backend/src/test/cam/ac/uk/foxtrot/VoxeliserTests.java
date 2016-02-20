package cam.ac.uk.foxtrot;


import cam.ac.uk.foxtrot.voxelisation.MeshVoxeliser;
import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.ArrayList;

/**
 * Created by robbowman on 19/02/2016.
 */
public class VoxeliserTests {

    Point3d F, S, T;
    Point3d R = new Point3d();
    double L;
    int I = 2;
    boolean val;

    // private boolean intersect(Point3d fir, Point3d sec, double line, int ignore, Point3d res)
    @Test
    public void Simple_Intersection(){
        //Basic intersection case
        F = new Point3d(0, 0, 0);
        S = new Point3d(2, 0, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(1, 0, 0);
        L = 1;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, true);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Simple_Non_Intersection(){
        //Basic not intersection case
        F = new Point3d(0, 0, 0);
        S = new Point3d(1, 0, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(0, 0, 0);
        L = 2;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, false);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Through_F_Intersection(){
        //Intersect through 1st vertex
        F = new Point3d(1, 0, 0);
        S = new Point3d(2, 0, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(1, -1, 0);
        L = 1;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, false);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Through_S_Intersection(){
        //Intersect through 2nd Vertex
        F = new Point3d(1, 0, 0);
        S = new Point3d(2, 0, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(1, 1, 0);
        L = 2;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, false);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Parallel_Intersection(){
        //In plane
        F = new Point3d(1, 0, 0);
        S = new Point3d(1, 2, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(0, 0, 0);
        L = 1;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, false);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Parallel_Non_Intersection(){
        //Parallel but not in plane
        F = new Point3d(1, 0, 0);
        S = new Point3d(1, 2, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(0, 0, 0);
        L = 2;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, false);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Diagonal_Intersection(){
        //Diagonal Intersection
        F = new Point3d(0, 0, 0);
        S = new Point3d(2, 2, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(1, 1, 0);
        L = 1;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, true);
        Assert.assertEquals(R,T);
    }

    @Test
    public void Diagonal_Non_Intersection(){
        //Diagonal Intersection
        F = new Point3d(0, 0, 0);
        S = new Point3d(2, 2, 0);
        R = new Point3d(0, 0, 0);
        T = new Point3d(0, 0, 0);
        L = 3;
        val = MeshVoxeliser.intersect(F, S, L, I, R);
        Assert.assertEquals(val, false);
        Assert.assertEquals(R,T);
    }




}

