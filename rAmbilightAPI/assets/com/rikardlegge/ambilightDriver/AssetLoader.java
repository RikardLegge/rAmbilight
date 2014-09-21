package com.rikardlegge.ambilightDriver;

import java.awt.Image;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;

public class AssetLoader {

    public static Image getImage(String path, String description) throws FileNotFoundException {
        Image image = (new ImageIcon(AssetLoader.class.getResource(path), description)).getImage();
        if (image == null)
            throw new FileNotFoundException("The image you specified does not exist.");
        return image;
    }
}
