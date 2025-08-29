package com.java.fx;

import java.util.List;

public class DownloadUtil {

    public static List<String> construirComandoDescarga(String ytDlpPath, String url, String formato) {
        List<String> comando;

        switch (formato.toLowerCase()) {
            case "mp3":
            case "wav":
            case "m4a":
            case "flac":
                comando = List.of(
                    ytDlpPath,
                    "--extract-audio",
                    "--audio-format", formato,
                    url
                );
                break;

            case "mp4":
                comando = List.of(
                    ytDlpPath,
                    "-f", "bestvideo+bestaudio",
                    "--merge-output-format", "mp4",
                    url
                );
                break;

            default:
                throw new IllegalArgumentException("Formato no soportado: " + formato);
        }

        return comando;
    }
}
