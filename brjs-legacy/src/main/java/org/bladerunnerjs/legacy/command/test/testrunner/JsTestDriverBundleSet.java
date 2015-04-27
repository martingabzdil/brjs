package org.bladerunnerjs.legacy.command.test.testrunner;

import java.util.List;

import org.bladerunnerjs.api.Asset;
import org.bladerunnerjs.api.BundleSet;
import org.bladerunnerjs.api.LinkedAsset;
import org.bladerunnerjs.api.SourceModule;
import org.bladerunnerjs.api.BundlableNode;

public class JsTestDriverBundleSet implements BundleSet {
	private BundleSet bundleSet;
	
	public JsTestDriverBundleSet(BundleSet bundleSet) {
		this.bundleSet = bundleSet;
	}
	
	public BundlableNode bundlableNode() {
		return new JsTestDriverBundlableNode(bundleSet.bundlableNode());
	}
	
	@Override
	public List<Asset> assets(String... prefixes)
	{
		return bundleSet.assets(prefixes);
	}
	
	@Override
	public <AT extends Asset> List<AT> getAssets(Class<? extends AT> assetType, List<String> prefixes)
	{
		return bundleSet.getAssets(assetType, prefixes);
	}
	
	@Override
	public List<Asset> getAssets(List<String> prefixes, List<Class<? extends Asset>> assetTypes)
	{
		return bundleSet.getAssets(prefixes, assetTypes);
	}
	
	@Override
	public List<SourceModule> getSourceModules() {
		return bundleSet.getSourceModules();
	}
	
	@Override
	public <AT extends SourceModule> List<AT> getSourceModules(Class<? extends AT> assetType) {
		return bundleSet.getSourceModules(assetType);
	}
	
	@Override
	public List<SourceModule> getSourceModules(List<Class<? extends SourceModule>> assetTypes) {
		return bundleSet.getSourceModules(assetTypes);
	}

	@Override
	public List<LinkedAsset> seedAssets()
	{
		return bundleSet.seedAssets();
	}

}
