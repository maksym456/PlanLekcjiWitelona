package com.example.planlekcjiwitelona;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomBaseAdapter extends BaseAdapter {

    Context context;
    String[] listlesson;
    String[] listhour;
    String[] listteacher;
    String[] listroom;
    String[] listnumber;
    LayoutInflater inflater;
    public CustomBaseAdapter (Context ctx, List<String> listLesson, List<String> listHour, List<String> listTeacher, List<String> listRoom, List<String> listNumber){
        this.context = ctx;
        this.listlesson = listLesson.toArray(new String[0]);
        this.listhour = listHour.toArray(new String[0]);
        this.listteacher = listTeacher.toArray(new String[0]);
        this.listroom = listRoom.toArray(new String[0]);
        this.listnumber = listNumber.toArray(new String[0]);
        inflater = LayoutInflater.from(ctx);
    }
    @Override
    public int getCount() {
        return listlesson.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.activity_list,null);
        TextView lessonView = convertView.findViewById(R.id.Lesson);
        lessonView.setText(listlesson[position]);
        TextView hourView = convertView.findViewById(R.id.Hour);
        hourView.setText(listhour[position]);
        TextView teacherView = convertView.findViewById(R.id.Teacher);
        teacherView.setText(listteacher[position]);
        TextView roomView = convertView.findViewById(R.id.Room);
        roomView.setText(listroom[position]);
        TextView numberView = convertView.findViewById(R.id.Number);
        numberView.setText(listnumber[position]);
        return convertView;
    }
}
