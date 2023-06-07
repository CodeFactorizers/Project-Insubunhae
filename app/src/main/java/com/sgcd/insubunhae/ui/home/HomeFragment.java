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
import com.gyso.treeview.GysoTreeView;
import com.gyso.treeview.layout.BoxRightTreeLayoutManager;
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
                Toast.makeText(requireContext(), "Ohs, your targetNode is null", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Ohs, demo removeCache is empty now!! Try to add some nodes firstly!!", Toast.LENGTH_SHORT).show();
                return;
            }
            NodeModel<Animal> toRemoveNode = removeCache.pop();
            targetNode = toRemoveNode.getParentNode();
            editor.removeNode(toRemoveNode);
        });

        adapter.setOnItemListener((item, node) -> {
            Animal animal = node.getValue();
            Toast.makeText(requireContext(), "you click the head of " + animal, Toast.LENGTH_SHORT).show();
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
        int space_50dp = 30;
        int space_20dp = 20;
        BaseLine line = getLine();
        return new BoxRightTreeLayoutManager(requireContext(),space_50dp,space_20dp,line);
        //return new BoxDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);


        //TODO !!!!! the layoutManagers below are just for test don't use in your projects. Just for test now
        //return new TableRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);

        //return new CompactRightTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactHorizonLeftAndRightLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactVerticalUpAndDownLayoutManager(this,space_50dp,space_20dp,line);

        //return new CompactRingTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new ForceDirectedTreeLayoutManager(this,line);
    }

    private BaseLine getLine() {
        return new SmoothLine();
        //return new StraightLine(Color.parseColor("#055287"),2);
        //return new DashLine(Color.parseColor("#F1286C"),3);
        //return new AngledLine();
    }

    private void setData(AnimalTreeViewAdapter adapter){
        //root
        NodeModel<Animal> root = new NodeModel<>(new Animal(R.drawable.btn_radio_off_mtrl,"root"));
        TreeModel<Animal> treeModel = new TreeModel<>(root);

        //child nodes
        NodeModel<Animal> capdi1_insubunhae = new NodeModel<>(new Animal(R.drawable.btn_radio_on_to_off_mtrl_animation,"캡디1\n인수분해 팀"));
        NodeModel<Animal> honghyoen_17 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"17유홍현"));
        NodeModel<Animal> sehee_17 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"17조세희"));
        NodeModel<Animal> junsu_18 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"18김준수"));
        NodeModel<Animal> sumin_19 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"19임수민"));


        NodeModel<Animal> sub0 = new NodeModel<>(new Animal(R.drawable.btn_radio_on_to_off_mtrl_animation,"소공"));
        NodeModel<Animal> sub1 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub01"));
        NodeModel<Animal> sub2 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"안녕"));
        NodeModel<Animal> sub4 = new NodeModel<>(new Animal(R.drawable.btn_radio_on_to_off_mtrl_animation,"동아리"));
        NodeModel<Animal> sub5 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub05000"));
        NodeModel<Animal> sub6 = new NodeModel<>(new Animal(R.drawable.btn_radio_on_to_off_mtrl_animation,"17학번"));
        NodeModel<Animal> sub7 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub07000"));
        NodeModel<Animal> sub8 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub08000"));
        NodeModel<Animal> sub9 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"팀원1"));
        NodeModel<Animal> sub10 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"팀원2"));
        NodeModel<Animal> sub11 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub11000"));
        NodeModel<Animal> sub14 = new NodeModel<>(new Animal(R.drawable.icon_user_5,".000000"));
        NodeModel<Animal> sub15 = new NodeModel<>(new Animal(R.drawable.icon_user_5,".000000"));
        NodeModel<Animal> sub16 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub16000"));
        NodeModel<Animal> sub34 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"1(멘토)"));
        NodeModel<Animal> sub38 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"2(멘토)"));
        NodeModel<Animal> sub39 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"조장"));
        NodeModel<Animal> sub40 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"친구2"));
        NodeModel<Animal> sub47 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"친구1"));
        NodeModel<Animal> sub48 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub48000"));
        NodeModel<Animal> sub49 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"sub49000"));
        NodeModel<Animal> sub52 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"팀장1"));
        NodeModel<Animal> sub53 = new NodeModel<>(new Animal(R.drawable.icon_user_5,"팀장2"));

        //build relationship
        treeModel.addNode(root,sub0,sub1,capdi1_insubunhae,sub4);
        treeModel.addNode(capdi1_insubunhae,honghyoen_17,sehee_17);
        treeModel.addNode(capdi1_insubunhae,junsu_18);
        treeModel.addNode(capdi1_insubunhae,sumin_19);
        treeModel.addNode(sub1,sub2);
        treeModel.addNode(sub0,sub34,sub5,sub38,sub39);
        treeModel.addNode(sub4,sub6);
        treeModel.addNode(sub5,sub7,sub8);
        treeModel.addNode(sub6,sub9,sub10,sub11);
        treeModel.addNode(sub11,sub14,sub15);
        treeModel.addNode(sub10,sub40);
        treeModel.addNode(sub40,sub16);
        treeModel.addNode(sub9,sub47,sub48);
        treeModel.addNode(sub47,sub49);
        treeModel.addNode(sub39,sub52,sub53);

        //mark
        parentToRemoveChildren = sub0;
        targetNode = sub1;

        //set data
        adapter.setTreeModel(treeModel);
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}