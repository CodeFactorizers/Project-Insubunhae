package com.sgcd.insubunhae.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.GysoTreeView;
import com.gyso.treeview.layout.BoxHorizonLeftAndRightLayoutManager;
import com.gyso.treeview.layout.BoxVerticalUpAndDownLayoutManager;
import com.gyso.treeview.layout.CompactDownTreeLayoutManager;
import com.gyso.treeview.layout.CompactHorizonLeftAndRightLayoutManager;
import com.gyso.treeview.layout.CompactRightTreeLayoutManager;
import com.gyso.treeview.layout.CompactRingTreeLayoutManager;
import com.gyso.treeview.layout.ForceDirectedTreeLayoutManager;
import com.gyso.treeview.layout.TableHorizonLeftAndRightLayoutManager;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.SmoothLine;
import com.gyso.treeview.listener.TreeViewControlListener;
import com.gyso.treeview.model.ITraversal;
import com.gyso.treeview.model.NodeModel;

import com.gyso.treeview.model.TreeModel;
import com.sgcd.insubunhae.MainActivity;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.base.Animal;
import com.sgcd.insubunhae.base.AnimalTreeViewAdapter;
import com.sgcd.insubunhae.base.ContactNode;
import com.sgcd.insubunhae.base.ContactTreeViewAdapter;
import com.sgcd.insubunhae.databinding.FragmentHomeBinding;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import android.widget.Toast;

import com.gyso.treeview.TreeViewEditor;
import com.sgcd.insubunhae.db.Contact;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;



public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private NavController navController;
    public static final String TAG = HomeFragment.class.getSimpleName();
    private final Stack<NodeModel<Animal>> removeCache = new Stack<>();
    private final Stack<NodeModel<ContactNode>> removeCacheC = new Stack<>();
    private NodeModel<Animal> targetNode;
    private NodeModel<ContactNode> targetNodeC;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private Handler handler = new Handler();

    private NodeModel<Animal> parentToRemoveChildren = null;
    private NodeModel<ContactNode> parentToRemoveChildrenC = null;
    private MainActivity activity;
    private final Object token = new Object();

    // Add a member variable to store the mind map view state
    private List<Node> nodeList = new ArrayList<>();

    public boolean na_flag = false;
    int space_count = 10;
    int space_20dp = 20;

    private static final int LEAF_MAX = 4;

    private TextView removeNodeButton;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        HomeViewModel homeViewModel =
