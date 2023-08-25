package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.ScheduleData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

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

            holder.tvDate.setText(item.stDate);
            holder.tvClass.setText(item.stClass);
            holder.tvContent.setText(item.stContent);

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

        private TextView tvDate, tvClass, tvContent;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_schedule_date);
            tvClass = itemView.findViewById(R.id.tv_schedule_class);
            tvContent = itemView.findViewById(R.id.tv_schedule_content);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                _listener.onItemClick(mList.get(position));
            });
        }
    }
}
