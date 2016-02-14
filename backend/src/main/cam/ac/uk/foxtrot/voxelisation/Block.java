package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.io.*;
import java.util.ArrayList;

public class Block
{

    @SerializedName("pos")
    private Point3d mPosition;

    @SerializedName("custom_part_array")
    private CustomPart[] mCustomPart;

    @SerializedName("use_custom_part")
    private boolean mUsingSuggestCustomPart;

    @SerializedName("suggested_custom_part")
    private int mSuggestedCustomPartIndex;

    private int triangleCnt; // number of triangles in the block
    private ArrayList<Point3d> triangles; // the triangles representing the part of the mesh which is within the block
    private boolean isCustom;

    public ArrayList<Point3d> getTriangles()
    {
        return triangles;
    }

    public void addTriangle(Point3d fir, Point3d sec, Point3d trd)
    {
        triangles.add(new Point3d(fir.x - mPosition.x, fir.y - mPosition.y, fir.z - mPosition.z));
        triangles.add(new Point3d(sec.x - mPosition.x, sec.y - mPosition.y, sec.z - mPosition.z));
        triangles.add(new Point3d(trd.x - mPosition.x, trd.y - mPosition.y, trd.z - mPosition.z));
        triangleCnt++;
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



    public Block(Point3d position, CustomPart[] customPart,
                 boolean suggestUseCustomPart, int suggestedCustomPartIndex)
    {
        // initialise internals
        triangles = new ArrayList<>();
        triangleCnt = 0;

        mPosition = position;
        mCustomPart = customPart;
        mUsingSuggestCustomPart = suggestUseCustomPart;
        mSuggestedCustomPartIndex = suggestedCustomPartIndex;
    }

    public Block(Point3d position, boolean isCustom)
    {
        // initialise internals
        triangles = new ArrayList<Point3d>();
        triangleCnt = 0;
        this.isCustom = isCustom;

        mPosition = position;
        mCustomPart = null;
        mUsingSuggestCustomPart = false;
        mSuggestedCustomPartIndex = 0;
    }

    public Point3d getPosition()
    {
        return new Point3d(mPosition);
    }

    public CustomPart[] getCustomPart()
    {
        return mCustomPart;
    }

    public boolean isCustom()
    {
        return isCustom;
    }

    public boolean isUsingSuggestCustomPart()
    {
        return mUsingSuggestCustomPart;
    }

    public int getSuggestedCustomPartIndex()
    {
        return mSuggestedCustomPartIndex;
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
