package org.bladerunnerjs.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;

import org.bladerunnerjs.appserver.ApplicationServer;
import org.bladerunnerjs.appserver.BRJSApplicationServer;
import org.bladerunnerjs.console.ConsoleWriter;
import org.bladerunnerjs.console.PrintStreamConsoleWriter;
import org.bladerunnerjs.logging.LogConfiguration;
import org.bladerunnerjs.logging.Logger;
import org.bladerunnerjs.logging.LoggerFactory;
import org.bladerunnerjs.logging.LoggerType;
import org.bladerunnerjs.logging.SLF4JLoggerFactory;
import org.bladerunnerjs.model.engine.Node;
import org.bladerunnerjs.model.engine.NodeItem;
import org.bladerunnerjs.model.engine.NodeList;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.InvalidSdkDirectoryException;
import org.bladerunnerjs.model.exception.command.CommandArgumentsException;
import org.bladerunnerjs.model.exception.command.CommandOperationException;
import org.bladerunnerjs.model.exception.command.NoSuchCommandException;
import org.bladerunnerjs.model.exception.modelupdate.ModelUpdateException;
import org.bladerunnerjs.plugin.PluginLocator;
import org.bladerunnerjs.plugin.utility.BRJSPluginLocator;
import org.bladerunnerjs.plugin.utility.PluginAccessor;
import org.bladerunnerjs.plugin.utility.command.CommandList;
import org.bladerunnerjs.utility.CommandRunner;
import org.bladerunnerjs.utility.PluginLocatorLogger;
import org.bladerunnerjs.utility.UserCommandRunner;
import org.bladerunnerjs.utility.VersionInfo;
import org.bladerunnerjs.utility.filemodification.FileModificationInfo;
import org.bladerunnerjs.utility.filemodification.FileModificationService;
import org.bladerunnerjs.utility.filemodification.Java7FileModificationService;


public class BRJS extends AbstractBRJSRootNode
{
	public static final String PRODUCT_NAME = "BladeRunnerJS";
	
	public class Messages {
		public static final String PERFORMING_NODE_DISCOVERY_LOG_MSG = "performing node discovery";
		public static final String CREATING_PLUGINS_LOG_MSG = "creating plugins";
		public static final String MAKING_PLUGINS_AVAILABLE_VIA_MODEL_LOG_MSG = "making plugins available via model";
		public static final String PLUGIN_FOUND_MSG = "found plugin %s";
		public static final String CLOSE_METHOD_NOT_INVOKED = "the BRJS.close() method was not manually invoked, which causes resource leaks that can lead to failure.";
	}
	
	private final NodeList<App> apps = new NodeList<>(this, App.class, "apps", null);
	private final NodeList<App> systemApps = new NodeList<>(this, App.class, "sdk/system-applications", null);
	private final NodeList<SdkJsLib> sdkLibs = new NodeList<>(this, SdkJsLib.class, "sdk/libs/javascript/br-libs", null);
	private final NodeList<SdkJsLib> sdkNonBladeRunnerLibs = new NodeList<>(this, SdkJsLib.class, "sdk/libs/javascript/thirdparty", null);
	private final NodeItem<DirNode> jsPatches = new NodeItem<>(this, DirNode.class, "js-patches");
	private final NodeList<NamedDirNode> templates = new NodeList<>(this, NamedDirNode.class, "sdk/templates", "-template$");
	private final NodeItem<DirNode> appJars = new NodeItem<>(this, DirNode.class, "sdk/libs/java/application");
	private final NodeItem<DirNode> configuration = new NodeItem<>(this, DirNode.class, "conf");
	private final NodeItem<DirNode> systemJars = new NodeItem<>(this, DirNode.class, "sdk/libs/java/system");
	private final NodeItem<DirNode> testJars = new NodeItem<>(this, DirNode.class, "sdk/libs/java/testRunner");
	private final NodeItem<DirNode> userJars = new NodeItem<>(this, DirNode.class, "conf/java");
	private final NodeItem<DirNode> logs = new NodeItem<>(this, DirNode.class, "sdk/log");
	private final NodeItem<DirNode> apiDocs = new NodeItem<>(this, DirNode.class, "sdk/docs/jsdoc");
	private final NodeItem<DirNode> testResults = new NodeItem<>(this, DirNode.class, "sdk/test-results");
	
