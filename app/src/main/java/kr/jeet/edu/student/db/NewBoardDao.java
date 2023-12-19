package kr.jeet.edu.student.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.threeten.bp.LocalDateTime;

import java.util.Date;
import java.util.List;

@Dao
public interface NewBoardDao {

    @Query("SELECT * FROM tbl_new_board WHERE memberSeq = :memberSeq AND type = :type AND insertDate >= :checkDate ORDER BY id DESC")
    List<NewBoardData> getAfterBoard(int memberSeq, String type, LocalDateTime checkDate);

    @Query("SELECT * FROM tbl_new_board WHERE memberSeq = :memberSeq AND type = :type AND insertDate >= :checkDate AND connSeq == :connSeq ORDER BY id DESC")
    NewBoardData getAfterBoardInfo(int memberSeq, String type, LocalDateTime checkDate, int connSeq);

    @Update
    void update(NewBoardData... newBoardData);

    @Insert
    void insert(NewBoardData newBoardData);

    @Insert
    void insertList(List<NewBoardData> newBoardData);

    @Delete
    void delete(NewBoardData newBoardData);
}
