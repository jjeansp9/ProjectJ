package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.TestReserveData;

public class TestReserveAdapter extends RecyclerView.Adapter<TestReserveAdapter.ViewHolder> {

    public interface ItemClickListener{ public void onItemClick(TestReserveData item); }

    private Context mContext;
    private ArrayList<TestReserveData> mList;
    private ItemClickListener _listener;

    public TestReserveAdapter(Context mContext, ArrayList<TestReserveData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_test_reserve_list_item, parent, false);
        return new TestReserveAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TestReserveData item = mList.get(position);

        if (item != null){
            holder.tvName.setText(item.name);

            Optional<LTCData> acaData = DataManager.getInstance().getLTCList().stream().filter(
                    ltcData -> ltcData.ltcCode.equals(item.bigo)
            ).findFirst();
            acaData.ifPresent(ltcData -> holder.tvCampus.setText(ltcData.ltcName));

            holder.tvDate.setText(item.reservationDate);
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvName, tvCampus, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_test_reserve_name);
            tvCampus = itemView.findViewById(R.id.tv_test_reserve_campus);
            tvDate = itemView.findViewById(R.id.tv_test_reserve_date);

            itemView.setOnClickListener(v -> {
                // 2개의 Adapter를 ConcatAdapter로 연결한 경우
                // getBindingAdapterPosition() : 터치한 Adapter 내에서의 위치 1을 반환 ex) 2개의 Adapter는 각각의 position을 반환
                // getAbsoluteAdapterPosition() : RecyclereView에서의 절대 위치 1을 반환 ex) 2개의 Adapter을 하나로 인식하여 절대 위치의 position을 반환
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
