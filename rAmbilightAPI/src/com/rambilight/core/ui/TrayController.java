package com.rambilight.core.ui;

import com.legge.utilities.AssetLoader;
import com.rambilight.core.Global;

import java.awt.*;
import java.awt.event.ItemListener;

public class TrayController {

    // The tray
    private SystemTray tray;
    private PopupMenu  popup;

    // Menu items in the tray
    private CheckboxMenuItem runToggle;
    private MenuItem         exit;

    // assets for the tray
    private TrayIcon trayIcon;
    private Image    Image_Active;
    private Image    Image_Idle;

    protected TrayController() throws Exception {
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
            runToggle = createCheckbox(Global.isActive ? "Active" : "Active", Global.isActive,
                    (target, selected) -> setState(selected));

            exit = createItem("Quit rAmbilight API", (e) -> {
                System.exit(0);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to create Tray controller." + e.getMessage());
        }
    }

    protected void addToTrayController(CustomCreator customCreator) {
        MenuItem[] customItems = customCreator.create();

        purge();

        popup.add(runToggle);

        if (customItems.length > 0)
            popup.addSeparator();
        for (MenuItem item : customItems)
            popup.add(item);

        popup.addSeparator();
        popup.add(exit);

        setState(Global.isActive);
    }

    protected void purge() {
        popup.removeAll();
    }

    protected void setState(boolean active) {
        Global.isActive = active;
        runToggle.setState(Global.isActive);
        runToggle.setLabel(Global.isActive ? "Active" : "Active");
        trayIcon.setToolTip(Global.isActive ? "rAmbilight API" : null);
        trayIcon.setImage(Global.isActive ? Image_Active : Image_Idle);

    }

    protected void removeItem(MenuComponent item) {
        popup.remove(item);
    }

    protected void remove() {
        tray.remove(trayIcon);
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
    public static Menu createRadioGroup(String name, CheckboxMenuItem[] items, RadioGroupStateChanged handle) {
        Menu item = new Menu(name);

        ItemListener listener = (e) -> {
            CheckboxMenuItem target = (CheckboxMenuItem) e.getSource();
            int index = -1;
            int i = 0;
            for (CheckboxMenuItem itemi : items) {
                if (itemi.getLabel().equals(target.getLabel()))
                    index = i;
                itemi.setState(false);
                i++;
            }
            target.setState(true);
            if (handle != null)
                handle.call(target, index, item);
        };

        for (CheckboxMenuItem subitem : items) {
            subitem.addItemListener(listener);
            item.add(subitem);
        }
        return item;
    }

    /**
     * Helper for creating a goup of menu items
     *
     * @param name   Lable of the item
     * @param items  The items to insert into the submenu
     * @param handle The callback handle when a touch event is initialized
     * @return A new Menu
     */
    public static Menu createGroup(String name, CheckboxMenuItem[] items, RadioGroupStateChanged handle) {
        Menu item = new Menu(name);

        ItemListener listener = (e) -> {
            CheckboxMenuItem target = (CheckboxMenuItem) e.getSource();
            int index = -1;
            int i = 0;
            for (CheckboxMenuItem itemi : items) {
                if (itemi.getLabel().equals(target.getLabel())) {
                    index = i;
                    break;
                }
                i++;
            }
            target.setState(true);
            if (handle != null)
                handle.call(target, index, item);
        };

        for (CheckboxMenuItem subitem : items) {
            subitem.addItemListener(listener);
            item.add(subitem);
        }
        return item;
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
    public interface RadioGroupStateChanged {
        public void call(CheckboxMenuItem target, int index, MenuItem parent);
    }

}
