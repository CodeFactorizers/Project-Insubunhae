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
import com.gyso.treeview.layout.CompactDownTreeLayoutManager;
import com.gyso.treeview.layout.CompactHorizonLeftAndRightLayoutManager;
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
import com.sgcd.insubunhae.databinding.FragmentHomeBinding;

import androidx.navigation.NavController;


import android.widget.Toast;

import com.gyso.treeview.TreeViewEditor;
import com.sgcd.insubunhae.db.Contact;
import com.sgcd.insubunhae.db.ContactsList;
import com.sgcd.insubunhae.db.Group;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;



public class HomeFragment extends Fragment {

    //public ContactsList contactsList;


    private FragmentHomeBinding binding;
    private NavController navController;
    public static final String TAG = HomeFragment.class.getSimpleName();
    private final Stack<NodeModel<Animal>> removeCache = new Stack<>();
    private NodeModel<Animal> targetNode;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private Handler handler = new Handler();
    private NodeModel<Animal> parentToRemoveChildren = null;

    public boolean na_flag = false;

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
        //return new CompactHorizonLeftAndRightLayoutManager(requireContext(),space_count,space_20dp,line);
        //return new CompactDownTreeLayoutManager(requireContext(),space_count,space_20dp,line);
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

    public void setData(AnimalTreeViewAdapter adapter) {
        ArrayList<Contact> contactsList = ((MainActivity) getActivity()).getContactsList().getContactsList();
        //contactsList = MainActivity.getContactsList();
        //Map<String, Group> groupMap = contactsList.getGroupMap();
        //group
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
        //GroupTrees.add(new TreeModel<>(root));

        //TreeModel<Animal> root = new TreeModel<>(GroupTmpNodes.get(0));//for test

        int c = 0;
//        int i = 0;
        while (contactsList.get(c++).getIsGrouped() != 0) {
            groupList.add(0, contactsList.get(c).getOnlyGroupName());
            break;
        }

        for (int i = 0; i < contactsList.size(); i++) {
            if (contactsList.get(i).getIsGrouped() == 0) {
                break;
            }
            //Log.d("groupsize", "size"+groupList.size()+", i:"+i);
            String s = contactsList.get(i).getOnlyGroupName();
            //Log.d("group tag", "group"+ i +","+s );
            //Log.d("contacts viewer", "groupId " + i);
            if (!groupList.contains(s)) {
                groupList.add(s);
            }
            //Log.d("contacts viewer", "groupList : " + groupList);

        }
        Animal[] GroupAnimalArray = new Animal[contactsList.size()];

        //set each GroupTrees[]'s group nodes into NodeModels and THEN TreeModels.
        for (int i=0; i< groupList.size(); i++) {
            String name = groupList.get(i);
            //Animal 생성(그룹용)
            GroupAnimalArray[i] = new Animal(name);
            //Log.d(".", "AnimalArray"+AnimalArray);
            Log.d(".", "GroupAnimalArray:" + GroupAnimalArray);

            //NodeModel<Animal> 추가
            GroupTmpNodes[i] = new NodeModel<Animal>(GroupAnimalArray[i]);
            //GroupTmpNodes[a].setName(name);

            //TreeModel<Animal> 추가
            GroupTrees.add(i, new TreeModel<Animal>(GroupTmpNodes[i]));
            GroupTrees.get(i).getRootNode().getValue().setName(name);

            Root.addNode(root, GroupTmpNodes[i]);
            //GroupTrees.get(i).addNode(GroupTmpNodes.get(i));
            //GroupTrees.set(i, GroupTrees.get(i).addNode(GroupTmpNodes.get(i)));
            //Log.d("mindmap", "groupNodes : " + GroupTmpNodes[i].getValue().getName() +"/GroupTrees"+ GroupTrees.get(i).get;
        }

        //이제 그룹말고 실제 연락처를 animal 및 node로 생성
        AnimalNodes.ensureCapacity(contactsList.size());

        int na_count= 0;
        for (int j = 0; j < contactsList.size(); j++) {
            Contact tmpContact = contactsList.get(j);
            Log.d("contact at 222 ","tmpContact: " + tmpContact.getName());

            AnimalArray[j] = new Animal(tmpContact.getName());
            //AnimalNodes.set(j, new NodeModel<Animal>(AnimalArray[j]));
            AnimalNodes.add(j, new NodeModel<>(AnimalArray[j]));
            Log.d("AnimalArray", "AnimalArray[j]:" +AnimalArray[j]);
            Log.d("AnimalNode", "AnimalNodes.get(j):" +AnimalNodes.get(j));

            if (tmpContact.getIsGrouped() != 0) {
                String s = tmpContact.getOnlyGroupName();
                int gI = groupList.indexOf(s);

                Log.d("animalnnodes", "s:" + s + "gI: " + gI + "groupList.get(gI): " + groupList.get(gI) + "j:" + j);
                GroupTrees.get(gI).addNode(GroupTmpNodes[gI], AnimalNodes.get(j));

            }
            else{
                if(!na_flag) {
                    na_flag = true;
                    Root.addNode(root,nANode);
                }
                naAnimalArray.add(na_count, new Animal(tmpContact.getName()));
                NotAssignedNodes.add(na_count,new NodeModel<>(naAnimalArray.get(na_count)));
                NotAssignedTree.addNode(nANode,NotAssignedNodes.get(na_count));
            }
        }

        //** sample nodes. going to set this into sample removing target node or something
        NodeModel<Animal> insu = new NodeModel<>(new Animal("인수"));
        NodeModel<Animal> insu_1 = new NodeModel<>(new Animal("인수1"));
        NodeModel<Animal> insu_2 = new NodeModel<>(new Animal("인수2"));
        NodeModel<Animal> insu_3 = new NodeModel<>(new Animal("인수3"));
        NodeModel<Animal> insu_4 = new NodeModel<>(new Animal("인수4"));
        TreeModel<Animal> Insu = new TreeModel<>(insu);
        Root.addNode(root, insu);
        Insu.addNode(insu, insu_1, insu_2, insu_3, insu_4);

        //mark
        parentToRemoveChildren = insu;
        targetNode = insu_1;
        //** sample nodes end
        //set data
        //adapter.setTreeModel(GroupTrees.get(0));
        adapter.setTreeModel(Root);


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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}




//        NodeModel<Animal> me = new NodeModel<>(new Animal(R.drawable.baseline_person_pin_circle_48,"나"));
//        TreeModel<Animal> Me = new TreeModel<>(me);
//        //TODO: 그룹명 get해서 TreeModel<Group> 하기, 모든 연락처에 대해 nodeModel하기, for(count){NodeModel<Contacts>, treemodel.addnode } 하기
//
//
//        //sehee's try
//        Log.d("0608", "home frag");
//        ArrayList<Contact> contactsList = ((MainActivity)getActivity()).getContactsList().getContactsList();
//
//        String[] contactNameArray = new String[contactsList.size()];
//        ArrayList<NodeModel<Animal>> nodeList = new ArrayList<>();
//
//
//        String[] contactIdArray = new String[contactsList.size()];
//        int[] contactIdIntArray = new int[contactIdArray.length];
//        for (int i = 0; i < contactsList.size(); i++) {
//            contactNameArray[i] = contactsList.get(i).getName();
//            contactIdArray[i] = contactsList.get(i).getId();
//            contactIdIntArray[i] = Integer.parseInt(contactIdArray[i]);
//
//            nodeList.add(new NodeModel<>(new Animal(contactNameArray[i])));
//            //treeModel.addNode(root, sub0, sub1, nodeList.get(i));
//            Me.addNode(me, nodeList.get(i));
//        }
//
//
//        //** sample nodes. going to set this into sample removing target node or something
//        NodeModel<Animal> insu = new NodeModel<>(new Animal("인수"));
//        NodeModel<Animal> insu_1 = new NodeModel<>(new Animal("인수1"));
//        NodeModel<Animal> insu_2 = new NodeModel<>(new Animal("인수2"));
//        NodeModel<Animal> insu_3 = new NodeModel<>(new Animal("인수3"));
//        NodeModel<Animal> insu_4 = new NodeModel<>(new Animal("인수4"));
//        TreeModel<Animal> Insu = new TreeModel<>(insu);
//        Me.addNode(me, insu);
//        Insu.addNode(insu, insu_1, insu_2, insu_3, insu_4);
//
//        //mark
//        parentToRemoveChildren = insu;
//        targetNode = insu_1;
//        //** sample nodes end


