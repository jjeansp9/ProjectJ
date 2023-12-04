package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class QnaDetailData implements Parcelable {
    public int seq;                // 글 seq
    public int writerSeq;          // 작성자 seq
    public String writerNm;        // 작성자명
    public int userGubun;          // 작성자 유저구분
    public int stCode;             // (부모앱) 원생 고유번호
    public String acaCode;         // 캠퍼스코드
    public String acaName;         // 캠퍼스이름
    public String acaGubunCode;    // 캠퍼스 구분코드
    public String acaGubunName;    // 캠퍼스 구분이름
    public String title;           // 제목
    public String content;         // 내용
    public String reply;           // 답변 내용
    public int replyMberSeq;       // 답변자 seq
    public String replyMberNm;     // 답변자명
    public int sfCode;             // 답변자 고유번호
    public String isOpen;          // 공개여부(Y/N)
    public String isMain;          // 공지여부(Y/N)
    public String state;           // 상태(신청1/접수2/완료3)
    public int rdcnt;              // 조회수
    public String insertDate;      // 작성일
    public String isSubAdmin;      // 중간관리자여부(Y/N)
    public String isReplyAdmin;    // 답변관리자여부(Y/N)

    public QnaDetailData() {}

    protected QnaDetailData(Parcel in) {
        seq = in.readInt();
        writerSeq = in.readInt();
        writerNm = in.readString();
        userGubun = in.readInt();
        stCode = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        title = in.readString();
        content = in.readString();
        reply = in.readString();
        replyMberSeq = in.readInt();
        replyMberNm = in.readString();
        sfCode = in.readInt();
        isOpen = in.readString();
        isMain = in.readString();
        state = in.readString();
        rdcnt = in.readInt();
        insertDate = in.readString();
        isSubAdmin = in.readString();
        isReplyAdmin = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeInt(writerSeq);
        dest.writeString(writerNm);
        dest.writeInt(userGubun);
        dest.writeInt(stCode);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(reply);
        dest.writeInt(replyMberSeq);
        dest.writeString(replyMberNm);
        dest.writeInt(sfCode);
        dest.writeString(isOpen);
        dest.writeString(isMain);
        dest.writeString(state);
        dest.writeInt(rdcnt);
        dest.writeString(insertDate);
        dest.writeString(isSubAdmin);
        dest.writeString(isReplyAdmin);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QnaDetailData> CREATOR = new Creator<QnaDetailData>() {
        @Override
        public QnaDetailData createFromParcel(Parcel in) {
            return new QnaDetailData(in);
        }

        @Override
        public QnaDetailData[] newArray(int size) {
            return new QnaDetailData[size];
        }
    };

}
