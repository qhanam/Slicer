public class Test {
	public ILaunchConfiguration[] getLaunchConfigurations (ILaunchConfigurationType type)  {
		Iterator iter = getAllLaunchConfigurations().iterator();
		List configs = new ArrayList();
		
		while (iter.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)iter.next();
	
			
			if (config.getType().equals(type)) { 
				configs.add(config);
			}
		}
		return (ILaunchConfiguration[])configs.toArray
				(new ILaunchConfiguration[configs.size()]);
	}
}