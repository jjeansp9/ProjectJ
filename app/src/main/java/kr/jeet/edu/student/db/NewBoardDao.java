package kr.jeet.edu.student.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NewBoardDao {

    @Query("SELECT * FROM tbl_new_board WHERE memberSeq = :memberSeq AND type = :type AND strftime('%Y%m', insertDate) = :insertDate ORDER BY id DESC")
    List<NewBoardData> getNewBoard(int memberSeq, String type, String insertDate);

    @Query("SELECT * FROM tbl_new_board WHERE memberSeq = :memberSeq AND isRead =:isread AND type=:type ORDER BY id DESC")
    List<NewBoardData> getBoardByReadFlagNType(int memberSeq, boolean isread, String type);

    @Update
    void update(NewBoardData... newBoardData);

    @Insert
    void insertAll(List<NewBoardData> newBoardData);

    @Delete
    void delete(NewBoardData newBoardData);
}
