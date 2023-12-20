package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.SelectStudentActivity;
import kr.jeet.edu.student.model.data.ChildStudentInfo;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

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
        String itemClass = "";

        String deptName = "";
        String clstName = "";
        String stGrade = "";

        try {
            if (mList.size() - 1 == position) {
                holder.rootNewStu.setVisibility(View.VISIBLE);
                holder.rootSelStu.setVisibility(View.INVISIBLE);
            } else {
                holder.rootNewStu.setVisibility(View.GONE);
                holder.rootSelStu.setVisibility(View.VISIBLE);
            }

            if (item != null) {

                if (!TextUtils.isEmpty(item.stGrade)) {
                    if (item.stGrade.length() <= 2) {
                        if (item.stGrade.contains("초")) itemClass = item.stGrade.replace("초", "") + "학년";
                        else if (item.stGrade.contains("중")) itemClass = item.stGrade.replace("중", "") + "학년";
                        else if (item.stGrade.contains("고")) itemClass = item.stGrade.replace("고", "") + "학년";
                        else itemClass = Utils.getStr(item.stGrade);

                    } else {
                        itemClass = Utils.getStr(item.stGrade);
                    }
                }

                if (TextUtils.isEmpty(item.scName)) holder.tvScName.setVisibility(View.GONE);
                else holder.tvScName.setText(item.scName);

                holder.tvName.setText(Utils.getStr(item.stName));
                holder.tvBirth.setText(Utils.getStr(Utils.formatDate(item.birth, "yyyy-MM-dd", "yyMMdd")));
                holder.tvClass.setText(itemClass);

                // gender
                if(Utils.getStr(item.gender).equals("M")) {
                    Glide.with(mContext).load(R.drawable.img_dot_man).into(holder.imgGender);
                    Glide.with(mContext).load(R.drawable.img_profile_man).into(holder.imgProfile);
                    Glide.with(mContext).load(R.drawable.bg_student_list_gender_man).into(holder.imgLineGender);

                } else if(Utils.getStr(item.gender).equals("F")) {
                    Glide.with(mContext).load(R.drawable.img_dot_woman).into(holder.imgGender);
                    Glide.with(mContext).load(R.drawable.img_profile_woman).into(holder.imgProfile);
                    Glide.with(mContext).load(R.drawable.bg_student_list_gender_woman).into(holder.imgLineGender);

                } else {
                    Glide.with(mContext).load(R.drawable.img_profile_unisex).into(holder.imgProfile);
                    holder.imgGender.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_circle));
                    ViewCompat.setBackgroundTintList(holder.imgGender, ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.darkgray)));
                    Glide.with(mContext).load(R.drawable.bg_student_list_gender_unisex).into(holder.imgLineGender);
                }
            }
        }catch (Exception e) {}
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile, imgGender, imgLineGender;
        private TextView tvName, tvBirth, tvDeptName, tvStGrade, tvClstName, tvScName, tvClass;
        private ConstraintLayout rootSelStu, rootNewStu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.profileImg);
            imgGender = itemView.findViewById(R.id.imgGender);
            imgLineGender = itemView.findViewById(R.id.img_line_gender);
            tvName = itemView.findViewById(R.id.name);
            tvScName = itemView.findViewById(R.id.tv_sc_name);
            tvBirth = itemView.findViewById(R.id.tv_birth);
            tvClass = itemView.findViewById(R.id.tv_class);
            rootSelStu = itemView.findViewById(R.id.root_sel_stu);
            rootNewStu = itemView.findViewById(R.id.root_new_stu);

            rootSelStu.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION && mList.size() > 1) ((SelectStudentActivity)mContext).goMain(position);
            });

            rootNewStu.setOnClickListener(v -> {
                if (mList.size() > 0) ((SelectStudentActivity)mContext).goTestForNewStu();
            });

        }
    }
}
