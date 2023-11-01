package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
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

        try{
            String itemClass = "";

            String deptName = "";
            String clstName = "";
            String stGrade = "";

            if (mList.size() - 1 == position) {
                holder.rootNewStu.setVisibility(View.VISIBLE);
                holder.rootSelStu.setVisibility(View.INVISIBLE);
            } else {
                holder.rootNewStu.setVisibility(View.INVISIBLE);
                holder.rootSelStu.setVisibility(View.VISIBLE);
            }

            if (item.stGrade.equals(item.clstName)) {
                deptName = Utils.getStr(item.deptName);
                clstName = TextUtils.isEmpty(item.clstName) ? "" : " / " + item.clstName;
                itemClass = deptName + clstName;
            }
            else {
                deptName = Utils.getStr(item.deptName);
                stGrade = Utils.getStr(item.stGrade);
                clstName = TextUtils.isEmpty(item.clstName) ? "" : " / " + item.clstName;

                itemClass = deptName + stGrade + clstName;
            }

            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyMMdd", Locale.KOREA);

            try {
                Date birthDate = originalFormat.parse(item.birth);
                if (birthDate != null) item.birth = targetFormat.format(birthDate);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tvName.setText(Utils.getStr(item.stName));
            holder.tvBirth.setText(Utils.getStr(item.birth));
            holder.tvAcaName.setText(Utils.getStr(item.acaName));
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
        private ConstraintLayout rootSelStu, rootNewStu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.profileImg);
            imgGender = itemView.findViewById(R.id.imgGender);
            imgLineGender = itemView.findViewById(R.id.img_line_gender);
            tvName = itemView.findViewById(R.id.name);
            tvAcaName = itemView.findViewById(R.id.tv_aca_name);
            tvBirth = itemView.findViewById(R.id.tv_birth);
            tvClass = itemView.findViewById(R.id.tv_class);
            rootSelStu = itemView.findViewById(R.id.root_sel_stu);
            rootNewStu = itemView.findViewById(R.id.root_new_stu);

            rootSelStu.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) ((SelectStudentActivity)mContext).goMain(position);
            });

            rootNewStu.setOnClickListener(v -> {

            });

        }
    }
}
