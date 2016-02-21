package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3d;

public class IntersectionRemover {
    Geometry union;
    Point3d[][] polygonArray;
    Point3d[][] holeArray;
    Point3dPolygon[] combinedArray;
    private static final double approximate_tolerance = 0.0000000001;
    double z;
    CustomPartMouldGenerator.ProjectionFace projectionFace;

    // checks if the three points can be approximated as on the same line
    public boolean approximatesToLine(Point3d A, Point3d B, Point3d C)
    {
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < approximate_tolerance;
    }

    public IntersectionRemover(Point3d[] originalCoordinates, CustomPartMouldGenerator.ProjectionFace face){ // in x-y plane
        projectionFace = face;
        convertBetweenPlanes(originalCoordinates);
        List<Geometry> geometryList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        if(originalCoordinates.length>1) {
            z = originalCoordinates[0].z;
        }
        for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            if(!approximatesToLine(originalCoordinates[i],originalCoordinates[i+1],originalCoordinates[i+2])) {
                Coordinate coordinate1 = toJTSCoordinate(originalCoordinates[i]);
                Coordinate coordinate2 = toJTSCoordinate(originalCoordinates[i + 1]);
                Coordinate coordinate3 = toJTSCoordinate(originalCoordinates[i + 2]);
                Coordinate[] coordinates = {coordinate1, coordinate2, coordinate3, coordinate1}; //coordinates of triangle
                geometryList.add(factory.createPolygon(factory.createLinearRing(coordinates), null)); //add triangle to list of geometries
            }
        }
        for (Geometry g: geometryList){
            if (union == null) {
                union = g;
            } else {
                union.union(g);
            }
        }
 //       union = UnaryUnionOp.union(geometryList); //merge geometries, overlapping triangles merged
        if (union != null) {
            generateArrays();
        } else {
            polygonArray = new Point3d[0][];
            holeArray = new Point3d[0][];
            combinedArray = new Point3dPolygon[0];
        }
    }

    private void zy(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.z, point.y, point.x);
        }
    }

    private void zx(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.x,point.z,point.y);
        }
    }

    private void convertBetweenPlanes(Point3d[] coordinates) {
        switch (projectionFace) {
            case ZX0: zx(coordinates); break;
            case ZX1: zx(coordinates); break;
            case ZY0: zy(coordinates); break;
            case ZY1: zy(coordinates); break;
            default: break;
        }
    }

    // convert original J3D Point3d to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3d point) {
        return new Coordinate(point.x, point.y, point.z);
    }

    // convert Geometry to array of arrays of Point3ds for original plane
    private void generateArrays() {
        int length = union.getNumGeometries();
        polygonArray = new Point3d[length][];
        combinedArray = new Point3dPolygon[length];
        ArrayList<Point3d[]> holeList = new ArrayList<>();
        for(int i = 0; i < length; i++) {
            Point3dPolygon polygon = new Point3dPolygon(((Polygon)union.getGeometryN(i)),z);
            convertBetweenPlanes(polygon.getExterior());
            Point3d[][] holes = polygon.getHoles();
            polygonArray[i] = polygon.getExterior();
            int numHoles = holes.length;
            for(int j = 0; j < numHoles; j++) {
                convertBetweenPlanes(holes[j]);
                holeList.add(holes[j]);
            }
            combinedArray[i] = polygon;
        }
        holeArray = holeList.toArray(new Point3d[0][]);
    }

    public Point3d[][] getPolygonArray() {
        return polygonArray;
    }

    public Point3d[][] getHoleArray() {
        return holeArray;
    }

    public Point3dPolygon[] getCombinedArray() {
        return combinedArray;
    }

    public static void main(String[] args) {
        Point3d[] Point3ds = {new Point3d(0,0,0), new Point3d(2,0,0), new Point3d(1,0,2)};
        IntersectionRemover merged = new IntersectionRemover(Point3ds, CustomPartMouldGenerator.ProjectionFace.ZX0);
        System.out.println(Arrays.toString(merged.getCombinedArray()[0].getExterior()));
        System.out.println(Arrays.deepToString(merged.getCombinedArray()[0].getHoles()));
        System.out.println(Arrays.deepToString(merged.getPolygonArray()));
        System.out.println(Arrays.toString(merged.getHoleArray()));
    }

    public void drawPolygon(String filename)
    {
        System.out.println("Drawing single face...");
        Point3d[][] polys = new Point3d[polygonArray.length + holeArray.length][];
        for(int i = 0; i < polygonArray.length; i++)
        {
            polys[i] = new Point3d[polygonArray[i].length];
            for(int j = 0; j < polygonArray[i].length; j++)
            {
                polys[i][j] = new Point3d(polygonArray[i][j]);
            }
        }
        for(int i = 0; i < holeArray.length; i++)
        {
            polys[polygonArray.length + i] = new Point3d[holeArray[i].length];
            for(int j = 0; j < holeArray[i].length; j++)
            {
                polys[polygonArray.length + i][j] = new Point3d(holeArray[i][j]);
            }
        }
        drawPolygonList(polys, filename);
        System.out.println("Single face drawn...");
    }

    public void drawPolygonList(Point3d[][] polys, String filename)
    {
        Writer writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            //System.err.println(ex.getMessage());
            return;
        }

        for (int i = 0; i < polys.length; i++)
        {
            for (int j = 0; j < polys[i].length; j++)
            {
                try
                {
                    writer.write("v " + (polys[i][j].x) + " "
                            + (polys[i][j].y) + " "
                            + (polys[i][j].z) + "\n");

                } catch (IOException err)
                {
                    System.err.println("Could not write blocks: " + err.getMessage());
                }
            }
        }
        int curr = 1;
        for (int poly = 0; poly < polys.length; poly++)
        {
            String out = "f";
            for (int i = 0; i < polys[poly].length; i++)
            {
                out += " " + curr;
                curr++;
            }
            try
            {
                writer.write(out + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write blocks: " + err.getMessage());
            }
        }

        try
        {
            writer.close();
        } catch (Exception ex)

        {/*ignore*/}
    }

}
