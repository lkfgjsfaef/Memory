package com.niit.memory.util;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    private FileUtils() {}

    public static File copyUriToTempFile(Context context, Uri uri) throws IOException {
        File file = File.createTempFile("upload_", ".jpg", context.getCacheDir());
        InputStream is = context.getContentResolver().openInputStream(uri);
        if (is == null) {
            file.delete();
            throw new IOException("无法读取图片文件");
        }
        try (is; FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
        }
        return file;
    }
}
