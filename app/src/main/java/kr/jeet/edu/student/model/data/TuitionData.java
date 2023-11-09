package kr.jeet.edu.student.model.data;

import java.text.ParseException;
import java.util.Objects;

import kr.jeet.edu.student.activity.MenuStudentInfoActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.utils.LogMgr;

public class TuitionData implements Constants.PayListItem {
    public String acaName;    // 캠퍼스명
    public String clsName;    // 학급명
    public String payment;    // 금액
    public String accountNO;  // 가상 계좌번호
    public String gubun;      // 수강료 구분
    public String payDate;    // 수강 날짜

    @Override
    public boolean isHeader() { return false; }

    @Override
    public Constants.PayType getPay() { return Constants.PayType.getByName(gubun); }

    @Override
    public int compareTo(Constants.PayListItem item) {
        int gubunComparison = 0;
        Constants.PayType type = Constants.PayType.getByName(gubun);
        Constants.PayType itemType = null;
        if (item instanceof  TuitionHeaderData){
            itemType = Constants.PayType.getByName(((TuitionHeaderData)item).gubun);

        }else if (item instanceof TuitionData){
            itemType = Constants.PayType.getByName(((TuitionData)item).gubun);

        }

        if (type != null && itemType != null){
            gubunComparison = type.getCode() - itemType.getCode();
        }

        if (gubunComparison != 0){
            return gubunComparison;
        }else{

            if (item instanceof TuitionData){
                TuitionData data = (TuitionData) item;
                int accountNoComparison = this.acaName.compareTo(data.acaName);
                if (accountNoComparison != 0){
                    LogMgr.e("compare7", "return weekendComparison " + accountNoComparison);
                    return accountNoComparison;
                }else{
                    if (!item.isHeader()) {
                        LogMgr.e("compare8", "return weekendComparison " + this.payDate.compareTo(data.payDate));
                        return this.payDate.compareTo(data.payDate);
                    }else{
                        return 0;
                    }
                }
            }else{
                TuitionHeaderData data = (TuitionHeaderData) item;
                int accountNoComparison = this.acaName.compareTo(data.acaName);
                if (accountNoComparison != 0){
                    LogMgr.e("compare7", "return weekendComparison " + accountNoComparison);
                    return accountNoComparison;
                }else{
                    if (item.isHeader()) {
                        LogMgr.e("compare10", "return weekendComparison ");
                        return 1;

                    }else{
                        return 0;
                    }

                    //return 0;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TuitionData that = (TuitionData) o;
        return Objects.equals(acaName, that.acaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acaName);
    }
}
