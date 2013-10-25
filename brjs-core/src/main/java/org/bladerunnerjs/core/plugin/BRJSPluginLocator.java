package org.bladerunnerjs.core.plugin;

import java.util.List;

import org.bladerunnerjs.core.plugin.bundler.BundlerPlugin;
import org.bladerunnerjs.core.plugin.command.CommandPlugin;
import org.bladerunnerjs.model.BRJS;



public class BRJSPluginLocator implements PluginLocator
{

	private TypedClassCreator<BundlerPlugin> bundlerPluginLocator = new TypedClassCreator<BundlerPlugin>();
	private TypedClassCreator<CommandPlugin> commandPluginLocator = new TypedClassCreator<CommandPlugin>();
	private TypedClassCreator<ModelObserverPlugin> modelObserverLocator = new TypedClassCreator<ModelObserverPlugin>();
	
	@Override
	public List<BundlerPlugin> createBundlerPlugins(BRJS brjs)
	{
		List<BundlerPlugin> plugins = bundlerPluginLocator.getSubTypesOfClass(brjs, BundlerPlugin.class);
		PluginLocatorUtils.setBRJSForPlugins(brjs, plugins);
		return plugins;
	}

	@Override
	public List<CommandPlugin> createCommandPlugins(BRJS brjs)
	{
		List<CommandPlugin> plugins = commandPluginLocator.getSubTypesOfClass(brjs, CommandPlugin.class);
		PluginLocatorUtils.setBRJSForPlugins(brjs, plugins);
		return plugins;
	}
	
	@Override
	public List<ModelObserverPlugin> createModelObservers(BRJS brjs)
	{
		List<ModelObserverPlugin> plugins = modelObserverLocator.getSubTypesOfClass(brjs, ModelObserverPlugin.class);
		PluginLocatorUtils.setBRJSForPlugins(brjs, plugins);
		return plugins;
	}
	
}
