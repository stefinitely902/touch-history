package com.savinov3696.phone.log;

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
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
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
        
        long startTime = System.currentTimeMillis();
        CopyFromCallLogProvider(m_Context,db);
    	long elapsedTime = System.currentTimeMillis() - startTime;
    	Log.d("myinfo", "CopyFromCallLogProvider time="+elapsedTime);

    	startTime = System.currentTimeMillis();
        CopyFromSMSLogProvider(m_Context,db);
        elapsedTime = System.currentTimeMillis() - startTime;
        Log.d("myinfo", "CopyFromSMSLogProvider time="+elapsedTime);
     

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
    public static final class TempContact
    {
    	String m_ContactName;
    	long   m_ContactID;
    	
    	TempContact(String name,long id)
    	{
    		m_ContactName=name;
    		m_ContactID=id;
    	}//TempContact
    }//private static final class TempContact
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
    	        	int _number = cursor.getColumnIndex(Calls.NUMBER);
    	        	int _name = cursor.getColumnIndex(Calls.CACHED_NAME);
    	        	int _date = cursor.getColumnIndex(Calls.DATE);
    	        	int _duration = cursor.getColumnIndex(Calls.DURATION);

    	        	cursor.moveToFirst();
    	        	ContentValues values = new ContentValues();

    	        	String ContactAddress=null;
    	        	TempContact tmp=null;
    	        	String strFIO=null;
    	        	
    	        	while(!cursor.isAfterLast() )
    	        	{
    	        		ContactAddress=cursor.getString(_number);
    	        		tmp = GetTempContactIDByNumber(ContactAddress,resolver);
    	        		if(tmp!=null)
    	        		{
    	        			values.put("fname_id", tmp.m_ContactID);
    	        			
    	        			strFIO = GetContactNameByID(tmp.m_ContactID,resolver);
    	        			if(strFIO!=null)
    	        				values.put("fname", strFIO );
    	        			else
    	        				values.put("fname", cursor.getString(_name) );
    	        			
    	        		}
    	        		
    	        		values.put("faccount", ContactAddress);
    	        		
    	        		
    	        		values.put("ftype", cursor.getInt(_type));
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
    	        	//int _id = cursor.getColumnIndex("_id");
    	        	//int _thread_id = cursor.getColumnIndex("thread_id");
    	        	int _address = cursor.getColumnIndex("address");
    	        	int _person = cursor.getColumnIndex("person");
    	        	int _date = cursor.getColumnIndex("date");

    	        	//int _protocol = cursor.getColumnIndex("protocol");
    	        	//int _read = cursor.getColumnIndex("read");
    	        	//int _status = cursor.getColumnIndex("status");
    	        	int _type = cursor.getColumnIndex("type");
    	        	//int _reply_path_present = cursor.getColumnIndex("reply_path_present");

    	        	int _subject = cursor.getColumnIndex("subject");
    	        	int _body = cursor.getColumnIndex("body");
    	        	//int _service_center = cursor.getColumnIndex("service_center");        	
    	        	//int _locked = cursor.getColumnIndex("locked");
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
    	        		
    	        		String ContactAddress=cursor.getString(_address);//cursor.getLong(_person); не всегда выдаЄт правильные значени€ контакта :(
    	        		
    	        		TempContact tmp = GetTempContactIDByNumber(ContactAddress,resolver);
    	        		if(tmp!=null)
    	        		{
    	        			values.put("fname_id", tmp.m_ContactID);
    	        			
    	        			String str_fname = GetContactNameByID(tmp.m_ContactID,resolver);
        	        		if(str_fname!=null)
        	        			values.put("fname", str_fname );
        	        		else
        	        			values.put("fname", tmp.m_ContactName );
    	        		}
    	        		
    	        		values.put("faccount", ContactAddress);
    	        		
    	        		
    	        		values.put("fdate", cursor.getLong(_date));
    	        		values.put("ftheme",cursor.getString(_subject));
    	        		values.put("fdata", cursor.getString(_body));

    	        		db.insert("ActLog",null, values);
    	        		values.clear();
        	
    	        		cursor.moveToNext();
    	        	}//while(RowCount--)
    	        }//if (cursor != null)		
    		}//if(resolver!=null)
    	}//if( m_Context!=null)
    }//protected void CopyFromSMSLogProvider()
    
//---------------------------------------------------------------------------------------    
    final static public TempContact GetTempContactIDByNumber(String account,ContentResolver resolver)
    {
   		if(resolver!=null)
   		{
   			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(account));
   			final String[] PROJECTION = new String[] {	PhoneLookup._ID,
   		    										PhoneLookup.DISPLAY_NAME    };
   		    //String SELECTION = Contacts._ID + "='"+id+"'";
   		    Cursor cur=resolver.query(uri, PROJECTION,null,null,null);
   		    if(cur.moveToFirst())
   		    {
   		    	cur.moveToFirst();
   		    	int col_name=cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);
   		    	int col_id=cur.getColumnIndex(PhoneLookup._ID);
   		    	
   		    	return new TempContact(cur.getString(col_name),cur.getLong(col_id));
    		}//if(cur.moveToFirst())
  		}//if(resolver!=null)
   		return null;   	
    }//static private TempContact GetTempContactIDByNumber(String account,ContentResolver resolver)
//---------------------------------------------------------------------------------------    
    final static public long GetContactIDByNumber(String account,ContentResolver resolver)
    {
    	final TempContact tmp=GetTempContactIDByNumber(account,resolver);
    	if(tmp!=null)
    		return tmp.m_ContactID;
   		return 0;
    }
//---------------------------------------------------------------------------------------    
    
    final static public String GetContactNameByID(long contact_id,ContentResolver resolver)
    {
    	if(resolver!=null)
    	{
    		final Uri uri = Data.CONTENT_URI;
    		final String[] columns = {	/*Data._ID,*/ 
    								//Data.CONTACT_ID, Data.LOOKUP_KEY,
    								ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
    								ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
    								ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
    								ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME   	};
    		final String   where=Data.CONTACT_ID + "=?" + " AND "+ Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'";
    		final String[] where_arg = {String.valueOf(contact_id)
    							};
    			
    		Cursor cur = resolver.query(uri,columns,where,where_arg,null);
    		if(cur.moveToFirst())
    		{
       			String result="";
       			final String family_name=cur.getString(cur.getColumnIndex(StructuredName.FAMILY_NAME));
       			if(family_name!=null)
       				result+=family_name;
       			final String given_name=cur.getString(cur.getColumnIndex(StructuredName.GIVEN_NAME));
       			if(given_name!=null)
       				result+=" "+given_name;
       			final String middle_name=cur.getString(cur.getColumnIndex(StructuredName.MIDDLE_NAME));
       			if(middle_name!=null)
       				result+=" "+middle_name;
       			//return String.format("%s %s %s",family_name,given_name,middle_name);
       			return result;
		   		//String cid=cur.getString(cur.getColumnIndex(Data.CONTACT_ID));
		   		//String lookup_name=cur.getString(cur.getColumnIndex(Data.LOOKUP_KEY));
		   		//Log.d("myinfo", "CONTACT_ID="+cid+"\tLOOKUP_KEY="+lookup_name+"\t"+family_name+" "+given_name);

    		}//if(cur.moveToFirst())
    	}//if(resolver!=null)
       	return null;
    }//static public String GetContactNameByID(long id,Context context)
    
    
    

}// public class ActLogTableHelper
//---------------------------------------------------------------------------------------