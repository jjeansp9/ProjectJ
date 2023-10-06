package kr.jeet.edu.student.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import kr.jeet.edu.student.utils.Converters;

@Database(entities = {PushMessage.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class JeetDatabase extends RoomDatabase {
    private static JeetDatabase JeetDBInstance = null;
    public abstract PushMessageDao pushMessageDao();

    public static JeetDatabase getInstance(Context context) {
        if(JeetDBInstance == null) {
            JeetDBInstance = Room.databaseBuilder(context.getApplicationContext(),
                            JeetDatabase.class,
                            "jeeteducation.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return JeetDBInstance;
    }

    public static void destroyInstance() {
        JeetDBInstance = null;
    }
}
