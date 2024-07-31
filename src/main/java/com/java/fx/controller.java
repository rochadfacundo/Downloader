package com.java.fx;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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
public class controller implements Initializable, Runnable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TextField txtLink;
    @FXML
    private Button btnDownloader;
    @FXML
    private Label lblLink;

    @FXML
    private Label lblLink2;
    @FXML
    private ComboBox comboBox;

    @FXML
    public void download() {
        String videoUrl = this.txtLink.getText();
        String format = this.comboBox.getValue().toString();
        // Asegúrate de extraer youtube-dl antes de iniciar la descarga
        String youtubeDlPath = extractYoutubeDlExecutable();
        // Extrae el ejecutable ffmpeg
        String ffmpegPath = FFmpegUtil.extractFFmpegExecutable();
        Thread thread = new Thread(new DownloadTask(videoUrl, youtubeDlPath, ffmpegPath, format));
        thread.start();
    }

    private String extractYoutubeDlExecutable() {
        try {
            String resourcePath = "/youtube-dl/youtube-dl.exe";
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el recurso: " + resourcePath);
            }

            String tempDir = System.getProperty("java.io.tmpdir");
            java.nio.file.Path tempFilePath = Paths.get(tempDir, "youtube-dl.exe");

            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            tempFilePath.toFile().setExecutable(true);

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
    public void run() {
        // Este método puede estar vacío porque estamos usando una clase separada para la descarga
    }

    ObservableList<String> formatList= FXCollections.observableArrayList("mp4","mp3","wav");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.comboBox.setValue("mp4");
        this.comboBox.setItems(formatList);
    }

    private class DownloadTask implements Runnable {


        private String videoUrl;
        private String youtubeDlPath;
        private String ffmpegPath;
        private String format;

        public DownloadTask(String videoUrl, String youtubeDlPath, String ffmpegPath, String format) {
            this.videoUrl = videoUrl;
            this.youtubeDlPath = youtubeDlPath;
            this.ffmpegPath = ffmpegPath;
            this.format = format;
        }

        @Override
        public void run() {
            // Directorio de descarga según el formato
            String userHome = System.getProperty("user.home");
            String downloadDir;
            if ("mp4".equals(format)) {
                downloadDir = Paths.get(userHome, "Videos", "Videos descargados").toString();
            } else {
                downloadDir = Paths.get(userHome, "Music", "Musica descargada").toString();
            }

            // Crear el directorio si no existe
            File dir = new File(downloadDir);
            if (!dir.exists()) {
                boolean result = dir.mkdirs();
                if (!result) {
                    System.err.println("No se pudo crear el directorio: " + downloadDir);
                    return;
                }
            }

            try {
                // Configurar youtube-dl
                YoutubeDL.setExecutablePath(youtubeDlPath);

                YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, downloadDir);
                request.setOption("ignore-errors");
                request.setOption("output", "%(title)s.%(ext)s");
                request.setOption("retries", 10);

                // Establecer la opción de formato si es necesario
                if ("mp3".equals(format)) {
                    request.setOption("format", "bestaudio[ext=m4a]");
                    // Puedes configurar otros parámetros para audio si es necesario
                } else if ("wav".equals(format)) {
                    request.setOption("format", "bestaudio[ext=m4a]");
                    // Puedes configurar otros parámetros para audio si es necesario
                }

                // Ejecutar la solicitud
                YoutubeDLResponse response = YoutubeDL.execute(request);

                String stdOut = response.getOut();
                System.out.println(stdOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
