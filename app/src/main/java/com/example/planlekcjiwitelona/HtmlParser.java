package com.example.planlekcjiwitelona;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.ArrayList;

public class HtmlParser {
    private static final TimeZone timeZone = TimeZone.getTimeZone("Europe/Warsaw");

    public static JSONObject parseSchedule(String htmlContent) throws JSONException {
        boolean didItJustSkipAWeek = true;
        int previousCalendarWeek = 0;
        Document doc = Jsoup.parse(htmlContent);
        JSONObject result = new JSONObject();
        JSONArray dni = new JSONArray();
        JSONObject dzien = new JSONObject();
        JSONArray godzina = new JSONArray();
        JSONObject tygodnie = new JSONObject();
        JSONObject tydzien = new JSONObject();
        String dayString = "";
        int previousweekday = -1;

        for (int i = 1; i <= 7; i++) {
            tydzien.put(Integer.toString(i), "siema");
        }
        Elements dane = doc.select("table").get(0).select("tbody > tr");
        for (int rowcount = 4; rowcount < dane.size(); rowcount += 10) {
            for (int j = 0; j < 7; j++) {
                for (int i = 0; i < 19; i++) {
                    godzina.put(dane.get(rowcount + j).select("td").get(i).text());
                }
                dzien.put(dane.get(rowcount + j).select("td").get(0).text(), godzina);
                godzina = new JSONArray();
            }
            dni.put(dzien);
            dayString = dane.get(rowcount - 3).select("td").get(0).text();
            int weekday = dayOfTheWeek(dayString.split(" ")[0]);
            if (weekday != 0) {
                if (previousweekday >= weekday) {
                    String dateString = dayString.split(" ")[1];
                    int year = Integer.parseInt(dateString.split("-")[0]);
                    int month = Integer.parseInt(dateString.split("-")[1]) - 1;
                    int day = Integer.parseInt(dateString.split("-")[2]);
                    Calendar calendar = new GregorianCalendar(timeZone);
                    calendar.set(year, month, day);

                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    if (!didItJustSkipAWeek && previousCalendarWeek + 1 != calendar.get(Calendar.WEEK_OF_YEAR) - 1) {
                        tygodnie.put(String.valueOf(previousCalendarWeek + 1), tydzien);
                        didItJustSkipAWeek = true;
                    } else {
                        tygodnie.put(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR) - 1), tydzien);
                        didItJustSkipAWeek = false;
                    }
                    previousCalendarWeek = calendar.get(Calendar.WEEK_OF_YEAR) - 1;
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
        Calendar calendar = new GregorianCalendar(Integer.parseInt(dayString.split(" ")[1].split("-")[0]), Integer.parseInt(dayString.split(" ")[1].split("-")[1]) - 1, Integer.parseInt(dayString.split(" ")[1].split("-")[2]));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        tygodnie.put(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)), tydzien);
        result.put("Json", tygodnie);
        return result;
    }

    public static int dayOfTheWeek(String dayAsText) {
        switch (dayAsText) {
            case "Poniedziałek":
                return 1;
            case "Wtorek":
                return 2;
            case "Środa":
                return 3;
            case "Czwartek":
                return 4;
            case "Piątek":
                return 5;
            case "Sobota":
                return 6;
            case "Niedziela":
                return 7;
            default:
                return 0;
        }
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
}
