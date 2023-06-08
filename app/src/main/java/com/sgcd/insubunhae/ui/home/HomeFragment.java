package com.sgcd.insubunhae.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gyso.treeview.layout.BoxHorizonLeftAndRightLayoutManager;
import com.gyso.treeview.layout.CompactRingTreeLayoutManager;
import com.gyso.treeview.layout.ForceDirectedTreeLayoutManager;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.listener.TreeViewControlListener;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.base.Animal;
import com.sgcd.insubunhae.base.AnimalTreeViewAdapter;
import com.sgcd.insubunhae.databinding.FragmentHomeBinding;

import androidx.navigation.NavController;


import android.widget.Toast;

import com.gyso.treeview.TreeViewEditor;

import com.sgcd.insubunhae.db.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NavController navController;
    public static final String TAG = HomeFragment.class.getSimpleName();
    private final Stack<NodeModel<Animal>> removeCache = new Stack<>();
    private NodeModel<Animal> targetNode;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private Handler handler = new Handler();
    private NodeModel<Animal> parentToRemoveChildren = null;

    int space_count = 10;
    int space_20dp = 20;
    int space_30dp = 30;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Other initialization or code for HomeFragment
        // Set click listeners for the buttons
        TextView viewCenterButton = view.findViewById(R.id.view_center_bt);
        TextView addNodesButton = view.findViewById(R.id.add_nodes_bt);
        TextView removeNodeButton = view.findViewById(R.id.remove_node_bt);

        viewCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to a destination when the button is clicked
                navController.navigate(R.id.view_center_bt);
            }
        });

        addNodesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to a destination when the button is clicked
                navController.navigate(R.id.add_nodes_bt);
            }
        });

        removeNodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to a destination when the button is clicked
                navController.navigate(R.id.remove_node_bt);
            }
        });

        initWidgets();
    }

    private void initWidgets() {
        // 1 customs adapter
        AnimalTreeViewAdapter adapter = new AnimalTreeViewAdapter();

        // 2 configure layout manager; unit dp
        TreeLayoutManager treeLayoutManager = getTreeLayoutManager();

        // 3 view setting
        binding.baseTreeView.setAdapter(adapter);
        binding.baseTreeView.setTreeLayoutManager(treeLayoutManager);

        // 4 nodes data setting
        setData(adapter);

        // 5 get an editor. Note: an adapter must set before get an editor.
        final com.gyso.treeview.TreeViewEditor editor = binding.baseTreeView.getEditor();

        // 6 you own others jobs
        doYourOwnJobs(editor, adapter);
    }

    void doYourOwnJobs(TreeViewEditor editor, AnimalTreeViewAdapter adapter) {
        // drag to move node
        binding.dragEditModeRd.setOnCheckedChangeListener((v, isChecked) -> {
            editor.requestMoveNodeByDragging(isChecked);
        });

        // focus, means that tree view fill center in your window viewport
        binding.viewCenterBt.setOnClickListener(v -> editor.focusMidLocation());

        // add some nodes
        binding.addNodesBt.setOnClickListener(v -> {
            if (targetNode == null) {
                Toast.makeText(requireContext(), /*"Ohs, your targetNode is null"*/"타겟 노드 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> a = new NodeModel<>(new Animal(R.drawable.ic_10, "add-" + atomicInteger.getAndIncrement()));
            NodeModel<Animal> b = new NodeModel<>(new Animal(R.drawable.ic_11, "add-" + atomicInteger.getAndIncrement()));
            NodeModel<Animal> c = new NodeModel<>(new Animal(R.drawable.ic_14, "add-" + atomicInteger.getAndIncrement()));
            editor.addChildNodes(targetNode, a, b, c);

            // add to remove demo cache
            removeCache.push(targetNode);
            targetNode = b;
        });

        // remove node
        binding.removeNodeBt.setOnClickListener(v -> {
            if (removeCache.isEmpty()) {
                Toast.makeText(requireContext(), /*"Ohs, demo removeCache is empty now!! Try to add some nodes firstly!!"*/"제거 캐시 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> toRemoveNode = removeCache.pop();
            targetNode = toRemoveNode.getParentNode();
            editor.removeNode(toRemoveNode);
        });

        adapter.setOnItemListener((item, node) -> {
            Animal animal = node.getValue();
            Toast.makeText(requireContext(), "선택: " + animal, Toast.LENGTH_SHORT).show();
        });

        // treeView control listener
        final Object token = new Object();
        Runnable dismissRun = () -> {
            binding.scalePercent.setVisibility(View.GONE);
        };


        binding.baseTreeView.setTreeViewControlListener(new TreeViewControlListener() {
            @Override
            public void onScaling(int state, int percent) {
                Log.e(TAG, "onScaling: "+state+"  "+percent);
                binding.scalePercent.setVisibility(View.VISIBLE);
                if(state == TreeViewControlListener.MAX_SCALE){
                    binding.scalePercent.setText("MAX");
                }else if(state == TreeViewControlListener.MIN_SCALE){
                    binding.scalePercent.setText("MIN");
                }else{
                    binding.scalePercent.setText(percent+"%");
                }
                handler.removeCallbacksAndMessages(token);
                handler.postAtTime(dismissRun,token, SystemClock.uptimeMillis()+2000);
            }

            @Override
            public void onDragMoveNodesHit(NodeModel<?> draggingNode, NodeModel<?> hittingNode, View draggingView, View hittingView) {
                Log.e(TAG, "onDragMoveNodesHit: draging["+draggingNode+"]hittingNode["+hittingNode+"]");
            }
        });
    }

    /**
     * Box[XXX]TreeLayoutManagers are recommend for your project for they are running stably. Others treeLayoutManagers are developing.
     * @return layout manager
     */
    private TreeLayoutManager getTreeLayoutManager() {

        BaseLine line = getLine();
        //return new BoxRightTreeLayoutManager(requireContext(),space_50dp,space_20dp,line);
        //return new BoxDownTreeLayoutManager(requireContext(),space_50dp,space_20dp,line);
        //return new BoxLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        return new BoxHorizonLeftAndRightLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new BoxVerticalUpAndDownLayoutManager(requireContext(),space_30dp,space_20dp,line);


        //TODO !!!!! the layoutManagers below are just for test don't use in your projects. Just for test now
        //return new TableRightTreeLayoutManager(requireContext(), space_30dp,space_20dp,line);
        //return new TableLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableHorizonLeftAndRightLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new TableVerticalUpAndDownLayoutManager(requireContext(),space_count,space_20dp,line);

        //return new CompactRightTreeLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new CompactLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactHorizonLeftAndRightLayoutManager(requireContext(),space_50dp,space_20dp,line);
        //return new CompactDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactVerticalUpAndDownLayoutManager(requireContext(),space_count,space_20dp,line);

        //return new CompactRingTreeLayoutManager(requireContext(), space_30dp,space_20dp,line);
        //return new ForceDirectedTreeLayoutManager(requireContext(),line);
    }

    private BaseLine getLine() {
        return new SmoothLine();
        //return new StraightLine(Color.parseColor("#055287"),2);
        //return new DashLine(Color.parseColor("#F1286C"),3);
        //return new AngledLine();
    }

    private void setData(AnimalTreeViewAdapter adapter){
        NodeModel<Animal> me = new NodeModel<>(new Animal(R.drawable.baseline_person_pin_circle_48,"나"));
        NodeModel<Animal> family = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"가족"));

        NodeModel<Animal> sogang_univ =new NodeModel<>(new Animal(R.drawable.baseline_school_48,"서강대"));
        NodeModel<Animal> insu = new NodeModel<>(new Animal(R.drawable.baseline_install_mobile_48,"캡디1 인수분해 팀"));
        NodeModel<Animal> dongari = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"동아리"));
        NodeModel<Animal> colleague = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"동기"));

        NodeModel<Animal> school_reunion = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"동창"));
        NodeModel<Animal> high_school = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"고등학교"));
        NodeModel<Animal> middle_school = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"중학교"));

        NodeModel<Animal> gatherings = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"모임"));
        NodeModel<Animal> study_group = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"캡디1 스터디"));
        NodeModel<Animal> photo_club = new NodeModel<>(new Animal(R.drawable.baseline_photo_camera_48,"사진 동호회"));

        NodeModel<Animal> parttime_job = new NodeModel<>(new Animal(R.drawable.baseline_groups_48,"알바"));

        NodeModel<Animal> not_assigned = new NodeModel<>(new Animal(R.drawable.baseline_group_off_48,"그룹 미분류"));

        TreeModel<Animal> Me = new TreeModel<>(me);

        TreeModel<Animal> Family = new TreeModel<>(family);

        TreeModel<Animal> SogangUniv = new TreeModel<>(sogang_univ);
        TreeModel<Animal> Insu = new TreeModel<>(insu);
        //TreeModel<Animal> Sogong = new TreeModel<>(sogong);
        TreeModel<Animal> Dongari = new TreeModel<>(dongari);
        TreeModel<Animal> Colleague = new TreeModel<>(colleague);

        TreeModel<Animal> SchoolReunion= new TreeModel<>(school_reunion);
        TreeModel<Animal> MiddleSchool= new TreeModel<>(high_school);
        TreeModel<Animal> HighSchool= new TreeModel<>(middle_school);

        TreeModel<Animal> Gatherings = new TreeModel<>(gatherings);
        TreeModel<Animal> StudyGroup = new TreeModel<>(study_group);
        TreeModel<Animal> PhotoClub = new TreeModel<>(photo_club);
        TreeModel<Animal> ParttimeJob = new TreeModel<>(parttime_job);
        TreeModel<Animal> NotAssigned= new TreeModel<>(not_assigned);


        //TODO: 그룹명 get해서 TreeModel<Group> 하기, 모든 연락처에 대해 nodeModel하기, for(count){NodeModel<Contacts>, treemodel.addnode } 하기

        //child nodes
        NodeModel<Animal> family_1 = new NodeModel<>(new Animal("동생"));
        NodeModel<Animal> family_2 = new NodeModel<>(new Animal("엄마"));
        NodeModel<Animal> family_3 = new NodeModel<>(new Animal("아빠"));
        NodeModel<Animal> insu_1 = new NodeModel<>(new Animal("17유홍현"));
        NodeModel<Animal> insu_2 = new NodeModel<>(new Animal("17조세희"));
        NodeModel<Animal> insu_3 = new NodeModel<>(new Animal("18김준수"));
        NodeModel<Animal> insu_4 = new NodeModel<>(new Animal("19임수민"));
        NodeModel<Animal> sogong_1 = new NodeModel<>(new Animal("소공팀원1"));
        NodeModel<Animal> sogong_2 = new NodeModel<>(new Animal("소공팀원2"));
        NodeModel<Animal> sogong_3 = new NodeModel<>(new Animal("소공팀원3"));
        NodeModel<Animal> sogong_4 = new NodeModel<>(new Animal("소공팀원4"));
        NodeModel<Animal> sogong_5 = new NodeModel<>(new Animal("소공팀원5"));
        NodeModel<Animal> sogong_6 = new NodeModel<>(new Animal("소공팀원6"));
        NodeModel<Animal> sogong_7 = new NodeModel<>(new Animal("소공팀원7"));
        NodeModel<Animal> sogong_8 = new NodeModel<>(new Animal("소공팀원8"));
        NodeModel<Animal> sogong_9 = new NodeModel<>(new Animal("소공팀원9"));
        NodeModel<Animal> sogong_10 = new NodeModel<>(new Animal("소공팀원10"));
        NodeModel<Animal> dong_1 = new NodeModel<>(new Animal("동아리원1"));
        NodeModel<Animal> dong_2 = new NodeModel<>(new Animal("동아리원2"));
        NodeModel<Animal> dong_3 = new NodeModel<>(new Animal("동아리원3"));
        NodeModel<Animal> dong_4 = new NodeModel<>(new Animal("동아리원4"));
        NodeModel<Animal> colleague_1 = new NodeModel<>(new Animal("동기1"));
        NodeModel<Animal> colleague_2 = new NodeModel<>(new Animal("동기2"));
        NodeModel<Animal> colleague_3 = new NodeModel<>(new Animal("동기3"));
        NodeModel<Animal> colleague_4 = new NodeModel<>(new Animal("동기4"));
        NodeModel<Animal> colleague_5 = new NodeModel<>(new Animal("동기5"));


        NodeModel<Animal> h_school_1 = new NodeModel<>(new Animal("고등동창1"));
        NodeModel<Animal> h_school_2 = new NodeModel<>(new Animal("고등동창2"));
        NodeModel<Animal> h_school_3 = new NodeModel<>(new Animal("고등동창3"));
        NodeModel<Animal> m_school_1 = new NodeModel<>(new Animal("중등동창1"));
        NodeModel<Animal> m_school_2 = new NodeModel<>(new Animal("중등동창2"));
        NodeModel<Animal> m_school_3 = new NodeModel<>(new Animal("중등동창3"));
        NodeModel<Animal> study_group_1 = new NodeModel<>(new Animal("스터디원1"));
        NodeModel<Animal> study_group_2 = new NodeModel<>(new Animal("스터디원2"));
        NodeModel<Animal> study_group_3 = new NodeModel<>(new Animal("스터디원3"));
        NodeModel<Animal> photo_club_1 = new NodeModel<>(new Animal("동호회 회원1"));
        NodeModel<Animal> photo_club_2 = new NodeModel<>(new Animal("동호회 회원2"));
        NodeModel<Animal> photo_club_3 = new NodeModel<>(new Animal("동호회 회원3"));
        NodeModel<Animal> parttime_job_1 = new NodeModel<>(new Animal("알바 동료1"));
        NodeModel<Animal> parttime_job_2 = new NodeModel<>(new Animal("알바 동료2"));
        NodeModel<Animal> parttime_job_3 = new NodeModel<>(new Animal("알바 동료3"));
        NodeModel<Animal> not_assigned_1 = new NodeModel<>(new Animal("기타1"));
        NodeModel<Animal> not_assigned_2 = new NodeModel<>(new Animal("기타2"));
        NodeModel<Animal> not_assigned_3 = new NodeModel<>(new Animal("기타3"));

        //build relationship
        Me.addNode(me,family, sogang_univ, school_reunion, gatherings, parttime_job, not_assigned);

        Family.addNode(family, family_1,family_2, family_3);

        SogangUniv.addNode(sogang_univ, insu, dongari, colleague);
        Insu.addNode(insu, insu_1, insu_2, insu_3, insu_4);
        Dongari.addNode(dongari,dong_1,dong_2,dong_3);
        Colleague.addNode(colleague, colleague_1, colleague_2, colleague_3, colleague_4);

        SchoolReunion.addNode(school_reunion, high_school, middle_school);
        HighSchool.addNode(high_school, h_school_1, h_school_2, h_school_3);
        MiddleSchool.addNode(middle_school, m_school_1, m_school_2);

        Gatherings.addNode(gatherings, study_group, photo_club);
        StudyGroup.addNode(study_group, study_group_1, study_group_2, study_group_3);

        PhotoClub.addNode(photo_club, photo_club_1,photo_club_2,photo_club_3);

        ParttimeJob.addNode(parttime_job, parttime_job_1,parttime_job_2);
        NotAssigned.addNode(not_assigned, not_assigned_1,not_assigned_2,not_assigned_3);

        //mark
        parentToRemoveChildren = parttime_job;
        targetNode = parttime_job_3;


        //set data
        adapter.setTreeModel(Me);








