package com.savinov3696.phone.log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.CursorJoiner.Result;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Handler;
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
/**
 * объ€вл€ем следующие данные

1 TouchHistory - “абличка с данными о всех совершЄнных звонках и —ћ—ках
2 TouchRescent - “абличка эквивалентнас TouchHistory но с уникальностью по имени аккаунта, т.е. последние действи€ контакта
3 “риггер, который при изменении TouchRescent

*/

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
	
	public static final int _ftype		= 0;
	public static final int _fseen		= 1;
	public static final int _faccount	= 2;
	public static final int _fname		= 3;
	public static final int _fname_id	= 4;
	public static final int _fdate		= 5;
	public static final int _ftheme		= 6;
	public static final int _fdata		= 7;
	public static final int _ID			= 8;
	
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
    
    
    public static final int CALLS_TYPE_COL_IDX = 7;
    public static final int CALLS_NUMBER_COL_IDX = 6;
    public static final int CALLS_CACHED_NAME_COL_IDX = 5;
    public static final int CALLS_DATE_COL_IDX = 8;
    public static final int CALLS_DURATION_COL_IDX = 2;
	
	
	private static final String m_TAG = "PhonedroidProvider";

    public static final String m_DBName = "phonedroid.db";
    public static final int m_DBVersion = 1;
    
    private Context m_Context;
    private SQLiteDatabase m_db;
    
    

    
    
    
    
    
    
    
    
	ActLogTableHelper(Context context, String db_name, CursorFactory factory,int db_version) 
	{
		super(context, db_name, factory, db_version);
		m_Context=context;
		
		m_db=m_Context.openOrCreateDatabase(db_name,0 , factory);
		
	}

    @Override 
    public void onCreate(SQLiteDatabase db) 
    {
    	
    	
    	db.execSQL("create table if not exists ActLog (	" +
        				"ftype 			INTEGER, "+				// тип действи€
        				"fseen 			BOOL, "+				// просмотрено ли
        				"faccount 		TEXT, "+ 				// номер телефона,почта,ICQ и т.д.
        				"fname 			TEXT,"+
        				"fname_id 		INTEGER, "+
        				"fdate  		LONG,"+ //"fdate  		TIMESTAMP,"+
        				"ftheme			TEXT,"+
        				"fdata			TEXT,"+
        				"_ID INTEGER 	PRIMARY KEY"+			// пор€дковый номер записи
        				 ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_faccount ON ActLog (faccount DESC); ");
        
        
    	String query_make_TouchRescent=	"create table if not exists TouchRescent (	" +
										"ftype 			INTEGER, "+				// тип действи€
										"fseen 			BOOL, "+				// просмотрено ли
										"faccount 		TEXT	CONSTRAINT pk_account PRIMARY KEY , "+ 				// номер телефона,почта,ICQ и т.д.
										"fname 			TEXT,"+
										"fname_id 		INTEGER, "+
										"fdate  		LONG,"+ //"fdate  		TIMESTAMP,"+
										"ftheme			TEXT,"+
										"fdata			TEXT"+
										 ");";
    	
    	db.execSQL(query_make_TouchRescent);
    	
    	
    	String query_tr_bu_TouchRescent=	"CREATE TRIGGER IF NOT EXISTS tr_ai_ActLog AFTER INSERT ON ActLog "+
    										"BEGIN "+
    									    " SELECT "+
    							            " CASE "+
    							            "    WHEN NEW.fdate < "+
    							            "         (SELECT fdate FROM TouchRescent WHERE faccount=NEW.faccount) "+	
    							                    " THEN " +
    							                    "RAISE(IGNORE) "+ //, 'date is less than rescent'
    							            " END; "+
						            		"   INSERT OR REPLACE INTO TouchRescent"+
						            		"          (ftype,fseen,faccount,fdate,ftheme,fdata)"+
						            		"          VALUES"+
						            		"          (NEW.ftype,NEW.fseen,NEW.faccount,NEW.fdate,NEW.ftheme,NEW.fdata); "+
    										" END;";
    	db.execSQL(query_tr_bu_TouchRescent);
			//" ( datetime(NEW.fdate, 'unixepoch', 'localtime')" +
		//" < datetime((SELECT fdate FROM TouchRescent WHERE faccount=NEW.faccount), 'unixepoch', 'localtime') ) "+

//    	
//> SELECT datetime(fdate) FROM TouchRescent WHERE faccount=NEW.faccount   )
        
        long startTime = 0;
        long elapsedTime= 0;
        		
        		
    	startTime = System.currentTimeMillis();
        CopyFromSMSLogProvider(m_Context,db);
        elapsedTime = System.currentTimeMillis() - startTime;
        Log.d("myinfo", "CopyFromSMSLogProvider time="+elapsedTime);

        startTime = System.currentTimeMillis();
        CopyFromCallLogProvider(m_Context,db);
    	elapsedTime = System.currentTimeMillis() - startTime;
    	Log.d("myinfo", "CopyFromCallLogProvider time="+elapsedTime);

    	

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
    static final class ContactInfo {
        public long 	m_PersonId;
        public int 		m_AccountType;	// phone(subtype: mobile,home,work)=0, email, SIP  				
        public String	m_Account;	    // phonenumber,

        public String m_Name;
        public String m_NameAlt;
        
        ContactInfo()
        {
    		m_Name=null;
    		m_PersonId=0;
    		m_NameAlt=null;
    		m_Account=null; 
    		m_AccountType=0;
        }
    	
    	ContactInfo(String name,long id)
    	{
    		m_Name=name;
    		m_PersonId=id;
    		m_NameAlt=null;
    	}//ContactInfo
    	
    	ContactInfo(String name,long id,String name_alt, String number)
    	{
    		m_Name=name;
    		m_PersonId=id;
    		m_NameAlt=name_alt;
    		m_Account=number;
    		m_AccountType=0;
    	}//ContactInfo    	
    	
    	final String TryGetAltName() 
    	{
			if(m_NameAlt !=null)
				return m_NameAlt;
			return m_Name;
    	}
    	
    }//private static final class ContactInfo
  //---------------------------------------------------------------------------------------  
    public void CopySMSCursor(Cursor cursor)
    {
    	int _address = cursor.getColumnIndex("address");
    	int _date = cursor.getColumnIndex("date");
    	int _type = cursor.getColumnIndex("type");
    	int _subject = cursor.getColumnIndex("subject");
    	int _body = cursor.getColumnIndex("body");
    	int _seen = cursor.getColumnIndex("seen");
    	ContentValues values = new ContentValues();
		
    	values.put("ftype", cursor.getInt(_type)+10 ); // Sms based +10
		values.put("fseen", cursor.getLong(_seen));
		values.put("faccount", cursor.getString(_address));
		values.put("fdate", cursor.getLong(_date));
		values.put("ftheme",cursor.getString(_subject));
		values.put("fdata", cursor.getString(_body));
		m_db.insert("ActLog",null, values);
    }    
//---------------------------------------------------------------------------------------  
    public void CopyCallCursor(Cursor cursor)
    {
    	int _type = cursor.getColumnIndex(Calls.TYPE);
    	ContentValues values = new ContentValues();
    	
		values.put("fname", cursor.getString(CALLS_CACHED_NAME_COL_IDX) );
		values.put("faccount", cursor.getString(CALLS_NUMBER_COL_IDX));
		values.put("ftype", cursor.getInt(CALLS_TYPE_COL_IDX));
		values.put("fdate",cursor.getLong(CALLS_DATE_COL_IDX) );
		values.put("fdata",cursor.getLong(CALLS_DURATION_COL_IDX)  );
  		
		m_db.insert("ActLog",null, values);
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
    			//--------resolver.registerContentObserver(Calls.CONTENT_URI, true, new MyContentObserver(handler) );
    			
    			
    			Cursor cursor = resolver.query(Calls.CONTENT_URI, null, null, null,Calls.DATE + " DESC " );
    			if (cursor != null)
    	        {
    				
    				//int _type = cursor.getColumnIndex(Calls.TYPE);
    	        	//int _number = cursor.getColumnIndex(Calls.NUMBER);
    	        	//int _name = cursor.getColumnIndex(Calls.CACHED_NAME);
    	        	//int _date = cursor.getColumnIndex(Calls.DATE);
    	        	//int _duration = cursor.getColumnIndex(Calls.DURATION);
    				int _seen = cursor.getColumnIndex(Calls.NEW);

    	        	cursor.moveToFirst();
    	        	ContentValues values = new ContentValues();

    	        	String ContactAddress=null;

    	        	
    	        	
    	        	while(!cursor.isAfterLast() )
    	        	{
    	        		//values.put("fname", cursor.getString(_name) );
    	        		//values.put("faccount", cursor.getString(_number));
    	        		//values.put("ftype", cursor.getInt(_type));
    	        		//values.put("fdate",cursor.getLong(_date) );
    	        		//values.put("fdata",cursor.getLong(_duration)  );
    	        		values.put("fname", cursor.getString(CALLS_CACHED_NAME_COL_IDX) );
    	        		values.put("faccount", cursor.getString(CALLS_NUMBER_COL_IDX));
    	        		values.put("ftype", cursor.getInt(CALLS_TYPE_COL_IDX));
    	        		values.put("fdate",cursor.getLong(CALLS_DATE_COL_IDX) );
    	        		values.put("fdata",cursor.getLong(CALLS_DURATION_COL_IDX)  );
    	        		
    	        		values.put("fseen", cursor.getInt(_seen));

    	        		
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
    			Cursor cursor = resolver.query(uriSMSURI, null, null, null,"_id DESC " );
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
    	final ContactInfo tmp=GetContactInfoIDByNumber(account,resolver);
    	if(tmp!=null)
    		return tmp.m_PersonId;
   		return 0;
    }    
//---------------------------------------------------------------------------------------    
    final static public ContactInfo GetContactInfoIDByNumber(String account,ContentResolver resolver)
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
   		    	return new ContactInfo(cur.getString(0),cur.getLong(1));
    		}//if(cur.moveToFirst())
  		}//if(resolver!=null)
   		return null;   	
    }//static private ContactInfo GetContactInfoIDByNumber(String account,ContentResolver resolver)
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
    final static public Map<String,ContactInfo> GetContactInfoMap(Set<String> nums,ContentResolver resolver)
    {
    	final long startTime = System.currentTimeMillis();
    	
    	if(nums.size()>0 && resolver!=null)
   		{
   			Uri id_uri = Data.CONTENT_URI;
   			
   			final String[] columns = new String[]{ 	Data.CONTACT_ID
   													,Phone.NUMBER
   													,ContactsContract.Contacts.DISPLAY_NAME
   													,"display_name_alt" };// {	Phone.CONTACT_ID };
   			
   			String   where=Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "' AND (";
   			final String[] where_arg=new String[nums.size()];
   			
   			int i=0;
            Iterator<String> it = nums.iterator();
            while(it.hasNext())
            {
            	where+=Phone.NUMBER+ "=? OR "; 
   				where_arg[i++]=it.next();
            }
   			
   			where=where.substring(0, where.length()-3)+")";
   			
    		Cursor cur = resolver.query(id_uri,columns,where,where_arg,null);
    		Map<String, ContactInfo > ret_num_contact =  new HashMap<String,ContactInfo>();
    		//cur.getCount()==finded contact count
   		    while(cur.moveToNext())
   		    {
   		    	/*
   		    	String[] values=new String [cur.getColumnCount()];
   		    	for(int k=0;k<cur.getColumnCount();++k)
   		    		values[k]=cur.getColumnName(k)+" =  "+cur.getString(k);
   		    	*/
   		    	//ContactInfo(String name,long id,String name_alt, String number)
   		    	final String num=cur.getString(1);
   		    	final ContactInfo ci=new ContactInfo(cur.getString(2),cur.getLong(0),cur.getString(3),num );

   		    	ret_num_contact.put(num, ci);
   		    	
   		    	
    		}//if(cur.moveToNext())
            long elapsedTime = System.currentTimeMillis() - startTime;
            Log.d("RecentCallsSmsList", "QUERY CONTACT NumCount=" + nums.size()+"\tContCount="+cur.getCount()+"\ttime="+elapsedTime);
   		    return ret_num_contact;
   		    
  		}//if(resolver!=null)
   		return null;   	
    }//static private ContactInfo GetContactInfoIDByNumber(String account,ContentResolver resolver)
//---------------------------------------------------------------------------------------       


}// public class ActLogTableHelper
//---------------------------------------------------------------------------------------