package com.example.planlekcjiwitelona;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    String whichweekday = "0";
    String whichweek = "7";
    String[] weekdays = {"12","13","14","15","16","17","18"};
    int Currentday = 1;
    int Currentmonth = 1;
    int Currentyear = 1;
    boolean noButtonClicked = true;
    int previousday = 0;
    int previousweekday = 0;
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Warsaw");
    int group = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            group = Integer.parseInt(readGroup());
        } catch (IOException e) {
            try {
                saveGroup(String.valueOf(group));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.title));
        }
        updateGroupOnActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v.getLayoutParams();
            layoutParams.topMargin = systemBars.top;
            v.setLayoutParams(layoutParams);
            return insets;
        });
        Calendar calendar = Calendar.getInstance();
        int customDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int whichweekdayfirst = (customDayOfWeek == Calendar.SUNDAY) ? 7 : customDayOfWeek - 1;
        whichweek = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
        Currentday = calendar.get(Calendar.DAY_OF_MONTH);
        Currentmonth = calendar.get(Calendar.MONTH);
        Currentyear = calendar.get(Calendar.YEAR);
        int daysUntilMonday = Calendar.MONDAY - whichweekdayfirst;
        if (daysUntilMonday > 0) {
            daysUntilMonday -= 7;
        }
        calendar.add(Calendar.DAY_OF_MONTH, daysUntilMonday);
        int firstdaymonth=calendar.get(Calendar.MONTH)+1;
        weekdays = new String[7];
        for (int i = 0; i < 7; i++) {
            weekdays[i] = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)-1);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        updateButtons(whichweekdayfirst, firstdaymonth);
        whichweekday = String.valueOf(whichweekdayfirst);
        updateListView(whichweek, whichweekday);
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> downloadHtml(() -> pullToRefresh.setRefreshing(false)));
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.ThemeColor));
    }
    @SuppressLint("SetTextI18n")
    public void updateButtons(int weekday, int month){
        noButtonClicked = false;
        Button calendar = findViewById(R.id.calendar);
        String monthname = getMonthname(month);
        calendar.setText(monthname);
        for (String day : weekdays) {
            Log.d("KALENDAR", "calendar: "+day);
        }
        Button monday = findViewById(R.id.monday);
        monday.setText("Pon.\n"+weekdays[0]);
        Button tuesday = findViewById(R.id.tuesday);
        tuesday.setText("Wt.\n"+weekdays[1]);
        Button wednesday = findViewById(R.id.wednesday);
        wednesday.setText("Śr.\n"+weekdays[2]);
        Button thursday = findViewById(R.id.thursday);
        thursday.setText("Czw.\n"+weekdays[3]);
        Button friday = findViewById(R.id.friday);
        friday.setText("Pt.\n"+weekdays[4]);
        Button saturday = findViewById(R.id.saturday);
        saturday.setText("Sob.\n"+weekdays[5]);
        Button sunday = findViewById(R.id.sunday);
        sunday.setText("Niedz.\n"+weekdays[6]);
        if(weekday==1){
            updateDay(monday, 1);
        } else if (weekday==2) {
            updateDay(tuesday, 2);
        } else if (weekday==3) {
            updateDay(wednesday, 3);
        } else if (weekday==4) {
            updateDay(thursday, 4);
        } else if (weekday==5) {
            updateDay(friday, 5);
        } else if (weekday==6) {
            updateDay(saturday, 6);
        } else if (weekday==7) {
            updateDay(sunday, 7);
        }
    }
    public void updateGroupOnActionBar() {
        ActionBar actionBar = getSupportActionBar();
        String groupText;
        if(group==0){
            groupText="1(1)";
        }else if(group==1){
            groupText="1(2)";
        }else if(group==2){
            groupText="2(1)u";
        }else if(group==3){
            groupText="2(2)u";
        }else if(group==4){
            groupText="3(1)u";
        }else if(group==5){
            groupText="3(2)u";
        }
        else{
            groupText="Błąd";
        }
        if (actionBar != null) {
            actionBar.setTitle("Harmonogram zajęć grupa "+groupText); // Ustaw nowy tytuł dla ActionBara
        }
    }

    @NonNull
    private static String getMonthname(int month) {
        String monthname;
        if (month ==1){
            monthname = "Sty";
        } else if (month ==2){
            monthname = "Lut";
        }else if (month ==3){
            monthname = "Mar";
        }else if (month ==4){
            monthname = "Kwi";
        }else if (month ==5){
            monthname = "Maj";
        }else if (month ==6){
            monthname = "Cze";
        }else if (month ==7){
            monthname = "Lip";
        }else if (month ==8){
            monthname = "Sie";
        }else if (month ==9){
            monthname = "Wrz";
        }else if (month ==10){
            monthname = "Paź";
        }else if (month ==11){
            monthname = "Lis";
        }else if (month ==12){
            monthname = "Gru";
        }
        else{
            monthname = "Wrng";
        }
        return monthname;
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
        updateTextView("");
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
            if (listLesson.isEmpty()){
                updateTextView("Tego dnia nie ma zajęć");
            }

        } catch (JSONException e) {
            List<String> List = new ArrayList<>();
            CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(context, List, List, List, List, List);
            listView.setAdapter(customBaseAdapter);
            updateTextView("Tego dnia nie ma zajęć");
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
    private void saveGroup(String value) throws IOException {
        FileOutputStream fos = openFileOutput("group.txt", MODE_PRIVATE);
        fos.write(value.getBytes());
        fos.close();
    }
    private String readGroup() throws IOException {
            FileInputStream fis = openFileInput("group.txt");
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
    public void downloadHtml(Runnable completionCallback) {
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
                        mainHandler.post(() -> Toast.makeText(MainActivity.this, "Zaktualizowano plan!", Toast.LENGTH_SHORT).show());
                        completionCallback.run();
                    } catch (Exception e) {
                        Log.e("EXCEPTION LOG", "Failed to write to file", e);
                    }
                });
            } catch (Exception e) {
                Log.e("EXCEPTION LOG", "Failed to download HTML", e);
                mainHandler.post(() -> Toast.makeText(MainActivity.this, "Błąd podczas pobierania informacji ze strony.", Toast.LENGTH_SHORT).show());
                completionCallback.run();
            }
        });
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId()==R.id.g11){
            group =0;
        }else if (item.getItemId()==R.id.g12) {
            group =1;
        }else if (item.getItemId()==R.id.g21) {
            group =2;
        }else if (item.getItemId()==R.id.g22) {
            group =3;
        }else if (item.getItemId()==R.id.g31) {
            group =4;
        }else if (item.getItemId()==R.id.g32) {
            group =5;
        }
        updateListView(whichweek, whichweekday);
        try {
            saveGroup(String.valueOf(group));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateGroupOnActionBar();
        return super.onOptionsItemSelected(item);
    }
    public void monday(View view) {
        updateDay(view, 1);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(Integer.parseInt(weekdays[0])>Integer.parseInt(weekdays[6]))
        {
            if(Integer.parseInt(weekdays[0])>previousday)
            {
                Currentmonth-=1;
            }
        }
        Currentday = Integer.parseInt(weekdays[0]);
        updateListView(whichweek,whichweekday);
    }
    public void tuesday(View view) {
        updateDay(view, 2);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(previousday==Integer.parseInt(weekdays[0])&&Integer.parseInt(weekdays[1])<previousday){
            Currentmonth+=1;
        }
        else if (previousday==Integer.parseInt(weekdays[6])&&Integer.parseInt(weekdays[1])>previousday){
            Currentmonth-=1;
        }
        else if (previousday!=Integer.parseInt(weekdays[6])&&previousday!=Integer.parseInt(weekdays[0]))
        {
            if(Integer.parseInt(weekdays[0])>Integer.parseInt(weekdays[6]))
            {
                if(Integer.parseInt(weekdays[1])<Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[1])<Integer.parseInt(weekdays[6])){
                    if(previousday>=Integer.parseInt(weekdays[0]) || previousday>=Integer.parseInt(weekdays[6])){
                        Currentmonth+=1;
                    }
                }
                else
                {
                    if(previousday<Integer.parseInt(weekdays[0]) && previousday<Integer.parseInt(weekdays[6])){
                        Currentmonth-=1;
                    }
                }
            }
        }
        Currentday = Integer.parseInt(weekdays[1]);
        updateListView(whichweek,whichweekday);
    }
    public void wednesday(View view) {
        updateDay(view, 3);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(previousday==Integer.parseInt(weekdays[0])&&Integer.parseInt(weekdays[2])<previousday){
            Currentmonth+=1;
        }
        else if (previousday==Integer.parseInt(weekdays[6])&&Integer.parseInt(weekdays[2])>previousday){
            Currentmonth-=1;
        }
        else if (previousday!=Integer.parseInt(weekdays[6])&&previousday!=Integer.parseInt(weekdays[0])) {
            if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                    if (Integer.parseInt(weekdays[2]) < Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[2]) < Integer.parseInt(weekdays[6])) {
                        if (previousday >= Integer.parseInt(weekdays[0]) || previousday >= Integer.parseInt(weekdays[6])) {
                            Currentmonth += 1;
                        }
                    } else {
                        if (previousday < Integer.parseInt(weekdays[0]) && previousday < Integer.parseInt(weekdays[6])) {
                            Currentmonth -= 1;
                        }
                    }
                }
            }
        }
        Currentday = Integer.parseInt(weekdays[2]);
        updateListView(whichweek,whichweekday);
    }
    public void thursday(View view) {
        updateDay(view, 4);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(previousday==Integer.parseInt(weekdays[0])&&Integer.parseInt(weekdays[3])<previousday){
            Currentmonth+=1;
        }
        else if (previousday==Integer.parseInt(weekdays[6])&&Integer.parseInt(weekdays[3])>previousday){
            Currentmonth-=1;
        }
        else if (previousday!=Integer.parseInt(weekdays[6])&&previousday!=Integer.parseInt(weekdays[0])) {
            if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                    if (Integer.parseInt(weekdays[3]) < Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[3]) < Integer.parseInt(weekdays[6])) {
                        if (previousday >= Integer.parseInt(weekdays[0]) || previousday >= Integer.parseInt(weekdays[6])) {
                            Currentmonth += 1;
                        }
                    } else {
                        if (previousday < Integer.parseInt(weekdays[0]) && previousday < Integer.parseInt(weekdays[6])) {
                            Currentmonth -= 1;
                        }
                    }
                }
            }
        }
        Currentday = Integer.parseInt(weekdays[3]);
        updateListView(whichweek,whichweekday);
    }
    public void friday(View view) {
        updateDay(view, 5);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(previousday==Integer.parseInt(weekdays[0])&&Integer.parseInt(weekdays[4])<previousday){
            Currentmonth+=1;
        }
        else if (previousday==Integer.parseInt(weekdays[6])&&Integer.parseInt(weekdays[4])>previousday){
            Currentmonth-=1;
        }
        else if (previousday!=Integer.parseInt(weekdays[6])&&previousday!=Integer.parseInt(weekdays[0])) {
            if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                    if (Integer.parseInt(weekdays[4]) < Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[4]) < Integer.parseInt(weekdays[6])) {
                        if (previousday >= Integer.parseInt(weekdays[0]) && previousday >= Integer.parseInt(weekdays[6])) {
                            Currentmonth += 1;
                        }
                    } else {
                        if (previousday < Integer.parseInt(weekdays[0]) && previousday < Integer.parseInt(weekdays[6])) {
                            Currentmonth -= 1;
                        }
                    }
                }
            }
        }
        Currentday = Integer.parseInt(weekdays[4]);
        updateListView(whichweek,whichweekday);
    }
    public void saturday(View view) {
        updateDay(view, 6);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(previousday==Integer.parseInt(weekdays[0])&&Integer.parseInt(weekdays[5])<previousday){
            Currentmonth+=1;
        }
        else if (previousday==Integer.parseInt(weekdays[6])&&Integer.parseInt(weekdays[5])>previousday){
            Currentmonth-=1;
        }
        else if (previousday!=Integer.parseInt(weekdays[6])&&previousday!=Integer.parseInt(weekdays[0])) {
            if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                    if (Integer.parseInt(weekdays[5]) < Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[5]) < Integer.parseInt(weekdays[6])) {
                        if (previousday >= Integer.parseInt(weekdays[0]) || previousday >= Integer.parseInt(weekdays[6])) {
                            Currentmonth += 1;
                        }
                    } else {
                        if (previousday < Integer.parseInt(weekdays[0]) && previousday < Integer.parseInt(weekdays[6])) {
                            Currentmonth -= 1;
                        }
                    }
                }
            }
        }
        Currentday = Integer.parseInt(weekdays[5]);
        updateListView(whichweek,whichweekday);
    }
    public void sunday(View view) {
        updateDay(view, 7);
        previousday = Integer.parseInt(weekdays[previousweekday-1]);
        if(Integer.parseInt(weekdays[0])>Integer.parseInt(weekdays[6]))
        {
            if(Integer.parseInt(weekdays[6])<previousday)
            {
                Currentmonth+=1;
            }
        }
        Currentday = Integer.parseInt(weekdays[6]);
        updateListView(whichweek,whichweekday);
    }
    public void calendar(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
            Calendar calendar = new GregorianCalendar(year,month,dayOfMonth);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int adjustedDayOfWeek = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            Currentyear=calendar.get(Calendar.YEAR);
            Currentmonth=calendar.get(Calendar.MONTH);
            Currentday=calendar.get(Calendar.DAY_OF_MONTH);
            int daysUntilMonday = Calendar.MONDAY - dayOfWeek;
            if (daysUntilMonday > 0) {
                daysUntilMonday -= 7;
            }
            calendar.add(Calendar.DAY_OF_MONTH, daysUntilMonday);
            int firstdaymonth=calendar.get(Calendar.MONTH)+1;
            weekdays = new String[7];
            for (int i = 0; i < 7; i++) {
                weekdays[i] = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            updateButtons(adjustedDayOfWeek, firstdaymonth);
            whichweek = String.valueOf(weekOfYear);
            updateListView(whichweek,whichweekday);
        }, Currentyear, Currentmonth, Currentday);
        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        datePickerDialog.show();
    }
    public JSONObject parseSchedule(String htmlContent) throws JSONException {
        boolean didItJustSkipAWeek = Boolean.TRUE;
        int previousCalendarWeek = 0;
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
            int weekday = dayOfTheWeek(dayString.split(" ")[0]);
            if (weekday!=0)
            {
                if (previousweekday >= weekday) {
                    String dateString = dayString.split(" ")[1];
                    int year = Integer.parseInt(dateString.split("-")[0]);
                    int month = Integer.parseInt(dateString.split("-")[1]) - 1; // Miesiąc w Java zaczyna się od 0
                    int day = Integer.parseInt(dateString.split("-")[2]);
                    Calendar calendar = new GregorianCalendar(timeZone);
                    calendar.set(year, month, day);

                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    if (didItJustSkipAWeek == Boolean.FALSE && previousCalendarWeek+1!=calendar.get(Calendar.WEEK_OF_YEAR)-1)
                    {
                        tygodnie.put(String.valueOf(previousCalendarWeek+1), tydzien);
                        didItJustSkipAWeek=Boolean.TRUE;
                    }
                    else
                    {
                        tygodnie.put(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)-1),tydzien);
                        didItJustSkipAWeek=Boolean.FALSE;
                    }
                    previousCalendarWeek=calendar.get(Calendar.WEEK_OF_YEAR)-1;
                    tydzien = new JSONObject();
                    for (int k = 1; k <= 7; k++) {
                        tydzien.put(String.valueOf(k), "siema");
                    }
                }
                tydzien.put(String.valueOf(weekday), dzien);
            }
            previousweekday = weekday;
            dzien = new JSONObject();
        }
        Calendar calendar = new GregorianCalendar(Integer.parseInt(dayString.split(" ")[1].split("-")[0]), Integer.parseInt(dayString.split(" ")[1].split("-")[1])-1, Integer.parseInt(dayString.split(" ")[1].split("-")[2]));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        tygodnie.put(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)),tydzien);
        Log.d("TYDZIEN MAPKA", String.valueOf(tygodnie));
        result.put("Json",tygodnie);
        return result;
    }
    public void updateDay(View view, int newWeekday) {
        noButtonClicked = false;
        previousweekday = Integer.parseInt(whichweekday);
        whichweekday = String.valueOf(newWeekday);
        resetButtonColors();
        ((Button)view).setTextColor(ContextCompat.getColor(this, R.color.ThemeColor));
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
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