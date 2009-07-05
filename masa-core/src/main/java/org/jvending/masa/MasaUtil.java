package org.jvending.masa;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class MasaUtil {
	
	public static String getToolnameWithPath(MavenSession session, MavenProject project, String toolname) {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
		pluginDescriptor.setGroupId("org.jvending.masa.plugins");
		pluginDescriptor.setArtifactId(PluginDescriptor
				.getDefaultPluginArtifactId("toolchains"));

		ToolchainModel model = ((ToolchainModel) session.getPluginContext(
				pluginDescriptor, project).get("toolchain"));
		if(model == null)
		{
			return toolname;
		}
		String command = toolname;
		Xpp3Dom dom = (Xpp3Dom) model.getConfiguration();
		for (Xpp3Dom d : dom.getChild("toolPaths").getChildren()) {
			if (new File(d.getValue(), command).exists()) {
				command = new File(d.getValue(), command).getAbsolutePath();
				break;
			}
		}
		return command;
	}
	
	
    public static File getAndroidJarFile(MavenProject project) throws MojoExecutionException {
        for(Artifact artifact : (Set<Artifact>) project.getArtifacts())
        {
        	if(artifact.getGroupId().equals("com.google.android") && artifact.getArtifactId().equals("android"))
        	{
        		return artifact.getFile();
        	}
        }
        
        throw new MojoExecutionException("Could not resolve android.jar artifact ");
    }
}
