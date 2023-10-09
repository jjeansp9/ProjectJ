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

import java.util.Date;
import java.util.Map;

import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.utils.Converters;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;

@Entity(tableName = "tbl_push_message")
public class PushMessage implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name="title")
    public String title;
    @ColumnInfo(name="body")
    public String body;
    @ColumnInfo(name="acaCode")
    public String acaCode;
    @ColumnInfo(name="date")
    public LocalDateTime date;
    @ColumnInfo(name="pushType")
    public String pushType;
    @ColumnInfo(name="memberSeq")
    public int memberSeq;
    @ColumnInfo(name="connSeq")
    public int connSeq;
    @ColumnInfo(name="pushId")
    public String pushId;
    @ColumnInfo(name="isRead", defaultValue = "false")
    public boolean isRead = false;
    public PushMessage(long id, String title, String body, String acaCode, LocalDateTime date, String pushType, int memberSeq, int connSeq, String pushId, boolean isRead) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.acaCode = acaCode;
        this.date = date;
        this.pushType = pushType;
        this.memberSeq = memberSeq;
        this.connSeq = connSeq;
        this.pushId = pushId;
        this.isRead = isRead;
    }
    public static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
            .optionalEnd()
            .toFormatter();

    protected PushMessage(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<PushMessage> CREATOR = new Creator<PushMessage>() {
        @Override
        public PushMessage createFromParcel(Parcel in) {
            return new PushMessage(in);
        }

        @Override
        public PushMessage[] newArray(int size) {
            return new PushMessage[size];
        }
    };

    public static PushMessage buildFromMap(Map<String, String> map, Context context) {
        LocalDateTime initDate = LocalDateTime.now();
        if(map.containsKey("date")) {
            String dateStr = map.get("date");
            if (dateStr != null) initDate = LocalDateTime.parse(dateStr, dateTimeFormatter);
        }
        long id = 0L;
        String title = map.containsKey("title")? map.get("title") : "";
        String content = map.containsKey("body")? map.get("body") : "";
        String acaCode = map.containsKey("acaCode")? map.get("acaCode") : "";
        LocalDateTime date = initDate;

        int userGubun = PreferenceUtil.getUserGubun(context);

        int memberSeq = 0;
        if (userGubun == Constants.USER_TYPE_PARENTS){
            if (map.get("pushType").equals(FCMManager.MSG_TYPE_ATTEND)){
                memberSeq = PreferenceUtil.getStuSeq(context);
            }
        }else{
            memberSeq = PreferenceUtil.getUserSeq(context);
        }

//        String memberSeqStr = map.containsKey("memberSeq")? map.get("memberSeq") : "";
//        try{
//            memberSeq = Integer.parseInt(memberSeqStr);
//        }catch(Exception ex){}
        int connSeq = -1;
        String connSeqStr = map.containsKey("connSeq")? map.get("connSeq") : "";
        try{
            connSeq = Integer.parseInt(connSeqStr);
        }catch(Exception ex){}
        String pushType = map.containsKey("pushType")? map.get("pushType") : "";
        String pushId = map.containsKey("pushId")? map.get("pushId") : "";
        return new PushMessage(id, title, content, acaCode, date, pushType, memberSeq, connSeq, pushId, false);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    public void readFromParcel(Parcel in) {
        id = in.readLong();
        title = in.readString();
        body = in.readString();
        acaCode = in.readString();
        date = LocalDateTime.parse(in.readString(), dateTimeFormatter);
        pushType = in.readString();
        memberSeq = in.readInt();
        connSeq = in.readInt();
        pushId = in.readString();
        isRead = in.readByte() != 0;
    }
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(acaCode);
        parcel.writeString(date.format(dateTimeFormatter));
        parcel.writeString(pushType);
        parcel.writeInt(memberSeq);
        parcel.writeInt(connSeq);
        parcel.writeString(pushId);
        parcel.writeByte((byte) (isRead ? 1 : 0));
    }
}