//        //root
//
//        NodeModel<Animal> root = new NodeModel<>(new Animal(R.drawable.ic_01,"내가루트여야지"));
//        TreeModel<Animal> treeModel = new TreeModel<>(root);
//
//
//
//        //TODO: 그룹명 get해서 TreeModel<Group> 하기, 모든 연락처에 대해 nodeModel하기, for(count){NodeModel<Contacts>, treemodel.addnode } 하기
//        NodeModel<Animal> Insu = new NodeModel<>(new Animal(R.drawable.ic_05,"캡디1\n인수분해 팀"));
//        NodeModel<Animal> Sogong_10_people = new NodeModel<>(new Animal(R.drawable.ic_02,"소공"));
//        NodeModel<Animal> Dongari = new NodeModel<>(new Animal(R.drawable.ic_06,"동아리"));
//
//
//        //child nodes
//
//        NodeModel<Animal> insu_1 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"17유홍현"));
//        NodeModel<Animal> insu_2 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"17조세희"));
//        NodeModel<Animal> insu_3 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"18김준수"));
//        NodeModel<Animal> insu_4 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"19임수민"));
//
//
//        NodeModel<Animal> sogong_1 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원1"));
//        NodeModel<Animal> sogong_2 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원2"));
//        NodeModel<Animal> sogong_3 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원3"));
//        NodeModel<Animal> sogong_4 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원4"));
//        NodeModel<Animal> sogong_5 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원5"));
//        NodeModel<Animal> sogong_6 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원6"));
//        NodeModel<Animal> sogong_7 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원7"));
//        NodeModel<Animal> sogong_8 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원8"));
//        NodeModel<Animal> sogong_9 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원9"));
//        NodeModel<Animal> sogong_10 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"소공팀원10"));
//
//
//        NodeModel<Animal> dong_1 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원1"));
//        NodeModel<Animal> dong_2 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원2"));
//        NodeModel<Animal> dong_3 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원3"));
//        NodeModel<Animal> dong_4 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원4"));
//        NodeModel<Animal> dong_5 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원5"));
//        NodeModel<Animal> dong_6 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원6"));
//        NodeModel<Animal> dong_7 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원7"));
//        NodeModel<Animal> dong_8 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원8"));
//        NodeModel<Animal> dong_9 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원9"));
//        NodeModel<Animal> dong_10 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원10"));
//        NodeModel<Animal> dong_11 = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"동아리원11"));
//
//        //build relationship
//        treeModel.addNode(root,Insu,Sogong_10_people,Dongari);
//        treeModel.addNode(Insu,insu_1,insu_2,insu_3,insu_4);
//        //treeModel.addNode(Insu,insu_3);
//        //treeModel.addNode(Insu,insu_4);
//        treeModel.addNode(Dongari,dong_1,dong_2,dong_3,dong_4,dong_5,dong_6,dong_7,dong_8,dong_9,dong_10,dong_11);
//        treeModel.addNode(Sogong_10_people,sogong_1,sogong_2,sogong_3,sogong_4, sogong_5, sogong_6, sogong_7, sogong_8, sogong_9, sogong_10);
//
//
//        //sehee's try
//        Log.d("0608", "home frag");
//        ArrayList<Contact> contactsList = ((MainActivity)getActivity()).getContactsList().getContactsList();
//
//        String[] contactNameArray = new String[contactsList.size()];
//        ArrayList<NodeModel<Animal>> nodeList = new ArrayList<>();
//        String[] contactIdArray = new String[contactsList.size()];
//        int[] contactIdIntArray = new int[contactIdArray.length];
//        for (int i = 0; i < contactsList.size(); i++) {
//            contactNameArray[i] = contactsList.get(i).getName();
//            contactIdArray[i] = contactsList.get(i).getId();
//            contactIdIntArray[i] = Integer.parseInt(contactIdArray[i]);
//
//            nodeList.add(new NodeModel<>(new Animal(R.drawable.icon_user_5, contactNameArray[i])));
//            //treeModel.addNode(root, sub0, sub1, nodeList.get(i));
//        }
//
//
//        //mark
//        parentToRemoveChildren = Sogong_10_people;
//        targetNode = sogong_1;
//
//
//        //set data
//        adapter.setTreeModel(treeModel);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}