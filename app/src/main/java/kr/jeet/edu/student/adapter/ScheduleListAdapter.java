package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.utils.LogMgr;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder> {

    private final static String TAG = "ScheduleListAdapter";

    public interface ItemClickListener{ public void onItemClick(ScheduleData item); }

    private Context mContext;
    private List<ScheduleData> mList;
    private ItemClickListener _listener;

    public ScheduleListAdapter(Context mContext, List<ScheduleData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_schedule_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleData item = mList.get(position);
        try{

            String date = item.month+"."+item.day;

            holder.tvDate.setText(date);
            holder.tvTarget.setText(TextUtils.isEmpty(item.target) ? "" : item.target);
            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);

        }catch (Exception e){
            LogMgr.e("ListAdapter Exception : " + e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvDate, tvTarget, tvTitle;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_schedule_date);
            tvTarget = itemView.findViewById(R.id.tv_schedule_target);
            tvTitle = itemView.findViewById(R.id.tv_schedule_title);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
