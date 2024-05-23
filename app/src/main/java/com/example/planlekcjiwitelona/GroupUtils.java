package com.example.planlekcjiwitelona;

public class GroupUtils {

    public static String getGroupText(int group) {
        switch (group) {
            case 0:
                return "1(1)";
            case 1:
                return "1(2)";
            case 2:
                return "2(1)u";
            case 3:
                return "2(2)u";
            case 4:
                return "3(1)u";
            case 5:
                return "3(2)u";
            default:
                return "Błąd";
        }
    }
}
