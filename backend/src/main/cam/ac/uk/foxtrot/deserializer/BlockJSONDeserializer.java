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

        CustomPart[] parts = new Gson().fromJson(jsonObj.get("custom_part_array"), CustomPart[].class);

        boolean usingCustomPart = jsonObj.getAsJsonPrimitive("use_custom_part").isBoolean();
        int partNumber = jsonObj.getAsJsonPrimitive("suggested_custom_part").getAsInt();


        return new Block(parts, usingCustomPart, partNumber);
    }
}
