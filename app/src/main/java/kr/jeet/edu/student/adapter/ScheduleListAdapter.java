package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
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

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("M.d ", Locale.KOREA);
        Date date = null;

        String resultDate = "";

        try {

            String getDate = String.format(Locale.KOREA, "%d%d%d", item.year,item.month, item.day);
            date = inputDateFormat.parse(getDate);

            String formattedDate = "";

            int dayOfWeek = 0;
            if (date != null) {
                formattedDate = outputFormat.format(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            }

            String dayOfWeekStr = Utils.formatDayOfWeek(dayOfWeek).replace("요일", "");

            resultDate = formattedDate+"("+dayOfWeekStr+")";

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvDate.setText(resultDate);
        holder.tvTarget.setText(TextUtils.isEmpty(item.target) ? "" : item.target);
        holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);

//        try{
//
//            String date = item.month+"."+item.day;
//
//            holder.tvDate.setText(date);
//            holder.tvTarget.setText(TextUtils.isEmpty(item.target) ? "" : item.target);
//            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
//
//        }catch (Exception e){
//            LogMgr.e("ListAdapter Exception : " + e.getMessage());
//        }
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
