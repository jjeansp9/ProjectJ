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
import kr.jeet.edu.student.model.data.TeacherClsData;

public class TeacherListAdapter extends RecyclerView.Adapter<TeacherListAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<TeacherClsData> mList = null;

    public TeacherListAdapter(Context mContext, ArrayList<TeacherClsData> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_teacher_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == NO_POSITION) return;
        TeacherClsData item = mList.get(position);

        holder.tvName.setText(TextUtils.isEmpty(item.sfName) ? "" : item.sfName);
        holder.tvClsName.setText(TextUtils.isEmpty(item.clsName) ? "" : item.clsName);
        holder.tvPhoneNum.setText(TextUtils.isEmpty(item.phoneNumber) ? "" : item.phoneNumber);
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile, imgLine;
        private TextView tvName, tvClsName, tvPhoneNum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.profileImg);
            tvName = itemView.findViewById(R.id.name);
            tvClsName = itemView.findViewById(R.id.tv_cls_name);
            tvPhoneNum = itemView.findViewById(R.id.tv_phone_num);
        }
    }
}
