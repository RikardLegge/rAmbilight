package com.rambilight.core.api;

import java.util.ArrayList;
import java.util.HashMap;

public class EventHandler {

    private static HashMap<String, ArrayList<EventListener>> callbackMap = new HashMap<>();

    public static void addEventListener(String eventName, EventListener eventListener) {
        ArrayList<EventListener> eventList;
        if (!callbackMap.containsKey(eventName)) {
            eventList = new ArrayList<>();
            callbackMap.put(eventName, eventList);
        }
        else
            eventList = callbackMap.get(eventName);

        if (eventList.contains(eventListener))
            System.err.println(String.format("This event listener is already added to %s", eventName));
        else
            eventList.add(eventListener);
    }

    public static boolean removeEventListener(String eventName, EventListener eventListener) {
        ArrayList<EventListener> eventList;
        if (callbackMap.containsKey(eventName))
            if ((eventList = callbackMap.get(eventName)).contains(eventListener)) {
                eventList.remove(eventListener);
                return true;
            }
        return false;
    }

    public static void triggerEvent(String eventName) {
        if (callbackMap.containsKey(eventName))
            callbackMap.get(eventName).forEach(EventHandler.EventListener::trigger);
    }

    public static void clear() {
        callbackMap.clear();
    }

    public static interface EventListener {
        void trigger();
    }
}
