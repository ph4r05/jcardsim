jCardSim (Official repo of the [jCardSim](http://jcardsim.org) project)
========

### Congratulations! jCardSim has won [Duke's Choice 2013 Award](https://www.java.net/dukeschoice/2013)!

![alt text](https://licelus.com/wp-content/uploads/DCA2013_Badge_Winner.jpg "jCardSim is a winner of Duke's Choice 2013")

This is a forked repo with additional fixes, e.g., POM file fixes, `-noverify` not needed / bytecode generation fixes, 
java 9+ compilation fixes.

jCardSim is an open source simulator for Java Card, v.2.2/3.0.5:

* `javacard.framework.*`
* `javacard.framework.security.*`
* `javacardx.crypto.*`

Key Features:

* Rapid application prototyping
* Simplifies unit testing (5 lines of code)

```java
// 1. create simulator
CardSimulator simulator = new CardSimulator();

// 2. install applet
AID appletAID = AIDUtil.create("F000000001");
simulator.installApplet(appletAID, HelloWorldApplet.class);

// 3. select applet
simulator.selectApplet(appletAID);

// 4. send APDU
CommandAPDU commandAPDU = new CommandAPDU(0x00, 0x01, 0x00, 0x00);
ResponseAPDU response = simulator.transmitCommand(commandAPDU);

// 5. check response
assertEquals(0x9000, response.getSW());
```

* Emulation of Java Card Terminal, ability to use `javax.smartcardio`
* APDU scripting (scripts are compatible with `apdutool` from Java Card Development Kit)
* Simplifies verification tests creation (Common Criteria)

*JavaDoc*: https://github.com/licel/jcardsim/tree/master/javadoc

  (Javadoc rendered: https://jcardsim.org/jcardsim/)

*Latest stable release 2.2.1*: https://github.com/licel/jcardsim/raw/master/jcardsim-2.2.1-all.jar

*Latest stable release 2.2.2*: https://github.com/licel/jcardsim/raw/master/jcardsim-2.2.2-all.jar

## Maven
*Maven Central Repository*
```xml
<dependency>
  <groupId>com.klinec</groupId>
  <artifactId>jcardsim</artifactId>
  <version>3.0.5.11</version>
</dependency>
```

Gradle:
```groovy
implementation('com.klinec:jcardsim:3.0.5.11')
```

### RandomData

RandomData class from the javacard API generates random numbers. It is implemented by RandomDataImpl in the JCardSim and by default it's seed is fixed, so after the applet load the random number stream is always the same. The original reason may be having deterministic tests, for example.

However, in [this PR](https://github.com/licel/jcardsim/pull/155), which is already merged in my fork, there are options how to change the default behavior of the RandomData.

If you wish to change the static seed used to seed the RandomData, add the following line before loading the applet (mainly before initializing RandomData), the seed is hex-coded string:

```java
System.setProperty("com.licel.jcardsim.randomdata.seed", "02");
```

Or if you want to seed the RandomData from the SecureRandom (non-deterministic):

```java
System.setProperty("com.licel.jcardsim.randomdata.secure", "1");
```

https://github.com/licel/jcardsim/pull/155

### What is the difference from Oracle Java Card Development Kit simulator?

* **Implementation of javacard.security.***

  One of the main differences is the implementation of `javacard.security.*`: the current version is analogous to an NXP JCOP 31/36k card. For example, in jCardSim we have support for on-card `KeyPair.ALG_EC_F2M/ALG_RSA_CRT` key generation. Oracle's simulator only supports `KeyPair.ALG_RSA` and `KeyPair.ALG_EC_FP`, which are not supported by real cards.

* **Execution of Java Card applications without converting into CAP**

  jCardSim can work with class files without any conversions. This allows us to simplify and accelerate the development and writing of unit tests.

* **Simulator API**

  jCardSim has a simple and usable API, which also allows you to work with the simulator using `javax.smartcardio.*`.

* **Cross-platform**

  jCardSim is completely written in Java and can therefore be used on all platforms which support Java (Windows, Linux, MacOS, etc).

### How to help jCardSim?

* Join the team of jCardSim developers.
* Try out [DexProtector](http://dexprotector.com). The product is designed for strong and robust protection of Android applications against reverse engineering and modification.
* Licel has one more product you may be interested in - [Stringer Java Obfuscator](https://jfxstore.com/stringer). This tool provides all the features you need to comprehensively protect your Java applications.

**License**: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

**Third-party libraries**: [Legion of the Bouncy Castle Java](http://www.bouncycastle.org/java.html)

**Trademarks**: Oracle, Java and Java Card are trademarks of Oracle Corporation.
