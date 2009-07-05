package org.jvending.masa.plugins.toolchains;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.model.PersistedToolchains;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.apache.maven.toolchain.model.io.xpp3.MavenToolchainsXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @goal toolchain
 * @phase validate
 */
public class ToolchainMojo
    extends AbstractMojo
{

    /**
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @parameter
     * @required
     */
    private Toolchains toolchains;
    
    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;    
 
    /**
     * @parameter
     */    
    private File toolchainsFile;
    

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
    	if(toolchainsFile == null)
    	{
    		toolchainsFile  = new File(new File(session.getLocalRepository().getBasedir()).getParentFile(), "toolchains.xml"); 
    	}
    	
    	PersistedToolchains toolchainModels = null;
        Reader in = null;
        try
        {
            in = ReaderFactory.newXmlReader( toolchainsFile );
            toolchainModels = new MavenToolchainsXpp3Reader().read( in );
        }
        catch ( Exception e )
        {
        	return;
        }
        finally
        {
            IOUtil.close( in );
        }

        Map<String, ToolchainModel> models = new HashMap<String, ToolchainModel>();
        
        //Capabilities
        List<List<Capability>> m = new ArrayList<List<Capability>>();
        for(ToolchainModel model : (List<ToolchainModel>) toolchainModels.getToolchains() )
        {
        	if(!model.getType().equals("android"))
        	{
        		continue;
        	}
        	        	
        	List<Capability> c = new ArrayList<Capability>();      	
            Xpp3Dom dom = (Xpp3Dom) model.getProvides();
            for(Xpp3Dom child : dom.getChildren())
            {
            	if(child.getName().equals("id"))
            	{
            		models.put(child.getValue(), model);
            	}
            	c.add(new Capability(child.getName(), child.getValue()));
            }
            m.add(c);
        }
        Matcher matcher = new Matcher(m);
        
        //Requirements from pom
        String capabilityId = matcher.findMatchIdFor(toolchains.android);   

        if(capabilityId == null)
        {
        	throw new MojoExecutionException("Could not match capability to toolchain requirements");
        }
        
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
        pluginDescriptor.setGroupId( "org.jvending.masa.plugins");
        pluginDescriptor.setArtifactId( PluginDescriptor.getDefaultPluginArtifactId( "toolchains" ) );
        session.getPluginContext(pluginDescriptor, project).put("toolchain", models.get(capabilityId));
        System.out.println("ID=" + capabilityId + ":" + models.get(capabilityId).getType());
        
    }

}