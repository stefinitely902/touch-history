package com.savinov3696.phone.log;

/*
SELECT * FROM 
   (SELECT DISTINCT ON(fname)fname,*  FROM "LogCall" ORDER BY fname ASC, fdate DESC) srt
	ORDER BY fdate DESC

SELECT fname,MAX(fdate) AS lastcall
	FROM "LogCall"
GROUP BY fname
ORDER BY lastcall DESC

SELECT  *
FROM    "LogCall" s
WHERE   "order" =
	(
	SELECT  "order"
	FROM    "LogCall" si
	WHERE   si.fname = s.fname
	ORDER BY
        si.fname DESC, si.fdate DESC, si."order" DESC
	LIMIT 1
	)
ORDER BY
fdate DESC,  "order" DESC

//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------
	public static boolean isIntentAvailable(Context context, String action) 
	{
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}	 
//---------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------
 
*/
 


import java.util.List;

import android.R.anim;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.CallLog;
import android.provider.CallLog.Calls;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Rect;

public class PhonedroidActivity extends Activity//ListActivity 
{
	/* непонятно,почему нельзя желать #DEFINE и так накладно обращаться к ресурсам через ColorDark)
	сделаем переменные :( и инициализируем их из конструктора взяв из ресурсов? Сразу?*/
	public static final int ColorIncoming=0xFF96C832;
	public static final int ColorOutgoing=0xFFFFA000;
	public static final int ColorOutgoing0=0xFFB4B4B4;
	public static final int ColorMissed=0xFFFF4000;
	public static final int ColorDark=0xFF202020;
	public static final int ColorBlack=0xFF000000;
	public static final int ColorTransparent=0;

	
	private static ActLogTableHelper 	myHelper;
	private static Cursor 				m_CallCursor;
	
	public static final String query_group_by_account= "SELECT  *  FROM ActLog s  WHERE _ID = "+
        												"(SELECT _ID FROM ActLog si WHERE   si.faccount = s.faccount "+
        												"ORDER BY si.faccount DESC, si.fdate DESC, si._ID DESC LIMIT 1 )"+
        												" ORDER BY fdate DESC";
	public static final String query_nogroup="SELECT * FROM ActLog ORDER BY fdate DESC";

		 
//---------------------------------------------------------------------------------------	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final ListView lst = (ListView ) findViewById(R.id.listView1);
        lst.setAdapter( new MyListAdapter(this) );
     
        //setListAdapter(new MyListAdapter(this));
        
        myHelper = new ActLogTableHelper(this,ActLogTableHelper.m_DBName , null, ActLogTableHelper.m_DBVersion);
     
        /*
           final ImageButton btn1 = (ImageButton) findViewById(R.id.imageButton1);
        btn1.setOnClickListener	(
        		new ImageButton.OnClickListener() 
        		{
        			public void onClick(View v)    {      ShowList();        }
        		}
        						);

       
       Intent result = new Intent();
       result.setClassName("Phonedroid.apk", "Phonedroid.apk.ExpandableList1");
       startActivity(result);
        
    	 Intent i = new Intent(this, ExpandableList1.class);
    	 String pname=i.getPackage();
    	 String cname=i.getComponent().toString();
    	 if(i!=null)
    	 	startActivity(i);
    	 */	
         
        
    }
//---------------------------------------------------------------------------------------
    @Override
    protected void onResume() 
    {
        super.onResume();
        long startTime = System.currentTimeMillis();


        
       m_CallCursor  = myHelper.getReadableDatabase().rawQuery(query_group_by_account, null);
        // m_CallCursor  = myHelper.getReadableDatabase().rawQuery(query_nogroup, null);
        //if (m_CallCursor != null)
        //	m_CallCursor.moveToFirst();

        long elapsedTime = System.currentTimeMillis() - startTime;
        Toast.makeText(getApplicationContext(), "onResume time = "+elapsedTime,Toast.LENGTH_SHORT).show();
        Log.d("RAW QUERY", "elapsedTime = "+elapsedTime);

        
    }//protected void onResume()
//---------------------------------------------------------------------------------------    
    @Override
    protected void onPause() 
    {
        super.onPause();
        m_CallCursor.close();
    }   
//---------------------------------------------------------------------------------------    
    @Override
    protected void onStop() 
    {
        super.onStop();
        
        
        
        //finish();
    }
