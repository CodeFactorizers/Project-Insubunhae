package com.sgcd.insubunhae.base;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.DashLine;
import com.gyso.treeview.model.NodeModel;
import com.sgcd.insubunhae.R;
import com.sgcd.insubunhae.databinding.NodeBaseLayoutBinding;
import com.sgcd.insubunhae.db.Contact;

public class ContactTreeViewAdapter extends TreeViewAdapter<ContactNode> {

    private DashLine dashLine =  new DashLine(Color.parseColor("#F06292"),6);
    private ContactTreeViewAdapter.OnItemClickListener listener;

    public void setOnItemListener(ContactTreeViewAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public TreeViewHolder<ContactNode> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<ContactNode> node) {
        NodeBaseLayoutBinding nodeBinding = NodeBaseLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()),viewGroup,false);
        return new TreeViewHolder<>(nodeBinding.getRoot(),node);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder<ContactNode> holder) {
        //todo get view and node from holder, and then show by you
        View itemView = holder.getView();
        NodeModel<ContactNode> node = holder.getNode();
        TextView nameView = itemView.findViewById(R.id.name);
        ImageView headView = itemView.findViewById(R.id.portrait);
        final ContactNode contact = node.value;
        nameView.setText(contact.getName());
        headView.setImageResource(contact.headId);
        headView.setOnClickListener(v -> {
            if(listener!=null){
                listener.onItemClick(v,node);
            }
        });
    }

    @Override
    public BaseLine onDrawLine(DrawInfo drawInfo) {
        // TODO If you return an BaseLine, line will be draw by the return one instead of TreeViewLayoutManager's
//        TreeViewHolder<?> toHolder = drawInfo.getToHolder();
//        NodeModel<?> node = toHolder.getNode();
//        Object value = node.getValue();
//        if(value instanceof Animal){
//            Animal animal = (Animal) value;
//            if("sub4".compareToIgnoreCase(animal.name)<=0){
//                return dashLine;
//            }
//        }
        return null;
    }

    public interface OnItemClickListener{
        void onItemClick(View item, NodeModel<ContactNode> node);
    }
}
