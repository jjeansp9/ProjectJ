package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.BriefingReservedListData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class BriefingReservedListAdapter extends RecyclerView.Adapter<BriefingReservedListAdapter.ViewHolder> {

    private final static String TAG = "BrfListAdapter";

    public interface ItemClickListener{ public void onItemClick(BriefingReservedListData item); }

    private Context mContext;
    private List<BriefingReservedListData> mList;
    private ItemClickListener _listener;

    public BriefingReservedListAdapter(Context mContext, List<BriefingReservedListData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_brf_reserve_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BriefingReservedListData item = mList.get(position);
        try{
            if (!mList.isEmpty()){
                String participantsCnt = String.valueOf(item.participantsCnt);
                String phoneNum = Utils.formatNum(item.phoneNumber);

                holder.tvName.setText(item.name);
                holder.tvCnt.setText(participantsCnt);
                holder.tvPhoneNum.setText(Utils.blindPhoneNumber(phoneNum));
            }

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
        private TextView tvName, tvCnt, tvPhoneNum;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_brf_reserved_name);
            tvCnt = itemView.findViewById(R.id.tv_brf_reserved_cnt);
            tvPhoneNum = itemView.findViewById(R.id.tv_brf_reserved_phone_number);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
