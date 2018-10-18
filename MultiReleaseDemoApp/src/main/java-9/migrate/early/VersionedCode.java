package migrate.early;

import java.util.Base64;
import java.lang.invoke.VarHandle;

/* This implementation of VersionedCode will be run on Java 9+ JDKS */
public class VersionedCode {
  public static void jdepsMultiDemo() {
    System.out.println("Run dependency example that is compatible with Java 9+.");

    // sun.misc.BASE64Encoder was renamed to java.util.Base64.Encoder starting in Java 9
    Base64.getEncoder();

    // sun.misc.Unsafe moved to an internal API jdk.internal.misc.Unsafe starting in  Java 9
    // Variable Handles were introduced as a safer way to access some of these operations.
    VarHandle.fullFence();
  }
}