	private WorkingDirNode workingDir;
	private final Logger logger;
	private final CommandList commandList;
	private BladerunnerConf bladerunnerConf;
	private TestRunnerConf testRunnerConf;
	private final Map<Integer, ApplicationServer> appServers = new HashMap<Integer, ApplicationServer>();
	private final Map<String, FileInfo> fileInfos = new HashMap<>();
	private final PluginAccessor pluginAccessor;
	private final FileModificationService fileModificationService;
	private final IO io = new IO();
	private final File libsDir = file("sdk/libs/javascript");
	private boolean closed = false;
	
	public BRJS(File brjsDir, PluginLocator pluginLocator, FileModificationService fileModificationService, LoggerFactory loggerFactory, ConsoleWriter consoleWriter) throws InvalidSdkDirectoryException
	{
		super(brjsDir, loggerFactory, consoleWriter);
		this.workingDir = new WorkingDirNode(this, brjsDir);
		
		fileModificationService.setRootDir(dir);
		
		this.fileModificationService = fileModificationService;
		
		logger = loggerFactory.getLogger(LoggerType.CORE, BRJS.class);
		
		logger.info(Messages.CREATING_PLUGINS_LOG_MSG);
		pluginLocator.createPlugins(this);
		PluginLocatorLogger.logPlugins(logger, pluginLocator);
		
		logger.info(Messages.PERFORMING_NODE_DISCOVERY_LOG_MSG);
		discoverAllChildren();
		
		logger.info(Messages.MAKING_PLUGINS_AVAILABLE_VIA_MODEL_LOG_MSG);
		
		pluginAccessor = new PluginAccessor(this, pluginLocator);
		commandList = new CommandList(this, pluginLocator.getCommandPlugins());
	}

	public BRJS(File brjsDir, LoggerFactory loggerFactory, ConsoleWriter consoleWriter) throws InvalidSdkDirectoryException {
		this(brjsDir, new BRJSPluginLocator(), new Java7FileModificationService(loggerFactory), loggerFactory, consoleWriter);
	}
	
	public BRJS(File brjsDir, FileModificationService fileModificationService) throws InvalidSdkDirectoryException {
		this(brjsDir, new BRJSPluginLocator(), fileModificationService, new SLF4JLoggerFactory(), new PrintStreamConsoleWriter(System.out));
	}
	
	public BRJS(File brjsDir, LogConfiguration logConfiguration) throws InvalidSdkDirectoryException
	{
		// TODO: what was the logConfiguration parameter going to be used for?
		this(brjsDir, new SLF4JLoggerFactory(), new PrintStreamConsoleWriter(System.out));
	}
	
	public BRJS(File brjsDir, LogConfiguration logConfigurator, FileModificationService fileModificationService) throws InvalidSdkDirectoryException {
		// TODO: what was the logConfiguration parameter going to be used for?
		this(brjsDir, new BRJSPluginLocator(), fileModificationService, new SLF4JLoggerFactory(), new PrintStreamConsoleWriter(System.out));
	}
	
	@Override
	public boolean isRootDir(File dir)
	{
		File sdkDir = new File(dir, "sdk");
		
		return (sdkDir.exists() && sdkDir.isDirectory());
	}
	
	@Override
	public void addTemplateTransformations(Map<String, String> transformations) throws ModelUpdateException {
	}
	
	@Override
	public void create() throws InvalidNameException, ModelUpdateException {
		// do nothing
	}
	
	@Override
	public void populate() throws InvalidNameException, ModelUpdateException {
		try {
			super.populate();
			bladerunnerConf().write();
		}
		catch (ConfigException e) {
			if(e.getCause() instanceof InvalidNameException) {
				throw (InvalidNameException) e.getCause();
			}
			else {
				throw new ModelUpdateException(e);
			}
		}
	}
	
	@Override
	public void finalize() {
		if(!closed) {
			logger.error(Messages.CLOSE_METHOD_NOT_INVOKED);
			close();
		}
	}
	
	public void close() {closed  = true;
		fileModificationService.close();
	}
	
	public BundlableNode locateFirstBundlableAncestorNode(File file)
	{
		Node node = locateFirstAncestorNode(file);
		BundlableNode bundlableNode = null;
		
		while((node != null) && (bundlableNode == null))
		{
			if(node instanceof BundlableNode)
			{
				bundlableNode = (BundlableNode) node;
			}
			
			node = node.parentNode();
		}
		
		return bundlableNode;
	}
	
