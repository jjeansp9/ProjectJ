package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.TeacherClsData;
import kr.jeet.edu.student.utils.Utils;

public class TeacherListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_BODY = 1;

    private Context mContext;
    private ArrayList<TeacherClsData> mList = null;
    private ItemClickListener _listener;

    public interface ItemClickListener{ public void onItemClick(TeacherClsData item); }

    public TeacherListAdapter(Context mContext, ArrayList<TeacherClsData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == VIEW_TYPE_HEADER){
            View view = inflater.inflate(R.layout.layout_teacher_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.layout_teacher_list_item, parent, false);
            return new BodyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;

        if (holder.getItemViewType() == VIEW_TYPE_HEADER){
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            headerViewHolder.tvHeader.setText(mContext.getString(R.string.main_tv_consult));

        } else {

            TeacherClsData item = mList.get(position);

            BodyViewHolder bodyHolder = (BodyViewHolder) holder;

            bodyHolder.tvName.setText(Utils.getStr(item.sfName));
            bodyHolder.tvClsName.setText(Utils.getStr(item.clsName));
            bodyHolder.tvPhoneNum.setText(Utils.getStr(item.extNumber));
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
}
