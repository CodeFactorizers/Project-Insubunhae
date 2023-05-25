package com.sgcd.insubunhae.ui.statistics;
// [통계] 미니 캘린더

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;


// [통계] 차트
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentStatisticsBinding;
import com.sgcd.insubunhae.db.DBHelper;
import com.sgcd.insubunhae.ui.statistics.StatisticsViewModel;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private Context context;
    DBHelper dbHelper;

    int cur_contact_id = 2; //현재 인물(임의로 2 대입)

    //onAttach : activity의 context 저장
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        dbHelper = new DBHelper(context);
        SQLiteDatabase idb = dbHelper.getWritableDatabase();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textStatistics;
        //statisticsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //현재 인물의 CALL_LOG에서 data받아오기 : datetime, duration
        List<String> c_dt = new ArrayList<>();
        c_dt = dbHelper.getAttributeValueFromTable("CALL_LOG",
                "datetime", "contact_id = " + cur_contact_id);
        //Log.d("StatisticsFragment", "result1 : " + c_dt);
        List<String> c_dr = new ArrayList<>();
        c_dr = dbHelper.getAttributeValueFromTable("CALL_LOG",
                "duration", "contact_id = " + cur_contact_id);
        //Log.d("StatisticsFragment", "result2 : " + c_dr);

        // 친밀도 계산
        calculateFamiliarity();

        // 캘린더
        MaterialCalendarView calendarView = binding.calendarView;
        paintMiniCalendar(calendarView);

        // 차트
        PieChart pieChart1 = binding.piechart1;
        drawPieChart(pieChart1);
        PieChart pieChart2 = binding.piechart2;
        drawPieChart(pieChart2);
        BarChart barChart = binding.barchart;
        drawBarChart(barChart);

        //final TextView textView = binding.textStatistics;
        //statisticsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 친밀도 계산
    public void calculateFamiliarity() {
        int calc_fam = 0; // 친밀도(계산값)
        int content_score = 1; // 최근 연락내용(점수 1~5점)
        int user_fam = 1; // 친밀도(유저 입력)
        int how_long_month = -1; // 알고 지낸 시간(월)
        int recent_days = -1; // 최근 연락일 ~ 현재(일)
        int recent_score = -1; // 최근 연락일(점수 1~5점)


        //현재 인물의 MESSENGER_HISTORY에서 data받아오기 : datetime, count
        List<String> m_dt = new ArrayList<>();
        m_dt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
                "datetime", "contact_id = " + cur_contact_id);
        Log.d("paintMiniCal", "sms_datetime : " + m_dt);
        List<String> m_cnt = new ArrayList<>();
        m_cnt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
                "count", "contact_id = " + cur_contact_id);
        Log.d("paintMiniCal", "sms_cnt : " + m_cnt);

        // DB에서 data 추출할 예정
        //String recent_contact = "23-05-09 13:30:00";
        Long recent_contact = dbHelper.getMaxOfAttribute("MESSENGER_HISTORY", "datetime");
        Log.d("paintMiniCal", "recent_contact : " + recent_contact);
        //String first_contact = "23-05-08 13:30:00";
        Long first_contact = dbHelper.getMinOfAttribute("MESSENGER_HISTORY", "datetime");
        Log.d("paintMiniCal", "first_contact : " + first_contact);


        // currentTimestamp = 현재 시간(yy-MM-dd HH:mm:ss) ---------------------------------*/
        Date currentDate = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        String currentTimestamp = dateFormat.format(calendar.getTime());

        //-------------------------------------------------------------------------------*/

        // how_long_month, recent_days, recent_score 계산 --------------------------------*/
        try {
            //Date date1 = dateFormat.parse(recent_contact);
            Date date1 = dateFormat.parse("23-05-09 13:30:00");
            Date date2 = dateFormat.parse(currentTimestamp);

            long milliseconds = date2.getTime() - date1.getTime();

            how_long_month = (int) (milliseconds / (30 * 24 * 60 * 60 * 1000));

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //Date date1 = dateFormat.parse(first_contact);
            Date date1 = dateFormat.parse("23-05-08 13:30:00");
            Date date2 = dateFormat.parse(currentTimestamp);

            long milliseconds = date2.getTime() - date1.getTime();

            recent_days = (int) (milliseconds / (24 * 60 * 60 * 1000));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recent_days >= 0 && recent_days <= 3) {
            recent_score = 5;
        } else if (recent_days >= 4 && recent_days <= 7) {
            recent_score = 4;
        } else if (recent_days >= 8 && recent_days <= 30) {
            recent_score = 3;
        } else if (recent_days >= 31 && recent_days <= 180) {
            recent_score = 2;
        } else if (recent_days >= 180) {
            recent_score = 1;
        }

        //-------------------------------------------------------------------------------*/

    }

    // [통계] 미니 캘린더 색칠
    public void paintMiniCalendar(MaterialCalendarView calendarView) {

        //우선 SMS만, 2번 인물만.
        List<Long> contactedDates = dbHelper.getLongFromTable("MESSENGER_HISTORY",
                "datetime", "contact_id = 2");

        List<CalendarDay> paintedDates = new ArrayList<>();
        for (Long paintingDate : contactedDates) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(paintingDate);

            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            paintedDates.add(calendarDay);
        }

        DayViewDecorator decorator = new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return paintedDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#9999FF")));
            }
        };

        calendarView.addDecorator(decorator);
    }

    public void drawPieChart(PieChart pieChart) {
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


        PieDataSet dataSet = new PieDataSet(entries, "LabelPie");

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);

        pieChart.setData(data); // chart에 data설정
        pieChart.invalidate(); // chart 그리기
    }


    public void drawBarChart(BarChart barChart) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 10f));
        entries.add(new BarEntry(1f, 20f));
        entries.add(new BarEntry(2f, 80f));
        entries.add(new BarEntry(3f, 40f));
        entries.add(new BarEntry(4f, 0f));
        entries.add(new BarEntry(5f, 20f));
        entries.add(new BarEntry(6f, 15f));
        entries.add(new BarEntry(7f, 70f));

        BarDataSet dataSet = new BarDataSet(entries, "LabelBar");

        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setDrawLabels(false);
        barChart.getXAxis().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisRight().setDrawAxisLine(false);

        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.invalidate();
    }

}