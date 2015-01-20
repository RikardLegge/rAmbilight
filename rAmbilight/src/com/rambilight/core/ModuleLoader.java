package com.rambilight.core;

import com.legge.preferences.Preferences;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.Platform;
import com.rambilight.core.api.ui.MessageBox;
import com.rambilight.plugins.Module;
import com.rambilight.plugins.extensions.Extension;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class for loading and handling modules
 */
public class ModuleLoader {

    // Fields for saving the modules as well as attributes that are related to them
    private static final String                      packageName       = "com.rambilight.plugins";
    private static       Hashtable<String, Class<?>> availableModules  = new Hashtable<>();
    private static       Hashtable<String, Module>   loadedModules     = new Hashtable<>();
    private static       List<String>                activeModules     = new ArrayList<>();
    private static       List<OnChangeListener>      onChangeListeners = new ArrayList<>();

    private static Hashtable<String, Class<?>> availableClasses = new Hashtable<>();


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
     * @param classLoaderSorurce The source which must be part of the same context as the Modules class
     * @return Array of the found classes
     * @throws Exception
     */
    public static Class<?>[] loadExternalModules(Class<?> classLoaderSorurce) throws Exception {

        String pluginPath = Global.applicationSupportPath + "/plugins";
        if (!new File(pluginPath).exists())
            new File(pluginPath).mkdir();

        ArrayList<URL> urls = new ArrayList<>();
        try {
            for (String name : new File(pluginPath).list())
                // Filter away any unwanted files
                if (name.endsWith(".jar") || name.endsWith(".class"))
                    urls.add(new URL(Platform.getFilePathFormat(pluginPath + "/" + name)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        URLClassLoader loader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), classLoaderSorurce.getClassLoader());
        ArrayList<Class<? extends Module>> classes = new ArrayList<>();

        if (urls.size() == 0)
            MessageBox.Error("No plugins where found", "No plugins found in the plugin folder '" + pluginPath + "'");
        for (URL url : urls) {
            // Parse the path into a name. Remove everything before the last "/"(Path) and after the last "."(Extension)
            String name = url.toString().substring(url.toString().lastIndexOf("plugins/") + 8, url.toString().lastIndexOf("."));
            Class<?> unknownClass;
            // Try to load packages as a module
            try {
                unknownClass = loader.loadClass(packageName + "." + name + "." + name);

                // Make sure it's a subclass of "Module" and if all goes well, add it to the list.
                classes.add(unknownClass.asSubclass(Module.class));
                System.out.println("Successfully loaded module '" + name + "'");
            } catch (Exception e) {
                try {
                    String[] namePieces = name.split(".");
                    unknownClass = loader.loadClass(packageName + "." + namePieces[0] + ".extensions." + namePieces[1]);
                    availableClasses.put(name, unknownClass);
                    System.out.println("Successfully loaded extension " + name);
                } catch (Exception e2) {
                    System.err.println("Failed to load '" + name + "' as an associated file");
                }
            }
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

    @SuppressWarnings("unchecked")
    public static boolean initializeModule(String name) throws IllegalAccessException, InstantiationException {
        Module newModule = (Module) (availableModules.get(name)).newInstance();

        newModule.lightHandler = new LightHandler(name);
        newModule.preferences = new Preferences(name);
        for (String key : availableClasses.keySet()) {
            String prefix;
            if (key.startsWith(prefix = name + "."))
                try {
                    newModule.loadExtension((Class<Extension>) availableClasses.get(key));
                } catch (Exception e) {
                    System.err.println("Unable to load extension: " + key.replace(prefix, ""));
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
        if (name == null)
            return false;
        if (!availableModules.containsKey(name)) {
            System.err.println("The module '" + name + "' isn't available");
            return false;
        }
        if (!loadedModules.containsKey(name))
            initializeModule(name);
        else
            try {
                loadedModules.get(name).resume();
            } catch (Exception e) {
                System.out.println("An error occurred when resuming " + name + ":" + e.getMessage());
                return false;
            }

        activeModules.add(name);
        for (OnChangeListener listener : onChangeListeners)
            listener.onChange(name);
        return true;
    }

    public static void deactivateModule(String name) {
        if (name == null)
            return;
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
            e.printStackTrace();
            // An exception is triggered since the TrayController thread needs to modify the active Modules list.
            // This is by design, but might be changed in a later version
            //e.printStackTrace();
        }
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

    public static interface OnChangeListener {

        public void onChange(String name);
    }

}
