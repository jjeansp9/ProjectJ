package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.NoticeListData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.utils.Converters;
import kr.jeet.edu.student.utils.Utils;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    public interface ItemClickListener{ public void onItemClick(PushMessage item); }

    private Context mContext;
    private ArrayList<PushMessage> mList;
    private ItemClickListener _listener;

    public NoticeListAdapter(Context mContext, ArrayList<PushMessage> mList, ItemClickListener _listener) {
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
        PushMessage item = mList.get(position);

        String noticeType = TextUtils.isEmpty(item.pushType) ? "" : item.pushType;
        if (noticeType.equals(FCMManager.MSG_TYPE_SYSTEM)) {
            holder.tvType.setText("시스템알림");
            holder.btnNext.setVisibility(View.VISIBLE);
            //holder.tvAttState.setVisibility(View.GONE);
        }
        else if (noticeType.equals(FCMManager.MSG_TYPE_ATTEND)) {
            holder.tvType.setText("출결현황");
            holder.btnNext.setVisibility(View.GONE);
            //holder.tvAttState.setVisibility(View.VISIBLE);
        }
        else {
            holder.tvType.setText(TextUtils.isEmpty(item.pushType) ? "정보없음" : item.pushType);
            holder.tvAttState.setVisibility(View.GONE);
            holder.btnNext.setVisibility(View.GONE);
        }

        holder.tvDate.setText(TextUtils.isEmpty(item.date.toString()) ? "" : item.date.toString().replace("T", " "));
        holder.tvTitle.setText(TextUtils.isEmpty(item.body) ? "" : item.body);

        Glide.with(mContext).load(R.drawable.img_receive).into(holder.imgSenderAndReceiver);
        holder.imgSenderAndReceiver.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvType, tvAttState, tvDate, tvReceiver, tvTitle;
        private ImageView imgSenderAndReceiver;
        private ImageButton btnNext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            btnNext = itemView.findViewById(R.id.btn_notice_next);
            tvType = itemView.findViewById(R.id.tv_notice_type);
            //tvAttState = itemView.findViewById(R.id.tv_notice_attendance_state);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvReceiver = itemView.findViewById(R.id.tv_notice_receiver);
            tvTitle = itemView.findViewById(R.id.tv_system_notice_title);
            imgSenderAndReceiver = itemView.findViewById(R.id.img_notice_send_and_receiver);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
