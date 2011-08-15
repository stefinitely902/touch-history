package com.savinov3696.phone.log;

import java.security.Provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.util.Log;
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------
/**
<a>тип действи€</a>
Phone Calls:
1 incoming
2 Outgoing 
3 missed
SMS
11 incoming
12 Outgoing
13 черновик
MMS
21 incoming
32 Outgoing
23 черновик


*/
public class ActLogTableHelper extends SQLiteOpenHelper 
{
	//индексы столбцов
	public static final int _ID			= 0;
	public static final int _ftype		= 1;
	public static final int _fseen		= 2;
	public static final int _faccount	= 3;
	public static final int _fname		= 4;
	public static final int _fname_id	= 5;
	public static final int _fdate		= 6;
	public static final int _ftheme		= 7;
	public static final int _fdata		= 8;
 
	
	public static final int GSM_CALL_INCOMING    = 1;
	public static final int GSM_CALL_OUTGOING    = 2;
	public static final int GSM_CALL_MISSED      = 3;
	
	public static final int MESSAGE_TYPE_ALL    = 10;
    public static final int MESSAGE_TYPE_INBOX  = 11;
    public static final int MESSAGE_TYPE_SENT   = 12;
    public static final int MESSAGE_TYPE_DRAFT  = 13;
    public static final int MESSAGE_TYPE_OUTBOX = 14;
    public static final int MESSAGE_TYPE_FAILED = 15; // for failed outgoing messages
    public static final int MESSAGE_TYPE_QUEUED = 16; // for messages to send later

	
	
	private static final String m_TAG = "PhonedroidProvider";

    public static final String m_DBName = "phonedroid.db";
    public static final int m_DBVersion = 1;
    
    private Context m_Context;
    
    
	ActLogTableHelper(Context context, String db_name, CursorFactory factory,int db_version) 
	{
		super(context, db_name, factory, db_version);
		m_Context=context;
	}

