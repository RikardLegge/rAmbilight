package com.legge.utilities;

import java.awt.Image;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;

public class AssetLoader {

    /**
     * This is an internal AssetLoader class for loading core parts of the application.
     * Most people won't need to use this, but it's available both for the applications core,
     * as well as future extendability for the plugins.
     *
     * @param path        Path of the image relative the asset loader
     * @param description Description of the image
     * @return An image loaded from the specified file
     * @throws FileNotFoundException If the file was not found.
     */
    public static Image getImage(String path, String description) throws FileNotFoundException {
        Image image = (new ImageIcon(AssetLoader.class.getResource(path), description)).getImage();
        if (image == null)
            throw new FileNotFoundException("The image you specified does not exist.");
        return image;
    }
}
