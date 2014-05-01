package org.bladerunnerjs.plugin.plugins.bundlers.css;

import java.io.File;
import java.util.List;

import org.bladerunnerjs.model.Asset;
import org.bladerunnerjs.model.AssetFileInstantationException;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.FileAsset;
import org.bladerunnerjs.plugin.base.AbstractAssetPlugin;

public class CssAssetPlugin extends AbstractAssetPlugin {
	@Override
	public void setBRJS(BRJS brjs) {
		super.setBRJS(brjs);
		// do nothing
	}
	
	@Override
	public List<Asset> getAssets(AssetLocation assetLocation) {
		return assetLocation._getAssets(this);
	}
	
	@Override
	public boolean canHandleAsset(File assetFile, AssetLocation assetLocation) {
		return assetFile.getName().endsWith(".css");
	}
	
	@Override
	public Asset createAsset(File assetFile, AssetLocation assetLocation) throws AssetFileInstantationException {
		return new FileAsset(assetLocation, assetFile.getParentFile(), assetFile.getName());
	}
}
