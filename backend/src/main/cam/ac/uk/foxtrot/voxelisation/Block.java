package cam.ac.uk.foxtrot.voxelisation;

import com.google.gson.annotations.SerializedName;

import javax.vecmath.Vector3d;



public class Block {

    @SerializedName("pos")
    private Vector3d mPosition;

    @SerializedName("custom_part_array")
    private CustomPart[] mCustomPart;

    @SerializedName("use_custom_part")
    private boolean mUsingSuggestCustomPart;

    @SerializedName("suggested_custom_part")
    private int mSuggestedCustomPartIndex;

    public Block(Vector3d position, CustomPart[] customPart,
                 boolean suggestUseCustomPart, int suggestedCustomPartIndex) {
        mPosition = position;
        mCustomPart = customPart;
        mUsingSuggestCustomPart = suggestUseCustomPart;
        mSuggestedCustomPartIndex = suggestedCustomPartIndex;
    }

    public Vector3d getPosition() {
        return mPosition;
    }

    public CustomPart[] getCustomPart() {
        return mCustomPart;
    }

    public boolean isUsingSuggestCustomPart() {
        return mUsingSuggestCustomPart;
    }

    public int getSuggestedCustomPartIndex() {
        return mSuggestedCustomPartIndex;
    }




}
