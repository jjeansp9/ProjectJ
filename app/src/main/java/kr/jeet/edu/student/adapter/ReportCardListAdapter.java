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

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.ReportCardSummaryData;

public class ReportCardListAdapter extends RecyclerView.Adapter<ReportCardListAdapter.ViewHolder>{

    public interface ItemClickListener{
        public void onItemClick(ReportCardSummaryData item);
    }

    private Context mContext;
    private ArrayList<ReportCardSummaryData> mList;
    private ReportCardListAdapter.ItemClickListener _listener;

    public ReportCardListAdapter(Context mContext, ArrayList<ReportCardSummaryData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_notice_list_item, parent, false);
        return new ReportCardListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position != NO_POSITION) {
            if (mList.size() > 0) {
                ReportCardSummaryData item = mList.get(position);

                if (item != null) {
                    holder.btnNext.setVisibility(View.GONE);

                    holder.tvDate.setText(TextUtils.isEmpty(item.insertDate.toString()) ? "" : item.insertDate.toString().replace("T", " "));
                    try {
                        if (item.reportList != null && item.reportList.size() > 0) {
                            holder.tvTitle.setText(TextUtils.isEmpty(item.reportList.get(0).etName) ? "" : item.reportList.get(0).etName);
                        } else {
                            holder.tvTitle.setText("(정보없음)");
                        }
                    }catch (Exception e) {}

                    holder.imgSenderAndReceiver.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout root;
        private TextView tvDate, tvTitle;
        private ImageView imgSenderAndReceiver;
        private ImageButton btnNext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.notice_root);
            btnNext = itemView.findViewById(R.id.btn_notice_next);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvTitle = itemView.findViewById(R.id.tv_system_notice_title);
            imgSenderAndReceiver = itemView.findViewById(R.id.img_notice_send_and_receiver);

            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            root.setBackgroundResource(typedValue.resourceId);
            root.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
