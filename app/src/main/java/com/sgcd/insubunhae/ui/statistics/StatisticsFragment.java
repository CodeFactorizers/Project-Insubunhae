package com.sgcd.insubunhae.ui.statistics;

// [통계] 미니 캘린더

import static com.sgcd.insubunhae.BR.statisticsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;


// [통계] 차트
import androidx.core.util.Pair;

import java.util.Comparator;

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

import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.FragmentStatisticsBinding;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.DBHelper;
import com.sgcd.insubunhae.ui.statistics.StatisticsViewModel;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private Context context;
    static DBHelper dbHelper;

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

        // 초기 '인물 이름'
        final TextView textView = binding.textView;
        statisticsViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            textView.setText(text);
        });

        ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();
        String[] contactNameArray = new String[contactsList.size()];
        //List<Integer> contact_id_list_int = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            contactNameArray[i] = contactsList.get(i).getName();
        }
        statisticsViewModel.setText(String.valueOf(cur_contact_id));

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
        //SQLiteDatabase db = dbHelper.getWritableDatabase();
        //calculateFamiliarity(db);
        //db.close();

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

        /*if (dbHelper != null) {
            dbHelper.close();
        }*/
    }

    // 인물 변경하기 버튼
    public void showContactIdSelectionDialog() {
        ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();

        String[] contactNameArray = new String[contactsList.size()];
        //List<Integer> contact_id_list_int = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            contactNameArray[i] = contactsList.get(i).getName();
            //contact_id_list_int.set(i, Integer.parseInt(contactsList.get(i).getId()));
        }
        int start_index = Integer.parseInt(contactsList.get(0).getId());

        // 다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select contact_id")
                .setItems(contactNameArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cur_contact_id = contactIds.get(which);
                        cur_contact_id = which + start_index + 1;
                        //cur_contact_id =
                        //Log.d("paintMiniCal", "cur_contact_id : " + cur_contact_id);

                        // name tag
                        StatisticsViewModel statisticsViewModel =
                                new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
                        final TextView textView = binding.textView;
                        statisticsViewModel.getText().observe(getViewLifecycleOwner(), text -> {
                            textView.setText(text);
                        });
                        statisticsViewModel.setText(String.valueOf(cur_contact_id));

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

//    // 친밀도 계산 [SMS only]
//    public static void calculateFamiliarity(SQLiteDatabase db) {
//        // MAIN_CONTACTS 에서 contact_id 리스트 가져오기
//        List<String> contact_id_list = new ArrayList<>();
//        contact_id_list = dbHelper.getAttributeValueFromTable("MAIN_CONTACTS", "contact_id", "contact_id >= 0");
//        List<Integer> contact_id_list_int = new ArrayList<>();
//        for (String str : contact_id_list) {
//            int number = Integer.parseInt(str);
//            contact_id_list_int.add(number);
//        }
//        Log.d("CalFam", "contact_id_list_int : " + contact_id_list_int);
//
//        //각 contact_id에 대하여, 친밀도(calc_fam) 계산
//        for (Integer cur_contact_id : contact_id_list_int) {
//
//            int calc_fam = 0; // 친밀도(계산값)
//            int recent_content = 0; //
//            int content_score = 1; // 최근 연락내용(점수 1~5점)
//            int user_fam = cur_contact_id; // 친밀도(유저 입력)
//            int how_long_month = -1; // 알고 지낸 시간(월)
//            int recent_days = -1; // 최근 연락일 ~ 현재(일)
//            int recent_score = -1; // 최근 연락일(점수 1~5점)
//
//            // [DB에서 추출] MESSENGER_HISTORY의 datetime, count
//            List<String> m_dt = new ArrayList<>();
//            m_dt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
//                    "datetime", "contact_id = " + cur_contact_id);
//            //Log.d("CalFam", "sms_datetime : " + m_dt);
//            List<String> m_cnt = new ArrayList<>();
//            m_cnt = dbHelper.getAttributeValueFromTable("MESSENGER_HISTORY",
//                    "count", "contact_id = " + cur_contact_id);
//            List<Integer> m_cnt_int = new ArrayList<>();
//            for (String str : m_cnt) {
//                int number = Integer.parseInt(str);
//                m_cnt_int.add(number);
//            }
//            //Log.d("CalFam", "sms_cnt : " + m_cnt);
//
//            // [DB에서 추출] recent_contact, first_contact
//            Long recent_contact = dbHelper.getMaxOfAttribute("MESSENGER_HISTORY", "datetime", cur_contact_id);
//            //Log.d("CalFam", "recent_contact : " + recent_contact);
//            Date date_recent_contact = new Date(recent_contact);
//            SimpleDateFormat dateFormat_recent_contact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
//            String timestamp_recent_contact = dateFormat_recent_contact.format(date_recent_contact);
//
//            Long first_contact = dbHelper.getMinOfAttribute("MESSENGER_HISTORY", "datetime", cur_contact_id);
//            //Log.d("CalFam", "first_contact : " + first_contact);
//            Date date_first_contact = new Date(first_contact);
//            SimpleDateFormat dateFormat_first_contact = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
//            String timestamp_first_contact = dateFormat_first_contact.format(date_first_contact);
//
//            // currentTimestamp = 현재 시간(yy-MM-dd HH:mm:ss) ---------------------------------*/
//            Date date_current = new Date();
//
//            SimpleDateFormat dateFormat_current = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
//
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(date_current);
//
//            String timestamp_current = dateFormat_current.format(calendar.getTime());
//            //-------------------------------------------------------------------------------*/
//
//            // how_long_month, recent_days, recent_score 계산 --------------------------------*/
//            try {
//                Date date1 = dateFormat_recent_contact.parse(timestamp_first_contact);
//                Date date2 = dateFormat_current.parse(timestamp_current);
//
//                double milliseconds = date2.getTime() - date1.getTime();
//
//                how_long_month = (int) (Math.round(milliseconds / (30.0 * 24.0 * 60.0 * 60.0 * 1000.0)));
//            } catch (
//                    Exception e) {
//                e.printStackTrace();
//            }
//
//            try {
//                Date date1 = dateFormat_first_contact.parse(timestamp_recent_contact);
//                Date date2 = dateFormat_current.parse(timestamp_current);
//
//                long milliseconds = date2.getTime() - date1.getTime();
//
//                recent_days = (int) (milliseconds / (24 * 60 * 60 * 1000));
//
//            } catch (
//                    Exception e) {
//                e.printStackTrace();
//            }
//
//            if (recent_days >= 0 && recent_days <= 3) {
//                recent_score = 5;
//            } else if (recent_days >= 4 && recent_days <= 7) {
//                recent_score = 4;
//            } else if (recent_days >= 8 && recent_days <= 30) {
//                recent_score = 3;
//            } else if (recent_days >= 31 && recent_days <= 180) {
//                recent_score = 2;
//            } else if (recent_days >= 180) {
//                recent_score = 1;
//            }
//            //-------------------------------------------------------------------------------*/
//
//            // recent_content
//            for (
//                    int number : m_cnt_int) {
//                recent_content += number;
//            }
//            if (recent_content == 0) {
//                content_score = 1;
//            } else if (recent_content >= 1 && recent_content <= 500) {
//                content_score = 2;
//            } else if (recent_content >= 501 && recent_content <= 1000) {
//                content_score = 3;
//            } else if (recent_content >= 1001 && recent_content <= 9999) {
//                content_score = 4;
//            } else if (recent_content >= 10000) {
//                content_score = 5;
//            }
//
//            // Calculate
//            calc_fam = content_score * user_fam * how_long_month * recent_score;
//
//            // Familiarity Equation Final Check
//            //Log.d("CalFam", "content_score : " + content_score); //최근 연락 내용
//            //Log.d("CalFam", "user_fam : " + user_fam); //친밀도 (유저 입력)
//            //Log.d("CalFam", "how_long_month : " + how_long_month); //알고 지낸 시간(월)
//            //Log.d("CalFam", "recent_score : " + recent_score); //최근 연락일
//            Log.d("CalFam", "contact_id : " + cur_contact_id + " || calc_fam : " + calc_fam); //친밀도(계산값)
//
//            // [DB에 data 추가] cur_contact_id에 대해 user_fam, recent_contact, first_contact, calc_fam 값 추가
//            ContentValues values = new ContentValues();
//            values.put("contact_id", cur_contact_id);
//            values.put("user_fam", user_fam);
//            values.put("calc_fam", calc_fam);
//            values.put("recent_contact", timestamp_recent_contact);
//            values.put("first_contact", timestamp_first_contact);
//            db.insert("ANALYSIS", null, values);
//
//
//        }
//    }

    public void drawPieChart(PieChart pieChart) {
        // contact_id list 가져오기
        ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();

        String[] contactNameArray = new String[contactsList.size()];
        String[] contactIdArray = new String[contactsList.size()];
        int[] contactIdIntArray = new int[contactIdArray.length];
        for (int i = 0; i < contactsList.size(); i++) {
            contactNameArray[i] = contactsList.get(i).getName();
            contactIdArray[i] = contactsList.get(i).getId();
            contactIdIntArray[i] = Integer.parseInt(contactIdArray[i]);
        }

        int[] calc_fam_list = new int[contactIdArray.length];
        for (int i = 0; i < contactsList.size(); i++) {
            calc_fam_list[i] = dbHelper.getIntFromTable("ANALYSIS", "calc_fam", "contact_id = " + contactIdIntArray[i]);
        }

        List<Pair<Integer, String>> chartData = new ArrayList<>();
        for (int i = 0; i < calc_fam_list.length; i++) {
            chartData.add(new Pair<>(calc_fam_list[i], contactNameArray[i]));
        }

        // 정렬
        Collections.sort(chartData, new Comparator<Pair<Integer, String>>() {
            @Override // 내림차순 정렬
            public int compare(Pair<Integer, String> o1, Pair<Integer, String> o2) {
                return o2.first - o1.first;
            }
        });

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);

        List<PieEntry> entries = new ArrayList<>();
        int count = Math.min(chartData.size(), 5);
        int[] colors = {0xFF66FF99, 0xFFFFFF99, 0xFFFF6666, 0xFF99CCFF, 0xFFCCFF99};
        for (int i = 0; i < count; i++) {
            Pair<Integer, String> data = chartData.get(i);
            entries.add(new PieEntry(data.first, data.second));
        }

        PieDataSet dataSet = new PieDataSet(entries, "LabelPie");

        //dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);

        pieChart.setData(data); // chart에 data설정
        pieChart.invalidate(); // chart 그리기
    }

    // [통계] 미니 캘린더 색칠
    public void paintMiniCalendar(MaterialCalendarView calendarView) {
        //Log.d("paintMiniCal", "let's paint!");

        //SMS
        contactedDates_sms = dbHelper.getLongFromTable("MESSENGER_HISTORY",
                "datetime", "contact_id = " + cur_contact_id);
        Log.d("paintMiniCal", "paint sms dates : " + contactedDates_sms);

        List<CalendarDay> paintedDates = new ArrayList<>();
        for (Long paintingDate : contactedDates_sms) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(paintingDate);

            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            paintedDates.add(calendarDay);
        }
        Log.d("paintMiniCal", "paint sms dates again : " + paintedDates);

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
        Log.d("paintMiniCal", "paint sms+call dates again : " + paintedDates);
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