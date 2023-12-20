package kr.jeet.edu.student.model.data;

public interface ReadData {
    String getDate();
    String getTime();
    int getSeq();
    boolean getIsRead();

    void setDate(String date);
    void setTime(String time);
    void setSeq(int seq);
    void setIsRead(boolean isRead);
}
