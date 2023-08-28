package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.view.DrawableAlwaysCrossFadeFactory;

public class BoardDetailImageListAdapter extends RecyclerView.Adapter<BoardDetailImageListAdapter.ViewHolder> {

    public interface ItemClickListener{
        public void onItemClick(ArrayList<FileData> item, int position);
    }

    private Context mContext;
    private ArrayList<FileData> mList;
    private ItemClickListener _listener;

    public BoardDetailImageListAdapter(Context mContext, ArrayList<FileData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_board_detail_img_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        try{
            if (position != NO_POSITION){
                FileData item = mList.get(position);

                if (item != null && mList.size() > 0) {
                    String url = RetrofitApi.FILE_SUFFIX_URL + item.path + "/" + item.saveName;
                    url = FileUtils.replaceMultipleSlashes(url);
                    LogMgr.i("urlTest ", url);

                    Glide.with(mContext)
                            .load(url)
                            //.thumbnail(0.2f)
                            //.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .error(R.drawable.ic_vector_image_error)
                            .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                            .into(holder.imgBoardDetail);
                }
            }

        }catch (Exception e){
            LogMgr.e("ImageAdapter Exception : " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgBoardDetail;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            imgBoardDetail = itemView.findViewById(R.id.img_board_detail);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (mList.size() > 0) _listener.onItemClick(mList, position);
            });
        }
    }
}
