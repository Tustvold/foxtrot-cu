package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;

public class Block
{
    private float cubeSize;    // size of standard cube (TODO to be set by user)
    private int triangleCnt;
    private ArrayList<Point3f> triangles;

    public ArrayList<Point3f> getTriangles()
    {
        return triangles;
    }

    public void addTriangle(Point3f fir, Point3f sec, Point3f trd)
    {
        triangles.add(fir);
        triangles.add(sec);
        triangles.add(trd);
        triangleCnt++;
    }

    public int getTriangleCount()
    {
        return triangleCnt;
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
        mPosition = position;
        mCustomPart = customPart;
        mUsingSuggestCustomPart = suggestUseCustomPart;
        mSuggestedCustomPartIndex = suggestedCustomPartIndex;
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
