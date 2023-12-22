package kr.jeet.edu.student.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import kr.jeet.edu.student.utils.Converters;

@Database(
        entities = {
                PushMessage.class
                , NewBoardData.class
        }
        , version = 4
        , exportSchema = true
        , autoMigrations = {
            @AutoMigration(from = 1, to = 2),
            @AutoMigration(from = 2, to = 3),
            @AutoMigration(from = 3, to = 4)
        }
        )
@TypeConverters({Converters.class})
public abstract class JeetDatabase extends RoomDatabase {
    private static JeetDatabase JeetDBInstance = null;
    public abstract PushMessageDao pushMessageDao();
    public abstract NewBoardDao newBoardDao();

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
