package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.IntersectionRemover;
import cam.ac.uk.foxtrot.voxelisation.ProjectionUtils;
import org.junit.Test;

import javax.vecmath.Point3d;

import static org.junit.Assert.*;


public class IntersectionRemoverTest {
    Point3d[] coordinates;
    Point3d[][] polygons;
    Point3d[][] holes;

    private boolean polygonsEquivalent(Point3d[] polygon1, Point3d[] polygon2) {
        boolean result = true;
        int length = polygon1.length;
        if (length != polygon2.length) {
            return false;
        }
        for (int i = 0; i < length; i++) { // try for different offsets of points
            for (int j = 0; j < length; j++) {
                if (!polygon1[j].equals(polygon2[(j+i)%length])){
                    result = false;
                    break;
                }
            } if (result == true) {
                return result;
            }
            // different winding order
            for (int j = 0; j < length; j++) {
                if (!polygon1[j].equals(polygon2[(length-i+j)%length])){
                    result = false;
                    break;
                }
            } if (result == true) {
                return result;
            } result = true;
        }
        return result;
    }
    @Test
    public void SingleXYTriangle() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,2,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(coordinates,polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void SingleXZTriangle() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,0,2)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.ZX0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(coordinates,polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void SingleYZTriangle() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(0,2,0), new Point3d(0,1,2)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.ZY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(coordinates,polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void NonIntersecting() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(0,2,0), new Point3d(0,1,2),
                                    new Point3d(0,0,-1), new Point3d(0,1,-3), new Point3d(0,2,-1)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.ZY1);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,2);
        assertTrue((polygonsEquivalent(new Point3d[]{coordinates[0],coordinates[1],coordinates[2]}, polygons[0]) && polygonsEquivalent(new Point3d[]{coordinates[3],coordinates[4],coordinates[5]}, polygons[1])) || (polygonsEquivalent(new Point3d[]{coordinates[0],coordinates[1],coordinates[2]}, polygons[1]) && polygonsEquivalent(new Point3d[]{coordinates[3],coordinates[4],coordinates[5]}, polygons[0])));
        assertEquals(holes.length,0);
    }
    @Test
    public void TouchingAtOnePoint() {
        coordinates = new Point3d[]{new Point3d(-1,2,0), new Point3d(0,0,0), new Point3d(1,2,0),
                                    new Point3d(-1,-2,0), new Point3d(1,-2,0),new Point3d(0,0,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY1);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,2);
        assertTrue((polygonsEquivalent(new Point3d[]{coordinates[0],coordinates[1],coordinates[2]}, polygons[0]) && polygonsEquivalent(new Point3d[]{coordinates[3],coordinates[4],coordinates[5]}, polygons[1])) || (polygonsEquivalent(new Point3d[]{coordinates[0],coordinates[1],coordinates[2]}, polygons[1]) && polygonsEquivalent(new Point3d[]{coordinates[3],coordinates[4],coordinates[5]}, polygons[0])));
        assertEquals(holes.length,0);
    }
    @Test
    public void ShareFullSide() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(0,2,0),
                                    new Point3d(2,0,0), new Point3d(2,2,0), new Point3d(0,2,0)};
        IntersectionRemover ir  = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(2,2,0),new Point3d(0,2,0)},polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void SharePartOfSide() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,2,0),
                                    new Point3d(0.5,0,0), new Point3d(1,-2,0), new Point3d(1.5,0,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY1);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(coordinates,polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void Intersecting() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,2,0),
                                    new Point3d(0,1,0), new Point3d(2,1,0), new Point3d(1,3,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1.5,1,0),
                                              new Point3d(2,1,0), new Point3d(1,3,0), new Point3d(0,1,0),
                                              new Point3d(0.5,1,0)},polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void GivesHoles() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,2,0),
                                    new Point3d(1,0,0), new Point3d(3,0,0), new Point3d(2,2,0),
                                    new Point3d(0.5,1.5,0), new Point3d(2.5,1.5,0), new Point3d(1.5,3.5,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(new Point3d[]{new Point3d(0,0,0), new Point3d(3,0,0), new Point3d(2.25,1.5,0), new Point3d(2.5,1.5,0), new Point3d(1.5,3.5,0), new Point3d(0.5,1.5,0), new Point3d(0.75,1.5,0)},polygons[0]));
        assertEquals(holes.length,1);
        assertTrue(polygonsEquivalent(new Point3d[]{new Point3d(1.5,1,0), new Point3d(1.25,1.5,0), new Point3d(1.75,1.5,0)},holes[0]));
    }
    @Test
    public void TotalOverlap() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,2,0),
                                    new Point3d(-0.5,-0.5,0), new Point3d(2.5,-0.5,0), new Point3d(1,2.5,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(new Point3d[]{new Point3d(-0.5,-0.5,0), new Point3d(2.5,-0.5,0), new Point3d(1,2.5,0)},polygons[0]));
        assertEquals(holes.length,0);
    }
    @Test
    public void OverlapHoleTotally() {
        coordinates = new Point3d[]{new Point3d(0,0,0), new Point3d(3,0,0), new Point3d(0,6,0),
                                    new Point3d(1,0,0), new Point3d(4,0,0), new Point3d(4,6,0),
                                    new Point3d(0,4,0), new Point3d(4,4,0), new Point3d(2,8,0),
                                    new Point3d(0.5,5,0), new Point3d(2,1,0), new Point3d(3.5,5,0)};
        IntersectionRemover ir = new IntersectionRemover(coordinates, ProjectionUtils.ProjectionFace.XY0);
        polygons = ir.getPolygonArray();
        holes = ir.getHoleArray();
        assertEquals(polygons.length,1);
        assertTrue(polygonsEquivalent(new Point3d[]{new Point3d(0,0,0), new Point3d(4,0,0), new Point3d(4,6,0), new Point3d(3.5,5,0), new Point3d(2,8,0), new Point3d(0.5,5,0), new Point3d(0,6,0)},polygons[0]));
        assertEquals(holes.length,0);
    }
}