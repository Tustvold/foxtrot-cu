package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;

public class Block
{
    private int triangleCnt; // number of triangles in the block
    private ArrayList<Point3f> triangles; // the triangles representing the part of the mesh which is within the block

    public ArrayList<Point3f> getTriangles()
    {
        return triangles;
    }

    public Vector3f getSize()
    {
        return new Vector3f(mPosition);
    }

    public void addTriangle(Point3f fir, Point3f sec, Point3f trd)
    {
        triangles.add(new Point3f(fir.x - (float)mPosition.x, fir.y - (float)mPosition.y, fir.z - (float)mPosition.z));
        triangles.add(new Point3f(sec.x - (float)mPosition.x, sec.y - (float)mPosition.y, sec.z - (float)mPosition.z));
        triangles.add(new Point3f(trd.x - (float)mPosition.x, trd.y - (float)mPosition.y, trd.z - (float)mPosition.z));
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
    private Vector3d mPosition;

    @SerializedName("custom_part_array")
    private CustomPart[] mCustomPart;

    @SerializedName("use_custom_part")
    private boolean mUsingSuggestCustomPart;

    @SerializedName("suggested_custom_part")
    private int mSuggestedCustomPartIndex;

    public Block(Vector3d position, CustomPart[] customPart,
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

    public Block(Vector3d position)
    {
        // initialise internals
        triangles = new ArrayList<Point3f>();
        triangleCnt = 0;

        mPosition = position;
        mCustomPart = null;
        mUsingSuggestCustomPart = false;
        mSuggestedCustomPartIndex = 0;
    }

    public Vector3d getPosition()
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


}
