package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.MenuStudentInfoActivity;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.TuitionData;
import kr.jeet.edu.student.model.data.TuitionHeaderData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class TuitionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public interface ItemClickListener{ public void onItemClick(MenuStudentInfoActivity.PayListItem item); }

    private static final int LAYOUT_HEADER = 0;
    private static final int LAYOUT_CONTENT = 1;

    private Context mContext;
    private List<MenuStudentInfoActivity.PayListItem> mList;
    private ItemClickListener _listener;
    private LayoutInflater inflater;

    public TuitionListAdapter(Context mContext, List<MenuStudentInfoActivity.PayListItem> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).isHeader()) return LAYOUT_HEADER;
        else return LAYOUT_CONTENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if(viewType == LAYOUT_HEADER) {
            View view = inflater.inflate(R.layout.layout_tuition_header, parent, false);
            holder = new HeaderViewHolder(view);
        }else if(viewType == LAYOUT_CONTENT){
            View view = inflater.inflate(R.layout.layout_tuition_item, parent, false);
            holder = new ContentViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MenuStudentInfoActivity.PayListItem item = mList.get(position);

        if (holder == null) return;

        if (holder.getItemViewType() == LAYOUT_HEADER){
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            TuitionHeaderData headerItem = (TuitionHeaderData) item;

            headerHolder.tvGubunBadge.setText(Utils.getStr(headerItem.gubun));
            headerHolder.tvHeaderAcaName.setText(Utils.getStr(headerItem.acaName));

        }else {
            ContentViewHolder contentHolder = (ContentViewHolder) holder;
            TuitionData contentItem = (TuitionData) item;

            contentHolder.tvContentPay.setText(Utils.getStr(contentItem.payment));
            contentHolder.tvContentClsName.setText(Utils.getStr(contentItem.clsName));
            contentHolder.tvContentDate.setText(Utils.getStr(contentItem.payDate));
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private AppCompatButton btnAccountLink;
        private TextView tvGubunBadge, tvHeaderAcaName;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAccountLink = itemView.findViewById(R.id.btn_account_header_link);
            tvGubunBadge = itemView.findViewById(R.id.tv_tuition_header_badge);
            tvHeaderAcaName = itemView.findViewById(R.id.tv_tuition_header_acaName);

            btnAccountLink.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
    public class ContentViewHolder extends RecyclerView.ViewHolder{
        private TextView tvContentPay, tvContentClsName, tvContentDate;

        public ContentViewHolder(@NonNull View itemView){
            super(itemView);
            tvContentPay = itemView.findViewById(R.id.tv_tuition_content_pay);
            tvContentClsName = itemView.findViewById(R.id.tv_tuition_content_cls_name);
            tvContentDate = itemView.findViewById(R.id.tv_tuition_content_date);
        }
    }
}
