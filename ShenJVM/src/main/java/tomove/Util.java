package tomove;

import java.io.InputStream;

public class Util {

    public static InputStream streamFromCL(String str) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(str);
    }

}
