package com.sgcd.insubunhae.ui.statistics;

// [통계] 미니 캘린더
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import android.content.DialogInterface;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

    int cur_contact_id = 1; //현재 인물(임의로 1 대입)
    private DayViewDecorator decorator;

    // 연락한 날짜 리스트
    List<Long> contactedDates_sms;
    List<Long> contactedDates_call;

    int[] weeklyFrequencies;

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

        // 인물 변경하기 버튼
        Button button = binding.contactChangeButton;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactIdSelectionDialog();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 인물 변경하기 버튼
    public void showContactIdSelectionDialog() {
        List<Integer> contactIds = dbHelper.getContactIds();

        // contact_id 목록을 문자열로 변환
        String[] contactIdArray = new String[contactIds.size()];
        String[] contactNameArray = new String[contactIds.size()];
        for (int i = 0; i < contactIds.size(); i++) {
            contactIdArray[i] = String.valueOf(contactIds.get(i));
            contactNameArray[i] = dbHelper.getNameFromContactID(Integer.parseInt(contactIdArray[i]));
            //Log.d("showContactIdSelectionDialog", "name of this contact_id : " + contactNameArray[i]);
        }

        // 다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select contact_id")
                .setItems(contactNameArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cur_contact_id = contactIds.get(which);
                        cur_contact_id = which + 1;
                        //Log.d("paintMiniCal", "cur_contact_id : " + cur_contact_id);

                        // [Draw Again] bar chart
                        BarChart barChart = binding.barchart;
                        drawBarChart(barChart);

                        // [Draw Again] calendar
                        MaterialCalendarView calendarView = binding.calendarView;
                        paintMiniCalendar(calendarView);
                    }
                })
                .show();
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
        Log.d("CalFam", "sms_datetime : " + m_dt);
        List<String> m_cnt = new ArrayList<>();
        m_cnt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
                "count", "contact_id = " + cur_contact_id);
        Log.d("CalFam", "sms_cnt : " + m_cnt);

        // [SMS only] DB에서 추출 : recent_contact, first_contact
        Long recent_contact = dbHelper.getMaxOfAttribute("MESSENGER_HISTORY", "datetime");
        Log.d("CalFam", "recent_contact : " + recent_contact);
        Date date_recent_contact = new Date(recent_contact);
        SimpleDateFormat dateFormat_recent_contact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String timestamp_recent_contact = dateFormat_recent_contact.format(date_recent_contact);

        Long first_contact = dbHelper.getMinOfAttribute("MESSENGER_HISTORY", "datetime");
        Log.d("CalFam", "first_contact : " + first_contact);
        Date date_first_contact = new Date(first_contact);
        SimpleDateFormat dateFormat_first_contact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String timestamp_first_contact = dateFormat_first_contact.format(date_first_contact);


        // currentTimestamp = 현재 시간(yy-MM-dd HH:mm:ss) ---------------------------------*/
        Date date_current = new Date();

        SimpleDateFormat dateFormat_current = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date_current);

        String timestamp_current = dateFormat_current.format(calendar.getTime());

        //-------------------------------------------------------------------------------*/

        // how_long_month, recent_days, recent_score 계산 --------------------------------*/
        try {
            Date date1 = dateFormat_recent_contact.parse(timestamp_recent_contact);
            //Date date1 = dateFormat.parse("23-05-09 13:30:00");
            Date date2 = dateFormat_current.parse(timestamp_current);

            long milliseconds = date2.getTime() - date1.getTime();

            how_long_month = (int) (milliseconds / (30 * 24 * 60 * 60 * 1000));

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //Date date1 = dateFormat.parse(first_contact);
            Date date1 = dateFormat_first_contact.parse(timestamp_first_contact);
            Date date2 = dateFormat_current.parse(timestamp_current);

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
        //Log.d("paintMiniCal", "let's paint!");

        //SMS
        contactedDates_sms = dbHelper.getLongFromTable("MESSENGER_HISTORY",
                "datetime", "contact_id = " + cur_contact_id);
        //Log.d("paintMiniCal", "paint sms dates : " + contactedDates_sms);

        List<CalendarDay> paintedDates = new ArrayList<>();
        for (Long paintingDate : contactedDates_sms) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(paintingDate);

            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            paintedDates.add(calendarDay);
        }
        //Log.d("paintMiniCal", "paint sms dates again : " + paintedDates);

        //CALL LOG
        contactedDates_call = dbHelper.getLongFromTable("CALL_LOG",
                "datetime", "contact_id = " + cur_contact_id);
        //Log.d("paintMiniCal", "paint call dates : " + contactedDates_call);

        //List<CalendarDay> paintedDates = new ArrayList<>();
        for (Long paintingDate : contactedDates_call) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(paintingDate);

            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            paintedDates.add(calendarDay);
        }
        //Log.d("paintMiniCal", "paint sms+call dates again : " + paintedDates);

        DayViewDecorator decorator = new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return paintedDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF0066")));
            }
        };

        calendarView.removeDecorators();
        calendarView.addDecorator(decorator);
        calendarView.invalidateDecorators();
        Log.d("paintMiniCal", "painting end");
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

    // [SMS only]
    public void aggregateWeekContact(List<Long> contactedDates_sms) {
        weeklyFrequencies = new int[7];

        for (Long contactedDate : contactedDates_sms) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(contactedDate);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int index = dayOfWeek - 1;

            weeklyFrequencies[index]++;
        }
    }

    public void drawBarChart(BarChart barChart) {
        aggregateWeekContact(contactedDates_sms); //일주일 데이터 누적 리스트

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < weeklyFrequencies.length; i++) {
            entries.add(new BarEntry(i, weeklyFrequencies[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Weekly SMS Frequency");
        BarData barData = new BarData(dataSet);

        // 차트 깔꼼하게 수정하기^~^ ---------------------------------------------------- */
        barData.setDrawValues(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};
                int index = (int) value;
                if (index >= 0 && index < daysOfWeek.length) {
                    return daysOfWeek[index];
                }
                return "";
            }
        });

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }
        });
        yAxisLeft.setGranularity(1f);
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);

        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setDescription(null);
        barChart.getLegend().setEnabled(false);
        /* ----------------------------------------------------------------------- */

        barChart.setData(barData);
        barChart.invalidate();
    }

}