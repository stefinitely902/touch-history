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
import com.savinov3696.phone.log.ActLogTableHelper.TempContact;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class PhonedroidActivity extends  Activity//ListActivity//ListActivity 
								implements ListView.OnScrollListener 
																	 
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
	private static Cursor				m_CallCursor;
	
	public static final String query_group_by_account= "SELECT  *  FROM ActLog s  WHERE _ID = "+
        												"(SELECT _ID FROM ActLog si WHERE   si.faccount = s.faccount "+
        												"ORDER BY si.faccount DESC, si.fdate DESC, si._ID DESC LIMIT 1 )"+
        												" ORDER BY fdate DESC";
	public static final String query_nogroup="SELECT * FROM ActLog ORDER BY fdate DESC";

	
	private static boolean mBusy = false;

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }
    

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE:
            mBusy = false;
            
            int first = view.getFirstVisiblePosition();
            int count = view.getChildCount();
            
            ViewHolder holder=null;

            for (int i=0; i<count; i++) 
            {
				holder = (ViewHolder) view.getChildAt(i).getTag();            	
                if (holder!=null && holder.fName.getTag() != null) 
                {
					m_CallCursor.moveToPosition(first + i);
					final String contactNumber= m_CallCursor.getString(ActLogTableHelper._faccount);
					holder.fNumber.setText(contactNumber);
					String contactName=null;
					final TempContact tmp= ActLogTableHelper.GetTempContactIDByNumber(contactNumber,getContentResolver());
	        		if(tmp!=null)
	        		{
	        			contactName = ActLogTableHelper.GetContactNameByID(tmp.m_ContactID,getContentResolver());
		        		if(contactName==null)
		        			contactName=tmp.m_ContactName;
		        	}
					
					if(contactName==null)
        			{
        				holder.fName.setText(contactNumber);
        				holder.fNumber.setVisibility(View.INVISIBLE  );
        			}
		        	else
		        	{
		        		holder.fName.setText(contactName);
		        		holder.fNumber.setVisibility(View.VISIBLE  );
		        	}
		        		
      		
		        	holder.fName.setTag(null);
	        	}//if (holder.fName.getTag() != null)
        	}//for (int i=0; i<count; i++)

            
            
            //mStatus.setText("Idle");
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            mBusy = true;
            //mStatus.setText("Touch scroll");
            break;
        case OnScrollListener.SCROLL_STATE_FLING:
            mBusy = true;
            //mStatus.setText("Fling");
            break;
        }
    }
	
	
//---------------------------------------------------------------------------------------	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	Log.d("myinfo", "start\t onCreate");
    	long startTime = System.currentTimeMillis();
    	super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        final ListView lst = (ListView ) findViewById(R.id.listView1);
        ListAdapter la=new MyListAdapter(this);
        lst.setAdapter( la );
        lst.setOnScrollListener(this);
        /*
     	ListAdapter la=new MyListAdapter(this);
     	this.setListAdapter(la);
        getListView().setOnScrollListener(this);
        */
        
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
         
        myHelper = new ActLogTableHelper(this,ActLogTableHelper.m_DBName , null, ActLogTableHelper.m_DBVersion);
        
        m_CallCursor  = myHelper.getReadableDatabase().rawQuery(query_group_by_account, null);
        Log.d("myinfo", "end\t onCreate");
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        Toast.makeText(getApplicationContext(), "onResume time = "+elapsedTime,Toast.LENGTH_SHORT).show();
        Log.d("RAW QUERY", "elapsedTime = "+elapsedTime);
        
    }
//---------------------------------------------------------------------------------------
    @Override
    protected void onStart()
    {
    	Log.d("myinfo", "start\t onStart");
    	super.onStart();
    	Log.d("myinfo", "end\t onStart");
    }
//---------------------------------------------------------------------------------------
    @Override
    protected void onResume() 
    {
        Log.d("myinfo", "start\t onResume");
        super.onResume();
        

       
        // m_CallCursor  = myHelper.getReadableDatabase().rawQuery(query_nogroup, null);
        //if (m_CallCursor != null)
        //	m_CallCursor.moveToFirst();


		Log.d("myinfo", "end\t onResume");        
    }//protected void onResume()
//---------------------------------------------------------------------------------------    
    @Override
    protected void onPause() 
    {
        super.onPause();
        Log.d("myinfo", "start\t onPause");
        Log.d("myinfo", "end\t onPause");
    }   
//---------------------------------------------------------------------------------------    
    @Override
    protected void onStop() 
    {
        Log.d("myinfo", "start\t onStop");
        super.onStop();

        
        //m_CallCursor.close();
        
        //finish();
        Log.d("myinfo", "end\t onStop");
    }
//---------------------------------------------------------------------------------------    
    @Override
    protected void onDestroy() 
    {
    	super.onDestroy();
    	
    	
    	
    	Log.d("myinfo", "start\t onDestroy");
    	SQLiteDatabase db = myHelper.getWritableDatabase();//  Access to database objects
    	db.close();
    	
    	Log.d("myinfo", "end\t onDestroy");
    }    


    public static class ViewHolder  
    {
       TextView fName;
       TextView fNumber;
       TextView fDuration;
       TextView fDateTime;
       ImageView fImg;
       TextView fMsgData;
       ImageButton	btnReply;
       
       int m_Pos;
       
       
       //private Object mTag;
       //final Object getTag(){return mTag;} 
       //final void setTag(Object tag){mTag=tag;}
    }
