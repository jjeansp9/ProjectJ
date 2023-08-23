package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.SelectStudentActivity;
import kr.jeet.edu.student.model.data.ChildStudentInfo;
import kr.jeet.edu.student.utils.LogMgr;

public class SelectStudentListAdapter extends RecyclerView.Adapter<SelectStudentListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ChildStudentInfo> mList = null;

    public SelectStudentListAdapter(Context mContext, ArrayList<ChildStudentInfo> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public SelectStudentListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_student_select_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectStudentListAdapter.ViewHolder holder, int position) {
        ChildStudentInfo item = mList.get(position);

        try{
            String itemClass = "";
            if (item.stGrade.equals(item.clstName)) itemClass = item.deptName + " / " + item.clstName;
            else itemClass = item.deptName + " " + item.stGrade + " / " + item.clstName;

            holder.tvName.setText(item.stName);
            holder.tvBirth.setText(item.birth);
            holder.tvAcaName.setText(item.acaName);
            holder.tvClass.setText(itemClass);

            // gender
            if(item.gender.equals("M")) {
                Glide.with(mContext).load(R.drawable.img_dot_man).into(holder.imgGender);
                Glide.with(mContext).load(R.drawable.img_profile_man).into(holder.imgProfile);
                Glide.with(mContext).load(R.drawable.bg_student_list_gender_man).into(holder.imgLineGender);
            } else {
                Glide.with(mContext).load(R.drawable.img_dot_woman).into(holder.imgGender);
                Glide.with(mContext).load(R.drawable.img_profile_woman).into(holder.imgProfile);
                Glide.with(mContext).load(R.drawable.bg_student_list_gender_woman).into(holder.imgLineGender);
            }

        }catch(Exception e){
            LogMgr.e("Adapter Exception : ", e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile, imgGender, imgLineGender;
        private TextView tvName, tvBirth, tvDeptName, tvStGrade, tvClstName, tvAcaName, tvClass;
        private ConstraintLayout layoutStudentInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.profileImg);
            imgGender = itemView.findViewById(R.id.imgGender);
            imgLineGender = itemView.findViewById(R.id.img_line_gender);
            tvName = itemView.findViewById(R.id.name);
            tvAcaName = itemView.findViewById(R.id.tv_aca_name);
            tvBirth = itemView.findViewById(R.id.tv_birth);
            tvClass = itemView.findViewById(R.id.tv_class);
            layoutStudentInfo = itemView.findViewById(R.id.layout_student_info);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    ((SelectStudentActivity)mContext).goMain(position);
                }
            });

        }
    }
}
