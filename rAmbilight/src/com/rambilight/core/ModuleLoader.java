package com.rambilight.core;

import com.rambilight.core.api.Global;
import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.Platform;
import com.rambilight.core.api.ui.MessageBox;
import com.rambilight.core.preferences.Preferences;
import com.rambilight.plugins.Module;
import com.rambilight.plugins.extensions.Extension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class for loading and handling modules
 */

enum ModuleType {
	MODULE,
	EXTENSION,
	UNKNOWN
}

class ModuleDefinition {
	String                     name;
	Class<? extends Module>    module;
	Class<? extends Extension> extension;
	ModuleType                 type;

	public ModuleDefinition() {
		this.type = ModuleType.UNKNOWN;
	}

	public ModuleDefinition(Class<? extends Module> module) {
		this.type = ModuleType.MODULE;
		this.module = module;
	}

	public ModuleDefinition(String name, Class<? extends Extension> extension) {
		this.type = ModuleType.EXTENSION;
		this.extension = extension;
		this.name = name;
	}
}

class ExternalClassLoader {

	private static final String packageName = "com.rambilight.plugins";

	private ArrayList<Class<? extends Module>>            modules    = new ArrayList<>();
	private Hashtable<String, Class<? extends Extension>> extensions = new Hashtable<>();

	private static String pluginPath = Global.applicationSupportPath + "/plugins";

	private Class<?> classLoaderSource;

	public ExternalClassLoader(Class<?> classLoaderSource) {
		this.classLoaderSource = classLoaderSource;
	}

	private String getExecutablePath() {
		String path = "";
		try {
			path = ExternalClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("[^/]*$", "");
		} catch (URISyntaxException ignored) { }

		return path;
	}


	private ArrayList<URL> getFileList(String path) {
		ArrayList<URL> urls = new ArrayList<>();

		try {
			for (String fileName : new File(path).list()) {
				// Filter away any unwanted files

				if (fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".rlplugin")) {
					String filePath = Platform.getFilePathFormat(path + "/" + fileName);
					urls.add(new URL(filePath));
				}
			}
		} catch (NullPointerException | MalformedURLException ignored) { }

		return urls;
	}


	private ModuleDefinition getModuleDefByName(String path, URLClassLoader classLoader) {
		// Parse the path into a name. Remove everything before the last "/"(Path) and after the last "."(Extension)
		String name = path.substring(path.lastIndexOf("plugins/") + 8, path.lastIndexOf("."));

		ModuleDefinition definition;

		// Try to load packages as a module
		try {
			Class<? extends Module> module = loadModule(name, classLoader);
			definition = new ModuleDefinition(module);

			System.out.println("Successfully loaded module '" + name + "'");
		} catch (ClassNotFoundException e) {
			try {
				Class<? extends Extension> extension = loadExtension(name, classLoader);

				definition = new ModuleDefinition(name, extension);

				System.out.println("Successfully loaded extension " + name);
			} catch (ClassNotFoundException e2) {
				definition = new ModuleDefinition();
				System.err.println("Failed to load '" + name + "' as an associated file");
			}
		}

		return definition;
	}


	public void loadExternalModules() {

		ArrayList<URL> pathList = new ArrayList<>();

		pathList.addAll(getPluginFileList());
		pathList.addAll(getDefaultFileList());
		pathList.addAll(getDevFileList());

		URL[] pluginPaths = pathList.toArray(new URL[pathList.size()]);
		URLClassLoader classLoader = URLClassLoader.newInstance(pluginPaths, classLoaderSource.getClassLoader());

		for (URL pluginPath : pluginPaths) {
			ModuleDefinition definition = getModuleDefByName(pluginPath.toString(), classLoader);

			switch (definition.type) {
				case MODULE:
					modules.add(definition.module);
					break;
				case EXTENSION:
					extensions.put(definition.name, definition.extension);
					break;
			}
		}

		if (pluginPaths.length == 0) {
			MessageBox.Error("No plugins where found", "No plugins found in the plugins folder '" + pluginPath + "'");
		}
	}

	private Class<? extends Module> loadModule(String className, URLClassLoader classLoader) throws ClassNotFoundException {
		Class<?> classToLoad = classLoader.loadClass(packageName + "." + className + "." + className);

		// Make sure it's a subclass of "Module".
		return classToLoad.asSubclass(Module.class);
	}

	private Class<? extends Extension> loadExtension(String classPath, URLClassLoader classLoader) throws ClassNotFoundException {
		String[] pieces = classPath.split(".");
		String pluginName = pieces[0];
		String extensionName = pieces[1];

		Class<?> classToLoad = classLoader.loadClass(packageName + "." + pluginName + ".extensions." + extensionName);

		return classToLoad.asSubclass(Extension.class);
	}


	private ArrayList<URL> getDefaultFileList() {
		String path = getExecutablePath() + "../plugins";
		return getFileList(path);
	}

	private ArrayList<URL> getPluginFileList() {
		String path = pluginPath;
		return getFileList(path);
	}

	private ArrayList<URL> getDevFileList() {
		String path = getExecutablePath() + "../../dist/rAmbilight.app/Contents/Resources/plugins";
		return getFileList(path);
	}


	public ArrayList<Class<? extends Module>> getModules() {
		return modules;
	}

	public Class<?>[] getModulesAsArray() {
		return modules.toArray(new Class<?>[modules.size()]);
	}

	public Hashtable<String, Class<? extends Extension>> getExtensions() {
		return extensions;
	}

}

