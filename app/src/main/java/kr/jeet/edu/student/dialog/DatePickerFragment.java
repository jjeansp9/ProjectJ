package kr.jeet.edu.student.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.utils.LogMgr;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private OnDateSetListener listener;
    private int year = 0;
    private int month = 0;
    private int dateOfMonth = 0;
    private boolean setDate = false;
    private boolean isBirth = false;
    private int minYear = 0;
    private int maxYear = 0;
    private int maxMonth = 0;
    private int maxDay = 0;
    private final int SET_MIN = Calendar.getInstance().get(Calendar.MONTH);
    private final int SET_DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    public DatePickerFragment(OnDateSetListener listener, boolean setDate, int minYear, int maxYear) {
        this.listener = listener;
        this.setDate = setDate;
        this.minYear = minYear;
        this.maxYear = maxYear;
    }
    public DatePickerFragment(OnDateSetListener listener, boolean setDate, int minYear, int maxYear, boolean isBirth) {
        this.listener = listener;
        this.setDate = setDate;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.isBirth = isBirth;
    }

    public DatePickerFragment(OnDateSetListener listener, boolean setDate, int minYear, int maxYear, int maxMonth, int maxDay, boolean isBirth) {
        this.listener = listener;
        this.setDate = setDate;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.maxMonth = maxMonth;
        this.maxDay = maxDay;
        this.isBirth = isBirth;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Holo_Light_Dialog);

        DatePickerDialog dialog;

        if (isBirth) dialog = new DatePickerDialog(builder.getContext(), this, year, month, day);
        else dialog = new DatePickerDialog(requireContext(), this, year, month, day);

        //최소날짜
        Calendar minDate = Calendar.getInstance();
        if (setDate) minDate.set(minYear, SET_MIN, SET_DAY);
        else minDate.set(minYear, 1, 1);
        dialog.getDatePicker().setMinDate(minDate.getTime().getTime());

        //최대날짜
        Calendar maxDate = Calendar.getInstance();
        if (isBirth) maxDate.set(maxYear, maxMonth, maxDay);
        else maxDate.set(maxYear, 11, 31);
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
