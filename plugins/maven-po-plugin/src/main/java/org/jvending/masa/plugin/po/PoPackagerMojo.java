/*
 * Copyright (C) 2007-2008 JVending Masa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvending.masa.plugin.po;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * @goal package
 * @requiresProject true
 * @description
 */
public class PoPackagerMojo extends AbstractMojo {

	/**
	 * The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Input directory
	 * 
	 * @parameter expression = "${inputDirectory}"
	 *            default-value="${project.build.directory}/po"
	 * @required
	 */
	private File inputDir;

	/**
	 * Classifier
	 * 
	 * @parameter expression = "${classifier}"
	 */
	private String classifier;

	/**
	 * @component
	 */
	private MavenProjectHelper projectHelper;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!inputDir.exists()) {
			getLog().info("No po resource files to package.");
			return;
		}

		File file = new File(project.getBuild().getDirectory(), project
				.getBuild().getFinalName()
				+ ((classifier != null) ? "-" + classifier : "") + ".pozip");

		ZipArchiver archiver = new ZipArchiver();
		archiver.setForced(true);
		archiver.setDestFile(file);

		try {
			archiver.addDirectory(inputDir);
			archiver.createArchive();
		} catch (ArchiverException e) {
			throw new MojoExecutionException("", e);
		} catch (IOException e) {
			throw new MojoExecutionException("", e);
		}

		if (classifier != null) {
			projectHelper.attachArtifact(project, "android:po", classifier,
					file);
		} else {
			project.getArtifact().setFile(file);
		}
	}
}