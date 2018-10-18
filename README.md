# Migrate early, migrate often! Tool demonstration
This demo is a supplement to the talk "Migrate early, migrate often!" at Oracle Code One 2018 and Eclipse EU 2018.

## Requirements: 
- install Docker (https://www.docker.com/get-started)

## Setup:
- Clone this repository
- Compile and run DemoApp application from the root repository directory with the following commands. For this example, Java 8 is the latest stable release I have tested my code with.
```
mkdir -p DemoApp/bin &&
mkdir -p DemoApp/target &&
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk8-openj9 javac -d /DemoApp/bin /DemoApp/src/migrate/early/Demo.java /DemoApp/src/migrate/early/DemoRunnable.java &&
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk8-openj9 jar cfe /DemoApp/target/DemoApp.jar migrate.early.Demo -C /DemoApp/bin . &&
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk8-openj9 java -jar /DemoApp/target/DemoApp.jar
```
## JDEPS
### Background
- JDEPS = Java Class Dependency Analyzer
- Support for Java 8+ JDKs
- Analyzes the dependencies by class or package (default) level
### Package level summary (default)
- Run `jdeps` with most recent JDK (currently 11) to view a list of dependencies of your application
- The default output is a summary of the modules that are being accessed followed by a list of package level dependencies
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeps /DemoApp/target/DemoApp.jar
```
```
Demo.class -> JDK removed internal API
Demo.class -> java.base
DemoApp.jar -> JDK removed internal API
DemoApp.jar -> java.base
DemoApp.jar -> java.desktop
DemoApp.jar -> jdk.unsupported
   migrate.early                                      -> java.applet                                        java.desktop
   migrate.early                                      -> java.io                                            java.base
   migrate.early                                      -> java.lang                                          java.base
   migrate.early                                      -> java.lang.reflect                                  java.base
   migrate.early                                      -> javax.accessibility                                java.desktop
   migrate.early                                      -> sun.misc                                           JDK internal API (jdk.unsupported)
   migrate.early                                      -> sun.misc                                           JDK internal API (JDK removed internal API)
```
### Class level summary
- The above package level dependencies don't give us many details about the internal APIs used in this application
- The command line option `-verbose:class` or `-v` shows dependencies at the class level which will be more useful when refactoring code to avoid internal APIs
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeps -v /DemoApp/target/DemoApp.jar
```
```
DemoApp.jar -> JDK removed internal API
DemoApp.jar -> java.base
DemoApp.jar -> java.desktop
DemoApp.jar -> jdk.unsupported
   migrate.early.Demo                                 -> java.applet.Applet                                 java.desktop
   migrate.early.Demo                                 -> java.io.PrintStream                                java.base
   migrate.early.Demo                                 -> java.lang.Class                                    java.base
   migrate.early.Demo                                 -> java.lang.Exception                                java.base
   migrate.early.Demo                                 -> java.lang.Object                                   java.base
   migrate.early.Demo                                 -> java.lang.String                                   java.base
   migrate.early.Demo                                 -> java.lang.System                                   java.base
   migrate.early.Demo                                 -> java.lang.reflect.Field                            java.base
   migrate.early.Demo                                 -> javax.accessibility.AccessibleResourceBundle       java.desktop
   migrate.early.Demo                                 -> sun.misc.BASE64Encoder                             JDK internal API (JDK removed internal API)
   migrate.early.Demo                                 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)
```
### Filtering dependencies
#### Filter by package
- `jdeps` option `-p packagename` shows only dependencies belonging to a certain package
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeps -v -p java.lang /DemoApp/target/DemoApp.jar
```
```
DemoApp.jar -> java.base
   migrate.early                                      -> java.lang                                          java.base
```
#### Filter in by pattern
- the above example does not show packages nested within java.lang. We can show these using the regex filter `java.lang.*`
- `-e` is the command line option
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeps -v -e java.lang.* /DemoApp/target/DemoApp.jar
```
```
DemoApp.jar -> java.base
   migrate.early                                      -> java.lang                                          java.base
   migrate.early                                      -> java.lang.reflect                                  java.base
```
#### Filter out by pattern
- `jdeps` option `-filter pattern` filters out  dependencies by regex pattern
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeps -v -filter java.lang.* /DemoApp/target/DemoApp.jar
```
```
DemoApp.jar -> JDK removed internal API
DemoApp.jar -> java.base
DemoApp.jar -> java.desktop
DemoApp.jar -> jdk.unsupported
   migrate.early                                      -> java.applet                                        java.desktop
   migrate.early                                      -> java.io                                            java.base
   migrate.early                                      -> javax.accessibility                                java.desktop
   migrate.early                                      -> sun.misc                                           JDK internal API (jdk.unsupported)
   migrate.early                                      -> sun.misc                                           JDK internal API (JDK removed internal API)
```
### Internal Dependencies
- `jdeps` also has a command line option to show only dependencies on JDK internal classes `-jdkinternals`
- This command may be useful to add to a Jenkins stage. The pipeline step should fail if there is output meaning a developer has added an internal class into the code base.
- Analyzes the dependencies ay class or package (default) level
- Not only are internal API dependencies listed, but more reliable alternatives suggested. For example BASE64Encoder has simply been renamed and VarHandles have been introduced as a safer alternative to sun.misc.Unsafe.
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeps -jdkinternals /DemoApp/target/DemoApp.jar
```
```
DemoApp.jar -> JDK removed internal API
DemoApp.jar -> jdk.unsupported
   migrate.early.Demo                                 -> sun.misc.BASE64Encoder                             JDK internal API (JDK removed internal API)
   migrate.early.Demo                                 -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)

Warning: JDK internal APIs are unsupported and private to JDK implementation that are
subject to be removed or changed incompatibly and could break your application.
Please modify your code to eliminate dependence on any JDK internal APIs.
For the most recent update on JDK internal API replacements, please check:
https://wiki.openjdk.java.net/display/JDK8/Java+Dependency+Analysis+Tool

JDK Internal API                         Suggested Replacement
----------------                         ---------------------
sun.misc.BASE64Encoder                   Use java.util.Base64 @since 1.8
sun.misc.Unsafe                          See http://openjdk.java.net/jeps/260
```
## JDEPRSCAN
### Background
- Support for Java 9+ JDKs
- Scans jar/class files to identify deprecated API elements
- Deprecated elements may cause behavioral issues or be removed
### List deprecated APIs
- `--list` all API elements that are marked as deprecated in the JDK
- includes `forRemoval()` and `since()` elements of the `@Deprecated` annotation
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeprscan --list
```
```
@Deprecated(since="1.5") class org.xml.sax.helpers.AttributeListImpl
@Deprecated(since="1.5") class org.xml.sax.helpers.ParserFactory
...
@Deprecated java.lang.String javax.management.monitor.StringMonitorMBean.getDerivedGauge()
@Deprecated long javax.management.monitor.StringMonitorMBean.getDerivedGaugeTimeStamp()
```
### Deprecated APIs used in DemoApp (default)
- Default output lists deprecated APIs for the JDK
- Error occurs here for `sun/misc/Base64Encoder` in this example because this class was renamed in Java 9 as was evident from the `jdeps` output previously
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeprscan /DemoApp/target/DemoApp.jar
```
```
Jar file /DemoApp/target/DemoApp.jar:
class migrate/early/Demo uses deprecated class java/applet/Applet
class migrate/early/Demo uses deprecated class java/lang/Compiler (forRemoval=true)
error: cannot find class sun/misc/BASE64Encoder
```
- **It's important to note that if a deprecated API was removed in the current release, it will not be recognized in the tool.** For example, `DemoApp` uses the element `java.lang.Thread.destroy()` that was deprecated in Java 10 and removed in Java 11. In our above example the tool does not point out that `Thread.destroy()` was deprecated or that it no longer exists in Java 11. Lets see what happens if we run this command with a Java 10 build:
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk10-openj9 jdeprscan /DemoApp/target/DemoApp.jar
```
```
Jar file /DemoApp/target/DemoApp.jar:
class migrate/early/Demo uses deprecated class java/applet/Applet
class migrate/early/Demo uses deprecated class java/lang/Compiler (forRemoval=true)
error: cannot find class sun/misc/BASE64Encoder
class migrate/early/Demo uses deprecated method java/lang/Thread::destroy()V (forRemoval=true)
```
- As is evident above Java 10 is aware of this deprecation while Java 11 is not. The most reliable way to catch this kind of error is compiling the project on both Java 8 and Java 11 in a parallel pipeline, or run `jdeprscan` on multiple JDK versions.
### Deprecated for removal
- best practice is to not use any deprecated APIs in your application. Deprecated APIs may be dangerous to use
- as a bare minimum make sure you aren't using deprecated classes that are marked for removal else they may not exist in the next release with `--for-removal`
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeprscan --for-removal /DemoApp/target/DemoApp.jar
```
```
Jar file /DemoApp/target/DemoApp.jar:
class migrate/early/Demo uses deprecated class java/lang/Compiler (forRemoval=true)
error: cannot find class sun/misc/BASE64Encoder
```
### Filter by release
- It is possible to restrict `jdeprscan` output to show classes deprecated at or before a certain release (`--release`). In this case since `java.applet.Applet` and `java.lang.Compiler` were deprecated in Java 9 they will be excluded if we are only looking at deprecated items in Java 8 and below.
- note that since we are running this command with Java 11 `Thread.destroy()` is not identified even though it was marked as deprecated before Java 8.
- the enum constant warning that is present in the output is a known bug in OpenJDK and is not meaningful to `DemoApp`
```
docker run --rm -it -v `pwd`/DemoApp/:/DemoApp adoptopenjdk/openjdk11-openj9 jdeprscan --release 8 /DemoApp/target/DemoApp.jar
```
```
warning: unknown enum constant javax.annotation.Resource.AuthenticationType.CONTAINER
Jar file /DemoApp/target/DemoApp.jar:
error: cannot find class sun/misc/BASE64Encoder
```
## Multi-release JAR
I can use a multi-release jar so my application supports JDK 8 and 9+ and still use JDEPs to continue migrating.
### Setup
- Compile and run MultiReleaseDemoApp, a multi-release JAR with the same behavior that will support JDK 8 and 9+
- First compile Java 8 shared code, then Java 9 specfic code, then create the multi-release JAR file
```
mkdir -p MultiReleaseDemoApp/bin/classes &&
mkdir -p MultiReleaseDemoApp/bin/classes-9 &&
mkdir -p MultiReleaseDemoApp/target &&
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk8-openj9 javac -d /MultiReleaseDemoApp/bin/classes  /MultiReleaseDemoApp/src/main/java/migrate/early/Demo.java /MultiReleaseDemoApp/src/main/java/migrate/early/VersionedCode.java /MultiReleaseDemoApp/src/main/java/migrate/early/DemoRunnable.java &&
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk9-openj9 javac -d /MultiReleaseDemoApp/bin/classes-9  --release 9 /MultiReleaseDemoApp/src/main/java-9/migrate/early/VersionedCode.java &&
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk9-openj9 jar cfe /MultiReleaseDemoApp/target/MultiReleaseDemoApp.jar migrate.early.Demo -C /MultiReleaseDemoApp/bin/classes . --release 9 -C /MultiReleaseDemoApp/bin/classes-9 . &&
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk8-openj9 java -jar /MultiReleaseDemoApp/target/MultiReleaseDemoApp.jar &&
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk9-openj9 java -jar /MultiReleaseDemoApp/target/MultiReleaseDemoApp.jar
```
### Multi release JARs with JDEPS
- The multi-release JAR you compiled during setup includes two different versions for `jdepsDemo()`, renamed `jdepsMultiDemo()`, replacing internal sun.misc APIs with the alternatives suggested by jdeps. This way my application can run on Java 8 as well as Java 9 and above.
- since Java 9 jdeps supports a `--multi-release` option allowing the user to view dependencies for various releases
- Java 8, or base jdeps output
```
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk9-openj9 jdeps -jdkinternals --multi-release base /MultiReleaseDemoApp/target/MultiReleaseDemoApp.jar
```
```
MultiReleaseDemoApp.jar -> JDK removed internal API
MultiReleaseDemoApp.jar -> jdk.unsupported
   migrate.early.VersionedCode                          -> sun.misc.BASE64Encoder                             JDK internal API (JDK removed internal API)
   migrate.early.VersionedCode                          -> sun.misc.Unsafe                                    JDK internal API (jdk.unsupported)

Warning: JDK internal APIs are unsupported and private to JDK implementation that are
subject to be removed or changed incompatibly and could break your application.
Please modify your code to eliminate dependence on any JDK internal APIs.
For the most recent update on JDK internal API replacements, please check:
https://wiki.openjdk.java.net/display/JDK8/Java+Dependency+Analysis+Tool

JDK Internal API                         Suggested Replacement
----------------                         ---------------------
sun.misc.BASE64Encoder                   Use java.util.Base64 @since 1.8
sun.misc.Unsafe                          See http://openjdk.java.net/jeps/260
```
- Java 9+ jdeps output. Notice there are no longer warnings about using internal APIs since this issue was fixed for Java 9+ in the multi-release jar
```
docker run --rm -it -v `pwd`/MultiReleaseDemoApp/:/MultiReleaseDemoApp adoptopenjdk/openjdk9-openj9 jdeps --jdk-internals --multi-release 11 /MultiReleaseDemoApp/target/MultiReleaseDemoApp.jar
```
```
(no output)
```
