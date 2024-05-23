package com.example.planlekcjiwitelona;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void downloadHtml(Context context, Runnable completionCallback) {
        executor.execute(() -> {
            String url = "http://www.plan.pwsz.legnica.edu.pl/checkSpecjalnoscStac.php?specjalnosc=s1INF";
            StringBuilder html = new StringBuilder();
            try {
                URL urlObj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "ISO-8859-2"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        html.append(line).append("\n");
                    }
                }
                String parsedHtml = HtmlParser.parseSchedule(html.toString()).toString();
                mainHandler.post(() -> {
                    try {
                        FileUtils.saveDownloadedHtml(context, parsedHtml);
                        Toast.makeText(context, "Zaktualizowano plan!", Toast.LENGTH_SHORT).show();
                        completionCallback.run();
                    } catch (Exception e) {
                        Log.e("EXCEPTION LOG", "Failed to write to file", e);
                    }
                });
            } catch (Exception e) {
                Log.e("EXCEPTION LOG", "Failed to download HTML", e);
                mainHandler.post(() -> {
                    Toast.makeText(context, "Błąd podczas pobierania informacji ze strony.", Toast.LENGTH_SHORT).show();
                    completionCallback.run();
                });
            }
        });
    }
}
