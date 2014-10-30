package com.rikardlegge.ambilightDriver;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

public class AssetLoader {

    public static Image getImage(String path, String description) throws FileNotFoundException {
        Image image = (new ImageIcon(AssetLoader.class.getResource(path), description)).getImage();
        if (image == null)
            throw new FileNotFoundException("The image you specified does not exist.");
        return image;
    }
}
