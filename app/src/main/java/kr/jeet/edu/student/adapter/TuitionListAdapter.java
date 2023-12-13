package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.model.data.TuitionData;
import kr.jeet.edu.student.model.data.TuitionHeaderData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class TuitionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = "TuitionListAdapter";

    public interface ItemClickListener{ public void onItemClick(Constants.PayListItem item); }

    public static int LAYOUT_HEADER = 0;
    public static int LAYOUT_CONTENT = 1;

    private Context mContext;
    private List<Constants.PayListItem> mList;
    private ItemClickListener _listener;
    private LayoutInflater inflater;

    public TuitionListAdapter(Context mContext, List<Constants.PayListItem> mList, ItemClickListener _listener) {
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
        Constants.PayListItem item = mList.get(position);

        if (holder == null) return;

        try{

            String str = "";

            if (holder.getItemViewType() == LAYOUT_HEADER){
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                TuitionHeaderData headerItem = (TuitionHeaderData) item;

                String btnText = "계좌연동";

                // acaLink항목이 없으면 accoutNo에 acaLinkIsNull 항목을 보여주고 버튼을 계좌복사로 변경, 클릭시 다이얼로그, 확인버튼만, 버튼누르면 계좌 복사
                if (headerItem.acaLink != null || !TextUtils.isEmpty(headerItem.acaLink)) {
                    if (headerItem.accountNO.isEmpty()) {
                        headerItem.accountNO = headerItem.acaLinkIsNull;
                        headerHolder.tvAccountNo.setText(Utils.getStr(headerItem.acaLinkIsNull));
                        btnText = "계좌복사";
                    } else {
                        LogMgr.e(TAG, "adapter Test2: " + headerItem.accountNO);
                        headerHolder.tvAccountNo.setText(Utils.getStr(headerItem.accountNO));
                    }

                }else {
                    headerItem.accountNO = headerItem.acaLinkIsNull;
                    headerHolder.tvAccountNo.setText(Utils.getStr(headerItem.acaLinkIsNull));
                    btnText = "계좌복사";
                }

                headerHolder.tvGubunBadge.setText(Utils.getStr(headerItem.gubun));
                headerHolder.tvHeaderAcaName.setText(Utils.getStr(headerItem.acaName));
                headerHolder.btnAccountLink.setText(btnText);

                headerHolder.tvPayTotal.setText(headerItem.payment);

            }else {
                ContentViewHolder contentHolder = (ContentViewHolder) holder;
                TuitionData contentItem = (TuitionData) item;

                str = TextUtils.isEmpty(contentItem.payment) ? "0" : contentItem.payment;
                contentHolder.tvContentPay.setText(str);
                contentHolder.tvContentDate.setText(Utils.getStr(contentItem.payDate));
                contentHolder.tvContentClsName.setText(Utils.getStr(contentItem.clsName));

                // 마지막 아이템 체크
                if (position == mList.size() - 1) {
                    // 마지막 아이템인 경우, contentHolder.line을 invisible로 설정
                    //contentHolder.lineContentBottom.setVisibility(View.INVISIBLE);

                    // 마지막 아이템 하단에 있는 line의 margin을 없애기
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentHolder.lineContentBottom.getLayoutParams();
                    int marginInDp = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            0,
                            mContext.getResources().getDisplayMetrics()
                    );
                    params.leftMargin = marginInDp;
                    params.rightMargin = marginInDp;
                    contentHolder.lineContentBottom.setLayoutParams(params);
                }
//                else {
//                    // 마지막 아이템이 아닌 경우, contentHolder.line을 visible로 설정
//                    int nextPosition = position + 1;
//                    if (nextPosition < mList.size()) {
//                        //contentHolder.lineContentBottom.setVisibility(View.INVISIBLE);
//                    }
//                }
            }

        }catch (Exception e){
            LogMgr.e(TAG, e.getMessage());
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
