package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.vecmath.Point3d;

public class FunctionParameters
{
    @SerializedName("center_of_mass")
    private Point3d InitialCenterOfMass;

    @SerializedName("offset")
    private Point3d MeshOffset;

    @SerializedName("block_list")
    Block[][][] Blocks;

    public FunctionParameters(Point3d CM, Point3d off, Block[][][] blocks)
    {
        InitialCenterOfMass = CM;
        MeshOffset = off;
        Blocks = blocks;
    }
}
