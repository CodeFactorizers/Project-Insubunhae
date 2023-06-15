package com.sgcd.insubunhae.ui.statistics;

// [통계] 미니 캘린더

import static com.sgcd.insubunhae.BR.statisticsViewModel;

import java.text.DecimalFormat;
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
    int contactedDates_sms_total = 0;
    List<Long> contactedDates_call;
    int contactedDates_call_total = 0;

    int[] weeklyFrequencies = new int[7];

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
        statisticsViewModel.setDBHelper(dbHelper);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false);
        View root = binding.getRoot();

        // 초기 '인물 이름'
        final TextView textView = binding.textView;
        statisticsViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            textView.setText("전체 통계");
        });

        ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();
        int start_index = Integer.parseInt(contactsList.get(0).getId());
        String[] contactNameArray = new String[contactsList.size()];
        //List<Integer> contact_id_list_int = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            contactNameArray[i] = contactsList.get(i).getName();
        }
        //statisticsViewModel.setText("전체 통계");
        //statisticsViewModel.setText(String.valueOf(contactNameArray[0]));

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
        //List<CalendarDay> paintedDates_sms = paintMiniCalendar_aggregateDates_sms();

        // [Draw] information table
        TextView textViewTable1 = binding.textViewTable1;
        //textViewTable1.setPaintFlags(textViewTable1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
//        statisticsViewModel.getFirstContact(cur_contact_id).observe(getViewLifecycleOwner(), text -> {
//            textViewTable1.setText(text);
//        });
        textViewTable1.setVisibility(View.INVISIBLE);
        TextView textViewTable2 = binding.textViewTable2;
//        statisticsViewModel.getRecentContact(cur_contact_id).observe(getViewLifecycleOwner(), text -> {
//            textViewTable2.setText(text);
//        });
        textViewTable2.setVisibility(View.INVISIBLE);
        TextView textViewTable3 = binding.textViewTable3;
