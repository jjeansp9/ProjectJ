package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import kr.jeet.edu.student.model.data.PrefAreaData;
import kr.jeet.edu.student.utils.LogMgr;

public class PrefCheckListAdapter extends RecyclerView.Adapter<PrefCheckListAdapter.ViewHolder> {

    public interface ItemClickListener{
        public void onItemClick(String item, int position);
    }

    private Context mContext;
    private List<String> mList = new ArrayList<String>();
    private List<String> checkList = new ArrayList<String>();
    private ItemClickListener _listener;

    public PrefCheckListAdapter(Context mContext, List<String> mList, List<String> checkList, ItemClickListener listener){
        this.mContext = mContext;
        this.mList = mList;
        this.checkList = checkList;
        this._listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_pref_sel_list_item, parent, false);
        return new ViewHolder(view);
    }
    int count = 0;
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (position >= 0) {
                String item = mList.get(position);

                holder.cbPref.setText(item);

                LogMgr.i("checkTest", checkList.toString() + "," + checkList.size());

                for (int i = 0; i < checkList.size(); i++) {
                    if (checkList.get(i).equals(mList.get(position))) {
                        LogMgr.i("CHECKEVENT", i + "," + count);
                        LogMgr.i("CHECKEVENT", checkList.get(i) + "," + mList.get(position));
                        holder.cbPref.setChecked(true);
                        break;
                    }
                }

                holder.cbPref.setOnClickListener(v -> {
                    if (holder.cbPref.isChecked()) {
                        count++;
                        _listener.onItemClick(item, position);
                    } else {
                        count--;
                        _listener.onItemClick("", position);
                    }
                });
            }
        } catch (Exception e) {
            LogMgr.e("Pref Check Adapter Exception : " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private CheckBox cbPref;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            cbPref = itemView.findViewById(R.id.cb_pref);

//            itemView.setOnClickListener(v -> {
//                int position = getAbsoluteAdapterPosition();
//                if (cbPref.isChecked()){
//                    _listener.onItemClick(mList.get(position).preference, position);
//                }else{
//                    _listener.onItemClick("", position);
//                }
//            });
        }
    }
}
