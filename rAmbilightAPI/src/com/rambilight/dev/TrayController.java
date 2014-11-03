package com.rambilight.dev;

import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.rambilight.core.Global;
import com.rambilight.core.ui.TrayController.CustomCreator;
import com.legge.utilities.AssetLoader;

class TrayController {

    // The tray
    private SystemTray tray;
    private PopupMenu  popup;

    // Menu items in the tray
    private CheckboxMenuItem runToggle;
    private MenuItem         exit;
    private MenuItem[]       customItems;

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
                    (e) -> setState(e.getStateChange() == ItemEvent.SELECTED));

            exit = createItem("Quit rAmbilight API", (e) -> {
                System.exit(0);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unable to create Tray controller." + e.getMessage());
        }
    }

    protected void addToTrayController(CustomCreator customCreator) {
        customItems = customCreator.create();

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

    protected static CheckboxMenuItem createCheckbox(String name, boolean state, ItemListener handle) {
        CheckboxMenuItem item = new CheckboxMenuItem(name);
        item.setState(state);
        item.addItemListener(handle);
        return item;
    }

    protected static MenuItem createItem(String name, ActionListener handle) {
        MenuItem item = new MenuItem(name);
        item.addActionListener(handle);
        return item;
    }

    protected void removeItem(MenuComponent item) {
        popup.remove(item);
    }

    protected void remove() {
        tray.remove(trayIcon);
    }

}
