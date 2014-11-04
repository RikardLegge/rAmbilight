package com.rambilight.core.serial;

import com.rambilight.dev.Visulizer;

/** Class for handling the Lights and what to output */
public class LightHandler {

    private Visulizer visulizer;
    
    public LightHandler(Visulizer visulizer){
        this.visulizer = visulizer;
    }
    
    /** Add light to the update buffer
     * 
     * @param l
     *        Lightposition
     * @param r
     *        The amount of read
     * @param g
     *        The amount of green
     * @param b
     *        The amount of blue */
    public void addToUpdateBuffer(int l, int r, int g, int b) {
        visulizer.setColor(l, r, g, b);
    }

    public Light[] getColorBuffer() {return visulizer.getColorBuffer();}

}
