package nl.modelingvalue.timesheets;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nl.modelingvalue.timesheets.util.GsonUtils;

public class Probeer {
    public static final String TEST_JSON = """
            {
                "aap"   : {"idExp":"aap" ,"indexExp":0},
                "noot"  : {"idExp":"noot","indexExp":1},
                "boer"  : {"idExp":"boer","indexExp":2}
            }
            """;

    @Test
    public void x() {
        X x = GsonUtils.withSpecials().fromJson(TEST_JSON, X.class);
        x.forEach((k, v) -> v.check(k));
    }

    public static class X extends HashMap<String,Y> {
    }

    public static class Y {
        public String id;
        public int    index;
        public String idExp;
        public int    indexExp;

        @Override
        public String toString() {
            return "Y(" + id + ":" + index + ")";
        }

        public void check(String k) {
            Assertions.assertEquals(id, k);
            Assertions.assertEquals(idExp, id);
            Assertions.assertEquals(indexExp, index);
        }
    }
}