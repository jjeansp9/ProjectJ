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

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.utils.Utils;

public class TeacherListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BODY = 1;
    private static final int VIEW_TYPE_CLS_TIME = 2;

    private Context mContext;
    private ArrayList<TeacherClsData> mList = null;
    private ItemClickListener _listener;

    public interface ItemClickListener{ public void onItemClick(TeacherClsData item); }

    public TeacherListAdapter(Context mContext, ArrayList<TeacherClsData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
    }

    public TeacherListAdapter(Context mContext, ArrayList<TeacherClsData> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == VIEW_TYPE_HEADER){
            View view = inflater.inflate(R.layout.layout_txt_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_BODY){

            View view = inflater.inflate(R.layout.layout_teacher_list_item, parent, false);
            return new BodyViewHolder(view);

        }else {
            View view = inflater.inflate(R.layout.layout_time_table_list_item, parent, false);
            return new ClsTimeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;

        if (holder.getItemViewType() == VIEW_TYPE_HEADER){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.tvHeader.setText(mContext.getString(R.string.main_tv_consult));

        } else if (holder.getItemViewType() == VIEW_TYPE_BODY){

            TeacherClsData item = mList.get(position);

            BodyViewHolder bodyHolder = (BodyViewHolder) holder;

            if (item != null) {
                bodyHolder.tvName.setText(Utils.getStr(item.sfName));
                bodyHolder.tvClsName.setText(Utils.getStr(item.clsName));
                bodyHolder.tvPhoneNum.setText(Utils.getStr(item.extNumber));
            }
        } else { // 시간표 메뉴
            TeacherClsData item = mList.get(position);

            ClsTimeViewHolder bodyHolder = (ClsTimeViewHolder) holder;

            if (item != null) {
                bodyHolder.tvName.setText(Utils.getStr(item.sfName));
                bodyHolder.tvClsName.setText(Utils.getStr(item.clsName));
                bodyHolder.tvClsTime.setText(Utils.getStr(item.clsTimeName));
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
        if (_listener != null) {
            if (position == 0) return VIEW_TYPE_HEADER;
            else return VIEW_TYPE_BODY;
        } else {
            return VIEW_TYPE_CLS_TIME;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tv_header);
        }
    }

    public class BodyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile, imgLine;
        private TextView tvName, tvClsName, tvPhoneNum;

        public BodyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.profileImg);
            tvName = itemView.findViewById(R.id.name);
            tvClsName = itemView.findViewById(R.id.tv_cls_name);
            tvPhoneNum = itemView.findViewById(R.id.tv_phone_num);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position > 0) if (mList.size() > 1) _listener.onItemClick(mList.get(position));
            });
        }
    }

    public class ClsTimeViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvClsName, tvClsTime;

        public ClsTimeViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvClsName = itemView.findViewById(R.id.tv_cls_name);
            tvClsTime = itemView.findViewById(R.id.tv_cls_time);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position > 0) if (mList.size() > 1) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
