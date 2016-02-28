package cam.ac.uk.foxtrot.voxelisation;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

public class IntersectionRemover {
    Geometry union;                       // list of non-intersecting polygons
    Point3d[][] polygonArray = new Point3d[0][];                        // array of all polygons in non-intersecting representation
    Point3d[][] holeArray = new Point3d[0][];                           // array of all holes in non-intersecting representation
    Point3dPolygon[] combinedArray = new Point3dPolygon[0];             // array of polygons with their holes
    private static final double approximate_tolerance = 0.0000000001;   // tolerance for deciding whether triangle approximates to line
    double z;                                                           // value of z if projected to x-y plane, y if x-z plane, x if y-z plane
    ProjectionUtils.ProjectionFace projectionFace;                      // which face mesh was project onto

    public IntersectionRemover(Point3d[] originalCoordinates, ProjectionUtils.ProjectionFace projectionFace){
        // rotate to x-y plane (JTS only operates in x-y plane)
        this.projectionFace = projectionFace;
        Point3d[] rotatedCoordinates = originalCoordinates.clone();
        convertBetweenPlanes(rotatedCoordinates);
        // store z value (in rotated plane) as JTS generated coordinates will use NaN by default
        if(rotatedCoordinates.length > 1) {
            z = rotatedCoordinates[0].z;
        }
        GeometryFactory factory = new GeometryFactory();
        // convert points to list of triangles
        ArrayList<Geometry> triangleList= new ArrayList<>();
        for(int i = 0; i < rotatedCoordinates.length; i+=3) { //i,i+1,i+2 vertices of one triangle, iterate through triangles
            if(!approximatesToLine(rotatedCoordinates[i],rotatedCoordinates[i+1],rotatedCoordinates[i+2])) {
                Coordinate coordinate1 = toJTSCoordinate(rotatedCoordinates[i]);
                Coordinate coordinate2 = toJTSCoordinate(rotatedCoordinates[i + 1]);
                Coordinate coordinate3 = toJTSCoordinate(rotatedCoordinates[i + 2]);
                Coordinate[] coordinates = {coordinate1, coordinate2, coordinate3, coordinate1}; //coordinates of triangle looped round
                triangleList.add(factory.createPolygon(factory.createLinearRing(coordinates), null)); //add triangle to list of geometries
            }
        }
        union = merge(triangleList, new ArrayList<>());
        if (union != null) {
            generateArrays();
        }
    }

    /**
     * Checks if points can be approximated as on the same line
     */
    public static boolean approximatesToLine(Point3d A, Point3d B, Point3d C)
    {
        return Math.abs((C.x - B.x) * (B.y - A.y) - (B.x - A.x) * (C.y - B.y)) < approximate_tolerance;
    }

    /**
     * Merges lists of intersecting polygons to return a list of non-intersecting polygons
     * @param polygons1 list of possibly intersecting polygons
     * @param polygons2 list of possibly intersecting polygons
     * @return list of non-intersecting polygons
     */
    private Geometry merge(List<Geometry> polygons1, List<Geometry> polygons2) {
        int length1 = polygons1.size();
        int length2 = polygons2.size();
        if (length1 == 0 && length2 == 0) {
            GeometryFactory factory = new GeometryFactory();
            return factory.createGeometry(null);
        } if (length1 == 0 && length2 == 1) {
            return polygons2.get(0);
        } if (length1 == 1 && length2 == 0) {
            return polygons1.get(0);
        } if (length1 == 0 && length2 >= 2) {
            int i = length2/2;
            return merge(polygons2.subList(0,i),polygons2.subList(i,length2));
        } if (length1 >= 2 && length2 == 0) {
            int i = length1/2;
            return merge(polygons1.subList(0,i),polygons1.subList(i,length1));
        }
        int i = length1/2;
        Geometry nonIntersecting1 = merge(polygons1.subList(0,i),polygons1.subList(i,length1));
        int j = length2/2;
        Geometry nonIntersecting2 = merge(polygons2.subList(0,j),polygons2.subList(j,length2));
        try {
            ArrayList<Geometry> list = new ArrayList<>();
            list.add(nonIntersecting1);
            list.add(nonIntersecting2);
            return UnaryUnionOp.union(list);
        } catch (TopologyException e) {
            if (nonIntersecting1.getArea() > nonIntersecting2.getArea()) {
                return nonIntersecting1;
            } else {
                return nonIntersecting2;
            }
        }
    }
    
    /**
     * Rotate between x-y plane and y-z plane
     * @param coordinates coordinates that should be rotated
     */
    private void yz(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.z, point.y, point.x);
        }
    }

    /**
     * Rotate between x-y plane and x-z plane
     * @param coordinates coordinates that should be rotated
     */
    private void zx(Point3d[] coordinates) {
        int length = coordinates.length;
        for (int i = 0; i < length; i++) {
            Point3d point = coordinates[i];
            coordinates[i] = new Point3d(point.x,point.z,point.y);
        }
    }

    // converts coordinates depending on which face it was projected onto

    /**
     * Convert coordinates between x-y plane and original plane
     */
    private void convertBetweenPlanes(Point3d[] coordinates) {
        switch (projectionFace) {
            case ZX0: zx(coordinates); break;
            case ZX1: zx(coordinates); break;
            case ZY0: yz(coordinates); break;
            case ZY1: yz(coordinates); break;
            default: break;
        }
    }

    /**
     * Convert from a Point3d from the Java3D library to a Coordinate used in the JTS library
     * @param point Point3d to be converted
     * @return Coordinate equivalent to the point
     */
    private Coordinate toJTSCoordinate(Point3d point) {
        return new Coordinate(point.x, point.y, point.z);
    }

    // create arrays of polygons, holes & their combinations from union

    /**
     * Use the list of merged polgyons to create the necessary arrays
     */
    private void generateArrays() {
//        int length = union.size();
        int length = union.getNumGeometries();
        polygonArray = new Point3d[length][];
        combinedArray = new Point3dPolygon[length];
        ArrayList<Point3d[]> holeList = new ArrayList<>();
        // take each element of union and use it to generate the equivalents for each array/list
        for(int i = 0; i < length; i++) {
//            Point3dPolygon polygon = new Point3dPolygon(union.get(i),z);
            Point3dPolygon polygon = new Point3dPolygon((Polygon)union.getGeometryN(i),z);
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

    /**
     * Getter for polygon array
     * @return Returns array of arrays of points representing the exterior of each non-intersecting polygon
     */
    public Point3d[][] getPolygonArray() {
        return polygonArray;
    }

    /**
     * Getter for hole array
     * @return Returns array of arrays of points representing each hole in the the non-intersecting polygons
     */
    public Point3d[][] getHoleArray() {
        return holeArray;
    }

    // getter for combined array

    /**
     * Getter for combined array
     * @return Returns array of each non-intersecting polygon, representing each exterior with associated holes
     */
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
