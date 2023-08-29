package kr.jeet.edu.student.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PushMessageDao {
    @Query("SELECT * FROM tbl_push_message")
    List<PushMessage> getAllMessage();

    @Query("SELECT * FROM tbl_push_message WHERE id <= :startSeq ORDER BY id DESC")
    List<PushMessage> getReverseAllMessage(long startSeq);

    @Query("SELECT * FROM tbl_push_message WHERE id <= :startSeq ORDER BY id DESC LIMIT :limit")
    List<PushMessage> getReverseMessages(long startSeq, int limit);

    @Query("SELECT * FROM tbl_push_message WHERE id =:id")
    List<PushMessage> getMessageById(int id);

    @Query("SELECT * FROM tbl_push_message WHERE isRead =:isread")
    List<PushMessage> getMessageByReadFlag(boolean isread);

    @Query("SELECT * FROM tbl_push_message WHERE connSeq=:connSeq AND pushType=:type")
    List<PushMessage> getMessageBySeqNType(int connSeq, String type);

    @Update
    void update(PushMessage... message);

    @Insert
    void insertAll(PushMessage... message);

    @Delete
    void delete(PushMessage message);
}
