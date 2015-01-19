package com.rambilight.core.clientInterface.debug;

import java.util.LinkedList;
import java.util.Queue;

public class SynchronizedArray {

    private Queue<Byte> array;
    private int         size;

    public SynchronizedArray(int size) {
        this.size = size;
        array = new LinkedList<>();
    }

    public void write(int element) {
        gateway(GatewayRoute.WRITE, new byte[]{(byte) element});
    }

    public void write(byte element) {
        gateway(GatewayRoute.WRITE, new byte[]{element});
    }

    public void write(byte[] elements) {
        gateway(GatewayRoute.WRITE, elements);
    }

    public byte peek() {
        return gateway(GatewayRoute.PEEK, null);
    }

    public byte read() {
        return gateway(GatewayRoute.READ, null);
    }

    public int length() {
        return array.size();
    }

    private synchronized byte gateway(GatewayRoute route, byte[] elements) {
        switch (route) {
            case WRITE:
                for (byte element : elements) {
                    if (array.size() >= size)
                        return 0;
                    array.add(element);
                }
                break;
            case PEEK:
                if (!array.isEmpty())
                    return array.peek();
                break;
            case READ:
                if (!array.isEmpty())
                    return array.remove();
                break;
            default:
                break;
        }
        return -1;
    }

    private enum GatewayRoute {
        WRITE, PEEK, READ
    }
}
