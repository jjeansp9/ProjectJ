package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.DataManager;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.utils.Utils;

public class TestReserveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface ItemClickListener{ public void onItemClick(TestReserveData item, int position); }

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BODY = 1;
    SimpleDateFormat _dateSecondFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Locale.KOREA);
    SimpleDateFormat _dateMinuteFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm, Locale.KOREA);
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_test_reserve_list_item, parent, false);
//        return new TestReserveAdapter.ViewHolder(view);

        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == VIEW_TYPE_HEADER){
            View view = inflater.inflate(R.layout.layout_txt_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.layout_test_reserve_list_item, parent, false);
            return new BodyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;

        if (holder.getItemViewType() == VIEW_TYPE_HEADER){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.tvHeader.setText(mContext.getString(R.string.menu_test_reserve_header));

            if (mList.size() <= 1) headerViewHolder.lineHeader.setVisibility(View.GONE);
            else headerViewHolder.lineHeader.setVisibility(View.VISIBLE);

        }else {
            TestReserveData item = mList.get(position);
            BodyViewHolder bodyHolder = (BodyViewHolder) holder;

            if (item != null) {
                bodyHolder.tvName.setText(Utils.getStr(item.name));

                Optional<LTCData> acaData = DataManager.getInstance().getLTCList().stream().filter(
                        ltcData -> ltcData.ltcCode.equals(Utils.getStr(item.bigo))
                ).findFirst();
                acaData.ifPresent(ltcData -> bodyHolder.tvCampus.setText(ltcData.ltcName));

                try {
                    Date insertDate = _dateSecondFormat.parse(item.insertDate);

                    bodyHolder.tvRegisterDate.setText(_dateMinuteFormat.format(insertDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

//                bodyHolder.tvReserveDate.setText(item.reservationDate);
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_HEADER;
        else return VIEW_TYPE_BODY;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHeader;
        private View lineHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_header);
            lineHeader = itemView.findViewById(R.id.line_header);
        }
    }

    public class BodyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvCampus, tvRegisterDate;

        public BodyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_test_reserve_name);
            tvCampus = itemView.findViewById(R.id.tv_test_reserve_campus);
            tvRegisterDate = itemView.findViewById(R.id.tv_test_register_date);
//            tvReserveDate = itemView.findViewById(R.id.tv_test_reserve_date);

            itemView.setOnClickListener(v -> {
                // 2개의 Adapter를 ConcatAdapter로 연결한 경우
                // getBindingAdapterPosition() : 터치한 Adapter 내에서의 위치 1을 반환 ex) 2개의 Adapter는 각각의 position을 반환
                // getAbsoluteAdapterPosition() : RecyclereView에서의 절대 위치 1을 반환 ex) 2개의 Adapter을 하나로 인식하여 절대 위치의 position을 반환
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position), position);
            });
        }
    }
}
