For the latest documentation, go to: http://code.google.com/p/masa/wiki/GettingStarted

==Prerequisites==
  * JDK 1.5+
  * Apache Maven 2.0.8+ (2.0.9+ is required if you use toolchains)
  * Android SDK: http://code.google.com/android/download.html

==Steps to get working==

 First, you need to setup the environment. You can either:

  # Add ANDROID_SDK/tools to your path
  # Add ANDROID_SDK/platforms/android-1.[x]/tools to your path
  
  Run masa-project/setup.sh script prior to building your project (create script for windows). 


or configure Toolchains
 
  # Create toolchains.xml
  # Add maven-toolchains-plugin to your pom


See [http://code.google.com/p/masa/wiki/Toolchains Toolchains] for more information

 Next setup your project:

  # Create an android project: http://developer.android.com/guide/developing/tools/othertools.html#android or use an [http://code.google.com/p/masa/wiki/Archetypes Archetype] 
  # Create a pom.xml file for the project

The primary packaging type is android:apk

Sample POM
{{{
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.jvending.masa</groupId>
   <artifactId>maven-test</artifactId>
   <version>1.0.1-sandbox</version>
   <packaging>android:apk</packaging>
   <name>maven-test</name>
   <description>Maven Plugin for Android DX</description>
   <dependencies>
      <dependency>
         <groupId>com.google.android</groupId>
         <artifactId>android</artifactId>
         <version>1.5_r2</version>
      </dependency>
   </dependencies>
   <build>
      <sourceDirectory>src</sourceDirectory>
      <plugins>
         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
               <source>1.5</source>
               <target>1.5</target>
            </configuration>
         </plugin>
         <plugin>
            <groupId>org.jvending.masa.plugins</groupId>
            <artifactId>maven-aapt-plugin</artifactId>
            <extensions>true</extensions>
         </plugin>
      </plugins>
   </build>
</project>
}}}

Next point to the remote repo:

Add the following to your pom or settings.xml file. You can use the Masa snapshots, without having to build from trunk.
{{{
    <repositories>
        <repository>
            <id>slideme.snapshots</id>
            <name>SlideME SNAPSHOT Repository</name>
            <url>http://repository.slideme.org/nexus/content/groups/public-snapshots</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <releases>
                <enabled>false</enabled>
            </releases>
        </repository>
        <repository>
            <id>slideme.releases</id>
            <name>Maven Release Repository</name>
            <url>http://repository.slideme.org/nexus/content/groups/public</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <releases>
                <enabled>true</enabled>
            </releases>
        </repository>
    </repositories>
}}}

Now, build project: 'mvn install' or to also deploy to a running emulator: 'mvn install -Dmasa.debug'

For doing platform unit tests, use android:apk:platformTest. Sample pom.xml:

{{{
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
   <modelVersion>4.0.0</modelVersion>
   <groupId>org.jvending.masa</groupId>
   <artifactId>ApiDemosPlatformTest</artifactId>
   <version>0.9_beta</version>
   <packaging>android:apk:platformTest</packaging>
   <name>maven-test</name>
   <description>Maven Plugin for Android DX</description>
   <dependencies>
      <dependency>
         <groupId>android</groupId>
         <artifactId>android</artifactId>
         <version>0.9_beta</version>
      </dependency>
      <dependency>
         <groupId>org.jvending.masa</groupId>
         <artifactId>ApiDemos</artifactId>
         <version>0.9_beta</version>
         <type>jar</type>
      </dependency>
   </dependencies>
   <build>
      <sourceDirectory>src</sourceDirectory>
      <plugins>
         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
               <source>1.5</source>
               <target>1.5</target>
            </configuration>
         </plugin>
         <plugin>
            <groupId>org.jvending.masa.plugins</groupId>
            <artifactId>maven-dx-plugin</artifactId>
            <extensions>true</extensions>
         </plugin>
      </plugins>
   </build>
</project>

}}}

To build and deploy the test apk to the emulator, type: 'maven integration-test -Dmasa.debug'
