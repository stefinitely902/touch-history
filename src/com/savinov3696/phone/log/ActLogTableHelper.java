package com.savinov3696.phone.log;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.CursorJoiner.Result;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
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
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_faccount ON ActLog (faccount DESC); ");
        
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
        db.execSQL("DROP TABLE IF EXISTS ActLog");
        db.execSQL("DROP INDEX IF EXISTS idx_faccount");
        onCreate(db);
        
    } 		
//---------------------------------------------------------------------------------------
    public static final class TempContact
    {
    	String m_Name;
    	long   m_ID;
    	String m_NameAlt;
    	String m_Number;
    	
    	TempContact(String name,long id)
    	{
    		m_Name=name;
    		m_ID=id;
    		m_NameAlt=null;
    	}//TempContact
    	
    	TempContact(String name,long id,String name_alt,String number)
    	{
    		m_Name=name;
    		m_ID=id;
    		m_NameAlt=name_alt;
    		m_Number=number;
    	}//TempContact    	
    	
    	final String TryGetAltName() 
    	{
			if(m_NameAlt !=null)
				return m_NameAlt;
			return m_Name;
    	}
    	
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

    	        	
    	        	
    	        	while(!cursor.isAfterLast() )
    	        	{
    	        		values.put("fname", cursor.getString(_name) );
    	        		values.put("faccount", cursor.getString(_number));
    	        		
    	        		
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
    	        		values.put("ftype", cursor.getInt(_type)+10 ); // Sms based +10
    	        		values.put("fseen", cursor.getLong(_seen));
    	        		
    	        		values.put("faccount", cursor.getString(_address));
    	        		
    	        		
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
    final static public long GetContactIDByNumber(String account,ContentResolver resolver)
    {
    	final TempContact tmp=GetTempContactIDByNumber(account,resolver);
    	if(tmp!=null)
    		return tmp.m_ID;
   		return 0;
    }    
//---------------------------------------------------------------------------------------    
    final static public TempContact GetTempContactIDByNumber(String account,ContentResolver resolver)
    {
   		if(resolver!=null)
   		{
   			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(account));
   			final String[] PROJECTION = new String[] {	PhoneLookup.DISPLAY_NAME	
   														,PhoneLookup._ID
   														//,PhoneLookup.LOOKUP_KEY
   														};
   		    //String SELECTION = Contacts._ID + "='"+id+"'";
   		    Cursor cur=resolver.query(uri, PROJECTION,null,null,null);
   			//Cursor cur=resolver.query(uri, null,null,null,null);
   		    if(cur.moveToFirst())
   		    {
   		    	/*
   		    	String[] values=new String [cur.getColumnCount()];
   		    	for(int k=0;k<cur.getColumnCount();++k)
   		    		values[k]=cur.getColumnName(k)+" =  "+cur.getString(k);
   		    	*/
   		    	return new TempContact(cur.getString(0),cur.getLong(1));
    		}//if(cur.moveToFirst())
  		}//if(resolver!=null)
   		return null;   	
    }//static private TempContact GetTempContactIDByNumber(String account,ContentResolver resolver)
//---------------------------------------------------------------------------------------    
    
    final static public String GetContactNameByID(long contact_id,ContentResolver resolver)
    {
    	if(resolver!=null)
    	{
    		final Uri uri = Data.CONTENT_URI;

    		final String[] columns = {	
    									ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
    									ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
    									ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME
    									//,ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
    									/*Data._ID,*/ 
        								//Data.CONTACT_ID, 
        								//Data.LOOKUP_KEY
    									};
    		final String   where=Data.CONTACT_ID + "=?" + " AND "+ Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'";
    		final String[] where_arg = {String.valueOf(contact_id)	};
    			
    		Cursor cur = resolver.query(uri,columns,where,where_arg,null);
    		if(cur.moveToFirst())
    		{
       			String result="";
       			final String family_name=cur.getString(0);
       			if(family_name!=null)
       				result+=family_name;
       			final String given_name=cur.getString(1);
       			if(given_name!=null)
       				result+=" "+given_name;
       			final String middle_name=cur.getString(2);
       			if(middle_name!=null)
       				result+=" "+middle_name;
       			//return String.format("%s %s %s",family_name,given_name,middle_name);
       			return result;
    		}//if(cur.moveToFirst())
    	}//if(resolver!=null)
       	return null;
    }//static public String GetContactNameByID(long id,Context context)
    
  //---------------------------------------------------------------------------------------    
    final static public TempContact[] GetTempContactNames(String[] account,ContentResolver resolver)
    {
    	final long startTime = System.currentTimeMillis();
    	
    	if(account.length>0 && resolver!=null)
   		{
   			Uri id_uri = Data.CONTENT_URI;
   			
   			final String[] columns = new String[]{ 	Data.CONTACT_ID
   													,Phone.NUMBER
   													,ContactsContract.Contacts.DISPLAY_NAME
   													,"display_name_alt" };// {	Phone.CONTACT_ID };
   			
   			String   where=Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND (";
   			final String[] where_arg=new String[account.length];
   			for(int i=0;i<account.length;++i)
    		{
   				where+=Phone.NUMBER+ "=?";
   				if(i<(account.length-1))
   					where+=" OR ";
   				where_arg[i]=String.valueOf(account[i]);
    		}
   			where+=" )";
   			
   			
    		Cursor cur = resolver.query(id_uri,columns,where,where_arg,null);
    		TempContact[] tmp=new TempContact[cur.getCount()];
   		    int i=0;

   		    while(cur.moveToNext())
   		    {
   		    	/*
   		    	String[] values=new String [cur.getColumnCount()];
   		    	for(int k=0;k<cur.getColumnCount();++k)
   		    		values[k]=cur.getColumnName(k)+" =  "+cur.getString(k);
   		    	*/
   		    	tmp[i++]= new TempContact(cur.getString(2),cur.getLong(0),cur.getString(3),cur.getString(1) );
    		}//if(cur.moveToNext())
   		    
        	
            long elapsedTime = System.currentTimeMillis() - startTime;
            Log.d("PAINT VIEW", "QUERY CONTACT NumCount=" + account.length+"\tContCount="+tmp.length+"\ttime="+elapsedTime);
            
   		    return tmp;
   		    
  		}//if(resolver!=null)
   		return null;   	
    }//static private TempContact GetTempContactIDByNumber(String account,ContentResolver resolver)
//---------------------------------------------------------------------------------------       


}// public class ActLogTableHelper
//---------------------------------------------------------------------------------------