package cam.ac.uk.foxtrot.serializer;

import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.FunctionParameters;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by joeisaacs on 11/02/2016.
 */
public class BlockJSONSerializer implements JsonSerializer<Block> {

    @Override
    public JsonElement serialize(Block block, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("custom_part_array", new Gson().toJsonTree(block.getCustomPart()));
        object.addProperty("use_custom_part", block.isCustom());
        object.addProperty("custom_part_index", block.getCustomPartIndex());

        return object;
    }
}
