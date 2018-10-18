package migrate.early;

import sun.misc.BASE64Encoder;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

/* This implementation of VersionedCode will be run on JDKs below Java 9 */
public class VersionedCode {
  public static void jdepsMultiDemo() {
    Unsafe myUnsafe = null;

    System.out.println("Run base code with dependency problems in multi-release jar.");

    // sun.misc.BASE64Encoder was renamed to java.util.Base64 starting in Java 9
    new BASE64Encoder();

    // sun.misc.Unsafe moved to an internal API jdk.internal.misc.Unsafe starting in  Java 9
    try {
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      myUnsafe = (Unsafe)f.get(null);
    } catch (Exception e) {
      System.out.println(e);
    }
    myUnsafe.fullFence();
  }
}
