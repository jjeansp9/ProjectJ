package kr.jeet.edu.student.db;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Map;

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
    public String date;
    @ColumnInfo(name="pushType")
    public String pushType;
    @ColumnInfo(name="connSeq")
    public int connSeq;
    @ColumnInfo(name="pushId")
    public String pushId;
    @ColumnInfo(name="isRead", defaultValue = "false")
    public boolean isRead = false;
    public PushMessage(long id, String title, String body, String acaCode, String date, String pushType,int connSeq, String pushId, boolean isRead) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.acaCode = acaCode;
        this.date = date;
        this.pushType = pushType;
        this.connSeq = connSeq;
        this.pushId = pushId;
        this.isRead = isRead;
    }

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

    public static PushMessage buildFromMap(Map<String, String> map) {
        long id = 0L;
        String title = map.containsKey("title")? map.get("title") : "";
        String content = map.containsKey("body")? map.get("body") : "";
        String acaCode = map.containsKey("acaCode")? map.get("acaCode") : "";
        String date = map.containsKey("date")? map.get("date") : "";
        int connSeq = -1;
        String connSeqStr = map.containsKey("connSeq")? map.get("connSeq") : "";
        try{
            connSeq = Integer.parseInt(connSeqStr);
        }catch(Exception ex){}
        String pushType = map.containsKey("pushType")? map.get("pushType") : "";
        String pushId = map.containsKey("pushId")? map.get("pushId") : "";
        return new PushMessage(id, title, content, acaCode, date, pushType, connSeq, pushId, false);
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
        date = in.readString();
        pushType = in.readString();
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
        parcel.writeString(date);
        parcel.writeString(pushType);
        parcel.writeInt(connSeq);
        parcel.writeString(pushId);
        parcel.writeByte((byte) (isRead ? 1 : 0));
    }
}
