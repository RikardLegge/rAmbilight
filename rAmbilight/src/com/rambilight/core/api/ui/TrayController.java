package com.rambilight.core.api.ui;

import com.legge.utilities.AssetLoader;
import com.rambilight.core.rAmbilight;
import com.rambilight.core.ModuleLoader;
import com.rambilight.core.api.Global;

import java.awt.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A controller for the tray.
 * Also provides methods for creating compatible menu items.
 * This should NOT be re-instantiated by a module since it might cause problems.
 */
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
                throw new Exception("The system doesn't support tray controllers.");

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
                    else if (!ModuleLoader.activateModule(moduleName))
                        target.setState(false);
                    setActiveInputType();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.err.println("Unable to create the selected module: " + e1.getMessage());
                }
            });

            // Error List
            MenuItemsCreator errorListContentCreator = () -> {
                MenuItem[] errorList = new MenuItem[Global.ERRORLOG.size()];
                int pos = 0;
                for (String errorString : Global.ERRORLOG)
                    errorList[pos++] = createItem(errorString, null);
                return errorList;
            };

            exit = createItem("Quit rAmbilight", (target) -> {
                if (rAmbilight.sleepLatch != null)
                    rAmbilight.sleepLatch.countDown();
                rAmbilight.requestExit();
            });
            openConfig = createItem("Reveal configuration", (target) -> {
                if (Desktop.isDesktopSupported())
                    try {
                        Desktop.getDesktop().open(new File(Global.applicationSupportPath));
                    } catch (Exception ex) {
                        MessageBox.Error("Unable to reveal configuration", "Unable to reveal configuration located at: \n" + Global.applicationSupportPath);
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
        setState(active, "");
    }

    public String getLabel() {
        return runToggle.getLabel();
    }

    public void setLabel(String message, boolean state) {
        runToggle.setLabel((Global.isActive ? "Active" : "Inactive") + (message.length() == 0 ? "" : " (" + message + ")"));
        trayIcon.setToolTip(message);
        trayIcon.setImage(state ? Image_Active : Image_Idle);
    }

    public void setState(boolean active, String message) {
        Global.isActive = active;

        if (Global.isActive)
            if (rAmbilight.sleepLatch != null)
                rAmbilight.sleepLatch.countDown();

        runToggle.setState(Global.isActive);
        runToggle.setLabel((Global.isActive ? "Active" : "Inactive") + (message.length() == 0 ? "" : " (" + message + ")"));
        trayIcon.setToolTip(Global.isActive ? Global.APPLICATIONNAME : null);
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

    /**
     * Helper for creating a checkboxMenuItem
     *
     * @param name   Lable of the item
     * @param state  The items original state
     * @param handle The callback handle when a touch event is initialized
     * @return A new menuitem
     */
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

    /**
     * Helper for creating a menuItem
     *
     * @param name   Lable of the item
     * @param handle The callback handle when a touch event is initialized
     * @return A new menuitem
     */
    public static MenuItem createItem(String name, MenuItemStateChanged handle) {
        MenuItem item = new MenuItem(name);
        if (handle != null)
            item.addActionListener((e) -> {
                handle.call((MenuItem) e.getSource());
            });
        return item;
    }

    /**
     * Helper for creating a radioGroupMenuItem
     *
     * @param name   Lable of the item
     * @param items  The items to insert into the submenu
     * @param handle The callback handle when a touch event is initialized
     * @return A new RadioMenu
     */
    public static Menu createRadioGroup(String name, CheckboxMenuItem[] items, GroupStateChanged handle) {
        Menu group = new Menu(name);

        for (CheckboxMenuItem item : items)
            addToRadioGroup(group, item, handle);
        return group;
    }

    /**
     * Helper for creating a goup of menu items
     *
     * @param name   Lable of the item
     * @param items  The items to insert into the submenu
     * @param handle The callback handle when a touch event is initialized
     * @return A new Menu
     */
    public static Menu createGroup(String name, CheckboxMenuItem[] items, GroupStateChanged handle) {
        Menu group = new Menu(name);

        for (CheckboxMenuItem item : items)
            addToGroup(group, item, handle);
        return group;
    }

    public static Menu createItemGroup(String name, MenuItem[] items, MenuItemStateChanged handle) {
        Menu group = new Menu(name);

        for (MenuItem item : items)
            addToItemGroup(group, item, handle);
        return group;
    }

    public static void addToItemGroup(Menu group, MenuItem item, MenuItemStateChanged handle) {
        item.addActionListener((e) -> {
            MenuItem target = (MenuItem) e.getSource();

            if (handle != null)
                handle.call(target);
        });
        group.add(item);
    }

    /**
     * Helper for adding an item to a goup
     *
     * @param group  Group which the item should be inserted into
     * @param item   The item to insert into the group
     * @param handle The callback handle when a touch event is initialized
     */
    public static void addToGroup(Menu group, CheckboxMenuItem item, GroupStateChanged handle) {
        int index = group.getItemCount();
        item.addItemListener((e) -> {
            CheckboxMenuItem target = (CheckboxMenuItem) e.getSource();

            if (handle != null)
                handle.call(target, index, group);
        });
        group.add(item);
    }

    /**
     * Helper for adding an item to a radio group
     *
     * @param group  Group which the item should be inserted into
     * @param item   The item to insert into the group
     * @param handle The callback handle when a touch event is initialized
     */
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

    /**
     * Callback for adding custom elements to the tray controller
     */
    public interface CustomCreator {
        public MenuItem[] create();
    }

    /**
     * Callback for when a checkbox's state is changed
     */
    public interface CheckboxMenuItemStateChanged {
        public void call(CheckboxMenuItem target, boolean selected);
    }

    /**
     * Callback for when a menu item is tapped
     */
    public interface MenuItemStateChanged {
        public void call(MenuItem target);
    }

    /**
     * Callback for when a RadioGroups selected item changes
     */
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
