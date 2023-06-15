package com.sgcd.insubunhae.ui.notifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.databinding.FragmentNotificationsBinding;
import com.sgcd.insubunhae.db.Familiarity;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private ArrayList<Familiarity> famList;
    private MainActivity mainActivity;
    private ArrayList<ArrayList<Familiarity>> allQueue;
    private int MAX_COL = 3;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        famList = mainActivity.getFamList();
        Collections.sort(famList,Collections.reverseOrder());
        for(Familiarity tmp : famList){
            Log.d("noti", "Familiarity : " + tmp.toString());
        }
        allQueue = new ArrayList<>();
        ArrayList<Familiarity> queue1 = new ArrayList<>();
        for(int i = 0; i < MAX_COL; i++){
            queue1.add(famList.subList(0,3).get(i));
        }
        Collections.sort(queue1);
        ArrayList<Familiarity> queue2 = new ArrayList<>();
        for(int i = 0; i < MAX_COL; i++){
            queue1.add(famList.subList(3,6).get(i));
        }
        Collections.sort(queue2);
        ArrayList<Familiarity> queue3 = new ArrayList<>();
        for(int i = 0; i < MAX_COL; i++){
            queue1.add(famList.subList(6,9).get(i));
        }
        Collections.sort(queue3);
        allQueue.add(queue1);
        allQueue.add(queue2);
        allQueue.add(queue3);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textNotifications;
        //notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        ArrayList<Familiarity> tmpQueue = allQueue.get(0);
        TextView tmpTxtView;
        tmpTxtView = root.findViewById(R.id.queue1_1);
        tmpTxtView.setText(allQueue.get(0).get(0).getName());
        tmpTxtView = root.findViewById(R.id.queue1_2);
        tmpTxtView.setText(allQueue.get(0).get(1).getName());
        tmpTxtView = root.findViewById(R.id.queue1_3);
        tmpTxtView.setText(allQueue.get(0).get(2).getName());

        root.findViewById(R.id.queue1_2);

        root.findViewById(R.id.queue1_3);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}