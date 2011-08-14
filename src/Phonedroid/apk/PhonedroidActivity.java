package Phonedroid.apk;

//import com.example.android.apis.view.List5.MyListAdapter;


import android.R.anim;
import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.provider.CallLog;
import android.provider.CallLog.Calls;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class PhonedroidActivity extends Activity 
{
//---------------------------------------------------------------------------------------
// fields	
	Cursor m_CallCursor;
	Integer m_RowCount;


	 private ActLogTableHelper myHelper;
/*	   
	 protected class CallLogProviderEx extends ContentResolver
	    {

	    	public CallLogProviderEx(Context context) 
	    	{
	    		super(context);
	    		// TODO Auto-generated constructor stub
	    	}
	    	
	    	public Cursor Exec(String sql, String[] selectionArgs)
	    	{
	    		get
	    		return getReadableDatabase().rawQuery(sql, selectionArgs);
	    	}
	    	
	    	
	    }
	    
	   */
	    
 
		 
//---------------------------------------------------------------------------------------	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        
        final ImageButton btn1 = (ImageButton) findViewById(R.id.imageButton1);
        
        btn1.setOnClickListener	(
        		new ImageButton.OnClickListener() 
        		{
        			public void onClick(View v)    {      ShowList();        }
        		}
        						);
        
        final ListView lst = (ListView ) findViewById(R.id.listView1);
        lst.setAdapter( new MyListAdapter(this) );
        
        
        //myHelper = new ActLogTableHelper(this,ActLogTableHelper.m_DBName , null, ActLogTableHelper.m_DBVersion);
        myHelper = new ActLogTableHelper(this,ActLogTableHelper.m_DBName , null, ActLogTableHelper.m_DBVersion);
        
        /*
        final ContentResolver resolver = getContentResolver();
        Cursor curs = ((CallLogProviderEx)resolver).Exec("select * from call_log", null);
        if(curs!=null)
        {
        	int count = m_CallCursor.getCount();
        	
        }
        */
        
    }
  //---------------------------------------------------------------------------------------    
    @Override
    protected void onDestroy() 
    {
    	/*
    	SQLiteDatabase db = myHelper.getWritableDatabase();//  Access to database objects  
            //  Delete all data in the table hero_info     Remove all lines that pass a  ------>  Click the back button  
            db.delete("logcall", null, null);
           */
        super.onDestroy();
    }    
//---------------------------------------------------------------------------------------
	public void ShowList()
	{ 
       Intent result = new Intent();
       result.setClassName("Phonedroid.apk", "Phonedroid.apk.ExpandableList1");
       startActivity(result);
       /*
        
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
  
         */
     
        String[] projection = new String[]{
        									"DISTINCT("+Calls.NUMBER+")",
        									//Calls.NUMBER,
        									Calls.CACHED_NAME,
        									Calls.DATE,
        									Calls.DURATION,
        									Calls.TYPE,
        									Calls._ID
        									};
        
        //m_CallCursor= managedQuery(Calls.CONTENT_URI, null, null, null,Calls.DATE+" DESC " );
        
        
        String sql = "SELECT  *  FROM ActLog s  WHERE _ID = "+
            	"(SELECT _ID FROM ActLog si WHERE   si.faccount = s.faccount "+
            										"ORDER BY si.faccount DESC, si.fdate DESC, si._ID DESC LIMIT 1 )"+
            	" ORDER BY fdate DESC";
        //String sql = "SELECT * FROM ActLog";
        
        m_CallCursor  = myHelper.getReadableDatabase().rawQuery(sql, null);
        
        if (m_CallCursor != null)
        {
        	m_RowCount = m_CallCursor.getCount();
        	m_CallCursor.moveToFirst();
        }
    
        
    }//protected void onResume()
