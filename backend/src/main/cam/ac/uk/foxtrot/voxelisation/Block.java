package cam.ac.uk.foxtrot.voxelisation;

import cam.ac.uk.foxtrot.sidefiller.Point;
import com.google.gson.annotations.SerializedName;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3d;
import java.io.*;
import java.util.ArrayList;

public class Block
{
    @SerializedName("internal_dimension")
    private double[] internalDim; // the dimension of the internal triangles
    // internalDim[0] = minimum x
    // internalDim[1] = minimum y
    // internalDim[2] = minimum z
    // internalDim[3] = maximum x
    // internalDim[4] = maximum y
    // internalDim[5] = maximum z

    @SerializedName("pos")
    private Point3d mPosition;

    @SerializedName("custom_part_array")
    private CustomPart[] customParts;

    @SerializedName("use_custom_part")
    private boolean isCustom;

    @SerializedName("custom_part_index")
    private int customPartIndex;

    private int triangleCnt; // number of triangles in the block
    private ArrayList<Point3d> triangles; // the triangles representing the part of the mesh which is within the block

    public ArrayList<Point3d> getTriangles()
    {
        return triangles;
    }

    /**
     * Adds the triangles from the given array WITHOUT SHIFTING THEM!
     */
    public void addTriangles(ArrayList<Point3d> points)
    {
        for(int i = 0; i < points.size(); i += 3)
        {
            if(MeshVoxeliser.areIdentical(points.get(i), points.get(i+1))
                    || MeshVoxeliser.areIdentical(points.get(i+1), points.get(i+2))
                    || MeshVoxeliser.areIdentical(points.get(i+2), points.get(i)))
                continue;
            triangles.add(adjustPoint01(points.get(i)));
            triangles.add(adjustPoint01(points.get(i+1)));
            triangles.add(adjustPoint01(points.get(i+2)));
            triangleCnt++;
        }
    }

    /**
     * Adds a single triangle with vertices fir sec and trd respectively.
     */
    public void addTriangle(Point3d fir, Point3d sec, Point3d trd)
    {
        if(MeshVoxeliser.areIdentical(fir, sec)
                || MeshVoxeliser.areIdentical(sec, trd)
                || MeshVoxeliser.areIdentical(trd, fir))
            return;
        triangles.add(adjustPoint(fir));
        triangles.add(adjustPoint(sec));
        triangles.add(adjustPoint(trd));
        triangleCnt++;
    }

    public void setInternalDim()
    {
        internalDim = new double[6];
        internalDim[0] = internalDim[3] = triangles.get(0).x;
        internalDim[1] = internalDim[4] = triangles.get(0).y;
        internalDim[2] = internalDim[5] = triangles.get(0).z;
        for (int i = 1; i < triangleCnt * 3; i++)
        {
            // go through all the points and find the minimums and maximums
            Point3d curr = triangles.get(i);
            if (curr.x < internalDim[0]) internalDim[0] = curr.x;
            else if (curr.x > internalDim[3]) internalDim[3] = curr.x;
            if (curr.y < internalDim[1]) internalDim[1] = curr.y;
            else if (curr.y > internalDim[4]) internalDim[4] = curr.y;
            if (curr.z < internalDim[2]) internalDim[2] = curr.z;
            else if (curr.z > internalDim[5]) internalDim[5] = curr.z;
        }
    }

    public double[] getInternalDim()
    {
        return internalDim;
    }

    public void modifInternalDim(double[] dimension)
    {
        internalDim = dimension;
    }

    /**
     * Grid aligns the coordinates of point A which are
     * within the tolerance from the nearest grid plane.
     *
     * @param A point to be adjusted
     */
    private Point3d adjustPoint(Point3d A)
    {
        double x = A.x - mPosition.x;
        double y = A.y - mPosition.y;
        double z = A.z - mPosition.z;

        // correct the new coordinates
        if (x < MeshVoxeliser.double_tolerance) x = 0;
        else if (x > 1 - MeshVoxeliser.double_tolerance) x = 1;
        if (y < MeshVoxeliser.double_tolerance) y = 0;
        else if (y > 1 - MeshVoxeliser.double_tolerance) y = 1;
        if (z < MeshVoxeliser.double_tolerance) z = 0;
        else if (z > 1 - MeshVoxeliser.double_tolerance) z = 1;

        return new Point3d(x, y, z);
    }

    private Point3d adjustPoint01(Point3d A)
    {
        double x = A.x;
        double y = A.y;
        double z = A.z;

        // correct the new coordinates
        if (x < MeshVoxeliser.double_tolerance) x = 0;
        else if (x > 1 - MeshVoxeliser.double_tolerance) x = 1;
        if (y < MeshVoxeliser.double_tolerance) y = 0;
        else if (y > 1 - MeshVoxeliser.double_tolerance) y = 1;
        if (z < MeshVoxeliser.double_tolerance) z = 0;
        else if (z > 1 - MeshVoxeliser.double_tolerance) z = 1;

        return new Point3d(x, y, z);
    }

    public void setIsCustom(boolean custom)
    {
        isCustom = custom;
    }

    public int getTriangleCount()
    {
        return triangleCnt;
    }

    public TriangleArray getTriangleArray()
    {
        TriangleArray ta = new TriangleArray(triangleCnt * 3, 387);
        for (int i = 0; i < triangleCnt; i++)
        {
            ta.setCoordinate(3 * i, triangles.get(i));
            ta.setCoordinate(3 * i + 1, triangles.get(i + 1));
            ta.setCoordinate(3 * i + 2, triangles.get(i + 2));
        }
        return ta;
    }

    public void setCustomPart(int i, CustomPart newPart)
    {
        if (i < 0 || i > 5)
        {
            throw new IllegalArgumentException("setCustomPart: part index out of bounds (should be between 0 and 5)");
        }

        customParts[i] = newPart;
    }

    public void setSuggestedCustomPartIndex(int index)
    {
        customPartIndex = index;
    }


    public Block(Point3d position, CustomPart[] customPart,
                 boolean suggestUseCustomPart, int suggestedCustomPartIndex)
    {
        // initialise internals
        triangles = new ArrayList<>();
        triangleCnt = 0;

        mPosition = position;
        customParts = customPart;
        isCustom = suggestUseCustomPart;
        customPartIndex = suggestedCustomPartIndex;
    }

    public Block(CustomPart[] parts, boolean usingCustomPart, int partNumber)
    {
        customParts = parts;
        isCustom = usingCustomPart;
        customPartIndex = partNumber;
    }

    public Block(Point3d position, boolean isCustom)
    {
        // initialise internals
        triangles = new ArrayList<Point3d>();
        triangleCnt = 0;
        this.isCustom = isCustom;

        mPosition = position;
        customParts = new CustomPart[6];
        customPartIndex = 0;
    }

    public Point3d getPosition()
    {
        return new Point3d(mPosition);
    }

    public CustomPart[] getCustomPart()
    {
        return customParts;
    }

    public boolean isCustom()
    {
        return isCustom;
    }

    public int getCustomPartIndex()
    {
        return customPartIndex;
    }
}
