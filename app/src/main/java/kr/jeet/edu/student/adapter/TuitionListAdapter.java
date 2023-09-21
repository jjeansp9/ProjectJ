package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.os.Build;
import android.text.Html;
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

    public static int LAYOUT_HEADER = 0;
    public static int LAYOUT_CONTENT = 1;

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
            headerHolder.tvAccountNo.setText(Utils.getStr(headerItem.accountNO));
            headerHolder.tvHeaderAcaName.setText(Utils.getStr(headerItem.acaName));

            headerHolder.tvPayTotal.setText(headerItem.payment);

        }else {
            ContentViewHolder contentHolder = (ContentViewHolder) holder;
            TuitionData contentItem = (TuitionData) item;

            contentHolder.tvContentPay.setText(Utils.getStr(contentItem.payment+"원"));
            contentHolder.tvContentClsName.setText(Utils.getStr(contentItem.clsName));
            contentHolder.tvContentDate.setText(Utils.getStr(contentItem.payDate));

//            // 마지막 아이템 체크
//            if (position == mList.size() - 1) {
//                // 마지막 아이템인 경우, contentHolder.line을 invisible로 설정
//                contentHolder.lineContentBottom.setVisibility(View.INVISIBLE);
//            } else {
//                // 마지막 아이템이 아닌 경우, contentHolder.line을 visible로 설정
//                contentHolder.lineContentBottom.setVisibility(View.VISIBLE);
//            }

            // 현재 아이템의 ViewType이 Content인 경우에만 다음 아이템의 ViewType을 확인
//            if (holder.getItemViewType() == LAYOUT_CONTENT) {
//                int nextItemType = getItemViewType(position + 1);
//                if (nextItemType != LAYOUT_HEADER) {
//                    // 다음 아이템의 ViewType이 Header가 아닌 경우, contentHolder.line을 invisible로 설정
//                    contentHolder.lineContentBottom.setVisibility(View.INVISIBLE);
//                } else {
//                    // 다음 아이템의 ViewType이 Header인 경우, contentHolder.line을 visible로 설정
//                    contentHolder.lineContentBottom.setVisibility(View.VISIBLE);
//                }
//            }

            // 마지막 아이템 체크
            if (position == mList.size() - 1) {
                // 마지막 아이템인 경우, contentHolder.line을 invisible로 설정
                contentHolder.lineContentBottom.setVisibility(View.INVISIBLE);
            } else {
                // 마지막 아이템이 아닌 경우, contentHolder.line을 visible로 설정
                int nextPosition = position + 1;
                if (nextPosition < mList.size() && getItemViewType(nextPosition) == LAYOUT_HEADER) {
                    contentHolder.lineContentBottom.setVisibility(View.VISIBLE);
                } else {
                    contentHolder.lineContentBottom.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private AppCompatButton btnAccountLink;
        private TextView tvGubunBadge, tvHeaderAcaName, tvAccountNo, tvPayTotal;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAccountLink = itemView.findViewById(R.id.btn_account_header_link);
            tvGubunBadge = itemView.findViewById(R.id.tv_tuition_header_badge);
            tvAccountNo = itemView.findViewById(R.id.tv_tuition_header_account_no);
            tvHeaderAcaName = itemView.findViewById(R.id.tv_tuition_header_aca_name);
            tvPayTotal = itemView.findViewById(R.id.tv_tuition_header_total);

            btnAccountLink.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
    public class ContentViewHolder extends RecyclerView.ViewHolder{
        private TextView tvContentPay, tvContentClsName, tvContentDate;
        private View lineContentBottom;

        public ContentViewHolder(@NonNull View itemView){
            super(itemView);
            tvContentPay = itemView.findViewById(R.id.tv_tuition_content_pay);
            tvContentClsName = itemView.findViewById(R.id.tv_tuition_content_cls_name);
            tvContentDate = itemView.findViewById(R.id.tv_tuition_content_date);
            lineContentBottom = itemView.findViewById(R.id.line_content_bottom);
        }
    }
}