//                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        na_flag = false;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);//확인필요

        // Other initialization or code for HomeFragment
        // Set click listeners for the buttons
        TextView viewCenterButton = view.findViewById(R.id.view_center_bt);
        TextView addNodesButton = view.findViewById(R.id.add_nodes_bt);
        removeNodeButton = view.findViewById(R.id.remove_node_bt);
        removeNodeButton.setText("선택 : 없음");

        binding.viewCenterBt.setOnClickListener(v -> navController.navigate(R.id.view_center_bt));
        binding.addNodesBt.setOnClickListener(v -> navController.navigate(R.id.add_nodes_bt));
        //binding.removeNodeBt.setOnClickListener(v -> navController.navigate(R.id.remove_node_bt));

        initWidgets();
    }


    private void initWidgets() {


        // 1 customs adapter
        //어댑터 교환
        //AnimalTreeViewAdapter adapter = new AnimalTreeViewAdapter();
        ContactTreeViewAdapter adapter = new ContactTreeViewAdapter();

        // 2 configure layout manager; unit dp
        TreeLayoutManager treeLayoutManager = getTreeLayoutManager();
        // 3 view setting
        binding.baseTreeView.setAdapter(adapter);
        binding.baseTreeView.setTreeLayoutManager(treeLayoutManager);
        // 4 nodes data setting

        setData(adapter);

        Log.d("002", "setData(adapter) finished.");
        // 5 get an editor. Note: an adapter must set before get an editor.
        final TreeViewEditor editor = binding.baseTreeView.getEditor();
        // 6 you own others jobs
        doYourOwnJobs(editor, adapter);
    }
    void doYourOwnJobs(TreeViewEditor editor, ContactTreeViewAdapter adapter) {
        // drag to move node
        binding.dragEditModeRd.setOnCheckedChangeListener((v, isChecked) -> {
            editor.requestMoveNodeByDragging(isChecked);
        });
        // focus, means that tree view fill center in your window viewport
        binding.viewCenterBt.setOnClickListener(v -> editor.focusMidLocation());

        // add some nodes
        binding.addNodesBt.setOnClickListener(v -> {
            if (targetNodeC == null) {
                Toast.makeText(requireContext(), /*"Ohs, your targetNodeC is null"*/"타겟 노드 없음", Toast.LENGTH_SHORT).show();
                return;
            }
            targetNodeC.getValue().setHeadId(R.drawable.baseline_groups_48);
            NodeModel<ContactNode> a = new NodeModel<>(new ContactNode("add-" + atomicInteger.getAndIncrement()));
//            NodeModel<ContactNode> b = new NodeModel<>(new ContactNode("add-" + atomicInteger.getAndIncrement()));
//            NodeModel<ContactNode> c = new NodeModel<>(new ContactNode("add-" + atomicInteger.getAndIncrement()));
            editor.addChildNodes(targetNodeC, a);

            // add to remove demo cache
            targetNodeC = a;
            removeCacheC.push(targetNodeC);
        });

        // remove node
//        binding.removeNodeBt.setOnClickListener(v -> {
//            if (removeCacheC.isEmpty()) {
//                Toast.makeText(requireContext(), /*"Ohs, demo removeCache is empty now!! Try to add some nodes firstly!!"*/"제거 캐시 없음", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            NodeModel<ContactNode> toRemoveNode = removeCacheC.pop();
//            targetNodeC = toRemoveNode.getParentNode();
//            editor.removeNode(toRemoveNode);
//        });

        adapter.setOnItemListener((item, node) -> {
            ContactNode contact = node.getValue();
            handler.removeCallbacksAndMessages(token);
            if(contact.getType() == ContactNode.CONTACT){
                Toast.makeText(requireContext(), "선택: " + contact.getId() + " "+ contact.getName(), Toast.LENGTH_SHORT).show();
                na_flag = false;
                activity.moveToViewer(contact.getId());
            }
            else{
                targetNodeC = node;
                node.getValue().setHeadId(R.drawable.baseline_groups_24);
                removeNodeButton.setText("선택 : " + contact.getName());
                Toast.makeText(requireContext(), "선택 그룹 : " + contact.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        // treeView control listener
        //final Object token = new Object();
        Runnable dismissRun = () -> {
            binding.scalePercent.setVisibility(View.VISIBLE);
        };


        binding.baseTreeView.setTreeViewControlListener(new TreeViewControlListener() {
            @Override
            public void onScaling(int state, int percent) {
                Log.e(TAG, "onScaling: " + state + "  " + percent);
                binding.scalePercent.setVisibility(View.VISIBLE);
                if (state == TreeViewControlListener.MAX_SCALE) {
                    binding.scalePercent.setText("MAX");
                } else if (state == TreeViewControlListener.MIN_SCALE) {
                    binding.scalePercent.setText("MIN");
                } else {
                    binding.scalePercent.setText(percent + "%");
                }
                handler.removeCallbacksAndMessages(token);
                handler.postAtTime(dismissRun, token, SystemClock.uptimeMillis() + 2000);
            }

            @Override
            public void onDragMoveNodesHit(NodeModel<?> draggingNode, NodeModel<?> hittingNode, View draggingView, View hittingView) {
                Log.e(TAG, "onDragMoveNodesHit: draging[" + draggingNode + "]hittingNode[" + hittingNode + "]");
            }
        });
    }


    /**
     * Box[XXX]TreeLayoutManagers are recommend for your project for they are running stably. Others treeLayoutManagers are developing.
     *
     * @return layout manager
     */
    private TreeLayoutManager getTreeLayoutManager() {

        BaseLine line = getLine();
        //return new BoxRightTreeLayoutManager(requireContext(),space_50dp,space_20dp,line);
        //return new BoxDownTreeLayoutManager(requireContext(),space_50dp,space_20dp,line);
        //return new BoxLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new BoxUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        return new BoxHorizonLeftAndRightLayoutManager(requireContext(), space_count, space_20dp, line);
        //return new BoxVerticalUpAndDownLayoutManager(requireContext(),30,space_20dp,line);

        //TODO !!!!! the layoutManagers below are just for test don't use in your projects. Just for test now
        //return new TableRightTreeLayoutManager(requireContext(), space_30dp,space_20dp,line);
        //return new TableLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableDownTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new TableHorizonLeftAndRightLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new TableVerticalUpAndDownLayoutManager(requireContext(),space_count,space_20dp,line);

        //return new CompactRightTreeLayoutManager(requireContext(),50,space_20dp,line);
        //return new CompactLeftTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactHorizonLeftAndRightLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new CompactDownTreeLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new CompactUpTreeLayoutManager(this,space_50dp,space_20dp,line);
        //return new CompactVerticalUpAndDownLayoutManager(requireContext(),space_count,space_20dp,line);

        //return new CompactRingTreeLayoutManager(requireContext(), 200,60,line);
        //return new ForceDirectedTreeLayoutManager(requireContext(),line);
    }


    private BaseLine getLine() {
        return new SmoothLine();
        //return new StraightLine(Color.parseColor("#055287"),2);
        //return new DashLine(Color.parseColor("#F1286C"),3);
        //return new AngledLine();
    }

    public void setData(ContactTreeViewAdapter adapter) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
        ArrayList<Contact> contactsList = activity.getContactsList().getContactsList();
        ArrayList<String> groupList = new ArrayList<>();
        ArrayList<NodeModel<ContactNode>> contactNodeList = new ArrayList<>();

        //Contacts 그대로 쓰니까 contactsList로 대체 가능
        ContactNode[] contactArray = new ContactNode[contactsList.size()];

        //node
        //nodeList.ensureCapacity(2000);
        NodeModel<ContactNode>[] groupTmpNodes = new NodeModel[contactsList.size()];
        ArrayList<TreeModel<ContactNode>> groupTreeList = new ArrayList<>();
        groupTreeList.ensureCapacity(100);

        //root 노드
        ContactNode rootContact = new ContactNode();
        rootContact.setName("나");
        rootContact.setId("0");
        rootContact.setHeadId(R.drawable.baseline_person_pin_circle_48);
        rootContact.setType(ContactNode.GROUP);
        NodeModel<ContactNode> rootNodeModel = new NodeModel<>(rootContact);
        TreeModel<ContactNode> rootTree = new TreeModel<>(rootNodeModel);

        targetNodeC = rootNodeModel;
        // 미분류 그룹
        ContactNode notAssigned = new ContactNode("미분류");
        notAssigned.setHeadId(R.drawable.baseline_group_off_48);
        NodeModel<ContactNode> notAssignedNode = new NodeModel<>(notAssigned);
        TreeModel<ContactNode> notAssignedTree = new TreeModel(notAssignedNode);
        ArrayList<ContactNode> notAssignedContactArray = new ArrayList<>();
        notAssignedContactArray.ensureCapacity(1000);
        ArrayList<NodeModel<ContactNode>> notAssignedNodes = new ArrayList<>();

        //연락처 속한 첫번째 그룹 이름만 뽑음. groupList
        for(int i = 0; i < contactsList.size(); i++){
            if(contactsList.get(i).getIsGrouped() == 0){
                continue;
            }
            String s = contactsList.get(i).getGroupName().get(0);
            if(!groupList.contains(s)){
                groupList.add(s);
            }
        }

        //group 노드 추가
        ContactNode[] groupContactArray = new ContactNode[contactsList.size()];
        for(int i = 0; i < groupList.size(); i++){
            String name = groupList.get(i);
            groupContactArray[i] = new ContactNode(name);
            groupContactArray[i].setHeadId(R.drawable.baseline_groups_48);
            groupTmpNodes[i] = new NodeModel<ContactNode>(groupContactArray[i]);
            //각 그룹의 하위 노드 추가를 위한 트리모델
            groupTreeList.add(new TreeModel<ContactNode>(groupTmpNodes[i]));
            groupTreeList.get(i).getRootNode().getValue().setName(name);

            rootTree.addNode(rootNodeModel, groupTmpNodes[i]);
        }


        int notAssignedCount = 0;
        for(int i = 0; i < contactsList.size(); i++){
            Contact tmpContact = contactsList.get(i);
            contactArray[i] = new ContactNode(tmpContact.getId(), tmpContact.getName(), tmpContact.getIsGrouped());
            contactNodeList.add(new NodeModel<>(contactArray[i]));
            if(tmpContact.getIsGrouped() == 1){
                //속한 그룹 있으면 해당 그룹의 NodeModelArray에 추가
                String groupName = tmpContact.getGroupName().get(0);
                int groupIndex = groupList.indexOf(groupName);
                if(groupTmpNodes[groupIndex].leafCount>=LEAF_MAX) continue;
                groupTreeList.get(groupIndex).addNode(groupTmpNodes[groupIndex], contactNodeList.get(i));
            }
            else{
                //없으면 notAssigned NodeModelArray에 추가
                if(!na_flag){
                    //notAssigned 그룹노드 없으면 추가
                    na_flag = true;
                    rootTree.addNode(rootNodeModel, notAssignedNode);
                }
                if(notAssignedNode.leafCount >= LEAF_MAX){
                    //notAssigend에 연락처 많으면 로그만 출력
//                    Log.d("nANode", "leafCount: "+ notAssignedNode.leafCount+" leavesList"+ notAssignedNode.leavesList+"child"+notAssignedNode.childNodes);
//                    Log.d("nANode", "node : " + tmpContact.getName());
                }
                else {
                    //notAssigned에 추가
                    notAssignedContactArray.add(notAssignedCount, contactArray[i]);
                    notAssignedNodes.add(notAssignedCount, new NodeModel<>(notAssignedContactArray.get(notAssignedCount)));
                    notAssignedTree.addNode(notAssignedNode, notAssignedNodes.get(notAssignedCount));
                }
            }
        }
            getActivity().runOnUiThread(() -> {
                adapter.setTreeModel(rootTree);

                // Update the adapter and notify the change\
            });
        });

        adapter.notifyDataSetChange();
    }

    // capitalize first character of the string
    public String capitalizeString(String variableName) {
        if (variableName == null || variableName.isEmpty()) {
            return variableName; // Return the original name if it's null or empty
        }
        char firstChar = Character.toUpperCase(variableName.charAt(0));
        String capitalized = firstChar + variableName.substring(1);

        return capitalized;
    }

//    private void updateMindMapView(GysoTreeView treeView, List<Node> nodeList) {
//        // Clear the existing nodes from the TreeView
//        TreeModel<Animal> tmpRoot = new TreeModel<>(new NodeModel<Animal>(new Animal("나")));
//        treeView.getAdapter().setTreeModel(tmpRoot);
//
//        // Add the new nodes from the nodeList to the TreeView
//        for (Node node : nodeList) {
//            treeView.getAdapter().getTreeModel().addNode(node);
//        }
//
//        // Notify the TreeView adapter that the data set has changed
//        treeView.getAdapter().notifyDataSetChange();
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
