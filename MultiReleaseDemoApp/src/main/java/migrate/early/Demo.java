package migrate.early;

import java.applet.Applet;
import java.lang.Compiler;
import java.lang.Thread;

public class Demo {
  public static void main(String[] args) {
    VersionedCode.jdepsMultiDemo();
    jdeprscanDemo();
    System.out.println("End of demo.");
  }

  /*********************************
   * Examples of deprecated elements
   *********************************/
  public static void jdeprscanDemo() {
    System.out.println("Run code with deprecated APIs.");

    try {
      // java.applet.Applet was marked as deprecated in Java 9
      new Applet();

      // java.lang.Compiler was deprecated and marked for removal in Java 9
      Compiler.enable();

      // import java.lang.Thread.destroy() was deprecated in 1.5 then removed in Java 11
      DemoRunnable demoRunnable = new DemoRunnable();
      Thread thread = new Thread(demoRunnable);
      thread.destroy();
    } catch(Throwable e) {
      // ignore
    }
  }
}
