package com.darieldelgadoo.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {
    public static File getFileFromUri(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        inputStream.close();
        return tempFile;
    }

    public static File getFileFromBitmap(Context context, Bitmap bitmap) throws Exception {
        File tempFile = new File(context.getCacheDir(), "cam_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream out = new FileOutputStream(tempFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        out.flush();
        out.close();
        return tempFile;
    }
}