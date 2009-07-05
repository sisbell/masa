package org.jvending.masa.plugins.toolchains;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Capability {

	final String name;
	
	final String value;
	
	public Capability(String name, String value)
	{
		this.name =name;
		this.value = value;
	}
	
	public static List<Capability> toCapabilities(Map<String, String> capabilities)
	{
		List<Capability> c = new ArrayList<Capability>();
        for(Entry<String, String> toolchain : capabilities.entrySet())
        {

        		c.add(new Capability(toolchain.getKey(), toolchain.getValue()));
        }		
		return c;
	}
}
