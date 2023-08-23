package kr.jeet.edu.student.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private OnDateSetListener listener;
    private int year = 0;
    private int month = 0;
    private int dateOfMonth = 0;
    private boolean setDate = false;
    private int minYear = 0;
    private int maxYear = 0;
    private final int SET_MIN = Calendar.getInstance().get(Calendar.MONTH);
    private final int SET_DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    public DatePickerFragment(OnDateSetListener listener, boolean setDate, int minYear, int maxYear) {
        this.listener = listener;
        this.setDate = setDate;
        this.minYear = minYear;
        this.maxYear = maxYear;
    }

    public interface OnDateSetListener {
        void onDateSet(int year, int month, int day);
    }

    public void setDate(int year, int month, int date) {
        this.year = year;
        this.month = month;
        this.dateOfMonth = date;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();

        int year = (this.year == 0) ? calendar.get(Calendar.YEAR) : this.year;
        int month = (this.month == 0) ? calendar.get(Calendar.MONTH) : this.month;
        int day = (this.dateOfMonth == 0) ? calendar.get(Calendar.DAY_OF_MONTH) : this.dateOfMonth;

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), this, year, month, day);
        //최소날짜
        Calendar minDate = Calendar.getInstance();
        if (setDate) minDate.set(minYear, SET_MIN, SET_DAY);
        else minDate.set(minYear, 1, 1);
        dialog.getDatePicker().setMinDate(minDate.getTime().getTime());
        //최대날짜
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(maxYear, 12, 31);
        dialog.getDatePicker().setMaxDate(maxDate.getTime().getTime());
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (listener != null) {
            listener.onDateSet(year, month, dayOfMonth);
        }
    }
}
