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
        NodeModel<Animal> root = new NodeModel<>(new Animal(R.drawable.ic_01,"root"));
        TreeModel<Animal> treeModel = new TreeModel<>(root);

        //child nodes
        NodeModel<Animal> sub0 = new NodeModel<>(new Animal(R.drawable.ic_02,"소공"));
        NodeModel<Animal> sub1 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub01"));
        NodeModel<Animal> sub2 = new NodeModel<>(new Animal(R.drawable.ic_04,"."));
        NodeModel<Animal> sub3 = new NodeModel<>(new Animal(R.drawable.ic_05,"캡디1\n인수분해 팀"));
        NodeModel<Animal> sub4 = new NodeModel<>(new Animal(R.drawable.ic_06,"동아리"));
        NodeModel<Animal> sub5 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub05000"));
        NodeModel<Animal> sub6 = new NodeModel<>(new Animal(R.drawable.ic_08,"17학번"));
        NodeModel<Animal> sub7 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub07000"));
        NodeModel<Animal> sub8 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub08000"));
        NodeModel<Animal> sub9 = new NodeModel<>(new Animal(R.drawable.ic_11,"ㄱㄴㄷ"));
        NodeModel<Animal> sub10 = new NodeModel<>(new Animal(R.drawable.ic_12,"ㄱㄴㄷ"));
        NodeModel<Animal> sub11 = new NodeModel<>(new Animal(R.drawable.ic_13,"sub11000"));
        NodeModel<Animal> sub12 = new NodeModel<>(new Animal(R.drawable.ic_14,"17유홍현"));
        NodeModel<Animal> sub13 = new NodeModel<>(new Animal(R.drawable.ic_15,"조교"));
        NodeModel<Animal> sub14 = new NodeModel<>(new Animal(R.drawable.ic_13,".000000"));
        NodeModel<Animal> sub15 = new NodeModel<>(new Animal(R.drawable.ic_14,".000000"));
        NodeModel<Animal> sub16 = new NodeModel<>(new Animal(R.drawable.ic_15,"sub16000"));
        NodeModel<Animal> sub17 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub17000"));
        NodeModel<Animal> sub18 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub18000"));
        NodeModel<Animal> sub19 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub19000"));
        NodeModel<Animal> sub20 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub20000"));
        NodeModel<Animal> sub21 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub21000"));
        NodeModel<Animal> sub22 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub22000"));
        NodeModel<Animal> sub23 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub23000"));
        NodeModel<Animal> sub24 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub24000"));
        NodeModel<Animal> sub25 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub25000"));
        NodeModel<Animal> sub26 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub26000"));
        NodeModel<Animal> sub27 = new NodeModel<>(new Animal(R.drawable.ic_09,"sub27000"));
        NodeModel<Animal> sub28 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub28000"));
        NodeModel<Animal> sub29 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub29000"));
        NodeModel<Animal> sub30 = new NodeModel<>(new Animal(R.drawable.ic_02,"sub30000"));
        NodeModel<Animal> sub31 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub31000"));
        NodeModel<Animal> sub32 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub32000"));
        NodeModel<Animal> sub33 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub33000"));
        NodeModel<Animal> sub34 = new NodeModel<>(new Animal(R.drawable.ic_06,"1(멘토)"));
        NodeModel<Animal> sub35 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub35000"));
        NodeModel<Animal> sub36 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub36"));
        NodeModel<Animal> sub37 = new NodeModel<>(new Animal(R.drawable.ic_09,"18김준수"));
        NodeModel<Animal> sub38 = new NodeModel<>(new Animal(R.drawable.ic_10,"2(멘토)"));
        NodeModel<Animal> sub39 = new NodeModel<>(new Animal(R.drawable.ic_11,"조장"));
        NodeModel<Animal> sub40 = new NodeModel<>(new Animal(R.drawable.ic_02,"아무개"));
        NodeModel<Animal> sub41 = new NodeModel<>(new Animal(R.drawable.ic_03,"sub41000"));
        NodeModel<Animal> sub42 = new NodeModel<>(new Animal(R.drawable.ic_04,"sub42000"));
        NodeModel<Animal> sub43 = new NodeModel<>(new Animal(R.drawable.ic_05,"sub43000"));
        NodeModel<Animal> sub44 = new NodeModel<>(new Animal(R.drawable.ic_06,"sub44000"));
        NodeModel<Animal> sub45 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub45000"));
        NodeModel<Animal> sub46 = new NodeModel<>(new Animal(R.drawable.ic_08,"sub46000"));
        NodeModel<Animal> sub47 = new NodeModel<>(new Animal(R.drawable.ic_09,"나"));
        NodeModel<Animal> sub48 = new NodeModel<>(new Animal(R.drawable.ic_10,"sub48000"));
        NodeModel<Animal> sub49 = new NodeModel<>(new Animal(R.drawable.ic_11,"sub49000"));
        NodeModel<Animal> sub50 = new NodeModel<>(new Animal(R.drawable.ic_05,"김철수(조교)"));
        NodeModel<Animal> sub51 = new NodeModel<>(new Animal(R.drawable.ic_07,"sub51000"));
        NodeModel<Animal> sub52 = new NodeModel<>(new Animal(R.drawable.ic_07,"팀장1"));
        NodeModel<Animal> sub53 = new NodeModel<>(new Animal(R.drawable.ic_07,"팀장2"));

        //build relationship
        treeModel.addNode(root,sub0,sub1,sub3,sub4);
        treeModel.addNode(sub3,sub12,sub13);
        treeModel.addNode(sub1,sub2);
        treeModel.addNode(sub0,sub34,sub5,sub38,sub39);
        treeModel.addNode(sub4,sub6);
        treeModel.addNode(sub5,sub7,sub8);
        treeModel.addNode(sub6,sub9,sub10,sub11);
        treeModel.addNode(sub11,sub14,sub15);
        treeModel.addNode(sub10,sub40);
        treeModel.addNode(sub40,sub16);
        //treeModel.addNode(sub8,sub17,sub18,sub19,sub20,sub21,sub22,sub23,sub41,sub42,sub43,sub44);
        treeModel.addNode(sub9,sub47,sub48);
        //treeModel.addNode(sub16,sub24,sub25,sub26,sub27,sub28,sub29,sub30,sub46,sub45);
        treeModel.addNode(sub47,sub49);
        treeModel.addNode(sub12,sub37);
        treeModel.addNode(sub0,sub36);
        treeModel.addNode(sub39,sub52,sub53);

        //treeModel.addNode(sub15,sub31,sub32,sub33,sub34,sub35,sub36,sub37);
        //treeModel.addNode(sub2,sub40,sub41,sub42,sub43,sub44,sub45,sub46);
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