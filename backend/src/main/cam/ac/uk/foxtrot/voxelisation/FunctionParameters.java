package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.vecmath.Point3d;

/**
 * Created by Milos on 27/02/2016.
 */
public class FunctionParameters
{
    @SerializedName("center_of_mass")
    private Point3d InitialCenterOfMass;

    @SerializedName("offset")
    private Point3d MeshOffset;

    public FunctionParameters(Point3d CM, Point3d off)
    {
        InitialCenterOfMass = CM;
        MeshOffset = off;
    }
}
