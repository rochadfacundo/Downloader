package com.java.fx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FFmpegUtil {
    public static String extractFFmpegExecutable() {
        try {
            String resourcePath = "/ffmpeg/bin/ffmpeg.exe";
            InputStream inputStream = FFmpegUtil.class.getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el recurso: " + resourcePath);
            }

            // Directorio temporal
            String tempDir = System.getProperty("java.io.tmpdir");
            java.nio.file.Path tempFilePath = Paths.get(tempDir, "ffmpeg.exe");

            // Copiar el archivo del recurso al directorio temporal
            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);

            // Asegurarse de que el archivo sea ejecutable
            tempFilePath.toFile().setExecutable(true);

            if (Files.exists(tempFilePath)) {
                System.out.println("Archivo ffmpeg extraído correctamente a: " + tempFilePath.toString());

                // Verificar si ffmpeg se ejecuta correctamente
                ProcessBuilder pb = new ProcessBuilder(tempFilePath.toString(), "-version");
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                process.waitFor(); // Esperar a que el proceso termine

                // Leer errores si hay
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }

                return tempFilePath.toString();
            } else {
                throw new RuntimeException("Error al extraer el archivo ffmpeg.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract ffmpeg executable", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to extract ffmpeg executable ", e);
        }
    }
}
