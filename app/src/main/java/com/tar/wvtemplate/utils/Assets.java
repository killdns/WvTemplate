package com.tar.wvtemplate.utils;

import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.tar.wvtemplate.controllers.ActivityController;

/**
 * Created by Кирилл Даньшин on 06.02.2018.
 */

/**
 * Класс работы с Assets
 */
public final class Assets {

    /**
     * Получение списка файлов в Assets
     */
    public static ArrayList<String> listAssetFiles(String path) {

        String [] list;
        ArrayList<String> result = new ArrayList<>();
        try {
            list = ActivityController.getBaseActivity().getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                        if (file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith(".bmp") || file.endsWith(".png"))
                            result.add(file);
                }
            }
        } catch (IOException e) {
            return result;
        }

        return result;
    }

    /**
     * Получение изображения из папки Assets
     */
    public static Drawable getImageFromAssets(String filename) {

        Drawable d = null;
        try {
            InputStream ims = ActivityController.getBaseActivity().getAssets().open(filename);
            d = Drawable.createFromStream(ims, null);
        }
        catch(IOException ex) {
        }

        return d;
    }
}
