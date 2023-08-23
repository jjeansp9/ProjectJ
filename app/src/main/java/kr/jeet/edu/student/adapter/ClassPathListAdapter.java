package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.ClassPathData;
import kr.jeet.edu.student.utils.LogMgr;

public class ClassPathListAdapter extends RecyclerView.Adapter<ClassPathListAdapter.ViewHolder> {

    public interface ItemClickListener{
        public void onDeleteClick(ClassPathData mList, int position);
    }

    private Context mContext;
    private List<ClassPathData> mList = new ArrayList<ClassPathData>();
    private ItemClickListener _listener;

    public ClassPathListAdapter(Context mContext, List<ClassPathData> mList, ItemClickListener listener){
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_class_path_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            if(position >= 0) {
                ClassPathData item = mList.get(position);

                holder.tvClassPath.setText(item.classPath);
                holder.tvClassLessonVol.setText(item.classLessonVol);
                holder.tvClassTerm.setText(item.classTerm);
            }
        }catch (Exception e){
            LogMgr.e("Class Path Adapter Exception : " + e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvClassPath, tvClassLessonVol, tvClassTerm;
        private ImageView imgDelete;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tvClassPath = itemView.findViewById(R.id.tv_class_path);
            tvClassLessonVol = itemView.findViewById(R.id.tv_weekly_lesson_vol);
            tvClassTerm = itemView.findViewById(R.id.tv_class_term);
            imgDelete = itemView.findViewById(R.id.btn_class_path_delete);

            imgDelete.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                _listener.onDeleteClick(mList.get(position), position);
            });
        }
    }
}