//---------------------------------------------------------------------------------------    
    @Override
    protected void onDestroy() 
    {
    	SQLiteDatabase db = myHelper.getWritableDatabase();//  Access to database objects
    	db.close();
    	super.onDestroy();
    }    



//---------------------------------------------------------------------------------------
    static private class MyListAdapter extends BaseAdapter 
    {
    	private Context 		mContext;
    	private LayoutInflater 	mInflater;
    	
    	//---------------------------------------------------------------------------------------
        static class ViewHolder  
        {
           TextView fName;
           TextView fNumber;
           TextView fDuration;
           TextView fDateTime;
           ImageView fImg;
           TextView fMsgData;
           ImageButton	btnReply;
        }
    	
    	public MyListAdapter(Context context) 
    	{
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() 
        {
        	if(m_CallCursor!=null)
        		return m_CallCursor.getCount();
        	return 0;
        }
/*
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) 
        {
        	//return !mStrings[position].startsWith("-");
        	return true;
        }
*/
        public Object getItem(int position) {
            return position;
        }
 
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	long startTime = System.currentTimeMillis();
        	
        	ViewHolder holder=null;
        	
        	
        	if ( convertView == null ) 
        	{
        		convertView = mInflater.inflate(R.layout.phonedroidadapterlayout, null );
        		
        		holder = new ViewHolder();
        		holder.fName = (TextView) convertView.findViewById(R.id.al_Text);
        		holder.fNumber = (TextView) convertView.findViewById(R.id.al_Number);
        		holder.fDuration = (TextView) convertView.findViewById(R.id.al_Duration);
        		holder.fDateTime = (TextView) convertView.findViewById(R.id.al_DateTime);
        		holder.fMsgData=(TextView) convertView.findViewById(R.id.al_Data);

        		holder.fImg = (ImageView) convertView.findViewById(R.id.al_Img);
        		
        		convertView.setTag(holder);
        		
        		holder.btnReply = (ImageButton) convertView.findViewById(R.id.btnReply); 

        		


        	}
        	else
        	{
        		holder = (ViewHolder) convertView.getTag();
        	}
        	
        	
        	if(m_CallCursor!=null )
        	{
        		
        		m_CallCursor.moveToPosition(position);
        		
        		if(position%2!=0)
        			convertView.setBackgroundColor(ColorDark);
        		else
        			convertView.setBackgroundColor(ColorBlack);

        		final String contactNumber= m_CallCursor.getString(ActLogTableHelper._faccount);
        		final String contactName=ActLogTableHelper.FindContact(0, contactNumber , mContext);
        		final int type=m_CallCursor.getInt(ActLogTableHelper._ftype);
        		//String contactName = m_CallCursor.getString(ActLogTableHelper._fname);

        		if(contactName==null)
        		{
        			holder.fName.setText(contactNumber);
        			holder.fNumber.setVisibility(View.INVISIBLE  );//holder.fNumber.setVisibility(View.GONE  );
        		}
        		else
        		{
        			holder.fName.setText(contactName);
        			holder.fNumber.setVisibility(View.VISIBLE  );
        			holder.fNumber.setText(contactNumber);
        		}
        		
        		
        		holder.btnReply.setOnClickListener	(
                		new ImageButton.OnClickListener() 
                		{
                			public void onClick(View v)    
                			{      
                				switch(type)
                				{
                					case ActLogTableHelper.GSM_CALL_INCOMING:
                					case ActLogTableHelper.GSM_CALL_OUTGOING:
                					case ActLogTableHelper.GSM_CALL_MISSED:
                					    //final boolean available = isIntentAvailable(getBaseContext(),Intent.ACTION_CALL);
                						//if(available)
                							mContext.startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + contactNumber)));
                						break;
                					
                					default:
                						Intent intent = new Intent(Intent.ACTION_VIEW);
                						//sendIntent.putExtra("sms_body", "smsBody");
                						//sendIntent.putExtra("address", "phoneNumber1;phoneNumber2;...");
                						intent.setType("vnd.android-dir/mms-sms");
                        				Uri uri = Uri.parse("sms:"+ contactNumber);
                        				intent.setData(uri);
                        				mContext.startActivity(intent);
                						break;
                				
                				}
               				
                			}
                		});
                		
                					        		

        		//String callDate = (String) DateFormat.format("kk:mm\ndd.MM.yy", m_CallCursor.getLong(ActLogTableHelper._fdate) );
        		String callDate = (String) DateFormat.format("dd MMM.kk:mm", m_CallCursor.getLong(ActLogTableHelper._fdate) );
        		holder.fDateTime.setText(callDate);
        		
        		Long dur =m_CallCursor.getLong(ActLogTableHelper._fdata);
        		String dur_str=String.format("%02d:%02d\t", dur/60,dur%60);
	

                
                switch(type)
                {
                	default: break;
                	// incoming
                	case ActLogTableHelper.GSM_CALL_INCOMING: 
                			holder.fMsgData.setVisibility(View.GONE );
                			holder.fDuration.setVisibility(View.VISIBLE );

                			holder.fImg.setImageResource(R.drawable.incoming);
                			holder.fName.setTextColor(ColorIncoming);
                			holder.fNumber.setTextColor(ColorIncoming);
                			holder.fDuration.setTextColor(ColorIncoming);
                			
                			holder.fDuration.setText( dur_str );
                			break;
                	// outgoing
                	case ActLogTableHelper.GSM_CALL_OUTGOING:
                			holder.fMsgData.setVisibility(View.GONE );
                			if(dur==0)
                			{
                				//holder.fDuration.setVisibility(View.INVISIBLE);
                				holder.fDuration.setVisibility(View.GONE);
                				holder.fImg.setImageResource(R.drawable.outgoing0);
                				holder.fName.setTextColor(ColorOutgoing0);
                				holder.fNumber.setTextColor(ColorOutgoing0);
                			}
                			else	
                			{
                				holder.fDuration.setVisibility(View.VISIBLE );
                				holder.fImg.setImageResource(R.drawable.outgoing);
                				holder.fName.setTextColor(ColorOutgoing);
                				holder.fNumber.setTextColor(ColorOutgoing);
                				holder.fDuration.setTextColor(ColorOutgoing);
                				holder.fDuration.setText( dur_str );
                			}
                			break;
                	// missed
                	case ActLogTableHelper.GSM_CALL_MISSED:
                			holder.fMsgData.setVisibility(View.GONE );
                			//holder.fDuration.setVisibility(View.INVISIBLE);
                			holder.fDuration.setVisibility(View.GONE);
                			
                			holder.fImg.setImageResource(R.drawable.missed);
                			holder.fName.setTextColor(ColorMissed);
                			holder.fNumber.setTextColor(ColorMissed);
                			break;
                	// incoming SMS
                	case ActLogTableHelper.MESSAGE_TYPE_INBOX:
                			holder.fMsgData.setVisibility(View.VISIBLE  );
                			holder.fDuration.setVisibility(View.GONE);
           				
                			holder.fName.setTextColor(ColorIncoming);
                			holder.fNumber.setTextColor(ColorIncoming);
                			
                			holder.fImg.setImageResource(R.drawable.incomingsms);
                			holder.fMsgData.setText(m_CallCursor.getString(ActLogTableHelper._fdata));
                			
                			break;
                	// outgoing SMS		
                	case ActLogTableHelper.MESSAGE_TYPE_OUTBOX:
                	case ActLogTableHelper.MESSAGE_TYPE_QUEUED:
                			holder.fName.setTextColor(ColorOutgoing0);
                			holder.fNumber.setTextColor(ColorOutgoing0);
                	case ActLogTableHelper.MESSAGE_TYPE_SENT:
            				holder.fMsgData.setVisibility(View.VISIBLE  );
            				holder.fDuration.setVisibility(View.GONE);
                			holder.fName.setTextColor(ColorOutgoing);
                			holder.fNumber.setTextColor(ColorOutgoing);
            				holder.fImg.setImageResource(R.drawable.outgoingsms);
            				holder.fMsgData.setText(m_CallCursor.getString(ActLogTableHelper._fdata));
            				break;
                	
                }//switch(type)
                
        	}//if(m_CallCursor!=null )
        
        	long elapsedTime = System.currentTimeMillis() - startTime;
        	Log.d("PAINT VIEW", "item="+position+"\ttime="+elapsedTime);
        	return convertView;
        }//public View getView(int position, View convertView, ViewGroup parent)
        

        
    }//private static class MyListAdapter extends BaseAdapter
 //---------------------------------------------------------------------------------------


    
    
    
    
    
    
    
    
    
    
    
    
    
}