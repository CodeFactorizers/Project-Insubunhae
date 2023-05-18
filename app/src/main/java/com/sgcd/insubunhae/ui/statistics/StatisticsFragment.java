package com.sgcd.insubunhae.ui.statistics;

// [통계] 미니 캘린더
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

// [통계] 차트
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentStatisticsBinding;

import java.util.ArrayList;
import java.util.List;

//import com.sgcd.insubunhae.databinding.FragmentStatisticsBinding;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textStatistics;
        statisticsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        CalendarView calendarView = binding.calendarView;
        paintMiniCalendar();

        PieChart pieChart = binding.piechart;
        drawChart(pieChart);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // [통계] 미니 캘린더 구현
    public void paintMiniCalendar() {
        int calc_fam = 0; // 친밀도(계산값)
        int content_score = 1; // 최근 연락내용(점수 1~5점)
        int user_fam = 1; // 친밀도(유저 입력)
        int how_long_month = -1; // 알고 지낸 시간(월)
        int recent_days = -1; // 최근 연락일 ~ 현재(일)
        int recent_score = -1; // 최근 연락일(점수 1~5점)

        // DB에서 data 추출할 예정
        String recent_contact = "23-05-09 13:30:00";
        String first_contact = "23-05-08 13:30:00";

        // currentTimestamp = 현재 시간(yy-MM-dd HH:mm:ss) ---------------------------------*/
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        String currentTimestamp = dateFormat.format(calendar.getTime());
        Log.d("Calendar", "currentTimestamp : " + currentTimestamp);
        //-------------------------------------------------------------------------------*/

        // how_long_month, recent_days, recent_score 계산 --------------------------------*/
        try {
            Date date1 = dateFormat.parse(recent_contact);
            Date date2 = dateFormat.parse(currentTimestamp);

            long milliseconds = date2.getTime() - date1.getTime();

            how_long_month = (int) (milliseconds / (30 * 24 * 60 * 60 * 1000));
            Log.d("Calendar", "how_long_month : " + how_long_month);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Date date1 = dateFormat.parse(first_contact);
            Date date2 = dateFormat.parse(currentTimestamp);

            long milliseconds = date2.getTime() - date1.getTime();

            recent_days = (int) (milliseconds / (24 * 60 * 60 * 1000));
            Log.d("Calendar", "recent_days : " + recent_days);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recent_days >= 0 && recent_days <= 3) {
            recent_score = 5;
        }
        else if (recent_days >= 4 && recent_days <= 7) {
            recent_score = 4;
        }
        else if (recent_days >= 8 && recent_days <= 30) {
            recent_score = 3;
        }
        else if (recent_days >= 31 && recent_days <= 180) {
            recent_score = 2;
        }
        else if (recent_days >= 180) {
            recent_score = 1;
        }
        Log.d("Calendar", "recent_score : " + recent_score);
        //-------------------------------------------------------------------------------*/

    }

    public void drawChart(PieChart pieChart) {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(10.0f, "Green"));
        entries.add(new PieEntry(20.0f, "Yellow"));
        entries.add(new PieEntry(30.0f, "Red"));
        entries.add(new PieEntry(40.0f, "Blue"));

        PieDataSet dataSet = new PieDataSet(entries, "Label");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);

        pieChart.setData(data); // chart에 data설정
        pieChart.invalidate(); // chart 그리기
    }
}