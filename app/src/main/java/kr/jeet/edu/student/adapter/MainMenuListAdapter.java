package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.MainMenuItemData;
import kr.jeet.edu.student.utils.LogMgr;

public class MainMenuListAdapter extends RecyclerView.Adapter<MainMenuListAdapter.ViewHolder> {

    public interface ItemClickListener{
        public void onItemClick(MainMenuItemData item);
    }

    private Context mContext;
    private List<MainMenuItemData> mList = new ArrayList<MainMenuItemData>();
    private ItemClickListener _listener;

    public MainMenuListAdapter(Context mContext, List<MainMenuItemData> mList, ItemClickListener listener){
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            if(position >= 0) {
                MainMenuItemData item = mList.get(position);

                Glide.with(mContext).load(item.getImgRes()).into(holder.imgMenu);
                holder.tvMenu.setText(item.getTitleRes());
            }
        }catch (Exception e){
            LogMgr.e("Main Menu Adapter Exception : " + e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout rootLayout;
        private ImageView imgMenu;
        private TextView tvMenu;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root);
            imgMenu = itemView.findViewById(R.id.img_menu);
            tvMenu = itemView.findViewById(R.id.tv_menu);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                _listener.onItemClick(mList.get(position));
            });
        }
    }
}
