package com.example.planlekcjiwitelona;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String whichweekday = "0";
    String whichweek = "7";
    String[] weekdays = {"12", "13", "14", "15", "16", "17", "18"};
    int Currentday = 1;
    int Currentmonth = 1;
    int Currentyear = 1;
    boolean noButtonClicked = true;
    int previousday = 0;
    int previousweekday = 0;
    int group = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!FileUtils.fileExists(this, "downloadedy_html.txt")){
            try {
                FileOutputStream fos = openFileOutput("downloadedy_html.txt", MODE_PRIVATE);
                fos.close();
            } catch (IOException e) {
                Log.d("BLAD","Błąd w tworzeniu pliku z danymi");
            }
        }
        try {
            group = Integer.parseInt(FileUtils.readGroup(this));
        } catch (IOException e) {
            try {
                FileUtils.saveGroup(this, String.valueOf(group));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.title));
        }
        updateGroupOnActionBar();
        final View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        int firstdaymonth = calendar.get(Calendar.MONTH) + 1;
        weekdays = new String[7];
        for (int i = 0; i < 7; i++) {
            weekdays[i] = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH) - 1);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        updateButtons(whichweekdayfirst, firstdaymonth);
        whichweekday = String.valueOf(whichweekdayfirst);
        updateListView(whichweek, whichweekday);
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> NetworkUtils.downloadHtml(this, () -> {
            pullToRefresh.setRefreshing(false);
            updateListView(whichweek, whichweekday);
        }));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ThemeColor));
    }

    @SuppressLint("SetTextI18n")
    public void updateButtons(int weekday, int month) {
        noButtonClicked = false;
        Button calendar = findViewById(R.id.calendar);
        String monthname = DateUtils.getMonthname(month);
        calendar.setText(monthname);
        for (String day : weekdays) {
            Log.d("KALENDAR", "calendar: " + day);
        }
        Button monday = findViewById(R.id.monday);
        monday.setText("Pon.\n" + weekdays[0]);
        Button tuesday = findViewById(R.id.tuesday);
        tuesday.setText("Wt.\n" + weekdays[1]);
        Button wednesday = findViewById(R.id.wednesday);
        wednesday.setText("Śr.\n" + weekdays[2]);
        Button thursday = findViewById(R.id.thursday);
        thursday.setText("Czw.\n" + weekdays[3]);
        Button friday = findViewById(R.id.friday);
        friday.setText("Pt.\n" + weekdays[4]);
        Button saturday = findViewById(R.id.saturday);
        saturday.setText("Sob.\n" + weekdays[5]);
        Button sunday = findViewById(R.id.sunday);
        sunday.setText("Niedz.\n" + weekdays[6]);
        if (weekday == 1) {
            updateDay(monday, 1);
        } else if (weekday == 2) {
            updateDay(tuesday, 2);
        } else if (weekday == 3) {
            updateDay(wednesday, 3);
        } else if (weekday == 4) {
            updateDay(thursday, 4);
        } else if (weekday == 5) {
            updateDay(friday, 5);
        } else if (weekday == 6) {
            updateDay(saturday, 6);
        } else if (weekday == 7) {
            updateDay(sunday, 7);
        }
    }

    public void updateGroupOnActionBar() {
        ActionBar actionBar = getSupportActionBar();
        String groupText = GroupUtils.getGroupText(group);
        if (actionBar != null) {
            actionBar.setTitle("Harmonogram zajęć grupa " + groupText);
        }
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
            JSONObject jsonified = new JSONObject(FileUtils.readFile(this));
            JSONObject jsonifiedDay = jsonified.getJSONObject("Json").getJSONObject(week).getJSONObject(weekday);
            List<String> hourList = HtmlParser.getValuesFromJson(jsonifiedDay);
            int LessonNumbers = 0;
            for (String hourData : hourList) {
                LessonNumbers++;
                JSONArray jsonifiedHour = new JSONArray(hourData);
                if (!Objects.equals(jsonifiedHour.getString(1 + group * 3), "-")) {
                    listLesson.add(jsonifiedHour.getString(1 + group * 3));
                    listHour.add(jsonifiedHour.getString(0) + ", ");
                    listTeacher.add(jsonifiedHour.getString(2 + group * 3));
                    listRoom.add("sala " + jsonifiedHour.getString(3 + group * 3));
                    listNumber.add(LessonNumbers + ".");
                }
            }

            CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(context, listLesson, listHour, listTeacher, listRoom, listNumber);
            listView.setAdapter(customBaseAdapter);
            if (listLesson.isEmpty()) {
                updateTextView("Tego dnia nie ma zajęć");
            }

        } catch (JSONException e) {
            List<String> List = new ArrayList<>();
            CustomBaseAdapter customBaseAdapter = new CustomBaseAdapter(context, List, List, List, List, List);
            listView.setAdapter(customBaseAdapter);
            updateTextView("Tego dnia nie ma zajęć");
            Log.d("EXCEPTION LOG", String.valueOf(e));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateTextView(String texttodisplay) {
        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText(texttodisplay);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.g11) {
            group = 0;
        } else if (item.getItemId() == R.id.g12) {
            group = 1;
        } else if (item.getItemId() == R.id.g21) {
            group = 2;
        } else if (item.getItemId() == R.id.g22) {
            group = 3;
        } else if (item.getItemId() == R.id.g31) {
            group = 4;
        } else if (item.getItemId() == R.id.g32) {
            group = 5;
        }
        updateListView(whichweek, whichweekday);
        try {
            FileUtils.saveGroup(this, String.valueOf(group));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateGroupOnActionBar();
        return super.onOptionsItemSelected(item);
    }

    public void monday(View view) {
        updateDay(view, 1);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
            if (Integer.parseInt(weekdays[0]) > previousday) {
                Currentmonth -= 1;
            }
        }
        Currentday = Integer.parseInt(weekdays[0]);
        updateListView(whichweek, whichweekday);
    }

    public void tuesday(View view) {
        updateDay(view, 2);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (previousday == Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[1]) < previousday) {
            Currentmonth += 1;
        } else if (previousday == Integer.parseInt(weekdays[6]) && Integer.parseInt(weekdays[1]) > previousday) {
            Currentmonth -= 1;
        } else if (previousday != Integer.parseInt(weekdays[6]) && previousday != Integer.parseInt(weekdays[0])) {
            if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
                if (Integer.parseInt(weekdays[1]) < Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[1]) < Integer.parseInt(weekdays[6])) {
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
        Currentday = Integer.parseInt(weekdays[1]);
        updateListView(whichweek, whichweekday);
    }

    public void wednesday(View view) {
        updateDay(view, 3);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (previousday == Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[2]) < previousday) {
            Currentmonth += 1;
        } else if (previousday == Integer.parseInt(weekdays[6]) && Integer.parseInt(weekdays[2]) > previousday) {
            Currentmonth -= 1;
        } else if (previousday != Integer.parseInt(weekdays[6]) && previousday != Integer.parseInt(weekdays[0])) {
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
        updateListView(whichweek, whichweekday);
    }

    public void thursday(View view) {
        updateDay(view, 4);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (previousday == Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[3]) < previousday) {
            Currentmonth += 1;
        } else if (previousday == Integer.parseInt(weekdays[6]) && Integer.parseInt(weekdays[3]) > previousday) {
            Currentmonth -= 1;
        } else if (previousday != Integer.parseInt(weekdays[6]) && previousday != Integer.parseInt(weekdays[0])) {
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
        updateListView(whichweek, whichweekday);
    }

    public void friday(View view) {
        updateDay(view, 5);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (previousday == Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[4]) < previousday) {
            Currentmonth += 1;
        } else if (previousday == Integer.parseInt(weekdays[6]) && Integer.parseInt(weekdays[4]) > previousday) {
            Currentmonth -= 1;
        } else if (previousday != Integer.parseInt(weekdays[6]) && previousday != Integer.parseInt(weekdays[0])) {
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
        updateListView(whichweek, whichweekday);
    }

    public void saturday(View view) {
        updateDay(view, 6);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (previousday == Integer.parseInt(weekdays[0]) && Integer.parseInt(weekdays[5]) < previousday) {
            Currentmonth += 1;
        } else if (previousday == Integer.parseInt(weekdays[6]) && Integer.parseInt(weekdays[5]) > previousday) {
            Currentmonth -= 1;
        } else if (previousday != Integer.parseInt(weekdays[6]) && previousday != Integer.parseInt(weekdays[0])) {
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
        updateListView(whichweek, whichweekday);
    }

    public void sunday(View view) {
        updateDay(view, 7);
        previousday = Integer.parseInt(weekdays[previousweekday - 1]);
        if (Integer.parseInt(weekdays[0]) > Integer.parseInt(weekdays[6])) {
            if (Integer.parseInt(weekdays[6]) < previousday) {
                Currentmonth += 1;
            }
        }
        Currentday = Integer.parseInt(weekdays[6]);
        updateListView(whichweek, whichweekday);
    }

    public void calendar(View view) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
            Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int adjustedDayOfWeek = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            Currentyear = calendar.get(Calendar.YEAR);
            Currentmonth = calendar.get(Calendar.MONTH);
            Currentday = calendar.get(Calendar.DAY_OF_MONTH);
            int daysUntilMonday = Calendar.MONDAY - dayOfWeek;
            if (daysUntilMonday > 0) {
                daysUntilMonday -= 7;
            }
            calendar.add(Calendar.DAY_OF_MONTH, daysUntilMonday);
            int firstdaymonth = calendar.get(Calendar.MONTH) + 1;
            weekdays = new String[7];
            for (int i = 0; i < 7; i++) {
                weekdays[i] = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            updateButtons(adjustedDayOfWeek, firstdaymonth);
            whichweek = String.valueOf(weekOfYear);
            updateListView(whichweek, whichweekday);
        }, Currentyear, Currentmonth, Currentday);
        datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        datePickerDialog.show();
    }

    public void updateDay(View view, int newWeekday) {
        noButtonClicked = false;
        previousweekday = Integer.parseInt(whichweekday);
        whichweekday = String.valueOf(newWeekday);
        resetButtonColors();
        ((Button) view).setTextColor(ContextCompat.getColor(this, R.color.ThemeColor));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
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
}
