package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.SystemNoticeListData;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    public interface ItemClickListener{ public void onItemClick(SystemNoticeListData item); }

    private Context mContext;
    private ArrayList<SystemNoticeListData> mList;
    private ItemClickListener _listener;

    public NoticeListAdapter(Context mContext, ArrayList<SystemNoticeListData> mList, ItemClickListener _listener) {
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
        if (position != NO_POSITION){

            SystemNoticeListData item = mList.get(position);

            String noticeType = TextUtils.isEmpty(item.searchType) ? "" : item.searchType;
            String strType = "";

            if (noticeType.equals(FCMManager.MSG_TYPE_SYSTEM)) {
                strType = "시스템알림";
                setClickEnabled(strType, holder, position);
            }
            else if (noticeType.equals(FCMManager.MSG_TYPE_ATTEND)) {
                strType = "출결현황";
                setClickDisabled(strType, holder);
            }
            else if (noticeType.equals(FCMManager.MSG_TYPE_REPORT)) {
                strType = "성적표";
                setClickEnabled(strType, holder, position);
            }
            else if (noticeType.equals(FCMManager.MSG_TYPE_TUITION)) {
                strType = "미납";
                setClickDisabled(strType, holder);
            }
            else {
                holder.tvType.setText(TextUtils.isEmpty(item.searchType) ? "정보없음" : item.searchType);
                holder.btnNext.setVisibility(View.GONE);
            }

            holder.tvDate.setText(TextUtils.isEmpty(item.insertDate.toString()) ? "" : item.insertDate.toString().replace("T", " "));
            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);

            Glide.with(mContext).load(R.drawable.img_receive).into(holder.imgSenderAndReceiver);
            holder.imgSenderAndReceiver.setVisibility(View.GONE);
        }
    }

    private void setClickDisabled(String str, ViewHolder holder) {
        holder.tvType.setText(str);
        holder.btnNext.setVisibility(View.GONE);
        holder.root.setOnClickListener(null);
        holder.root.setBackgroundResource(R.color.transparent);
    }

    private void setClickEnabled(String str, ViewHolder holder, int position) {
        holder.tvType.setText(str);
        holder.btnNext.setVisibility(View.VISIBLE);
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        holder.root.setBackgroundResource(typedValue.resourceId);

        holder.root.setOnClickListener(v -> {if (mList.size() > 0) _listener.onItemClick(mList.get(position));});
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout root;
        private TextView tvType, tvDate, tvReceiver, tvTitle;
        private ImageView imgSenderAndReceiver;
        private ImageButton btnNext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.notice_root);
            btnNext = itemView.findViewById(R.id.btn_notice_next);
            tvType = itemView.findViewById(R.id.tv_notice_type);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvReceiver = itemView.findViewById(R.id.tv_notice_receiver);
            tvTitle = itemView.findViewById(R.id.tv_system_notice_title);
            imgSenderAndReceiver = itemView.findViewById(R.id.img_notice_send_and_receiver);
        }
    }
}
