package com.java.fx;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

@Component
public class controller implements Initializable, IProgressCallback {

    @Bean
    String title(){
        return "Descargador de videos serial";
    }


    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField txtLink;
    @FXML
    private Button btnDownloader;
    @FXML
    private Label lblLink;


    @FXML
    public void download() {
        String videoUrl = this.txtLink.getText();
        String userHome = System.getProperty("user.home");
        String downloadDir = Paths.get(userHome, "Videos", "Videos descargados").toString();

        // Crear el directorio si no existe
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            boolean result = dir.mkdirs();
            if (!result) {
                System.err.println("No se pudo crear el directorio: " + downloadDir);
                return;
            }
        }

        // Extraer el ejecutable youtube-dl del JAR a un directorio temporal
        String youtubeDlPath = extractYoutubeDlExecutable();

        try {
            // Especificar la ruta del ejecutable youtube-dl
            YoutubeDL.setExecutablePath(youtubeDlPath);

            // Build request
            YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, downloadDir);
            request.setOption("ignore-errors");
            request.setOption("output", "%(title)s.%(ext)s");
            request.setOption("retries", 10);

            // Make request and return response
            YoutubeDLResponse response = YoutubeDL.execute(request);

            // Output
            String stdOut = response.getOut();
            System.out.println(stdOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractYoutubeDlExecutable() {
        try {
            // Ruta al recurso dentro del JAR
            String resourcePath = "/youtube-dl/youtube-dl.exe";
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el recurso: " + resourcePath);
            }

            // Directorio temporal
            String tempDir = System.getProperty("java.io.tmpdir");
            java.nio.file.Path tempFilePath = Paths.get(tempDir, "youtube-dl.exe");

            // Copiar el archivo del recurso al directorio temporal
            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);

            // Asegurarse de que el archivo sea ejecutable
            tempFilePath.toFile().setExecutable(true);

            // Verificación
            if (Files.exists(tempFilePath)) {
                System.out.println("Archivo youtube-dl extraído correctamente a: " + tempFilePath.toString());
            } else {
                throw new RuntimeException("Error al extraer el archivo youtube-dl.");
            }

            return tempFilePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract youtube-dl executable", e);
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void onProgressUpdate(float progress, long etaInSeconds) {
        System.out.println("Progress: " + progress + "%, ETA: " + etaInSeconds + " seconds");
    }
}
