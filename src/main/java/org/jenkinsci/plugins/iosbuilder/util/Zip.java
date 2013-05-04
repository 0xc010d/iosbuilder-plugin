package org.jenkinsci.plugins.iosbuilder.util;

import hudson.FilePath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
    public static void archive(FilePath input, File output) {
        try {
            output.getParentFile().mkdirs();
            FileOutputStream fileOutputStream = new FileOutputStream(output);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            archive(input, input.getName(), zipOutputStream);
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void archive(FilePath filePath, String base, ZipOutputStream zipOutputStream) throws Exception {
        byte[] buffer = new byte[1024];
        if (filePath.isDirectory()) {
            for (Iterator<FilePath> iterator = filePath.list().iterator(); iterator.hasNext();){
                FilePath nextFilePath = iterator.next();
                archive(nextFilePath, base + File.separator + nextFilePath.getName(), zipOutputStream);
            }
        }
        else {
            String name = base + File.separator + filePath.getName();
            ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            InputStream inputStream = filePath.read();
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
        }
    }
}