//---------------------------------------------------------------------------------------
    private class MyListAdapter extends BaseAdapter 
    {
    	private Context mContext;
    	private LayoutInflater mInflater;
    	
    	//---------------------------------------------------------------------------------------
        class ViewHolder  
        {
           TextView fName;
           TextView fNumber;
           TextView fDuration;
           TextView fDateTime;
           ImageView fImg;
           TextView fMsgData;
        }
    	
    	
    	public MyListAdapter(Context context) 
    	{
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() 
        {
        	if(m_RowCount!=null)
        		return m_RowCount;
        	return 0;
        }

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

        public Object getItem(int position) {
            return position;
        }
 
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) 
        {
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
        	}
        	else
        	{
        		holder = (ViewHolder) convertView.getTag();
        	}
        	
        	
        	if(m_CallCursor!=null )
        	{
        		m_CallCursor.moveToPosition(position);
        		
        		//if(position%2!=0)
        		//	convertView.setBackgroundColor(getResources().getColor(R.color.ColorDark));
        		//else
        		//	convertView.setBackgroundColor(getResources().getColor(R.color.ColorBlack));
        			
        		
        		String contactNumber = m_CallCursor.getString(ActLogTableHelper._faccount);
        		
        		
        		//String contactName = m_CallCursor.getString(ActLogTableHelper._fname);
        		
        		String contactName=null;
        		if(contactName==null)
        			contactName = ActLogTableHelper.FindContact(0, contactNumber , mContext);
        			

        		if(contactName==null)
        		{
        			holder.fName.setText(contactNumber);
        			holder.fNumber.setVisibility(View.INVISIBLE  );//holder.fNumber.setVisibility(View.GONE  );
        			
        			
        		}
        		else
        		{
        			holder.fName.setText(contactName);
        			holder.fNumber.setVisibility(View.VISIBLE  );
        			holder.fNumber.setText("\t"+contactNumber);
        		}
        		

        		//String callDate = (String) DateFormat.format("kk:mm\ndd.MM.yy", m_CallCursor.getLong(ActLogTableHelper._fdate) );
        		String callDate = (String) DateFormat.format("dd MMM.kk:mm", m_CallCursor.getLong(ActLogTableHelper._fdate) );
        		holder.fDateTime.setText(callDate);
        		
        		Long dur =m_CallCursor.getLong(ActLogTableHelper._fdata);
        		String dur_str=String.format("%02d:%02d\t", dur/60,dur%60);
	

                
                switch(m_CallCursor.getInt(ActLogTableHelper._ftype) )
                {
                	default: break;
                	// incoming
                	case ActLogTableHelper.GSM_CALL_INCOMING: 
                			holder.fMsgData.setVisibility(View.GONE );
                			holder.fDuration.setVisibility(View.VISIBLE );

                			holder.fImg.setImageResource(R.drawable.incoming);
                			holder.fName.setTextColor(getResources().getColor(R.color.ColorIncoming));
                			holder.fNumber.setTextColor(getResources().getColor(R.color.ColorIncoming));
                			holder.fDuration.setTextColor(getResources().getColor(R.color.ColorIncoming));
                			
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
                				holder.fName.setTextColor(getResources().getColor(R.color.ColorOutgoing0));
                				holder.fNumber.setTextColor(getResources().getColor(R.color.ColorOutgoing0));
                			}
                			else	
                			{
                				holder.fDuration.setVisibility(View.VISIBLE );
                				holder.fImg.setImageResource(R.drawable.outgoing);
                				holder.fName.setTextColor(getResources().getColor(R.color.ColorOutgoing));
                				holder.fNumber.setTextColor(getResources().getColor(R.color.ColorOutgoing));
                				holder.fDuration.setTextColor(getResources().getColor(R.color.ColorOutgoing));
                				holder.fDuration.setText( dur_str );
                			}
                			break;
                	// missed
                	case ActLogTableHelper.GSM_CALL_MISSED:
                			holder.fMsgData.setVisibility(View.GONE );
                			//holder.fDuration.setVisibility(View.INVISIBLE);
                			holder.fDuration.setVisibility(View.GONE);
                			
                			holder.fImg.setImageResource(R.drawable.missed);
                			holder.fName.setTextColor(getResources().getColor(R.color.ColorMissed));
                			holder.fNumber.setTextColor(getResources().getColor(R.color.ColorMissed));
                			break;
                	// incoming SMS
                	case ActLogTableHelper.MESSAGE_TYPE_INBOX:
                			holder.fMsgData.setVisibility(View.VISIBLE  );
                			holder.fDuration.setVisibility(View.GONE);
           				
                			holder.fName.setTextColor(getResources().getColor(R.color.ColorIncoming));
                			holder.fNumber.setTextColor(getResources().getColor(R.color.ColorIncoming));
                			
                			holder.fImg.setImageResource(R.drawable.incomingsms);
                			holder.fMsgData.setText(m_CallCursor.getString(ActLogTableHelper._fdata));
                			
                			break;
                	// outgoing SMS		
                	case ActLogTableHelper.MESSAGE_TYPE_OUTBOX:
                	case ActLogTableHelper.MESSAGE_TYPE_QUEUED:
                			holder.fName.setTextColor(getResources().getColor(R.color.ColorOutgoing0));
                			holder.fNumber.setTextColor(getResources().getColor(R.color.ColorOutgoing0));
                	case ActLogTableHelper.MESSAGE_TYPE_SENT:
            				holder.fMsgData.setVisibility(View.VISIBLE  );
            				holder.fDuration.setVisibility(View.GONE);
                			holder.fName.setTextColor(getResources().getColor(R.color.ColorOutgoing));
                			holder.fNumber.setTextColor(getResources().getColor(R.color.ColorOutgoing));
            				holder.fImg.setImageResource(R.drawable.outgoingsms);
            				holder.fMsgData.setText(m_CallCursor.getString(ActLogTableHelper._fdata));
            				break;
                			
                	
                }
                
                
                
        	}
        	return convertView;
        }//public View getView(int position, View convertView, ViewGroup parent)
        

        
    }//private static class MyListAdapter extends BaseAdapter
 //---------------------------------------------------------------------------------------


    
    
    
    
    
    
    
    
    
    
    
    
    
}