	public WorkingDirNode workingDir() {
		return workingDir;
	}
	
	public void setWorkingDir(File workingDir) {
		this.workingDir = new WorkingDirNode(this, workingDir);
	}
	
	@Override
	public IO io() {
		return io;
	}
	
	public List<App> apps()
	{
		return apps.list();
	}
	
	public App app(String appName)
	{
		return apps.item(appName);
	}
	
	public List<App> systemApps()
	{
		return systemApps.list();
	}
	
	public App systemApp(String appName)
	{
		return systemApps.item(appName);
	}
	
	public File libsDir() {
		return libsDir;
	}
	
	public List<SdkJsLib> sdkLibs()
	{
		return new ArrayList<SdkJsLib>( sdkLibs.list() );
	}
	
	public JsLib sdkLib(String libName)
	{
		return sdkLibs.item(libName);
	}
	
	public List<SdkJsLib> sdkNonBladeRunnerLibs()
	{
		List<SdkJsLib> typeCastLibs = new ArrayList<>();
		for (SdkJsLib jsLib : sdkNonBladeRunnerLibs.list())
		{
			typeCastLibs.add(jsLib);
		}
		return typeCastLibs;
	}
	
	public SdkJsLib sdkNonBladeRunnerLib(String libName)
	{
		return sdkNonBladeRunnerLibs.item(libName);
	}
	
	public DirNode jsPatches()
	{
		return jsPatches.item();
	}
	
	public List<NamedDirNode> templates()
	{
		return templates.list();
	}
	
	public NamedDirNode template(String templateName)
	{
		return templates.item(templateName);
	}
	
	// TODO: delete this method -- the test results should live within a generated directory
	public DirNode testResults()
	{
		return testResults.item();
	}
	
	public DirNode appJars()
	{
		return appJars.item();
	}
	
	public DirNode conf()
	{
		return configuration.item();
	}
	
	public DirNode systemJars()
	{
		return systemJars.item();
	}
	
	public DirNode testJars()
	{
		return testJars.item();
	}
	
	public DirNode userJars()
	{
		return userJars.item();
	}
	
	public DirNode logs()
	{
		return logs.item();
	}
	
	public DirNode apiDocs()
	{
		return apiDocs.item();
	}
	
	public VersionInfo versionInfo()
	{
		return new VersionInfo(this);
	}
	
	public File loginRealmConf()
	{
		return new File(dir(), "sdk/loginRealm.conf");
	}
	
	public File usersPropertiesConf()
	{
		return new File(dir(), "conf/users.properties");
	}
	
	public BladerunnerConf bladerunnerConf() throws ConfigException {
		if(bladerunnerConf == null) {
			bladerunnerConf = new BladerunnerConf(this);
		}
		
		return bladerunnerConf;
	}
	
	public TestRunnerConf testRunnerConf() throws ConfigException {
		if(testRunnerConf == null) {
			testRunnerConf = new TestRunnerConf(this);
		}
		
		return testRunnerConf;
	}
	
	public PluginAccessor plugins() {
		return pluginAccessor;
	}
	
	public int runCommand(String... args) throws NoSuchCommandException, CommandArgumentsException, CommandOperationException
	{
		return CommandRunner.run(commandList, args);
	}
	
	public int runUserCommand(LogLevelAccessor logLevelAccessor, String... args) throws CommandOperationException
	{
		return UserCommandRunner.run(this, commandList, logLevelAccessor, args);
	}
	
	public ApplicationServer applicationServer() throws ConfigException
	{
		return applicationServer( bladerunnerConf().getJettyPort() );
	}
	
	public ApplicationServer applicationServer(int port)
	{
		ApplicationServer appServer = appServers.get(port);
		if (appServer == null)
		{
			appServer = new BRJSApplicationServer(this, port);
			appServers.put(port, appServer);
		}
		return appServer;
	}
	
	public FileInfo getFileInfo(File file) {
		String filePath = file.getPath();
		
		if(!fileInfos.containsKey(filePath)) {
			FileModificationInfo fileModificationInfo = fileModificationService.getModificationInfo(file);
			fileInfos.put(filePath, new StandardFileInfo(file, this,  fileModificationInfo));
		}
		
		return fileInfos.get(filePath);
	}
}
