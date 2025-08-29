package com.java.fx;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class controller implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField txtLink;

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnDownloader;

    @FXML
    private Label lblLink;

    @FXML
    private Label lblLink2;

    @FXML
    private ComboBox<String> comboBox;

    ObservableList<String> formatList = FXCollections.observableArrayList("mp4", "mp3", "wav");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBox.setValue("mp4");
        comboBox.setItems(formatList);
    }

    @FXML
    public void download() {
        String videoUrl = txtLink.getText();
        String format = comboBox.getValue();

        String ytDlpPath = YtDlpUtil.extractYtDlpExecutable(); // ✅
        String ffmpegPath = FFmpegUtil.extractFFmpegExecutable();

        Thread thread = new Thread(new DownloadTask(videoUrl, ytDlpPath, ffmpegPath, format));
        thread.start();
    }

    private class DownloadTask implements Runnable {

        private final String videoUrl;
        private final String ytDlpPath;
        private final String ffmpegPath;
        private final String format;

        public DownloadTask(String videoUrl, String ytDlpPath, String ffmpegPath, String format) {
            this.videoUrl = videoUrl;
            this.ytDlpPath = ytDlpPath;
            this.ffmpegPath = ffmpegPath;
            this.format = format;
        }

        @Override
        public void run() {
            String userHome = System.getProperty("user.home");
            String downloadDir = "mp4".equals(format)
                    ? Paths.get(userHome, "Videos", "Videos descargados").toString()
                    : Paths.get(userHome, "Music", "Musica descargada").toString();

            File dir = new File(downloadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("No se pudo crear el directorio: " + downloadDir);
                Platform.runLater(() -> lblEstado.setText("❌ Error al crear el directorio de descarga."));
                return;
            }

            Platform.runLater(() -> lblEstado.setText("⏳ Descargando..."));

            try {
                ProcessBuilder builder = new ProcessBuilder();

                if ("mp4".equalsIgnoreCase(format)) {
                    builder.command(
                            ytDlpPath,
                            "--ffmpeg-location", ffmpegPath,
                            "-o", "%(title)s.%(ext)s",
                            "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best",
                            "--merge-output-format", "mp4",
                            "--no-playlist",
                            videoUrl
                    );
                } else {
                    builder.command(
                            ytDlpPath,
                            "--ffmpeg-location", ffmpegPath,
                            "-o", "%(title)s.%(ext)s",
                            "--extract-audio",
                            "--audio-format", format,
                            "--no-playlist",
                            videoUrl
                    );
                }

                builder.directory(new File(downloadDir));
                builder.redirectErrorStream(true);

                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    Platform.runLater(() -> lblEstado.setText("✅ Descarga completada con éxito."));
                } else {
                    Platform.runLater(() -> lblEstado.setText("❌ Error: yt-dlp finalizó con código " + exitCode));
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> lblEstado.setText("❌ Error durante la descarga."));
            }
        }
    }
}
