package cam.ac.uk.foxtrot.voxelisation;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

public class Block {

    private Vector3d mPosition;

    private CustomPart[] mCustomPart;

    private boolean mUsingSuggestCustomPart;

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
