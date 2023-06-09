//
//package com.sgcd.insubunhae.ui.home;
//
//import android.util.Log;
//
//import androidx.fragment.app.Fragment;
//
//import com.gyso.treeview.model.NodeModel;
//import com.gyso.treeview.model.TreeModel;
//import com.sgcd.insubunhae.MainActivity;
//import com.sgcd.insubunhae.R;
//import com.sgcd.insubunhae.base.Animal;
//import com.sgcd.insubunhae.base.AnimalTreeViewAdapter;
//import com.sgcd.insubunhae.db.Contact;
//import com.sgcd.insubunhae.db.ContactsList;
//import com.sgcd.insubunhae.db.Group;
//
//import java.util.ArrayList;
//import java.util.Map;
//
//public class MindmapFragment extends Fragment {
//
//    int icon = R.drawable.btn_radio_off_mtrl;
//    private ContactsList contactsList;
//    private ArrayList contactsArray;
//    private ArrayList<Contact> contactsArrayList;
//
//    NodeModel[] AnimalNodes;
//    NodeModel[] GroupTmpNodes;
//    static TreeModel[] GroupTrees;
//
//    protected void onCreate() {
//        CreateNodeModelArray();
//    }
//
//
//    public NodeModel<Animal>[] CreateNodeModelArray() {
//        contactsArray = ((MainActivity) getActivity()).getContactsList().getContactsList();
//        //contactsList = MainActivity.getContactsList();
//        Map<String, Group> groupMap = contactsList.getGroupMap();
//        //group
//        ArrayList<String> groupList = new ArrayList<>();
//
//        contactsArrayList = this.getArguments().getParcelableArrayList("contactsListToViewer");
//        Contact tmp = contactsArrayList.get(0);
//        for (String i : tmp.getGroupId()) {
//            Log.d("contacts viewer", "groupId " + i);
//            groupList.add(groupMap.get(i).getGroupName());
//        }
//        Log.d("contacts viewer", "groupList : " + groupList);
//
//
//        int listSize = contactsArrayList.size();
//        int groupCount = groupList.size();
//
//        //Animal[] AnimalArray = new Animal[listSize];
//        //ArrayList<NodeModel<Animal>> AnimalNodesList = new ArrayList<NodeModel<Animal>>;
//        AnimalNodes = new NodeModel[listSize];
//        GroupTmpNodes = new NodeModel[groupCount];
//        GroupTrees = new TreeModel[groupCount];
//
//
//        //set each GroupTrees[]'s group nodes into NodeModels and THEN TreeModels.
//        for (int i = 0; i < groupCount; i++) {
//            GroupTmpNodes[i].groupName = groupList.get(i);
//            GroupTrees[i] = new TreeModel<>(GroupTmpNodes[i]);
//
//            Log.d("mindmap", "groupNodes : " + i + "groupName" + GroupTrees[i].name);
//        }
//
//        // Create the root node
//        AnimalNodes[0].name = "나";
//        //AnimalArray[0].setIcon(R.drawable.baseline_person_outline_48);
//
//        for (int i = 1; i <= listSize; i++) {
//            //AnimalArray[i].setName(contactsArrayList.get(i).getName());
//            Contact c = contactsArrayList.get(i);
//            AnimalNodes[i].name = c.getName();
//
//            if (c.getIsGrouped() != 0) {
//                String gN = c.getOnlyGroupName();
//
//                /*
//                int indexOf(Object o)
//                It is used to return the index in this list of the first occurrence of the specified
//                 element, or -1 if the List does not contain this element.
//                */
//                if (gN != null) {
//                    AnimalNodes[i].groupName = gN;
//                    int gI = groupList.indexOf(gN);
//                    GroupTrees[gI].addNode(AnimalNodes[i]);
//                }
//            }
//        }
////
////        // Create other nodes based on the contacts list
////        for (int i=0; i<groupCount; i++) {
////        }
//        return AnimalNodes;
//    }
//
//
//    public static void setData(AnimalTreeViewAdapter adapter) {
//
//
////        NodeModel<Animal> me = new NodeModel<>(new Animal(R.drawable.baseline_person_pin_circle_48,"나"));
////        TreeModel<Animal> Me = new TreeModel<>(me);
////        //TODO: 그룹명 get해서 TreeModel<Group> 하기, 모든 연락처에 대해 nodeModel하기, for(count){NodeModel<Contacts>, treemodel.addnode } 하기
////
////
////        //sehee's try
////        Log.d("0608", "home frag");
////        ArrayList<Contact> contactsList = ((MainActivity)getActivity()).getContactsList().getContactsList();
////
////        String[] contactNameArray = new String[contactsList.size()];
////        ArrayList<NodeModel<Animal>> nodeList = new ArrayList<>();
////
////
////        String[] contactIdArray = new String[contactsList.size()];
////        int[] contactIdIntArray = new int[contactIdArray.length];
////        for (int i = 0; i < contactsList.size(); i++) {
////            contactNameArray[i] = contactsList.get(i).getName();
////            contactIdArray[i] = contactsList.get(i).getId();
////            contactIdIntArray[i] = Integer.parseInt(contactIdArray[i]);
////
////            nodeList.add(new NodeModel<>(new Animal(contactNameArray[i])));
////            //treeModel.addNode(root, sub0, sub1, nodeList.get(i));
////            Me.addNode(me, nodeList.get(i));
////        }
////
////
////        //** sample nodes. going to set this into sample removing target node or something
////        NodeModel<Animal> insu = new NodeModel<>(new Animal("인수"));
////        NodeModel<Animal> insu_1 = new NodeModel<>(new Animal("인수1"));
////        NodeModel<Animal> insu_2 = new NodeModel<>(new Animal("인수2"));
////        NodeModel<Animal> insu_3 = new NodeModel<>(new Animal("인수3"));
////        NodeModel<Animal> insu_4 = new NodeModel<>(new Animal("인수4"));
////        TreeModel<Animal> Insu = new TreeModel<>(insu);
////        Me.addNode(me, insu);
////        Insu.addNode(insu, insu_1, insu_2, insu_3, insu_4);
////
////        //mark
////        parentToRemoveChildren = insu;
////        targetNode = insu_1;
////        //** sample nodes end
//
//        //set data
//        adapter.setTreeModel(GroupTrees[0]);
//    }
//
//}