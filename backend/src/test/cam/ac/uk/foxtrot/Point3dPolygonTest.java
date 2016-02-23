package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.voxelisation.Point3dPolygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import org.junit.Test;

import javax.vecmath.Point3d;

import static org.junit.Assert.*;


public class Point3dPolygonTest {
    private GeometryFactory factory = new GeometryFactory();
    private Polygon jtsPolygon;
    private Point3dPolygon j3dPolygon;

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
    public void SimpleTriangle() {
        jtsPolygon = factory.createPolygon(factory.createLinearRing(new Coordinate[]{new Coordinate(0,0), new Coordinate(2,0), new Coordinate(1,2), new Coordinate(0,0)}));
        j3dPolygon = new Point3dPolygon(jtsPolygon,0);
        assertTrue(polygonsEquivalent(j3dPolygon.getExterior(), new Point3d[]{new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,2,0)}));
        assertEquals(j3dPolygon.getHoles().length,0);
    }
    @Test
    public void ConcaveShape() {
        jtsPolygon = factory.createPolygon(factory.createLinearRing(new Coordinate[]{new Coordinate(0,0), new Coordinate(1,1), new Coordinate(2,1), new Coordinate(3,0), new Coordinate(3,1), new Coordinate(2,2), new Coordinate(1,2), new Coordinate(0,1), new Coordinate(0,0)}));
        j3dPolygon = new Point3dPolygon(jtsPolygon,1);
        assertTrue(polygonsEquivalent(j3dPolygon.getExterior(), new Point3d[]{new Point3d(0,0,1), new Point3d(1,1,1), new Point3d(2,1,1), new Point3d(3,0,1), new Point3d(3,1,1), new Point3d(2,2,1), new Point3d(1,2,1), new Point3d(0,1,1)}));
        assertEquals(j3dPolygon.getHoles().length,0);
    }
    @Test
    public void SingleHole() {
        jtsPolygon = factory.createPolygon(factory.createLinearRing(new Coordinate[]{new Coordinate(0,0), new Coordinate(8,0), new Coordinate(8,8), new Coordinate(0,8), new Coordinate(0,0)}), new LinearRing[]{factory.createLinearRing(new Coordinate[]{new Coordinate(1,1), new Coordinate(2,1), new Coordinate(3,2), new Coordinate(2,3), new Coordinate(1,3), new Coordinate(1,1)})});
        j3dPolygon = new Point3dPolygon(jtsPolygon,1);
        assertTrue(polygonsEquivalent(j3dPolygon.getExterior(), new Point3d[]{new Point3d(0,0,1), new Point3d(8,0,1), new Point3d(8,8,1), new Point3d(0,8,1)}));
        assertEquals(j3dPolygon.getHoles().length,1);
        assertTrue(polygonsEquivalent(j3dPolygon.getHoles()[0], new Point3d[]{new Point3d(1,1,1), new Point3d(2,1,1), new Point3d(3,2,1), new Point3d(2,3,1), new Point3d(1,3,1)}));
    }
    @Test
    public void MultipleHoles() {
        jtsPolygon = factory.createPolygon(factory.createLinearRing(new Coordinate[]{new Coordinate(0,0), new Coordinate(8,0), new Coordinate(8,8), new Coordinate(0,8), new Coordinate(0,0)}),new LinearRing[]{factory.createLinearRing(new Coordinate[]{new Coordinate(1,1), new Coordinate(2,1), new Coordinate(1,2), new Coordinate(1,1)}), factory.createLinearRing(new Coordinate[]{new Coordinate(4,4), new Coordinate(5,3), new Coordinate(6,4), new Coordinate(5,5), new Coordinate(4,4)})});
        j3dPolygon = new Point3dPolygon(jtsPolygon,1);
        assertTrue(polygonsEquivalent(j3dPolygon.getExterior(), new Point3d[]{new Point3d(0,0,1), new Point3d(8,0,1), new Point3d(8,8,1), new Point3d(0,8,1)}));
        assertEquals(j3dPolygon.getHoles().length,2);
        assertTrue((polygonsEquivalent(j3dPolygon.getHoles()[0], new Point3d[]{new Point3d(1,1,1), new Point3d(1,2,1), new Point3d(2,1,1)}) && polygonsEquivalent(j3dPolygon.getHoles()[1], new Point3d[]{new Point3d(4,4,1), new Point3d(5,3,1), new Point3d(6,4,1), new Point3d(5,5,1)})) || (polygonsEquivalent(j3dPolygon.getHoles()[1],  new Point3d[]{new Point3d(1,1,1), new Point3d(1,2,1), new Point3d(2,1,1)}) && polygonsEquivalent(j3dPolygon.getHoles()[0], new Point3d[]{new Point3d(4,4,1), new Point3d(5,3,1), new Point3d(6,4,1), new Point3d(5,5,1)})));
    }
}