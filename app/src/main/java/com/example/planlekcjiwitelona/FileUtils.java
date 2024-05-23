package com.example.planlekcjiwitelona;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
    private static final String GROUP_FILE_NAME = "group.txt";
    private static final String HTML_FILE_NAME = "downloadedy_html.txt";

    public static void saveGroup(Context context, String value) throws IOException {
        FileOutputStream fos = context.openFileOutput(GROUP_FILE_NAME, Context.MODE_PRIVATE);
        fos.write(value.getBytes());
        fos.close();
    }

    public static String readGroup(Context context) throws IOException {
        FileInputStream fis = context.openFileInput(GROUP_FILE_NAME);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        fis.close();
        return sb.toString();
    }

    public static void saveDownloadedHtml(Context context, String html) throws IOException {
        FileOutputStream fos = context.openFileOutput(HTML_FILE_NAME, Context.MODE_PRIVATE);
        fos.write(html.getBytes("ISO-8859-2"));
        fos.close();
    }

    public static String readFile(Context context) throws IOException {
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = context.openFileInput(HTML_FILE_NAME);
        InputStreamReader inputStreamReader = new InputStreamReader(fis, "ISO-8859-2");
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        reader.close();
        return sb.toString();
    }
    public static boolean fileExists(Context context, String fileName) {
        return context.getFileStreamPath(fileName).exists();
    }
}
