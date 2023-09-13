package kr.jeet.edu.student.model.request;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class LevelTestRequest implements Parcelable {
    @SerializedName("memberSeq")
    public int memberSeq;

    @SerializedName("name")
    public String name;

    @SerializedName("birth")
    public String birth;

    @SerializedName("sex")
    public int sex;

    @SerializedName("address")
    public String address;

    @SerializedName("addressSub")
    public String addressSub;

    @SerializedName("scCode")
    public int scCode;

    @SerializedName("grade")
    public String grade;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("parentPhoneNumber")
    public String parentPhoneNumber;

    @SerializedName("parentName")
    public String parentName;

    @SerializedName("reason")
    public String reason;

    @SerializedName("reservationDate")
    public String reservationDate;

    @SerializedName("bigo")
    public String bigo;

    @SerializedName("bigoText")
    public String bigoText;

    @SerializedName("cashReceiptNumber")
    public String cashReceiptNumber;

    @SerializedName("registerDate")
    public String registerDate;

    @SerializedName("time1")
    public String time1;

    @SerializedName("time2")
    public String time2;

    @SerializedName("time3")
    public String time3;

    @SerializedName("time4")
    public String time4;

    @SerializedName("date1")
    public String date1;

    @SerializedName("date2")
    public String date2;

    @SerializedName("date3")
    public String date3;

    @SerializedName("date4")
    public String date4;

    @SerializedName("process1")
    public int process1;

    @SerializedName("processEtc1")
    public String processEtc1;

    @SerializedName("processText1")
    public String processText1;

    @SerializedName("process2")
    public int process2;

    @SerializedName("processEtc2")
    public String processEtc2;

    @SerializedName("processText2")
    public String processText2;

    @SerializedName("process3")
    public int process3;

    @SerializedName("processEtc3")
    public String processEtc3;

    @SerializedName("processText3")
    public String processText3;

    @SerializedName("wish")
    public String wish;

    @SerializedName("study")
    public String study;

    @SerializedName("highSchool")
    public String highSchool;

    @SerializedName("gifted")
    public String gifted;

    @SerializedName("etc")
    public String etc;

    @SerializedName("check1")
    public String check1;

    @SerializedName("check2")
    public String check2;

    @SerializedName("check3")
    public String check3;

    @SerializedName("check4")
    public String check4;

    public LevelTestRequest() {
    }

    protected LevelTestRequest(Parcel in) {
        memberSeq = in.readInt();
        name = in.readString();
        birth = in.readString();
        sex = in.readInt();
        address = in.readString();
        addressSub = in.readString();
        scCode = in.readInt();
        grade = in.readString();
        phoneNumber = in.readString();
        parentPhoneNumber = in.readString();
        parentName = in.readString();
        reason = in.readString();
        reservationDate = in.readString();
        bigo = in.readString();
        bigoText = in.readString();
        cashReceiptNumber = in.readString();
        registerDate = in.readString();
        time1 = in.readString();
        time2 = in.readString();
        time3 = in.readString();
        time4 = in.readString();
        date1 = in.readString();
        date2 = in.readString();
        date3 = in.readString();
        date4 = in.readString();
        process1 = in.readInt();
        processEtc1 = in.readString();
        processText1 = in.readString();
        process2 = in.readInt();
        processEtc2 = in.readString();
        processText2 = in.readString();
        process3 = in.readInt();
        processEtc3 = in.readString();
        processText3 = in.readString();
        wish = in.readString();
        study = in.readString();
        highSchool = in.readString();
        gifted = in.readString();
        etc = in.readString();
        check1 = in.readString();
        check2 = in.readString();
        check3 = in.readString();
        check4 = in.readString();
    }

    public static final Creator<LevelTestRequest> CREATOR = new Creator<LevelTestRequest>() {
        @Override
        public LevelTestRequest createFromParcel(Parcel in) {
            return new LevelTestRequest(in);
        }

        @Override
        public LevelTestRequest[] newArray(int size) {
            return new LevelTestRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(memberSeq);
        dest.writeString(name);
        dest.writeString(birth);
        dest.writeInt(sex);
        dest.writeString(address);
        dest.writeString(addressSub);
        dest.writeInt(scCode);
        dest.writeString(grade);
        dest.writeString(phoneNumber);
        dest.writeString(parentPhoneNumber);
        dest.writeString(parentName);
        dest.writeString(reason);
        dest.writeString(reservationDate);
        dest.writeString(bigo);
        dest.writeString(bigoText);
        dest.writeString(cashReceiptNumber);
        dest.writeString(registerDate);
        dest.writeString(time1);
        dest.writeString(time2);
        dest.writeString(time3);
        dest.writeString(time4);
        dest.writeString(date1);
        dest.writeString(date2);
        dest.writeString(date3);
        dest.writeString(date4);
        dest.writeInt(process1);
        dest.writeString(processEtc1);
        dest.writeString(processText1);
        dest.writeInt(process2);
        dest.writeString(processEtc2);
        dest.writeString(processText2);
        dest.writeInt(process3);
        dest.writeString(processEtc3);
        dest.writeString(processText3);
        dest.writeString(wish);
        dest.writeString(study);
        dest.writeString(highSchool);
        dest.writeString(gifted);
        dest.writeString(etc);
        dest.writeString(check1);
        dest.writeString(check2);
        dest.writeString(check3);
        dest.writeString(check4);
    }
}