public class ModuleLoader {

	// Fields for saving the modules as well as attributes that are related to them
	private static Hashtable<String, Class<?>> availableExtensions = new Hashtable<>();
	private static Hashtable<String, Class<?>> availableModules    = new Hashtable<>();
	private static Hashtable<String, Module>   loadedModules       = new Hashtable<>();
	private static List<String>                activeModules       = new ArrayList<>();
	private static List<OnChangeListener>      onChangeListeners   = new ArrayList<>();

	public static void loadModules(Class<?> modules[]) {
		for (Class<?> module : modules)
			loadModule(module);
	}

	public static void loadModule(Class<?> module) {
		String name = module.getSimpleName().replace("_", " ");
		if (Module.class.isAssignableFrom(module))
			if (!availableModules.containsKey(name))
				availableModules.put(name, module);
			else
				System.err.println("Unable to load module '" + name + "' since it's already loaded.");
		else
			System.err.println("Unable to load module '" + name + "' since it isn't a subclass of " + Module.class.getSimpleName());
	}

	/**
	 * @param classLoaderSource The source which must be part of the same context as the Modules class
	 */
	@SuppressWarnings("unchecked")
	public static void loadExternalModules(Class<?> classLoaderSource) {
		ExternalClassLoader classLoader = new ExternalClassLoader(classLoaderSource);

		classLoader.loadExternalModules();
		loadModules(classLoader.getModulesAsArray());
		availableExtensions.putAll(classLoader.getExtensions());
	}

	@SuppressWarnings("unchecked")
	public static boolean initializeModule(String name) throws IllegalAccessException, InstantiationException {
		Module newModule = (Module) (availableModules.get(name)).newInstance();

		newModule.lightHandler = new LightHandler(name);
		newModule.preferences = new Preferences(name);

		for (String key : availableExtensions.keySet()) {
			String prefix;
			if (key.startsWith(prefix = name + ".")) {
				try {
					newModule.loadExtension((Class<Extension>) availableExtensions.get(key));
				} catch (Exception e) {
					System.err.println("Unable to load extension: " + key.replace(prefix, ""));
				}
			}
		}

		newModule.loadPreferences();

		try {
			newModule.loaded();
		} catch (Exception e) {
			System.out.println("An error occurred when loading " + name + ":" + e.getMessage());
			return false;
		}

		loadedModules.put(name, newModule);

		return true;
	}

	public static boolean activateModule(String name) throws InstantiationException, IllegalAccessException {
		if (name != null) {
			if (!availableModules.containsKey(name)) {
				System.err.println("The module '" + name + "' isn't available");
				return false;
			}

			if (!loadedModules.containsKey(name)) {
				initializeModule(name);
			} else {
				try {
					loadedModules.get(name).resume();
				} catch (Exception e) {
					System.out.println("An error occurred when resuming " + name + ":" + e.getMessage());
					return false;
				}
			}

			activeModules.add(name);
			for (OnChangeListener listener : onChangeListeners)
				listener.onChange(name);

			return true;
		} else {
			return false;
		}
	}

	public static void deactivateModule(String name) {
		if (name != null) {
			if (activeModules.contains(name)) {
				try {
					loadedModules.get(name).suspend();
				} catch (Exception e) {
					e.printStackTrace();
				}
				activeModules.remove(name);
			}
			for (OnChangeListener listener : onChangeListeners)
				listener.onChange(name);
		}
	}


	public static Module getModuleByName(String name) {
		if (activeModules.contains(name))
			return loadedModules.get(name);
		return null;
	}

	public static List<String> getActiveModules() {
		return activeModules;
	}

	public static Hashtable<String, Class<?>> getAvailableModules() {
		return availableModules;
	}


	public static void addOnChangeListener(OnChangeListener onChangeListener) {
		onChangeListeners.add(onChangeListener);
	}

	public static void removeOnChangeListener(OnChangeListener onChangeListener) {
		onChangeListeners.remove(onChangeListener);
	}


	public static void step() {
		try {
			for (String moduleName : activeModules)
				loadedModules.get(moduleName).step();
		} catch (Exception e) {
			// An exception is triggered since the TrayController thread needs to modify the active Modules list.
			// This is by design, but might be changed in a later version
			//e.printStackTrace();
		}
	}

	public static void resume() {
		for (String moduleName : activeModules)
			loadedModules.get(moduleName).resume();
	}

	public static void suspend() {
		for (String moduleName : activeModules)
			loadedModules.get(moduleName).suspend();
	}

	public static void dispose() {
		loadedModules.forEach((key, module) -> {
			module.savePreferences();
			module.dispose();
		});
	}

	public interface OnChangeListener {

		void onChange(String name);
	}

}