    @Override 
    public void onCreate(SQLiteDatabase db) 
    {
        db.execSQL("create table if not exists ActLog (	" +
        				"_ID INTEGER 	PRIMARY KEY,"+			// пор€дковый номер записи
        				"ftype 			INTEGER, "+				// тип действи€
        				"fseen 			BOOL, "+				// просмотрено ли
        				"faccount 		TEXT, "+ 				// номер телефона,почта,ICQ и т.д.
        				"fname 			TEXT,"+
        				"fname_id 		INTEGER, "+
        				"fdate  		LONG,"+ //"fdate  		TIMESTAMP,"+
        				"ftheme			TEXT,"+
        				"fdata			TEXT"+
        				 ");");
        
        CopyFromCallLogProvider(m_Context,db);
        CopyFromSMSLogProvider(m_Context,db);
    }// public void onCreate(SQLiteDatabase db)


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(m_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data and copy all from system");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
        
    } 		
//---------------------------------------------------------------------------------------
    protected void CopyFromCallLogProvider(Context context,SQLiteDatabase db)
    {
    	if( context!=null)
    	{
    		final ContentResolver resolver = m_Context.getContentResolver();
    		if(resolver!=null)
    		{
    			//Uri allCalls = Uri.parse(Уcontent://call_log/callsФ);
    			Cursor cursor = resolver.query(Calls.CONTENT_URI, null, null, null,Calls.DATE + " DESC " );
    			if (cursor != null)
    	        {
    	        	int _type = cursor.getColumnIndex(Calls.TYPE);
    	        	int _account = cursor.getColumnIndex(Calls.NUMBER);
    	        	int _name = cursor.getColumnIndex(Calls.CACHED_NAME);
    	        	int _date = cursor.getColumnIndex(Calls.DATE);
    	        	int _duration = cursor.getColumnIndex(Calls.DURATION);

    	        	cursor.moveToFirst();
    	        	ContentValues values = new ContentValues();

    	        	while(!cursor.isAfterLast() )
    	        	{
    	        		values.put("ftype", cursor.getInt(_type));
    	        		values.put("faccount", cursor.getString(_account));
    	        		values.put("fname", cursor.getString(_name));
    	        		values.put("fdate",cursor.getLong(_date) );
    	        		values.put("fdata",cursor.getLong(_duration)  );
    	        		
    	        		db.insert("ActLog",null, values);
    	        		values.clear();
    	        		
    	        		cursor.moveToNext();
    	        	}//while(RowCount--)
    	        }//if (cursor != null)		
    		}//if(resolver!=null)
    	}//if( m_Context!=null)
    	
    }// protected void CopyFromCallLogProvider()
	 
//---------------------------------------------------------------------------------------
    protected void CopyFromSMSLogProvider(Context context,SQLiteDatabase db)
	{
    	if( context!=null)
    	{
    		final ContentResolver resolver = m_Context.getContentResolver();
    		if(resolver!=null)
    		{
    			Uri uriSMSURI = Uri.parse("content://sms");
    			Cursor cursor = resolver.query(uriSMSURI, null, null, null,Calls.DATE + " DESC " );
    			if (cursor != null)
    	        {
    	        	int _id = cursor.getColumnIndex("_id");
    	        	int _thread_id = cursor.getColumnIndex("thread_id");
    	        	int _address = cursor.getColumnIndex("address");
    	        	int _person = cursor.getColumnIndex("person");
    	        	int _date = cursor.getColumnIndex("date");

    	        	int _protocol = cursor.getColumnIndex("protocol");
    	        	int _read = cursor.getColumnIndex("read");
    	        	int _status = cursor.getColumnIndex("status");
    	        	int _type = cursor.getColumnIndex("type");
    	        	int _reply_path_present = cursor.getColumnIndex("reply_path_present");

    	        	int _subject = cursor.getColumnIndex("subject");
    	        	int _body = cursor.getColumnIndex("body");
    	        	int _service_center = cursor.getColumnIndex("service_center");        	
    	        	int _locked = cursor.getColumnIndex("locked");
    	        	int _seen = cursor.getColumnIndex("seen");        	        	

    	        	cursor.moveToFirst();
    	        	ContentValues values = new ContentValues();

    	        	while(!cursor.isAfterLast() )
    	        	{
    	        		/* 
    	        		Log.d("DEBUG", "\n id="+cursor.getString(_id)+"\t _thread_id="+cursor.getString(_thread_id)+
    	        				 		"\t _address="+cursor.getString(_address)+
    	        				 		"\t _person="+cursor.getString(_person)+
    	        				 		"\t _date="+cursor.getString(_date)+
    	        				 		
    	        				 		"\t _protocol="+cursor.getString(_protocol)+
//    	        				 		"\n _read="+cursor.getString(_read)+
//    	        				 		"\n _status="+cursor.getString(_status)+
//    	        				 		"\n _type="+cursor.getString(_type)+
    	        				 		"\t _reply_path_present="+cursor.getString(_reply_path_present)+

//    	        				 		"\n _subject="+cursor.getString(_subject)+
    	        				 		"\t _body="+cursor.getString(_body)+
    	        				 		"\t _service_center="+cursor.getString(_service_center)
//    	        				 		+"\n _locked="+cursor.getString(_locked)+
//    	        				 		+"\n _seen="+cursor.getString(_seen)
    	        				 		);
    	        		
    	        		*/
    	        		
    	        		
    	        		
    	        		values.put("ftype", cursor.getInt(_type)+10 ); // Sms based +10
    	        		values.put("fseen", cursor.getLong(_seen));
    	        		values.put("faccount", cursor.getString(_address));
    	        		
    	        		String v_number=cursor.getString(_address);
    	        		String v_personID=cursor.getString(_person);
    	        		
    	        		
    	        		//String str_fname = FindContact(cursor.getLong(_person),v_number,context);
       	        		//values.put("fname", str_fname );

    	        		
    	        		values.put("fname_id", cursor.getString(_person));
    	        		values.put("fdate", cursor.getLong(_date));
    	        		values.put("ftheme",cursor.getString(_date));
    	        		values.put("fdata", cursor.getString(_body));

    	        		db.insert("ActLog",null, values);
    	        		values.clear();
        	
    	        		cursor.moveToNext();
    	        	}//while(RowCount--)
    	        }//if (cursor != null)		
    		}//if(resolver!=null)
    	}//if( m_Context!=null)
    }//protected void CopyFromSMSLogProvider()
    
    static public String FindContact(long id,String account,Context context)
    {
       	if( context!=null)
    	{
    		final ContentResolver resolver = context.getContentResolver();
    		if(resolver!=null)
    		{
    			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(account));
    		    String[] PROJECTION = new String[] {	PhoneLookup._ID,
    		    										PhoneLookup.DISPLAY_NAME    };
    		    //String SELECTION = Contacts._ID + "='"+id+"'";
    		    Cursor cur=resolver.query(uri, PROJECTION,null,null,null);
    		    
    		   
    		    int qty=cur.getCount();
    		    if(cur.getCount()>0)
    		    {
    		    	cur.moveToNext();
    		    	int col_name=cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);
    	        	return cur.getString(col_name);
    		    }
    		    
    	            	
    		}//if(resolver!=null)
    	}//if( m_Context!=null)
       	return null;
    }
    
    

}// public class ActLogTableHelper
//---------------------------------------------------------------------------------------