package liza.com.rateinstructor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.CursorAdapter;

/**
 * Created by SONY on 3/2/2015.
 */
public class DBAdapter {

    static final String TAG = "DBAdapter";
    static final String DATABASE_NAME = "InstructorsDB";
    static final String DATABASE_TABLE ="instructorProfile";
    static final String KEY_ROW_ID ="_id";
    static final int DATABASE_VERSION = 1;


    //SQL statement for creating the DATABASE_TABLE table within the DATABASE_NAME database.
    static final String DATABASE_CREATE =
        "create table "+ DATABASE_TABLE + " ("+KEY_ROW_ID +" integer primary key autoincrement, "
                + Instructor.KEY_FULL_NAME + " text not null, "
                + Instructor.KEY_FIRST_NAME+" text, "
                + Instructor.KEY_LAST_NAME+" text, "
                + Instructor.KEY_OFFICE+" text, "
                + Instructor.KEY_PHONE+" text, "
                + Instructor.KEY_EMAIL+" text, "
                + Instructor.KEY_AVG_RATING+" text, "
                + Instructor.KEY_TOTAL_RATING+" text, "
                + Instructor.KEY_COMMENT_LIST+" text);";

    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    //To create a database in application
    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    //extend the SQLiteOpenHelper class, which is a helper class in Android to manage database
    // creation and version management. In particular, override the onCreate() and onUpgrade()
    // methods:

    private static class DatabaseHelper extends SQLiteOpenHelper
    {

        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null,DATABASE_VERSION);
        }



        //creates a new database if the required database is not present
        @Override
        public void onCreate(SQLiteDatabase db){

            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //calls when the database needs to be upgraded
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.i(TAG, "Upgrading database from version " + oldVersion + "to "
                            + newVersion + ", which will destroy all old data");

            //drop the table and create it again
            db.execSQL("DROP TABLE IF EXISTS profiles");
            onCreate(db);
        }
    }
    //----------opens the database--------
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }

    //---insert a profile into the database---
    public long insertProfile(Instructor instructor)
    {
        ContentValues initialValues = new ContentValues();//object to store name/value pairs
        initialValues.put(Instructor.KEY_FULL_NAME,instructor.toString());
        return db.insert(DATABASE_TABLE, null, initialValues);
    }


    //------deletes a particular profile---
    public boolean deleteProfile(long rowId)
    {
        return db.delete(DATABASE_TABLE,KEY_ROW_ID + "=" + rowId, null) > 0;
    }
    //-----retrieves all the profiles---
    public Cursor getAllProfiles()
    {
        return db.query(DATABASE_TABLE, new String[] {KEY_ROW_ID, Instructor.KEY_FULL_NAME}
                , null, null, null, null, null);
    }

    //-----------retrieves a particular profile-------------
    public Cursor getProfile(long rowId) throws SQLException
    {
        //uses the Cursor class as a return value for queries
        //retrieves all columns for a given rowId
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, null, KEY_ROW_ID + "=" + rowId, null,
                                            null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //----------updates FullName of an instructor's Profile----------------
    public boolean updateFullName(Instructor instructor)
    {
        ContentValues args = new ContentValues();
        args.put(Instructor.KEY_FULL_NAME, instructor.toString());
        return db.update(DATABASE_TABLE, args, KEY_ROW_ID + "=" + instructor.get_id(), null) > 0;
    }

    //----------updates Basic Details of an instructor's Profile----------------
    public boolean updateBasicDetails(Instructor instructor)
    {
        ContentValues args = new ContentValues();
        args.put(Instructor.KEY_FIRST_NAME,instructor.getFirstName());
        args.put(Instructor.KEY_LAST_NAME,instructor.getLastName());
        args.put(Instructor.KEY_OFFICE,instructor.getOffice());
        args.put(Instructor.KEY_PHONE,instructor.getPhone());
        args.put(Instructor.KEY_EMAIL,instructor.getEmail());
        args.put(Instructor.KEY_AVG_RATING,instructor.getAverage());
        args.put(Instructor.KEY_TOTAL_RATING,instructor.getTotal());
        return db.update(DATABASE_TABLE, args,KEY_ROW_ID + "=" + instructor.get_id(), null) > 0;
    }
    //----------updates comments of an instructor's Profile----------------
    public boolean updateComments(Instructor instructor)
    {
        ContentValues args = new ContentValues();

        args.put(Instructor.KEY_COMMENT_LIST,instructor.getComments());
        return db.update(DATABASE_TABLE, args,KEY_ROW_ID + "=" + instructor.get_id(), null) > 0;
    }


}
