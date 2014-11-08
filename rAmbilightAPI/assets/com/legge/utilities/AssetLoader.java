package com.legge.utilities;

import java.awt.Image;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;

public class AssetLoader {

    /**
     * @param path        Path of the image relative the asset loader
     * @param description Description of the image
     * @return An image loaded from the specified file
     * @throws FileNotFoundException
     */
    public static Image getImage(String path, String description) throws FileNotFoundException {
        Image image = (new ImageIcon(AssetLoader.class.getResource(path), description)).getImage();
        if (image == null)
            throw new FileNotFoundException("The image you specified does not exist.");
        return image;
    }
}
