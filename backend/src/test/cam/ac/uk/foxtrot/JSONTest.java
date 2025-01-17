package cam.ac.uk.foxtrot;

import cam.ac.uk.foxtrot.deserializer.BlockJSONDeserializer;
import cam.ac.uk.foxtrot.voxelisation.Block;
import cam.ac.uk.foxtrot.voxelisation.CustomPart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 * Created by joeisaacs on 18/02/2016.
 */
public class JSONTest {
    @Test
    public void testGSON() {
        String jsonInput = "{ \"custom_part_array\": [{\"triangle_array\":[{\"x\":3.0,\"y\":3.0,\"z\":3.0},{\"x\":3.0,\"y\":3.0,\"z\":4.0}]}], \"use_custom_part\": 0, \"custom_part_index\": 0 }";
        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(Block.class, new BlockJSONDeserializer());
        Gson g = gb.create();
        Block b = g.fromJson(jsonInput, Block.class);
        int a = 2;
    }

    @Test
    public void testPart() {
        Block[][][] blocks = new Block[1][1][1];

        Point3d[] points = new Point3d[2];
        points[0] = new Point3d(3,3,3);
        points[1] = new Point3d(3,3,4);
        CustomPart[] p = new CustomPart[1];
        p[0] = new CustomPart(points);

        blocks[0][0][0] = new Block(p,true,2);
        System.out.println(new Gson().toJson(blocks));

    }
}