//        statisticsViewModel.getFam(cur_contact_id).observe(getViewLifecycleOwner(), text -> {
//            textViewTable3.setText(text);
//        });
        textViewTable3.setVisibility(View.INVISIBLE);

        // 차트
        PieChart pieChart1 = binding.piechart1;
        drawPieChart_totalFam(pieChart1);
        pieChart1.setVisibility(View.VISIBLE);

        PieChart pieChart2 = binding.piechart2;
        drawPieChart_compareCallvsSms_initial(pieChart2);
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
                        Log.d("Check CI in dialog", "cur_contact_id : " + cur_contact_id);

                        // name tag
                        StatisticsViewModel statisticsViewModel =
                                new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
                        final TextView textView = binding.textView;
                        //statisticsViewModel.setText(String.valueOf(contactNameArray[cur_contact_id - 1 - start_index]));
                        statisticsViewModel.getText().observe(getViewLifecycleOwner(), text -> {
                            textView.setText(text);
                        });
                        //statisticsViewModel.setText(String.valueOf(cur_contact_id - 1));
                        statisticsViewModel.setText(String.valueOf(contactNameArray[cur_contact_id - 1 - start_index]));

                        // [Draw Again] calendar
                        MaterialCalendarView calendarView = binding.calendarView;
                        paintMiniCalendar(calendarView);
                        //List<CalendarDay> paintedDates_sms = paintMiniCalendar_aggregateDates_sms();
                        //paintMiniCalendar(calendarView, paintedDates_sms, "#000000");
                        //List<CalendarDay> paintedDates_call = paintMiniCalendar_aggregateDates_call();

                        TextView textView_pieTitle = binding.textViewPieTitle;
                        textView_pieTitle.setVisibility(View.INVISIBLE);

                        //Log.d("AddTable", "cur id : " + cur_contact_id);
                        // [Draw] information table
                        TextView textViewTable1 = binding.textViewTable1;
                        statisticsViewModel.getFirstContact(cur_contact_id).observe(getViewLifecycleOwner(), text -> {
                            //Log.d("AddTable>", "cur id : " + cur_contact_id);
                            textViewTable1.setText(text);
                        });
                        textViewTable1.setVisibility(View.VISIBLE);
                        TextView textViewTable2 = binding.textViewTable2;
                        statisticsViewModel.getRecentContact(cur_contact_id).observe(getViewLifecycleOwner(), text -> {
                            //Log.d("AddTable>>", "cur id : " + cur_contact_id);
                            textViewTable2.setText(text);
                        });
                        textViewTable2.setVisibility(View.VISIBLE);
                        TextView textViewTable3 = binding.textViewTable3;
                        statisticsViewModel.getFam(cur_contact_id).observe(getViewLifecycleOwner(), text -> {
                            //Log.d("AddTable>>>", "cur id : " + cur_contact_id);
                            textViewTable3.setText(text);
                        });
                        textViewTable3.setVisibility(View.VISIBLE);

                        // [Draw Again] pie chart
                        PieChart pieChart1 = binding.piechart1;
                        drawPieChart_totalFam(pieChart1);
                        pieChart1.setVisibility(View.INVISIBLE);

                        PieChart pieChart2 = binding.piechart2;
                        drawPieChart_compareCallvsSms(pieChart2);
                        pieChart2.setVisibility(View.VISIBLE);

                        // [Draw Again] bar chart
                        BarChart barChart = binding.barchart;
                        drawBarChart(barChart);
                    }
                })
                .show();
    }

    // 파이 그래프에 %넣으려고 포맷팅하는
    public class PercentValueFormatter extends ValueFormatter {
        private DecimalFormat format;

        public PercentValueFormatter() {
            format = new DecimalFormat("##.#%");
        }

        @Override
        public String getFormattedValue(float value) {
            return format.format(value);
        }
    }

    public void drawPieChart_compareCallvsSms_initial(PieChart pieChart) {
        //Log.d("sehee update", "second pie start");

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);

        ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();
        int start_index = Integer.parseInt(contactsList.get(0).getId());
        List<Long> contactedDates_sms_initial = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            List<Long> tmp = dbHelper.getLongFromTable("MESSENGER_HISTORY",
                    "datetime", "contact_id = " + (i + start_index + 1));
            contactedDates_sms_initial.addAll(tmp);
            contactedDates_sms_total += contactedDates_sms_initial.size();
        }
        List<Long> contactedDates_call_initial = new ArrayList<>();
        for (int i = 0; i < contactsList.size(); i++) {
            List<Long> tmp = dbHelper.getLongFromTable("CALL_LOG",
                    "datetime", "contact_id = " + (i + start_index + 1));
            contactedDates_call_initial.addAll(tmp);
            contactedDates_call_total += contactedDates_call_initial.size();
        }

        float total_sms_portion = (float) contactedDates_sms_total / (contactedDates_sms_total + contactedDates_call_total);
        float total_call_portion = (float) contactedDates_call_total / (contactedDates_sms_total + contactedDates_call_total);
        //Log.d("sehee update", "total portion sms : " + total_sms_portion);
        //Log.d("sehee update", "total portion call : " + total_call_portion);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(total_call_portion, "Call"));
        entries.add(new PieEntry(total_sms_portion, "SMS"));

        int[] colors = {0xFF66FF99, 0xFFFFFF99, 0xFFFF6666, 0xFF99CCFF, 0xFFCCFF99};

        PieDataSet dataSet = new PieDataSet(entries, "LabelPie");

        //dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new PercentValueFormatter());

        PieData data = new PieData(dataSet);

        pieChart.setData(data); // chart에 data설정
        pieChart.invalidate(); // chart 그리기
    }

    public void drawPieChart_compareCallvsSms(PieChart pieChart) {
        //Log.d("sehee update", "second pie start");

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);

        float total_sms_portion = (float) contactedDates_sms.size() / (contactedDates_sms.size() + contactedDates_call.size());
        float total_call_portion = (float) contactedDates_call.size() / (contactedDates_sms.size() + contactedDates_call.size());
        //Log.d("sehee update", "total portion sms : " + total_sms_portion);
        //Log.d("sehee update", "total portion call : " + total_call_portion);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(total_call_portion, "Call"));
        entries.add(new PieEntry(total_sms_portion, "SMS"));

        int[] colors = {0xFF66FF99, 0xFFFFFF99, 0xFFFF6666, 0xFF99CCFF, 0xFFCCFF99};

        PieDataSet dataSet = new PieDataSet(entries, "LabelPie");

        //dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new PercentValueFormatter());

        PieData data = new PieData(dataSet);

        pieChart.setData(data); // chart에 data설정
        pieChart.invalidate(); // chart 그리기
    }

    public void drawPieChart_totalFam(PieChart pieChart) {
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
            Log.d("Check CI in totalpie", "id : " + calc_fam_list[i] + ", name : " + contactNameArray[i]);
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
        int count = Math.min(chartData.size(), 10);
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

//    public List<CalendarDay> paintMiniCalendar_aggregateDates_sms() {
//        //SMS
//        contactedDates_sms = dbHelper.getLongFromTable("MESSENGER_HISTORY",
//                "datetime", "contact_id = " + cur_contact_id);
//        Log.d("paintMiniCal", "paint sms dates : " + contactedDates_sms);
//
//        List<CalendarDay> paintedDates_sms = new ArrayList<>();
//        for (Long paintingDate_sms : contactedDates_sms) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(paintingDate_sms);
//
//            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH) + 1,
//                    calendar.get(Calendar.DAY_OF_MONTH));
//
//            paintedDates_sms.add(calendarDay);
//        }
//        Log.d("paintMiniCal", "paint sms dates again : " + paintedDates_sms);
//
//        return paintedDates_sms;
//
//    }

//    public List<CalendarDay> paintMiniCalendar_aggregateDates_call() {
//        //CALL LOG
//        contactedDates_call = dbHelper.getLongFromTable("CALL_LOG",
//                "datetime", "contact_id = " + cur_contact_id);
//        Log.d("paintMiniCal", "paint call dates : " + contactedDates_call);
//
//        List<CalendarDay> paintedDates_call = new ArrayList<>();
//        for (Long paintingDate_call : contactedDates_call) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(paintingDate_call);
//
//            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH) + 1,
//                    calendar.get(Calendar.DAY_OF_MONTH));
//
//            paintedDates_call.add(calendarDay);
//        }
//        Log.d("paintMiniCal", "paint call dates again : " + paintedDates_call);
//
//        return paintedDates_call;
//    }

    // [통계] 미니 캘린더 색칠
    public void paintMiniCalendar(MaterialCalendarView calendarView) {
        //Log.d("sehee update", "cal start");

        //SMS
        contactedDates_sms = dbHelper.getLongFromTable("MESSENGER_HISTORY",
                "datetime", "contact_id = " + cur_contact_id);
        //Log.d("paintMiniCal", "paint sms dates : " + contactedDates_sms);

        List<CalendarDay> paintedDates = new ArrayList<>();
        for (Long paintingDate_sms : contactedDates_sms) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(paintingDate_sms);

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

        //List<CalendarDay> paintedDates_call = new ArrayList<>();
        for (Long paintingDate_call : contactedDates_call) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(paintingDate_call);

            CalendarDay calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));

            paintedDates.add(calendarDay);
        }
        //Log.d("paintMiniCal", "paint sms + call dates again : " + paintedDates);

        DayViewDecorator decorator = new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                //CalendarDay day = view.getDate();
                return paintedDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF0066")));
                //view.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
            }
        };

        calendarView.removeDecorators();
        calendarView.addDecorator(decorator);
        calendarView.invalidateDecorators();
        //Log.d("paintMiniCal", "painting end");
    }

    // [SMS + Call Log] 일주일 연락 비교
    public void aggregateWeekContact(List<Long> contactedDates_sms) {
        //weeklyFrequencies = new int[7];

        for (Long contactedDate : contactedDates_sms) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(contactedDate);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int index = dayOfWeek - 1;

            weeklyFrequencies[index]++;
        }
    }

    public void drawBarChart(BarChart barChart) {
        //Log.d("sehee update", "bar start");

        aggregateWeekContact(contactedDates_sms); //일주일 데이터 누적 리스트
        aggregateWeekContact(contactedDates_call);

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
        //Log.d("sehee update", "bar fin");
    }

}