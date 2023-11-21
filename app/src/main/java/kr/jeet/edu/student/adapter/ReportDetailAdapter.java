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

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.ReportDetailData;

public class ReportDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int VIEW_TYPE_GRADE = 0;
    private static final int VIEW_TYPE_MATH = 1;
    private Context mContext;
    private ArrayList<ReportDetailData> mList = new ArrayList<>();

    public ReportDetailAdapter(Context mContext, ArrayList<ReportDetailData> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder holder;

        if (viewType == VIEW_TYPE_GRADE) holder = new GradeViewHolder(inflater.inflate(R.layout.layout_report_detail_grade_item, parent, false));
        else holder = new MathViewHolder(inflater.inflate(R.layout.layout_report_detail_math_item, parent, false));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;

        if (holder.getItemViewType() == VIEW_TYPE_GRADE) {

        } else {

        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_GRADE;
        else return VIEW_TYPE_MATH;
    }

    public class GradeViewHolder extends RecyclerView.ViewHolder {

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }

    public class MathViewHolder extends RecyclerView.ViewHolder {

        public MathViewHolder(@NonNull View itemView) {
            super(itemView);


        }
    }
}
