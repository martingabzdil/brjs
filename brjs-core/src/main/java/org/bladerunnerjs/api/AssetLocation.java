package org.bladerunnerjs.api;

import java.util.List;

import org.bladerunnerjs.api.aliasing.NamespaceException;
import org.bladerunnerjs.api.aliasing.aliasdefinitions.AliasDefinitionsFile;
import org.bladerunnerjs.api.memoization.MemoizedFile;
import org.bladerunnerjs.api.model.exception.RequirePathException;
import org.bladerunnerjs.api.plugin.LegacyAssetPlugin;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.model.BRJSNode;

/**
 * Represents the parent directory of any AssetFile, so in the example src/a/b/c/someFile.xml it would represent the src/a/b/c directory.
 * Due to legacy resources the AssetLocation for any file in resources is always the resources directory. So in the example resources/a/b/c/someFile.xml, resouces is the AssetLocation.
 *
 */
public interface AssetLocation extends BRJSNode {
	String requirePrefix();
	AssetLocation parentAssetLocation();
	AssetContainer assetContainer();
	List<AssetLocation> dependentAssetLocations();
	AliasDefinitionsFile aliasDefinitionsFile();
	List<AliasDefinitionsFile> aliasDefinitionsFiles();
	List<LinkedAsset> linkedAssets();
	List<Asset> bundlableAssets(LegacyAssetPlugin assetProducer);
	List<SourceModule> sourceModules();
	String jsStyle();
	void assertIdentifierCorrectlyNamespaced(String identifier) throws NamespaceException, RequirePathException;
	List<MemoizedFile> getCandidateFiles();
}
