package org.jvending.masa.plugins.toolchains;

import java.util.List;
import java.util.Map;

public class Matcher {
	
	private List<List<Capability>> capabilities;
	
	public Matcher(List<List<Capability>> capabilities)
	{
		if(capabilities == null || capabilities.isEmpty())
		{
			throw new IllegalArgumentException("capabilities: null or empty");
		}
		this.capabilities = capabilities;	
	}

	public String findMatchIdFor(Map<String, String> requirements)
	{
		if(requirements == null || requirements.isEmpty())
		{
			return "masa:default";
		}
		for(List<Capability> cs : capabilities)
		{			
			boolean isMatch = true;
			for(Map.Entry<String, String> entry : requirements.entrySet())
			{			
					if(!isMatch(cs, entry.getKey(), entry.getValue()))
					{
						isMatch = false;
						break;
					}
			}
			if(isMatch)
			{
				return getIdFrom(cs);
			}
		}
		
		return null;	
	}
	
	private static String getIdFrom(List<Capability> capabilities)
	{
		for(Capability c : capabilities)
		{
			if(c.name.equals("id"))
			{
				return c.value;
			}
		}
		throw new IllegalArgumentException("capabilities: no id found");
	}
	
	private static boolean isMatch(List<Capability> cs, String name, String value)
	{
		for(Capability c : cs)
		{
			if(c.name.equals(name) && c.value.equals(value))
			{
				return true;
			}
		}
		return false;
	}
}
