package com.example.planlekcjiwitelona;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.content.Context;
import android.widget.ListView;
import java.util.List;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    String whichweekday = "0";
    int whichday = 0;
    String whichweek = "7";
    int group=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Calendar calendar = Calendar.getInstance();
        int customDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int whichweekdayfirst = (customDayOfWeek == Calendar.SUNDAY) ? 6 : customDayOfWeek - 1;
        updateButtons(whichweekdayfirst);
        updateTextView(String.valueOf(whichweekday));
        updateListView(String.valueOf(whichweek), String.valueOf(whichweekday));

    }
    public void updateButtons(int weekday){
        if(weekday==1){
            Button monday = findViewById(R.id.monday);
            updateDay(monday, 1);
        } else if (weekday==2) {
            Button tuesday = findViewById(R.id.tuesday);
            updateDay(tuesday, 2);
        } else if (weekday==3) {
            Button wednesday = findViewById(R.id.wednesday);
            updateDay(wednesday, 3);
        } else if (weekday==4) {
            Button thursday = findViewById(R.id.thursday);
            updateDay(thursday, 4);
        } else if (weekday==5) {
            Button friday = findViewById(R.id.friday);
            updateDay(friday, 5);
        } else if (weekday==6) {
            Button saturday = findViewById(R.id.saturday);
            updateDay(saturday, 6);
        } else if (weekday==7) {
            Button sunday = findViewById(R.id.sunday);
            updateDay(sunday, 7);
        }
    }
    private String readFile() {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = openFileInput("downloaded_html.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(fis, "ISO-8859-2");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
        } catch (IOException e) {
            Log.d("EXCEPTION LOG", String.valueOf(e));
        }
        return sb.toString();
    }
    public static List<String> getValuesFromJson(JSONObject jsonObject) throws JSONException {
        List<String> valuesList = new ArrayList<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonObject.getString(key);
            valuesList.add(value);
        }
        return valuesList;
    }
    private void updateListView(String week, String weekday) {
        updateButtons(Integer.parseInt(weekday));
        Context context = getApplicationContext();
        ListView listView = findViewById(R.id.List);
        List<String> listHour = new ArrayList<>();
        List<String> listLesson = new ArrayList<>();
        List<String> listTeacher = new ArrayList<>();
        List<String> listRoom = new ArrayList<>();
        List<String> listNumber = new ArrayList<>();
        try {
            JSONObject jsonified = new JSONObject(readFile());
            JSONObject jsonifiedDay = jsonified.getJSONObject("Json").getJSONObject(week).getJSONObject(weekday);
            List<String> hourList = getValuesFromJson(jsonifiedDay);
            int LessonNumbers = 0;
            for (String hourData : hourList) {
                LessonNumbers++;
                JSONArray jsonifiedHour = new JSONArray(hourData);
                if (!Objects.equals(jsonifiedHour.getString(1 + group * 3), "-"))
                {
                    listLesson.add(jsonifiedHour.getString(1+group*3));
                    listHour.add(jsonifiedHour.getString(0)+", ");
                    listTeacher.add(jsonifiedHour.getString(2+group*3));
                    listRoom.add("sala "+jsonifiedHour.getString(3+group*3));
                    listNumber.add(LessonNumbers+".");
                }
            }

            CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(context, listLesson, listHour, listTeacher, listRoom, listNumber);
            listView.setAdapter(customBaseAdapter);

        } catch (JSONException e) {
            Log.d("EXCEPTION LOG", String.valueOf(e));
        }
    }
    private void updateTextView(String texttodisplay) {
        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText(texttodisplay);
    }
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
    public void downloadHtml(View view) {
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
                String parsedHtml = String.valueOf(parseSchedule(html.toString()));
                mainHandler.post(() -> {
                    try {
                        FileOutputStream fos = openFileOutput("downloaded_html.txt", MODE_PRIVATE);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos, "ISO-8859-2");
                        outputStreamWriter.write(parsedHtml);
                        outputStreamWriter.close();
                        updateTextView(parsedHtml);
                    } catch (Exception e) {
                        Log.e("EXCEPTION LOG", "Failed to write to file", e);
                    }
                });
            } catch (Exception e) {
                Log.e("EXCEPTION LOG", "Failed to download HTML", e);
                mainHandler.post(() -> Toast.makeText(MainActivity.this, "Błąd podczas pobierania HTML.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void left(View view) {
        if(whichday>0)
        {
            whichday--;
            updateListView("7","1");
        }
    }
    public void right(View view) {
        if(whichday<76)
        {
            whichday++;
            updateListView("7","1");
        }
    }
    public void monday(View view) {
        updateDay(view, 1);
    }
    public void tuesday(View view) {
        updateDay(view, 2);
    }
    public void wednesday(View view) {
        updateDay(view, 3);
    }
    public void thursday(View view) {
        updateDay(view, 4);
    }
    public void friday(View view) {
        updateDay(view, 5);
    }
    public void saturday(View view) {
        updateDay(view, 6);
    }
    public void sunday(View view) {
        updateDay(view, 7);
    }
    public void calendar(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
            Calendar calendar = new GregorianCalendar(year,month,dayOfMonth);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int adjustedDayOfWeek = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - 1;
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            updateTextView(weekOfYear+"\nweekday:"+adjustedDayOfWeek);
            whichweekday = String.valueOf(adjustedDayOfWeek);
            whichweek = String.valueOf(weekOfYear);
            updateListView(whichweek,whichweekday);
        }, 2024, 1, 12);
        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        datePickerDialog.show();
    }
    public JSONObject parseSchedule(String htmlContent) throws JSONException {
        Document doc = Jsoup.parse(htmlContent);
        JSONObject result = new JSONObject();
        JSONArray dni = new JSONArray();
        JSONObject dzien = new JSONObject();
        JSONArray godzina = new JSONArray();
        JSONObject tygodnie = new JSONObject();
        JSONObject tydzien = new JSONObject();
        String dayString ="";
        int previousweekday = -1;
        for (int i = 1; i <= 7; i++) {
            tydzien.put(Integer.toString(i), "siema");
        }
        Elements dane = doc.select("table").get(0).select("tbody > tr");
        for (int rowcount = 4; rowcount < dane.size(); rowcount+=10) {
            for (int j=0;j<7;j++)
            {
                for(int i=0; i<19;i++) {
                    godzina.put(dane.get(rowcount+j).select("td").get(i).text());
                }
                dzien.put(dane.get(rowcount+j).select("td").get(0).text(), godzina);
                godzina = new JSONArray();
            }
            dni.put(dzien);
            dayString = dane.get(rowcount-3).select("td").get(0).text();
            Log.d("WRONGWEONG", "SOMETHINGISSERIOUSLY WRONG: "+dayString);
            int weekday = dayOfTheWeek(dayString.split(" ")[0]);
            if (weekday!=0)
            {
                if (previousweekday >= weekday) {
                    Calendar calendar = new GregorianCalendar(Integer.parseInt(dayString.split(" ")[1].split("-")[0]), Integer.parseInt(dayString.split(" ")[1].split("-")[1])-1, Integer.parseInt(dayString.split(" ")[1].split("-")[2]));
                    tygodnie.put(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)-1),tydzien);
                    tydzien = new JSONObject();
                    for (int k = 1; k <= 7; k++) {
                        tydzien.put(String.valueOf(k), "siema");
                    }
                }
                tydzien.put(String.valueOf(weekday), dzien);
                Log.d("WRONGWEONG", "SOMETHINGISSERIOUSLY WRONG: "+tydzien);
            }
            else{
                Log.d("WRONGWEONG", "SOMETHINGISSERIOUSLY WRONG: "+dzien);
            }
            previousweekday = weekday;
            dzien = new JSONObject();
        }
        Calendar calendar = new GregorianCalendar(Integer.parseInt(dayString.split(" ")[1].split("-")[0]), Integer.parseInt(dayString.split(" ")[1].split("-")[1])-1, Integer.parseInt(dayString.split(" ")[1].split("-")[2]));
        tygodnie.put(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)),tydzien);
        Log.d("TYDZIEN MAPKA", String.valueOf(tygodnie));
        result.put("Json",tygodnie);
        return result;
    }
    public void updateDay(View view, int newWeekday) {
        if (!Objects.equals(whichweekday, String.valueOf(newWeekday))) {
            whichweekday = String.valueOf(newWeekday);
            updateTextView(whichweekday);
            resetButtonColors();
            ((Button)view).setTextColor(Color.RED);
        }
    }
    private void resetButtonColors() {
        Button monday = findViewById(R.id.monday);
        Button tuesday = findViewById(R.id.tuesday);
        Button wednesday = findViewById(R.id.wednesday);
        Button thursday = findViewById(R.id.thursday);
        Button friday = findViewById(R.id.friday);
        Button saturday = findViewById(R.id.saturday);
        Button sunday = findViewById(R.id.sunday);

        int defaultTextColor = Color.WHITE;
        monday.setTextColor(defaultTextColor);
        tuesday.setTextColor(defaultTextColor);
        wednesday.setTextColor(defaultTextColor);
        thursday.setTextColor(defaultTextColor);
        friday.setTextColor(defaultTextColor);
        saturday.setTextColor(defaultTextColor);
        sunday.setTextColor(defaultTextColor);
    }
    public static int dayOfTheWeek(String dayAsText) {
        if (Objects.equals(dayAsText, "Poniedziałek")){
            return 1;
        } else if (Objects.equals(dayAsText, "Wtorek")) {
            return 2;
        } else if (Objects.equals(dayAsText, "Środa")) {
            return 3;
        } else if (Objects.equals(dayAsText, "Czwartek")) {
            return 4;
        } else if (Objects.equals(dayAsText, "Piątek")) {
            return 5;
        } else if (Objects.equals(dayAsText, "Sobota")) {
            return 6;
        } else if (Objects.equals(dayAsText, "Niedziela")) {
            return 7;
        }
        else{
            return 0;
        }
    }
}