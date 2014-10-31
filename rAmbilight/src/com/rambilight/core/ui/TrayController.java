package com.rambilight.core.ui;

import com.rambilight.core.AmbilightDriver;
import com.rambilight.core.ModuleLoader;
import com.rambilight.core.preferences.Global;
import com.rikardlegge.ambilightDriver.AssetLoader;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Hashtable;

public class TrayController {

    // The tray
    private SystemTray tray;
    private PopupMenu  popup;

    // Menu items in the tray
    private CheckboxMenuItem              runToggle;
    private Menu                          inputs;
    private MenuItem                      exit;
    private Hashtable<String, MenuItem[]> itemGroups;

    // assets for the tray
    private TrayIcon trayIcon;
    private Image    Image_Active;
    private Image    Image_Idle;

    public TrayController() throws Exception {
        itemGroups = new Hashtable<>();
        popup = new PopupMenu();

        // Safe way of creating the tray. If something fails, throw an error
        try {
            if (!SystemTray.isSupported())
                throw new Exception();

            // Load static assets
            Image_Active = AssetLoader.getImage("Tray_Active.png", "Active");
            Image_Idle = AssetLoader.getImage("Tray_Idle.png", "Idle");
            tray = SystemTray.getSystemTray();
            popup = new PopupMenu();

            // Add assets to the tray
            trayIcon = new TrayIcon(Image_Idle);
            trayIcon.setImageAutoSize(true);
            trayIcon.setPopupMenu(popup);
            trayIcon.addActionListener((e) -> setState(!Global.isActive));
            tray.add(trayIcon);

            // Gets a list of all available modules from the ModuleLoader
            Enumeration<String> controllerKeys = ModuleLoader.getAvailableModules().keys();
            ItemListener inputsHandler = (e) -> {
                CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();
                try {
                    System.out.println(item.getLabel());
                    String moduleName = item.getLabel();
                    if (!item.getState())
                        ModuleLoader.deactivateModule(moduleName);
                    else
                        ModuleLoader.activateModule(moduleName);
                    setActiveInputType();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.err.println("Unable to create the selected module: " + e1.getMessage());
                }
            };
            CheckboxMenuItem[] inputslist = new CheckboxMenuItem[ModuleLoader.getAvailableModules().size()];
            int i = 0;
            while (controllerKeys.hasMoreElements())
                inputslist[i++] = createCheckbox(controllerKeys.nextElement(), false, inputsHandler);

            // Create static controls for the tray
            runToggle = createCheckbox(Global.isActive ? "Active" : "Active", Global.isActive,
                    (e) -> setState(e.getStateChange() == ItemEvent.SELECTED));

            inputs = createRadioGroup("Modules", inputslist, (e) -> {});
            exit = createItem("Quit", (e) -> AmbilightDriver.requestExit());

            // Add a listener for when the module changes
            ModuleLoader.addOnChangeListener((s) -> {
                if (ModuleLoader.getActiveModules().contains(s))
                    addToTrayController(s, ModuleLoader.getModuleByName(s).getTrayCreator());
                setTrayController();
                setActiveInputType();
            });

            // Sets the tray controller to an empty instance in the case of no loaded controllers
            setTrayController();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to create Tray controller." + e.getMessage());
        }
    }

    public void addToTrayController(String name, CustomCreator customCreator) {
        MenuItem[] items;
        if (!itemGroups.containsKey(name)) {
            items = customCreator.create();
            itemGroups.put(name, items);
        }
    }

    private void setTrayController() {
        purge();

        popup.add(runToggle);
        for (String moduleName : ModuleLoader.getActiveModules())
            if (itemGroups.containsKey(moduleName)) {
                MenuItem[] items = itemGroups.get(moduleName);
                if (items.length > 0)
                    popup.addSeparator();
                for (MenuItem item : items)
                    popup.add(item);
            }

        popup.addSeparator();
        popup.add(inputs);
        popup.add(exit);

        setState(Global.isActive);
    }

    public void setActiveInputType() {
        String names = "";
        for (int i = 0; i < inputs.getItemCount(); i++) {
            MenuItem MIitem = inputs.getItem(i);
            CheckboxMenuItem item;
            try {
                item = (CheckboxMenuItem) MIitem;
            } catch (Exception e) {
                continue;
            }
            item.setState(false);
            for (String activeModuleName : ModuleLoader.getActiveModules())
                if (item.getLabel().equals(activeModuleName)) {
                    names += activeModuleName + ", ";
                    item.setState(true);
                }
        }
        if (names.length() > 0) {
            names = names.substring(0, names.length() - 2);
            inputs.setLabel("Modules (" + names + ")");
        }
        else
            inputs.setLabel("Modules");
    }

    public void purge() {
        popup.removeAll();
    }

    public void setState(boolean active) {
        Global.isActive = active;
        runToggle.setState(Global.isActive);
        runToggle.setLabel(Global.isActive ? "Active" : "Active");
        trayIcon.setToolTip(Global.isActive ? "Running..." : null);
        trayIcon.setImage(Global.isActive ? Image_Active : Image_Idle);

    }

    public static CheckboxMenuItem createCheckbox(String name, boolean state, ItemListener handle) {
        CheckboxMenuItem item = new CheckboxMenuItem(name);
        item.setState(state);
        item.addItemListener(handle);
        return item;
    }

    public static MenuItem createItem(String name, ActionListener handle) {
        MenuItem item = new MenuItem(name);
        item.addActionListener(handle);
        return item;
    }

    public static Menu createRadioGroup(String name, CheckboxMenuItem[] items, ItemListener handle) {
        Menu item = new Menu(name);

        for (CheckboxMenuItem subitem : items) {
            subitem.addItemListener(handle);
            item.add(subitem);
        }
        return item;
    }

    public void removeItem(MenuComponent item) {
        popup.remove(item);
    }

    public void remove() {
        tray.remove(trayIcon);
    }

    public interface CustomCreator {

        public MenuItem[] create();
    }

}
