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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.TuitionData;
import kr.jeet.edu.student.utils.LogMgr;

public class TuitionListAdapter extends RecyclerView.Adapter<TuitionListAdapter.ViewHolder>{

    public interface ItemClickListener{ public void onItemClick(TuitionData item); }

    private Context mContext;
    private List<TuitionData> mList;
    private ItemClickListener _listener;

    public TuitionListAdapter(Context mContext, List<TuitionData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_tuition_item, parent, false);
        return new TuitionListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionData item = mList.get(position);

        try{
            if (item != null){

                String payment = item.payment + mContext.getString(R.string.won);

                holder.tvClstName.setText(item.clsName);
                holder.tvPayment.setText(payment);
                holder.tvAccountNo.setText(item.accountNO);
            }
        }catch (Exception e){
            LogMgr.e("Tuition Adapter Exception : " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private AppCompatButton btnAccountLink;
        private TextView tvClstName, tvPayment, tvAccountNo;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            btnAccountLink = itemView.findViewById(R.id.btn_account_link);
            tvClstName = itemView.findViewById(R.id.tv_tuition_clst_name);
            tvPayment = itemView.findViewById(R.id.tv_tuition_payment);
            tvAccountNo = itemView.findViewById(R.id.tv_tuition_account_no);

            btnAccountLink.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
