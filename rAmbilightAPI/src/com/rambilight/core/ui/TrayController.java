package com.rambilight.core.ui;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

public class TrayController {

    /** Helper for creating a checkboxMenuItem
     * 
     * @param name
     *        Lable of the item
     * @param state
     *        The items original state
     * @param handle
     *        The callback handle when a touch event is initialized
     * @return A new menuitem */
    public static CheckboxMenuItem createCheckbox(String name, boolean state, ItemListener handle) {
        CheckboxMenuItem item = new CheckboxMenuItem(name);
        item.setState(state);
        item.addItemListener(handle);
        return item;
    }

    /** Helper for creating a menuItem
     * 
     * @param name
     *        Lable of the item
     * @param handle
     *        The callback handle when a touch event is initialized
     * @return A new menuitem */
    public static MenuItem createItem(String name, ActionListener handle) {
        MenuItem item = new MenuItem(name);
        item.addActionListener(handle);
        return item;
    }

    /** Helper for creating a radioGroupMenuItem
     * 
     * @param name
     *        Lable of the item
     * @param items
     *        The items to insert into the submenu
     * @param handle
     *        The callback handle when a touch event is initialized
     * @return A new menuitem */
    public static Menu createRadioGroup(String name, CheckboxMenuItem[] items, ItemListener handle) {
        Menu item = new Menu(name);

        for (CheckboxMenuItem subitem : items) {
            subitem.addItemListener(handle);
            item.add(subitem);
        }
        return item;
    }

    /** Remove specific item
     * 
     * @item Item to remove */
    public void removeItem(MenuComponent item) {}

    /** Callback for adding custom elements to the tray controller */
    public interface CustomCreator {

        public MenuItem[] create();
    }

}
