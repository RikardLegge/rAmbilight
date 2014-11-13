package com.rambilight.core.api.ui;

import com.legge.utilities.AssetLoader;
import com.rambilight.core.Main;
import com.rambilight.core.ModuleLoader;
import com.rambilight.core.api.Global;

import java.awt.*;
import java.awt.event.ItemListener;
import java.io.File;
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
    private MenuItem                      openConfig;
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

            // Create static controls for the tray
            runToggle = createCheckbox(Global.isActive ? "Active" : "Active", Global.isActive, (target, selected) -> setState(selected));

            // Gets a list of all available modules from the ModuleLoader
            Enumeration<String> controllerKeys = ModuleLoader.getAvailableModules().keys();
            CheckboxMenuItem[] inputslist = new CheckboxMenuItem[ModuleLoader.getAvailableModules().size()];
            int i = 0;
            while (controllerKeys.hasMoreElements())
                inputslist[i++] = createCheckbox(controllerKeys.nextElement(), false, null);

            inputs = createGroup("Modules", inputslist, (target, index, parent) -> {
                try {
                    String moduleName = target.getLabel();
                    if (!target.getState())
                        ModuleLoader.deactivateModule(moduleName);
                    else
                        if(!ModuleLoader.activateModule(moduleName))
                            target.setState(false);
                    setActiveInputType();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.err.println("Unable to create the selected module: " + e1.getMessage());
                }
            });
            exit = createItem("Quit rAmbilight", (target) -> Main.requestExit());
            openConfig = createItem("Reveal configuration", (target) -> {
                if (Desktop.isDesktopSupported())
                    try {
                        Desktop.getDesktop().open(new File(Global.applicationSupportPath));
                    } catch (Exception ex) {
                        MessageBox.Error("Unable to reveal configuration located at: \n" + Global.applicationSupportPath);
                    }
            });

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


    public void setState(boolean active) {
        if (!runToggle.isEnabled())
            return;
        Global.isActive = active;
        runToggle.setState(Global.isActive);
        runToggle.setLabel(Global.isActive ? "Active" : "Activate");
        trayIcon.setToolTip(Global.isActive ? "rAmbilight" : null);
        trayIcon.setImage(Global.isActive ? Image_Active : Image_Idle);
    }

    public boolean isRunEnabled() {
        return runToggle.isEnabled();
    }


    public void disableRun(String label) {
        runToggle.setState(Global.isActive);
        runToggle.setLabel("Inactive (" + label + ")");
        trayIcon.setToolTip(label);
        trayIcon.setImage(Image_Idle);
        runToggle.setEnabled(false);
    }

    public void enableRun() {
        runToggle.setEnabled(true);
        setState(Global.isActive);
    }

    private void setTrayController() {
        purge();

        popup.add(runToggle);
        popup.add(inputs);
        for (String moduleName : ModuleLoader.getActiveModules())
            if (itemGroups.containsKey(moduleName)) {
                MenuItem[] items = itemGroups.get(moduleName);
                if (items.length > 0)
                    popup.addSeparator();
                for (MenuItem item : items)
                    popup.add(item);
            }

        popup.addSeparator();
        popup.add(openConfig);
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


    public void addToTrayController(String name, CustomCreator customCreator) {
        MenuItem[] items;
        if (!itemGroups.containsKey(name)) {
            items = customCreator.create();
            itemGroups.put(name, items);
        }
    }

    public void removeItem(MenuComponent item) {
        popup.remove(item);
    }

    public void remove() {
        tray.remove(trayIcon);
    }

    public void purge() {
        popup.removeAll();
    }


    public static CheckboxMenuItem createCheckbox(String name, boolean state, CheckboxMenuItemStateChanged handle) {
        CheckboxMenuItem item = new CheckboxMenuItem(name);
        item.setState(state);
        if (handle != null)
            item.addItemListener((e) -> {
                CheckboxMenuItem target = (CheckboxMenuItem) e.getSource();
                handle.call(target, target.getState());
            });
        return item;
    }

    public static MenuItem createItem(String name, MenuItemStateChanged handle) {
        MenuItem item = new MenuItem(name);
        if (handle != null)
            item.addActionListener((e) -> {
                handle.call((MenuItem) e.getSource());
            });
        return item;
    }

    public static Menu createRadioGroup(String name, CheckboxMenuItem[] items, GroupStateChanged handle) {
        Menu group = new Menu(name);

        for (CheckboxMenuItem item : items)
            addToRadioGroup(group, item, handle);
        return group;
    }

    public static Menu createGroup(String name, CheckboxMenuItem[] items, GroupStateChanged handle) {
        Menu group = new Menu(name);

        for (CheckboxMenuItem item : items)
            addToGroup(group, item, handle);
        return group;
    }


    public static void addToGroup(Menu group, CheckboxMenuItem item, GroupStateChanged handle) {
        int index = group.getItemCount();
        item.addItemListener((e) -> {
            CheckboxMenuItem target = (CheckboxMenuItem) e.getSource();

            if (handle != null)
                handle.call(target, index, group);
        });
        group.add(item);
    }

    public static void addToRadioGroup(Menu group, CheckboxMenuItem item, GroupStateChanged handle) {
        int index = group.getItemCount();
        item.addItemListener((e) -> {
            CheckboxMenuItem target = (CheckboxMenuItem) e.getSource();
            for (int i = 0; i < group.getItemCount(); i++)
                ((CheckboxMenuItem) group.getItem(i)).setState(false);
            target.setState(true);

            if (handle != null)
                handle.call(target, index, group);
        });
        group.add(item);
    }


    public interface CustomCreator {
        public MenuItem[] create();
    }

    public interface CheckboxMenuItemStateChanged {
        public void call(CheckboxMenuItem target, boolean selected);
    }

    public interface MenuItemStateChanged {
        public void call(MenuItem target);
    }

    public interface GroupStateChanged {
        public void call(CheckboxMenuItem target, int index, Menu parent);
    }

    public interface CheckboxCreator {
        CheckboxMenuItem create();
    }

    public interface CheckboxesCreator {
        CheckboxMenuItem[] create();
    }

    public interface MenuItemCreator {
        MenuItem create();
    }

    public interface MenuItemsCreator {
        MenuItem[] create();
    }

    public interface MenuCreator {
        Menu create();
    }

    public interface MenusCreator {
        Menu[] create();
    }

}
