// src/main/java/com/java/fx/YtDlpUtil.java
package com.java.fx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class YtDlpUtil {
    public static String extractYtDlpExecutable() {
        try {
            String resourcePath = "/youtube-dl/yt-dlp.exe";
            InputStream inputStream = YtDlpUtil.class.getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el recurso: " + resourcePath);
            }

            String tempDir = System.getProperty("java.io.tmpdir");
            java.nio.file.Path tempFilePath = Paths.get(tempDir, "yt-dlp.exe");

            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            tempFilePath.toFile().setExecutable(true);

            return tempFilePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al extraer yt-dlp", e);
        }
    }
}
