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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.data.ReportCardSummaryData;
import kr.jeet.edu.student.model.data.SystemNoticeListData;
import kr.jeet.edu.student.utils.Utils;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    public interface ItemClickListener{
        public void onItemClick(SystemNoticeListData item);
    }

    private Context mContext;
    private ArrayList<SystemNoticeListData> mList;
    //private ArrayList<ReportCardSummaryData> mReportList;
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

            if (mList.size() > 0) {
                SystemNoticeListData item = mList.get(position);

                String noticeType = TextUtils.isEmpty(item.searchType) ? "" : item.searchType;
                String strType = "";
                if (noticeType.equals(FCMManager.MSG_TYPE_SYSTEM)) {
                    strType = "시스템알림";
                    setClickEnabled(strType, holder, position, item);
                }
                else if (noticeType.equals(FCMManager.MSG_TYPE_ATTEND)) {
                    strType = "출결현황";
                    setClickEnabled(strType, holder, position, item);
                }
                else if (noticeType.equals(FCMManager.MSG_TYPE_REPORT)) {
                    strType = "성적표";
                    setClickEnabled(strType, holder, position, item);
                }
                else if (noticeType.equals(FCMManager.MSG_TYPE_TUITION)) {
                    strType = "미납";
                    setClickEnabled(strType, holder, position, item);
                } else {
                    holder.tvTitle.setText("(정보없음)");
                    holder.btnNext.setVisibility(View.GONE);
                }
            }
//            else if (mReportList.size() > 0) {
//                ReportCardSummaryData reportItem = mReportList.get(position);
//
//                if (reportItem != null) {
//                    holder.tvDate.setText(TextUtils.isEmpty(reportItem.insertDate.toString()) ? "" : reportItem.insertDate.toString().replace("T", " "));
//                    if (reportItem.reportList != null) {
//                        holder.tvTitle.setText(TextUtils.isEmpty(reportItem.reportList.get(0).etName) ? "" : reportItem.reportList.get(0).etName);
//                    } else {
//                        holder.tvTitle.setText("(정보없음)");
//                    }
//
//                } else {
//                    holder.tvTitle.setText("(정보없음)");
//                    holder.btnNext.setVisibility(View.GONE);
//                }
//            }
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.linearLayout.getLayoutParams();
            layoutParams.endToStart = R.id.btn_notice_next;

            Glide.with(mContext).load(R.drawable.img_receive).into(holder.imgSenderAndReceiver);
            holder.imgSenderAndReceiver.setVisibility(View.GONE);
        }
    }

    private void setClickDisabled(String str, ViewHolder holder, SystemNoticeListData item) {
        //holder.tvType.setText(str);
        holder.btnNext.setVisibility(View.GONE);
        holder.root.setOnClickListener(null);
        holder.root.setBackgroundResource(R.color.transparent);

        try {
            holder.tvDate.setText(TextUtils.isEmpty(item.insertDate.toString()) ? "" : item.insertDate.toString().replace("T", " "));
            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
        }catch (Exception e) {}
    }

    private void setClickEnabled(String str, ViewHolder holder, int position, SystemNoticeListData item) {
        //holder.tvType.setText(str);
        holder.btnNext.setVisibility(View.VISIBLE);
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        holder.root.setBackgroundResource(typedValue.resourceId);

        holder.root.setOnClickListener(v -> {if (mList.size() > 0) _listener.onItemClick(mList.get(position));});

        try {
            String date = Utils.formatDate(item.insertDate, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
            holder.tvDate.setText(date);
            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
        }catch (Exception e) {}

    }

    @Override
    public int getItemCount() {
        if (mList != null) if (mList.size() > 0) return mList.size();
        //if (mReportList != null) if (mReportList.size() > 0) return mReportList.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout root;
        private LinearLayoutCompat linearLayout;
        private TextView tvType, tvDate, tvReceiver, tvTitle;
        private ImageView imgSenderAndReceiver;
        private ImageButton btnNext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.notice_root);
            linearLayout = itemView.findViewById(R.id.layout_notice_state);
            btnNext = itemView.findViewById(R.id.btn_notice_next);
            tvType = itemView.findViewById(R.id.tv_notice_type);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvReceiver = itemView.findViewById(R.id.tv_notice_receiver);
            tvTitle = itemView.findViewById(R.id.tv_system_notice_title);
            imgSenderAndReceiver = itemView.findViewById(R.id.img_notice_send_and_receiver);
        }
    }
}
