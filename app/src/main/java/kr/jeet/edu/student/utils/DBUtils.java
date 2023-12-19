package kr.jeet.edu.student.utils;

import android.content.Context;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.NewBoardDao;
import kr.jeet.edu.student.db.NewBoardData;
import kr.jeet.edu.student.model.data.ReadData;

public class DBUtils {
    public static void insertReadDB(ReadData readData, Context context, int memberSeq, String type) {
        new Thread(() -> {
            NewBoardDao jeetDBNewBoard = JeetDatabase.getInstance(context).newBoardDao();

            LocalDateTime today = LocalDateTime.now(); // 현재날짜
            LocalDateTime sevenDaysAgo = today.minusDays(Constants.IS_READ_DELETE_DAY); // 현재 날짜에서 7일을 뺀 날짜
            NewBoardData boardInfo = jeetDBNewBoard.getReadInfo(memberSeq, type, sevenDaysAgo, readData.getSeq()); // 읽은글

            String date = "";
            try {
                if (readData.getDate() != null && !readData.getDate().isEmpty()) date = readData.getDate();
                if (readData.getPtTime() != null && !readData.getPtTime().isEmpty()) date += " " + readData.getPtTime();
            }catch (Exception e) {}

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
            LocalDateTime insertDate = LocalDateTime.parse(date, formatter);

            if (boardInfo == null) {
                if (sevenDaysAgo.isBefore(insertDate)) {
                    // 최근 7일 이내의 데이터인 경우
                    NewBoardData newBoardData = new NewBoardData(
                            type,
                            readData.getSeq(),
                            memberSeq,
                            readData.getIsRead(),
                            insertDate,
                            insertDate
                    );
                    jeetDBNewBoard.insert(newBoardData);
                    LogMgr.e("DBUtils", "dbTest Insert!");
                }
            }
        }).start();
    }
}
