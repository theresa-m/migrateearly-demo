package migrate.early;

import sun.misc.BASE64Encoder;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

import java.applet.Applet;
import java.lang.Compiler;
import java.lang.Thread;

public class Demo {
  public static void main(String[] args) {
    jdepsDemo();
    jdeprscanDemo();
  }

  /*************************
   * Class dependency issues
   *************************/
  public static void jdepsDemo() {
    Unsafe myUnsafe = null;

    System.out.println("Run code with dependency problems.");

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

  /*********************************
   * Examples of deprecated elements
   *********************************/
  public static void jdeprscanDemo() {
    System.out.println("Run code with deprecated APIs.");

    // java.applet.Applet was marked as deprecated in Java 9
    new Applet();

    // java.lang.Compiler was deprecated and marked for removal in Java 9
    Compiler.enable();

    // import java.lang.Thread.destroy() was deprecated in 1.5 then removed in Java 11
    DemoRunnable demoRunnable = new DemoRunnable();
    Thread thread = new Thread(demoRunnable);
    thread.destroy();
  }
}
