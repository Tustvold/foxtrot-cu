package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3d;
import java.io.*;
import java.util.ArrayList;

public class Block
{

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

    public void addTriangles(ArrayList<Point3d> points)
    {
        triangles.addAll(points);
        triangleCnt += points.size()/3;
    }

    public void addTriangle(Point3d fir, Point3d sec, Point3d trd)
    {
        triangles.add(adjustPoint(fir));
        triangles.add(adjustPoint(sec));
        triangles.add(adjustPoint(trd));
        triangleCnt++;
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

    public void drawBlock(String filename)
    {
        System.out.println("Drawing single block...");
        Writer writer = null;

        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
        } catch (IOException ex)
        {
            System.err.println(ex.getMessage());
        }

        int totalTriangles = 0;

        for (int i = 0; i < triangleCnt * 3; i++)
        {
            try
            {
                Point3d currPt = triangles.get(i);
                writer.write("v " + currPt.x + " " + currPt.y + " " + currPt.z + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write vertex: " + err.getMessage());
            }
        }

        for (int i = 1; i < triangleCnt * 3; i += 3)
        {
            try
            {
                writer.write("f " + i + " " + (i + 1) + " " + (i + 2) + "\n");
            } catch (IOException err)
            {
                System.err.println("Could not write triangle: " + err.getMessage());
            }
        }
        try
        {
            writer.close();
        } catch (Exception ex)
        {/*ignore*/}
        System.out.println("Done...");
    }
}
