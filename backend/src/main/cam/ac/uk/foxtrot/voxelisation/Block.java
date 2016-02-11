package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.*;
import java.util.ArrayList;

public class Block
{
    private int triangleCnt; // number of triangles in the block
    private ArrayList<Point3f> triangles; // the triangles representing the part of the mesh which is within the block

    public ArrayList<Point3f> getTriangles()
    {
        return triangles;
    }

    public void addTriangle(Point3f fir, Point3f sec, Point3f trd)
    {
        triangles.add(new Point3f(fir.x - mPosition.x, fir.y - mPosition.y, fir.z - mPosition.z));
        triangles.add(new Point3f(sec.x - mPosition.x, sec.y - mPosition.y, sec.z - mPosition.z));
        triangles.add(new Point3f(trd.x - mPosition.x, trd.y - mPosition.y, trd.z - mPosition.z));
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

    @SerializedName("pos")
    private Vector3f mPosition;

    @SerializedName("custom_part_array")
    private CustomPart[] mCustomPart;

    @SerializedName("use_custom_part")
    private boolean mUsingSuggestCustomPart;

    @SerializedName("suggested_custom_part")
    private int mSuggestedCustomPartIndex;

    public Block(Vector3f position, CustomPart[] customPart,
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

    public Block(Vector3f position)
    {
        // initialise internals
        triangles = new ArrayList<Point3f>();
        triangleCnt = 0;

        mPosition = position;
        mCustomPart = null;
        mUsingSuggestCustomPart = false;
        mSuggestedCustomPartIndex = 0;
    }

    public Vector3f getPosition()
    {
        return mPosition;
    }

    public CustomPart[] getCustomPart()
    {
        return mCustomPart;
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
                Point3f currPt = triangles.get(i);
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
