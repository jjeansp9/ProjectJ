package kr.jeet.edu.student.db;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.ChronoField;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.utils.Converters;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;

//@Entity(tableName = "tbl_push_message")
//public class PushMessage implements Parcelable {
//    @PrimaryKey(autoGenerate = true)
//    public long id;
//    @ColumnInfo(name="title")
//    public String title;
//    @ColumnInfo(name="body")
//    public String body;
//    @ColumnInfo(name="acaCode")
//    public String acaCode;
//    @ColumnInfo(name="stCode")
//    public int stCode;
//    @ColumnInfo(name="userGubun")
//    public int userGubun;
//    @ColumnInfo(name="pushType")
//    public String pushType;
//    @ColumnInfo(name="memberSeq")
//    public int memberSeq;
//    @ColumnInfo(name="connSeq")
//    public int connSeq;
//    @ColumnInfo(name="pushId")
//    public String pushId;
//    @ColumnInfo(name="isRead", defaultValue = "false")
//    public boolean isRead = false;
//    @ColumnInfo(name="date")
//    public LocalDateTime date;
//
//    public PushMessage(long id, String title, String body, String acaCode, int stCode, int userGubun, LocalDateTime date, String pushType, int memberSeq, int connSeq, String pushId, boolean isRead) {
//        this.id = id;
//        this.title = title;
//        this.body = body;
//        this.acaCode = acaCode;
//        this.stCode = stCode;
//        this.userGubun = userGubun;
//        this.pushType = pushType;
//        this.memberSeq = memberSeq;
//        this.connSeq = connSeq;
//        this.pushId = pushId;
//        this.isRead = isRead;
//        try{ this.date = date; }
//        catch (Exception e){ LogMgr.e("PushMessage()", e.getMessage()); }
//    }
//
//    public PushMessage() {}
//
//    public static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
//            .appendPattern("yyyy-MM-dd HH:mm:ss")
//            .optionalStart()
//            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
//            .optionalEnd()
//            .toFormatter();
//
//    protected PushMessage(Parcel in) {
//        readFromParcel(in);
//    }
//
//    public static final Creator<PushMessage> CREATOR = new Creator<PushMessage>() {
//        @Override
//        public PushMessage createFromParcel(Parcel in) {
//            return new PushMessage(in);
//        }
//
//        @Override
//        public PushMessage[] newArray(int size) {
//            return new PushMessage[size];
//        }
//    };
//
//    public static PushMessage buildFromMap(Map<String, String> map, Context context) {
//        LocalDateTime initDate = LocalDateTime.now();
//        try{
//            if(map.containsKey("date")) {
//                String dateStr = map.get("date");
//                if (dateStr != null) initDate = LocalDateTime.parse(dateStr, dateTimeFormatter);
//            }
//        }catch (Exception e) {}
//
//        long id = 0L;
//        String title = map.containsKey("title")? map.get("title") : "";
//        String content = map.containsKey("body")? map.get("body") : "";
//        String acaCode = map.containsKey("acaCode")? map.get("acaCode") : "";
//        int stCode = -1;
//        String stCodeStr = map.containsKey("stCode")? map.get("stCode") : "";
//        int userGubun = -1;
//        String userGubunStr = map.containsKey("userGubun")? map.get("userGubun") : "";
//        int connSeq = -1;
//        String connSeqStr = map.containsKey("connSeq")? map.get("connSeq") : "";
//        try {
//            stCode = Integer.parseInt(stCodeStr);
//        }catch (Exception e){}
//
//        try {
//            userGubun = Integer.parseInt(userGubunStr);
//        }catch (Exception e){}
//
//        try{
//            connSeq = Integer.parseInt(connSeqStr);
//        }catch(Exception ex){}
//
//        int memberSeq = 0;
////        if (userGubun == Constants.USER_TYPE_PARENTS){
////            if (map.get("pushType").equals(FCMManager.MSG_TYPE_ATTEND)){
////                memberSeq = PreferenceUtil.getStuSeq(context);
////            }else{
////                memberSeq = PreferenceUtil.getUserSeq(context);
////            }
////        }else{
////            memberSeq = PreferenceUtil.getUserSeq(context);
////        }
//
//        String memberSeqStr = map.containsKey("memberSeq")? map.get("memberSeq") : "";
//        try{
//            memberSeq = Integer.parseInt(memberSeqStr);
//        }catch(Exception ex){}
//        String pushType = map.containsKey("pushType")? map.get("pushType") : "";
//        String pushId = map.containsKey("pushId")? map.get("pushId") : "";
//
//        LocalDateTime date = initDate;
//
//        return new PushMessage(id, title, content, acaCode, stCode, userGubun, date, pushType, memberSeq, connSeq, pushId, false);
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//    public void readFromParcel(Parcel in) {
//        id = in.readLong();
//        title = in.readString();
//        body = in.readString();
//        acaCode = in.readString();
//        stCode = in.readInt();
//        userGubun = in.readInt();
//        pushType = in.readString();
//        memberSeq = in.readInt();
//        connSeq = in.readInt();
//        pushId = in.readString();
//        isRead = in.readByte() != 0;
//        try{
//            date = LocalDateTime.parse(Objects.requireNonNull(in.readString()), dateTimeFormatter);
//        }catch (Exception e) { LogMgr.e("PushMessage readFromParcel()", e.getMessage()); }
//
//        //date = LocalDateTime.parse(in.readString(), dateTimeFormatter);
//    }
//    @Override
//    public void writeToParcel(@NonNull Parcel parcel, int i) {
//        parcel.writeLong(id);
//        parcel.writeString(title);
//        parcel.writeString(body);
//        parcel.writeString(acaCode);
//        parcel.writeInt(stCode);
//        parcel.writeInt(userGubun);
//        parcel.writeString(pushType);
//        parcel.writeInt(memberSeq);
//        parcel.writeInt(connSeq);
//        parcel.writeString(pushId);
//        parcel.writeByte((byte) (isRead ? 1 : 0));
//        try{
//            parcel.writeString(date.format(dateTimeFormatter));
//        }catch (Exception e) { LogMgr.e("PushMessage writeToParcel()", e.getMessage()); }
//
//    }
//}
@Entity(tableName = "tbl_push_message")
public class PushMessage implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "body")
    public String body;

    @ColumnInfo(name = "acaCode")
    public String acaCode;

    @ColumnInfo(name = "stCode")
    public int stCode;

    @ColumnInfo(name = "userGubun")
    public int userGubun;

    @ColumnInfo(name = "pushType")
    public String pushType;

    @ColumnInfo(name = "memberSeq")
    public int memberSeq;

    @ColumnInfo(name = "connSeq")
    public int connSeq;

    @ColumnInfo(name = "pushId")
    public String pushId;

    @ColumnInfo(name = "isRead", defaultValue = "false")
    public boolean isRead = false;

    @ColumnInfo(name = "date")
    public LocalDateTime date;

        public PushMessage(long id, String title, String body, String acaCode, int stCode, int userGubun, LocalDateTime date, String pushType, int memberSeq, int connSeq, String pushId, boolean isRead) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.acaCode = acaCode;
        this.stCode = stCode;
        this.userGubun = userGubun;
        this.pushType = pushType;
        this.memberSeq = memberSeq;
        this.connSeq = connSeq;
        this.pushId = pushId;
        this.isRead = isRead;
        try{ this.date = date; }
        catch (Exception e){ LogMgr.e("PushMessage()", e.getMessage()); }
    }

        public static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
            .optionalEnd()
            .toFormatter();

        public static PushMessage buildFromMap(Map<String, String> map, Context context) {
        LocalDateTime initDate = LocalDateTime.now();
        try{
            if(map.containsKey("date")) {
                String dateStr = map.get("date");
                if (dateStr != null) initDate = LocalDateTime.parse(dateStr, dateTimeFormatter);
            }
        }catch (Exception e) {}

        long id = 0L;
        String title = map.containsKey("title")? map.get("title") : "";
        String content = map.containsKey("body")? map.get("body") : "";
        String acaCode = map.containsKey("acaCode")? map.get("acaCode") : "";
        int stCode = -1;
        String stCodeStr = map.containsKey("stCode")? map.get("stCode") : "";
        int userGubun = -1;
        String userGubunStr = map.containsKey("userGubun")? map.get("userGubun") : "";
        int connSeq = -1;
        String connSeqStr = map.containsKey("connSeq")? map.get("connSeq") : "";
        try {
            stCode = Integer.parseInt(stCodeStr);
        }catch (Exception e){}

        try {
            userGubun = Integer.parseInt(userGubunStr);
        }catch (Exception e){}

        try{
            connSeq = Integer.parseInt(connSeqStr);
        }catch(Exception ex){}

        int memberSeq = 0;
//        if (userGubun == Constants.USER_TYPE_PARENTS){
//            if (map.get("pushType").equals(FCMManager.MSG_TYPE_ATTEND)){
//                memberSeq = PreferenceUtil.getStuSeq(context);
//            }else{
//                memberSeq = PreferenceUtil.getUserSeq(context);
//            }
//        }else{
//            memberSeq = PreferenceUtil.getUserSeq(context);
//        }

        String memberSeqStr = map.containsKey("memberSeq")? map.get("memberSeq") : "";
        try{
            memberSeq = Integer.parseInt(memberSeqStr);
        }catch(Exception ex){}
        String pushType = map.containsKey("pushType")? map.get("pushType") : "";
        String pushId = map.containsKey("pushId")? map.get("pushId") : "";

        LocalDateTime date = initDate;

        return new PushMessage(id, title, content, acaCode, stCode, userGubun, date, pushType, memberSeq, connSeq, pushId, false);
    }
}