//---------------------------------------------------------------------------------------
    static private class MyListAdapter extends BaseAdapter implements ImageButton.OnClickListener
    {
    	private Context 		mContext;
    	private LayoutInflater  mInflater;
    	
    	//---------------------------------------------------------------------------------------

    	
    	public MyListAdapter(Context context) 
    	{
            if(context!=null)
            {
            	mContext = context;
	            //mInflater = LayoutInflater.from(context);
            	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
        }

        public int getCount() 
        {
        	if(m_CallCursor!=null)
        		return m_CallCursor.getCount();
        	return 0;
        }

        public Object getItem(int position) {	return position;	}
        public long getItemId(int position) {	return position;	}

        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	long startTime = System.currentTimeMillis();
        	
        	ViewHolder holder=null;
        	
        	
        	if ( convertView == null ) 
        	{
        		convertView = (View)mInflater.inflate(R.layout.phonedroidadapterlayout, parent, false);
        		
        		holder = new ViewHolder();
        		holder.fName = (TextView) convertView.findViewById(R.id.al_Text);
        		holder.fNumber = (TextView) convertView.findViewById(R.id.al_Number);
        		holder.fDuration = (TextView) convertView.findViewById(R.id.al_Duration);
        		holder.fDateTime = (TextView) convertView.findViewById(R.id.al_DateTime);
        		holder.fMsgData=(TextView) convertView.findViewById(R.id.al_Data);
        		holder.fImg = (ImageView) convertView.findViewById(R.id.al_Img);
        		holder.btnReply = (ImageButton) convertView.findViewById(R.id.btnReply);
        		 
        		convertView.setTag(holder);
        	}
        	else
        	{
        		holder = (ViewHolder) convertView.getTag();
        	}
        	
        	holder.m_Pos=position;
        	
        	if(m_CallCursor!=null )
        	{
        		
        		m_CallCursor.moveToPosition(position);
        		
        		if(position%2!=0)
        			convertView.setBackgroundColor(ColorDark);
        		else
        			convertView.setBackgroundColor(ColorBlack);

        		final int type=m_CallCursor.getInt(ActLogTableHelper._ftype);
        		
        		final String contactNumber= m_CallCursor.getString(ActLogTableHelper._faccount);
        		holder.fNumber.setText(contactNumber);

        		//String contactName = m_CallCursor.getString(ActLogTableHelper._fname);
        		
        		String contactName=null;
        		 if (!mBusy)  //slow view? loading in view
        		 {
	        		final TempContact tmp = ActLogTableHelper.GetTempContactIDByNumber(contactNumber,mContext.getContentResolver());
	        		if(tmp!=null)
	        		{
	        			contactName = ActLogTableHelper.GetContactNameByID(tmp.m_ContactID,mContext.getContentResolver());
		        		if(contactName==null)
		        			contactName=tmp.m_ContactName;
	        		}
	        		
	        		if(contactName==null)
        			{
        				holder.fName.setText(contactNumber);
        				holder.fNumber.setVisibility(View.INVISIBLE  );//holder.fNumber.setVisibility(View.GONE  );
        			}
		        	else
		        	{
		        		holder.fName.setText(contactName);
		        		holder.fNumber.setVisibility(View.VISIBLE  );
		        	}
	        		
	        		holder.fName.setTag(null);
        		 }
        		 else
        		 {
        			 holder.fName.setTag(this);
        			 holder.fName.setText(contactNumber);
        			 holder.fNumber.setVisibility(View.INVISIBLE  );
        			 
        		 }
  
        		
        		
        		//holder.btnReply.setTag(holder);
        		holder.btnReply.setOnClickListener	( this);
        		/*
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
                							mContext.startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + Uri.encode(contactNumber) )));
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
					*/
                		
                					        		

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

		@Override
		public void onClick(View view) 
		{
			Log.d("myinfo", "outgoing call");
			
			ImageButton btn = (ImageButton) view;
			View parent= (View) btn.getParent() ; 
			ViewHolder holder = (ViewHolder) parent.getTag();          	
			
			if (holder!=null) 
			{
				final String contactNumber=(String) holder.fNumber.getText();
				Toast.makeText(mContext,"Reply "+holder.fName.getText()+"\n"+contactNumber,Toast.LENGTH_SHORT).show();
				m_CallCursor.moveToPosition(holder.m_Pos);
				final int type=m_CallCursor.getInt(ActLogTableHelper._ftype);
				switch(type)
                {
                	case ActLogTableHelper.GSM_CALL_INCOMING:
                	case ActLogTableHelper.GSM_CALL_OUTGOING:
                	case ActLogTableHelper.GSM_CALL_MISSED:
                    //final boolean available = isIntentAvailable(getBaseContext(),Intent.ACTION_CALL);
                	//if(available)
                		mContext.startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + Uri.encode(contactNumber) )));
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
                }//switch(type)	
			}//if (holder!=null)

		}//public void onClick(View view) 
        

        
    }//private static class MyListAdapter extends BaseAdapter
 //---------------------------------------------------------------------------------------


    
    
    
    
    
    
    
    
    
    
    
    
    
}