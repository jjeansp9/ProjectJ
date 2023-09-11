package kr.jeet.edu.student.utils;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.lang.reflect.Type;

@ProvidedTypeConverter
public class Converters {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @TypeConverter
    public static LocalDateTime fromString(String value) {
        try{
            return value == null ? null : LocalDateTime.parse(value, dateFormatter);
        }catch (DateTimeParseException e){
            return null;
        }
    }

    @TypeConverter
    public static String fromDate(LocalDateTime date) {
        return date == null ? null : date.format(dateFormatter);
    }
}
