package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Optional;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.NoticeListData;
import kr.jeet.edu.student.model.data.TestReserveData;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    public interface ItemClickListener{ public void onItemClick(NoticeListData item); }

    private Context mContext;
    private ArrayList<NoticeListData> mList;
    private ItemClickListener _listener;

    public NoticeListAdapter(Context mContext, ArrayList<NoticeListData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_notice_list_item, parent, false);
        return new NoticeListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoticeListData item = mList.get(position);

        holder.tvType.setText(TextUtils.isEmpty(item.noticeType) ? "" : item.noticeType);
        holder.tvAttState.setText(TextUtils.isEmpty(item.noticeAttendanceState) ? "" : item.noticeAttendanceState);
        holder.tvDate.setText(TextUtils.isEmpty(item.noticeDate) ? "" : item.noticeDate);
        holder.tvReceiver.setText(TextUtils.isEmpty(item.noticeReceiver) ? "" : item.noticeReceiver);

        Glide.with(mContext).load(R.drawable.img_dot_woman).into(holder.imgSenderAndReceiver);
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvType, tvAttState, tvDate, tvReceiver;
        private ImageView imgSenderAndReceiver;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tv_notice_type);
            tvAttState = itemView.findViewById(R.id.tv_notice_attendance_state);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvReceiver = itemView.findViewById(R.id.tv_notice_receiver);
            imgSenderAndReceiver = itemView.findViewById(R.id.img_notice_send_and_receiver);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                _listener.onItemClick(mList.get(position));
            });
        }
    }
}
