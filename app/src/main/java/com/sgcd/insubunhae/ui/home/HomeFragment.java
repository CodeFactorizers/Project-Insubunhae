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
    private NodeModel<Animal> targetNode;
    private NodeModel<Contact> targetNodeC;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private Handler handler = new Handler();

    private NodeModel<Animal> parentToRemoveChildren = null;
    private MainActivity activity;
    private final Object token = new Object();

    // Add a member variable to store the mind map view state
    private List<Node> nodeList = new ArrayList<>();

    public boolean na_flag = false;
    int space_count = 10;
    int space_20dp = 20;

    private static final int LEAF_MAX = 4;



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
        TextView removeNodeButton = view.findViewById(R.id.remove_node_bt);

        binding.viewCenterBt.setOnClickListener(v -> navController.navigate(R.id.view_center_bt));
        binding.addNodesBt.setOnClickListener(v -> navController.navigate(R.id.add_nodes_bt));
        binding.removeNodeBt.setOnClickListener(v -> navController.navigate(R.id.remove_node_bt));

        initWidgets();
    }


    private void initWidgets() {


        // 1 customs adapter
//        AnimalTreeViewAdapter adapter = new AnimalTreeViewAdapter();
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


    void doYourOwnJobs(TreeViewEditor editor, @NonNull AnimalTreeViewAdapter adapter) {

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
        //final Object token = new Object();
        Runnable dismissRun = () -> {
            binding.scalePercent.setVisibility(View.GONE);
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
            NodeModel<Contact> a = new NodeModel<>(new Contact("add-" + atomicInteger.getAndIncrement()));
            NodeModel<Contact> b = new NodeModel<>(new Contact("add-" + atomicInteger.getAndIncrement()));
            NodeModel<Contact> c = new NodeModel<>(new Contact("add-" + atomicInteger.getAndIncrement()));
            editor.addChildNodes(targetNodeC, a, b, c);

            // add to remove demo cache
            removeCache.push(targetNode);
            targetNodeC = b;
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
            ContactNode contact = node.getValue();
            handler.removeCallbacksAndMessages(token);
            if(contact.getType() == ContactNode.CONTACT){
                Toast.makeText(requireContext(), "선택: " + contact.getId() + " "+ contact.getName(), Toast.LENGTH_SHORT).show();
                na_flag = false;
                activity.moveToViewer(contact.getId());
            }
            else{
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

    public void setData(AnimalTreeViewAdapter adapter) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Perform heavy operations here

            Log.d("setData() 1", "getContactsList starts");
            ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();
            Log.d("setData() 2", "getContactsList finished");
            ArrayList<String> groupList = new ArrayList<>();

            int contactSize = contactsList.size();
            Animal[] AnimalArray = new Animal[contactSize];
            for (int i = 0; i < AnimalArray.length; i++) {
                AnimalArray[i] = new Animal(contactsList.get(i).getName());
            }



            ArrayList<NodeModel<Animal>> AnimalNodes = new ArrayList<>(contactSize);
            AnimalNodes.ensureCapacity(2000);
            NodeModel<Animal>[] GroupTmpNodes = new NodeModel[contactSize];
            //GroupTmpNodes.ensureCapacity(2000);
            ArrayList<TreeModel<Animal>> GroupTrees = new ArrayList<>();
            GroupTrees.ensureCapacity(100);

            //미분류
            Animal notAssigned = new Animal("미분류");
            NodeModel<Animal> nANode = new NodeModel<Animal>(notAssigned);
            TreeModel<Animal> NotAssignedTree = new TreeModel(nANode);

            ArrayList<Animal> naAnimalArray = new ArrayList<Animal>();
            naAnimalArray.ensureCapacity(1000);
            ArrayList<NodeModel<Animal>> NotAssignedNodes = new ArrayList<>();
            NotAssignedNodes.ensureCapacity(1000);
            //아래부분에서 Root.addNode(...);하자 (미분류 그룹은 가장 마지막에 추가해두자..)


            // Create the root node
            Animal rootAnimal = new Animal(R.drawable.baseline_person_outline_48, "나");
            NodeModel<Animal> root = new NodeModel<>(rootAnimal);
            TreeModel<Animal> Root = new TreeModel<>(root);


            for (int i = 0; i < contactsList.size(); i++) {
                if (contactsList.get(i).getIsGrouped() == 0) {
                    continue;
                }
                String s = getOnlyGroupName(contactsList.get(i).getGroupName());

                if (!groupList.contains(s)) {
                    groupList.add(s);
                }
            }
            Log.d("setData() 3", "grouplist set, line315");

            Animal[] GroupAnimalArray = new Animal[contactsList.size()];


            //set each GroupTrees[]'s group nodes into NodeModels and THEN TreeModels.
            for (int i=0; i< groupList.size(); i++) {
                String name = groupList.get(i);
                //Animal 생성(그룹용)
                GroupAnimalArray[i] = new Animal(name);
                //Log.d(".", "GroupAnimalArray:" + GroupAnimalArray);

                //NodeModel<Animal> 추가
                GroupTmpNodes[i] = new NodeModel<Animal>(GroupAnimalArray[i]);
                //GroupTmpNodes[a].setName(name);

                //TreeModel<Animal> 추가
                GroupTrees.add(i, new TreeModel<Animal>(GroupTmpNodes[i]));
                GroupTrees.get(i).getRootNode().getValue().setName(name);

                Root.addNode(root, GroupTmpNodes[i]);
            }
            //Log.d("setData() 4", "groupnodes and trees set");

            //이제 그룹말고 실제 연락처를 animal 및 node로 생성
            AnimalNodes.ensureCapacity(contactsList.size());

            int na_count= 0;

            for (int j = 0; j < contactsList.size(); j++) {
                Contact tmpContact = contactsList.get(j);
                //Log.d("contact at 222 ","tmpContact: " + tmpContact.getName());

                AnimalArray[j] = new Animal(tmpContact.getName());
                AnimalNodes.add(new NodeModel<>(AnimalArray[j]));
                //Log.d("AnimalNode", "AnimalNodes.get(j):" +AnimalNodes.get(j));
                //Log.d("leafcount and j", "leafCount: "+GroupTmpNodes[0].leafCount+"/ j: "+j);

                if (tmpContact.getIsGrouped() != 0) {
                    String s = getOnlyGroupName(tmpContact.getGroupName());
                    int gI = groupList.indexOf(s);
                    if(gI<0) continue;
                    //Log.d("1111", "j:"+j+", name: "+tmpContact.getName()+ "GroupName: " + tmpContact.getGroupName());
                    if(j >= 300){
                        //Log.d("1112", "j = 490");
                    }
                    //Log.d("leafcount and j", "leafCount: "+GroupTmpNodes[gI].leafCount+"/ j: "+j);

                    if(GroupTmpNodes[gI].leafCount>=4) continue;
                    GroupTrees.get(gI).addNode(GroupTmpNodes[gI], AnimalNodes.get(j));
                }
                else{
                    if(!na_flag) {
                        na_flag = true;
                        Root.addNode(root,nANode);
                    }
                    if(nANode.leafCount>=3){//3개까지만 나오도록
                        //Log.d("nANode", "leafCount: "+ nANode.leafCount+"leavesList"+ nANode.leavesList+"child"+nANode.childNodes);
                        continue;
                    }
                    naAnimalArray.add(na_count, new Animal(tmpContact.getName()));
                    NotAssignedNodes.add(na_count,new NodeModel<>(naAnimalArray.get(na_count)));
                    NotAssignedTree.addNode(nANode,NotAssignedNodes.get(na_count));
                }
                Log.d("groupList", "groupList:"+groupList);
            }

            //** sample nodes. going to set this into sample removing target node or something
//        NodeModel<Animal> insu = new NodeModel<>(new Animal("인수"));
//        NodeModel<Animal> insu_1 = new NodeModel<>(new Animal("인수1"));
//        NodeModel<Animal> insu_2 = new NodeModel<>(new Animal("인수2"));
//        TreeModel<Animal> Insu = new TreeModel<>(insu);
//        Root.addNode(root, insu);
//        Insu.addNode(insu, insu_1, insu_2);
//
//        //mark
//        parentToRemoveChildren = insu;
//        targetNode = insu_1;
            //** sample nodes end
            getActivity().runOnUiThread(() -> {
            adapter.setTreeModel(Root);

                // Update the adapter and notify the change

            });
        });
        adapter.notifyDataSetChange();
    }

    public void setData(ContactTreeViewAdapter adapter) {
        ArrayList<Contact> contactsList = activity.getContactsList().getContactsList();
        ArrayList<String> groupList = new ArrayList<>();
        ArrayList<NodeModel<ContactNode>> contactNodeList = new ArrayList<>();

        //Contacts 그대로 쓰니까 contactsList로 대체 가능
        ContactNode[] contactArray = new ContactNode[contactsList.size()];
        //밑에서 하는데 여기서도 함
//        for(int i = 0; i < contactsList.size(); i++){
//            contactArray[i] = new Contact(contactsList.get(i).getName());
//        }

        //node
        //nodeList.ensureCapacity(2000);
        NodeModel<ContactNode>[] groupTmpNodes = new NodeModel[contactsList.size()];
        ArrayList<TreeModel<ContactNode>> groupTreeList = new ArrayList<>();
        groupTreeList.ensureCapacity(100);

        //root 노드
        ContactNode rootContact = new ContactNode();
        rootContact.setName("나");
        rootContact.setId("0");
        NodeModel<ContactNode> rootNodeModel = new NodeModel<>(rootContact);
        TreeModel<ContactNode> rootTree = new TreeModel<>(rootNodeModel);

        // 미분류 그룹
        ContactNode notAssigned = new ContactNode("미분류");
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
                if(groupTmpNodes[i].leafCount>=LEAF_MAX) continue;
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
                    Log.d("nANode", "node : " + notAssignedNode.getValue().getName());
                }
                else {
                    //notAssigned에 추가
                    notAssignedContactArray.add(notAssignedCount, contactArray[i]);
                    notAssignedNodes.add(notAssignedCount, new NodeModel<>(notAssignedContactArray.get(notAssignedCount)));
                    notAssignedTree.addNode(notAssignedNode, notAssignedNodes.get(notAssignedCount));
                }
            }
        }

//        Contact parentContact = new Contact();
//        parentContact.setName("parent");
//        parentContact.setId("par");
//        NodeModel<Contact> parent = new NodeModel<>(parentContact);
//        for(int i = 0; i < contactsList.size(); i++){
//            Contact tmp = new Contact();
//            tmp.setId(contactsList.get(i).getId());
//            tmp.setName(contactsList.get(i).getName());
//            nodeList.add(new NodeModel<>(tmp));
//
//            treeModel.addNode(root, nodeList.get(i));
//
//        }
        adapter.setTreeModel(rootTree);
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


    public String getOnlyGroupName(ArrayList<String> groups){
        return groups.get(0);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}