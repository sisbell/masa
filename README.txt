### SDK path

You will need to either add both of the following paths to your environment

    * ${android-sdk-path}/tools
    * ${android-sdk-path}/platform-tools
    
    and set environmental variable
    
    ANDROID_SDK=${android-sdk-path}

OR

configure the environment through [Toolchains](https://github.com/sisbell/masa/wiki/toolchains)

### Packaging
The packaging type should be specified as below:

    <project>
      ....
      <packaging>android:apk</packaging>
      ....
     </project>

### Dependencies
Add the following dependency to your project to use the Android API. This jar is hosted on Nexus instance at zapvine.org and comes in transitively with the maven-aapt-plugin (this will change later).

	<dependencies>
	  <dependency>
	    <groupId>com.android</groupId>
            <artifactId>android</artifactId>
            <version>14</version>
          </dependency>	
	</dependencies>

### Build 
Make sure to set the resources directory to 'res' so that the maven project is compatible with Eclipse development environment. By setting the filtering option to 'true' you can interpolate Android resources with variables. Keep in mind that while useful for automated builds, adding property placeholders to Android resource files will break the Eclipse development environment.

The maven-aapt-plugin is the core masa plugin that you need to add. And don't forget to configure the output directory for processed resources with the maven-resources-plugin.

	<build>
		<sourceDirectory>src</sourceDirectory>
		<resources>
			<resource>
				<directory>res</directory>
				<filtering>true</filtering>
			</resource>
		</resources>
		<plugins>
			<plugin>
				<groupId>org.jvending.masa.plugins</groupId>
				<artifactId>maven-aapt-plugin</artifactId>
				<version>1.2-SNAPSHOT</version>
				<extensions>true</extensions>
			</plugin>
			<plugin>
				<artifactId>maven-resources-plugin</artifactId>
				<configuration>
					<outputDirectory>./target/processed-resources</outputDirectory>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>2.0.2</version>
				<configuration>
					<source>1.5</source>
					<target>1.5</target>
				</configuration>
			</plugin>
		</plugins>
	</build>

### Repository configuration 
The final step is to add the plugin respositories so you don't have to build masa directly. Currently, the masa plugins are deployed as snapshots (1.2-SNAPSHOT) but you can configure the location by adding the following to your pom.

	<pluginRepositories>
	  <pluginRepository>
	    <id>zapvine.snapshots</id>
	    <name>ZapVine SNAPSHOT Repository</name>
	    <url>http://zapvine.org/content/repositories/snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <releases>
                <enabled>false</enabled>
            </releases>
	</pluginRepository>
	<pluginRepository>
	    <id>zapvine.public</id>
	    <name>ZapVine Public Repository</name>
	    <url>http://zapvine.org/content/repositories/public/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <releases>
                <enabled>true</enabled>
            </releases>
	</pluginRepository>		
    </pluginRepositories>	