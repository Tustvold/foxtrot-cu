package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;

import java.io.*;
import java.util.ArrayList;

import javax.vecmath.Point3d;

public class IntersectionRemover {
    ArrayList<Polygon> union = new ArrayList<>();                       // list of non-intersecting polygons
    Point3d[][] polygonArray = new Point3d[0][];                        // array of all polygons in non-intersecting representation
    Point3d[][] holeArray = new Point3d[0][];                           // array of all holes in non-intersecting representation
    Point3dPolygon[] combinedArray = new Point3dPolygon[0];             // array of polygons with their holes
    private static final double approximate_tolerance = 0.0000000001;   // tolerance for deciding whether triangle approximates to line
    double z;                                                           // value of z if projected to x-y plane, y if x-z plane, x if y-z plane
    ProjectionUtils.ProjectionFace projectionFace;                      // which face mesh was project onto

    public IntersectionRemover(Point3d[] originalCoordinates, ProjectionUtils.ProjectionFace projectionFace){
        // rotate to x-y plane (JTS only operates in x-y plane)
        this.projectionFace = projectionFace;
        convertBetweenPlanes(originalCoordinates);
        // store z value (in rotated plane) as JTS generated coordinates will use NaN by default
        if(originalCoordinates.length > 1) {
            z = originalCoordinates[0].z;
        }
        GeometryFactory factory = new GeometryFactory();
        // convert points to list of triangles
        ArrayList<Polygon> triangleList= new ArrayList<>();
        for(int i = 0; i < originalCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            if(!approximatesToLine(originalCoordinates[i],originalCoordinates[i+1],originalCoordinates[i+2])) {
                Coordinate coordinate1 = toJTSCoordinate(originalCoordinates[i]);
                Coordinate coordinate2 = toJTSCoordinate(originalCoordinates[i + 1]);
                Coordinate coordinate3 = toJTSCoordinate(originalCoordinates[i + 2]);
                Coordinate[] coordinates = {coordinate1, coordinate2, coordinate3, coordinate1}; //coordinates of triangle looped round
                triangleList.add(factory.createPolygon(factory.createLinearRing(coordinates), null)); //add triangle to list of geometries
            }
        }
        // iteratively merge triangles
        for (Polygon triangle: triangleList){
            if (union.size() == 0) {
                union.add(triangle);
            } else {
                mergeIntersecting(union,triangle);
            }
        }
        if (union != null) {
            generateArrays();
        }
    }

    // checks if the three points can be approximated as on the same line
    public boolean approximatesToLine(Point3d A, Point3d B, Point3d C)
    {
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < approximate_tolerance;
    }

    // recursively merges polygon into list until it is non-intersecting, returns if it succeeded at merging polygon in
    private boolean mergeIntersecting(ArrayList<Polygon> list, Polygon polygon) {
        boolean success;
        int length = list.size();
        if (length == 0) {
            list.add(polygon);
            return true;
        } else {
            Polygon element = list.get(0);
            list.remove(0);
            try {
                Geometry temp = polygon.union(element); // merge element & polygon
                if (temp instanceof Polygon) { // forms one polygon
                    element = (Polygon)temp;
                    success = mergeIntersecting(list,element); // does recursively merging this succeed
                } else { // forms multiple polygon
                    success = mergeIntersecting(list,polygon); // recursively merge polygon with rest of list
                    if(success) {
                        list.add(element); // add non-intersecting element back into list
                    }
                }
            } catch (TopologyException e) { // ignore this polygon
                return false; // failed to fully merge polygon into list
            }

        }
        return success;
    }

    // rotate between x-y and y-z plane for all coordinates
    private void yz(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.z, point.y, point.x);
        }
    }

    // rotate between x-y and x-z plane for all coordinates
    private void zx(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.x,point.z,point.y);
        }
    }

    // converts coordinates depending on which face it was projected onto
    private void convertBetweenPlanes(Point3d[] coordinates) {
        switch (projectionFace) {
            case ZX0: zx(coordinates); break;
            case ZX1: zx(coordinates); break;
            case ZY0: yz(coordinates); break;
            case ZY1: yz(coordinates); break;
            default: break;
        }
    }

    // convert original J3D Point3d to JTS Coordinates
    private Coordinate toJTSCoordinate(Point3d point) {
        return new Coordinate(point.x, point.y, point.z);
    }

    // create arrays of polygons, holes & their combinations from union
    private void generateArrays() {
        int length = union.size();
        polygonArray = new Point3d[length][];
        combinedArray = new Point3dPolygon[length];
        ArrayList<Point3d[]> holeList = new ArrayList<>();
        // take each element of union and use it to generate the equivalents for each array/list
        for(int i = 0; i < length; i++) {
            Point3dPolygon polygon = new Point3dPolygon(union.get(i),z);
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

    // getter for polygon array
    public Point3d[][] getPolygonArray() {
        return polygonArray;
    }

    // getter for hole array
    public Point3d[][] getHoleArray() {
        return holeArray;
    }

    // getter for combined array
    public Point3dPolygon[] getCombinedArray() {
        return combinedArray;
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
