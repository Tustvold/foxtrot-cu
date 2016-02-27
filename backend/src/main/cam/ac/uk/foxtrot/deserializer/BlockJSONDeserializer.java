package cam.ac.uk.foxtrot.deserializer;

import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.CustomPart;
import com.google.gson.*;

import javax.vecmath.Point3d;
import java.lang.reflect.Type;

/**
 * Created by joeisaacs on 18/02/2016.
 */
public class BlockJSONDeserializer implements JsonDeserializer<Block> {
    @Override
    public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();

        CustomPart[] parts = null;
        if (jsonObj.get("custom_part_array") != null) {
            parts = new Gson().fromJson(jsonObj.get("custom_part_array"), CustomPart[].class);
        }

        double[] dim = null;
        if (jsonObj.get("internal_dimension") != null) {
            dim = new Gson().fromJson(jsonObj.get("internal_dimension"), double[].class);
        }

        boolean usingCustomPart = jsonObj.getAsJsonPrimitive("use_custom_part").getAsBoolean();
        int partNumber = jsonObj.getAsJsonPrimitive("custom_part_index").getAsInt();

        Block block = new Block(parts, usingCustomPart, partNumber);
        if(usingCustomPart)
            block.modifInternalDim(dim);
        return block;
    }
}
