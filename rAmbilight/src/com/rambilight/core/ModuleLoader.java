package com.rambilight.core;

import com.legge.preferences.Preferences;
import com.rambilight.core.serial.LightHandler;
import com.rambilight.core.ui.MessageBox;
import com.rambilight.plugins.Module;

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
    private static Hashtable<String, Class<?>> availableModules  = new Hashtable<>();
    private static Hashtable<String, Module>   loadedModules     = new Hashtable<>();
    private static List<String>                activeModules     = new ArrayList<>();
    private static List<OnChangeListener>      onChangeListeners = new ArrayList<>();

    public static void loadModules(Class<?> modules[]) {
        for (Class<?> module : modules)
            loadModule(module);
    }

    public static void loadModule(Class<?> module) {
        String name = module.getSimpleName().replace("_", " ");
        if (Module.class.isAssignableFrom(module))
            availableModules.put(name, module);
        else
            System.err.println("Unable to load module '" + name + "' since it isn't a subclass of " + Module.class.getSimpleName());
    }

    public static void activateModule(String name) throws Exception {
        if (name == null)
            return;
        if (!availableModules.containsKey(name)) {
            System.err.println("The module '" + name + "' isn't available");
            return;
        }
        if (!loadedModules.containsKey(name)) {
            Module newModule = (Module) (availableModules.get(name)).newInstance();
            newModule.lightHandler = new LightHandler(name);
            newModule.preferences = new Preferences(name);
            newModule.loadPreferences();
            newModule.loaded();
            loadedModules.put(name, newModule);
        }

        activeModules.add(name);
        for (OnChangeListener listener : onChangeListeners)
            listener.onChange(name);
    }

    public static Module getModuleByName(String name) {
        if (activeModules.contains(name))
            return loadedModules.get(name);
        return null;
    }

    public static void deactivateModule(String name) throws Exception {
        if (name == null)
            return;
        if (activeModules.contains(name))
            activeModules.remove(name);
        for (OnChangeListener listener : onChangeListeners)
            listener.onChange(name);
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
        }
    }

    public static void suspend() {
        for (String moduleName : activeModules)
            loadedModules.get(moduleName).suspend();
    }

    public static List<String> getActiveModules() {
        return activeModules;
    }

    public static Hashtable<String, Class<?>> getAvailableModules() {
        return availableModules;
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

    // TODO Not currently working...
    public static Class<?>[] loadExternalModules(Class<?> classLoaderSoruce) throws Exception {
        String pluginDir = Global.pluginPath;  // The root directory of the plugins
        if (pluginDir.length() == 0) {
            String platform = System.getProperty("os.name").toLowerCase();
            if (platform.contains("win"))
                pluginDir = "/AppData/Local/";
            else if (platform.contains("mac"))
                pluginDir = "/Library/Application Support/";
            else
                pluginDir = "/.";

            pluginDir = System.getProperty("user.home") + pluginDir;
        }

        if (!new File(pluginDir).exists())
            new File(pluginDir).mkdir();
        pluginDir += "/plugins";
        if (!new File(pluginDir).exists())
            new File(pluginDir).mkdir();

        ArrayList<String> TMP_paths = new ArrayList<>();  // A Temporary variable to store the path before creating a URL array.
        try {   // Caches valid paths
            for (String name : new File(pluginDir).list())
                if (name.endsWith(".jar") || name.endsWith(".class"))   // Filter away any unwanted classes
                    TMP_paths.add("jar:file:/" + pluginDir + "/" + name + "!/");   // Add the valid paths to the temporary path array
        } catch (Exception e) {
            e.printStackTrace();
        }

        URL[] urls = new URL[TMP_paths.size()]; // Required by the classLoader to load the selected paths
        for (int i = 0; i < TMP_paths.size(); i++)
            urls[i] = new URL(TMP_paths.get(i));
        TMP_paths.clear();  // Just to clarify that this is temporary

        URLClassLoader loader = URLClassLoader.newInstance(urls);//, classLoaderSoruce.getClassLoader());  // New instance of the class loader that allows the
        ArrayList<Class<? extends Module>> classes = new ArrayList<>(); // The successfully loaded classes

        if (urls.length == 0)
            MessageBox.Error("No plugins found in the plugin folder '" + pluginDir + "'");

        for (URL url : urls) {
            // Parse the path into a name. Remove everything before the last "/"(Path) and after the last "."(Extension)
            String name = url.toString().substring(url.toString().lastIndexOf("plugins/") + 8, url.toString().lastIndexOf("."));
            String postError = "";

            try {
                //Class<?> RawClass = jarFile.getClass();//loader.loadClass(jarFile.getName().substring(0,jarFile.getName().lastIndexOf(".")));
                Class<?> RawClass = Class.forName("com.rambilight.plugins." + name + "." + name, false, loader);
                //Class<?> RawClass = loader.loadClass("com.rambilight.plugins." + name + "." + name);
                postError = "Loading as asset instead";
                Class<? extends Module> modulePlugin = RawClass.asSubclass(Module.class);                          // Make sure it's a subclass of "Module"
                classes.add(modulePlugin);                                                                         // If all goes well, add it to the list.
                System.err.println("Successfully loaded module '" + name + "'.");
            } catch (Exception e) {
                System.err.println("Unable to load plugin '" + name + "'. " + postError);
                e.printStackTrace();
            }
        }
        Class<?>[] toRet = new Class<?>[classes.size()];
        for (int i = 0; i < classes.size(); i++)
            toRet[i] = classes.get(i);

        return toRet;
    }

}
