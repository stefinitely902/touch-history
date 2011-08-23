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
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.savinov3696.phone.log.ActLogTableHelper.ContactInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ViewFlipper;
import  android.view.View.OnFocusChangeListener;

public class PhonedroidActivity extends  Activity//ListActivity//ListActivity 
								implements ListView.OnScrollListener
																	 
{
	private static final String TAG = "RecentCallsSmsList";
	



    static abstract class ConnectionInfo
    {
    	abstract public ContactInfo	getContactInfo();
    	abstract public long 		getDate();
    	abstract public String 		getContent();// call=duration, SMS -text
    	abstract public int 		getType();
    }
    
    static final class ConnectionInfoData {
    	ContactInfo		mContact;
    	public long 	mDate;
    	public long 	mData;
    	public long 	mType;
    }    
    
   
    
	
	/* ���������,������ ������ ������ #DEFINE � ��� �������� ���������� � �������� ����� ColorDark)
	������� ���������� :( � �������������� �� �� ������������ ���� �� ��������? �����?*/
	public static final int ColorIncoming=0xFF96C832;
	public static final int ColorOutgoing=0xFFFFA000;
	public static final int ColorOutgoing0=0xFFB4B4B4;
	public static final int ColorMissed=0xFFFF4000;
	public static final int ColorDark=0xFF202020;
	public static final int ColorBlack=0xFF000000;
	public static final int ColorTransparent=0;

	private MyListAdapter m_Adapter;
	private boolean m_ScrollToTop;
	
	private static ActLogTableHelper 	myHelper;
	private static Cursor				m_CallCursor;
	
	
	
	public static final String query_group_by_account= "SELECT  *  FROM ActLog s  WHERE _ID = "+
        												"(SELECT _ID FROM ActLog si WHERE   si.faccount = s.faccount "+
        												"ORDER BY si.faccount DESC, si.fdate DESC, si._ID DESC LIMIT 1 )"+
        												" ORDER BY fdate DESC";
	public static final String query_nogroup="SELECT * FROM ActLog ORDER BY fdate DESC";

	
	private static boolean mBusy = false;


	final long globalstartTime = System.currentTimeMillis();
	long globalstartCall = System.currentTimeMillis();
	
	final void LogTimeAfterStart(String place)
	{
		long cur=System.currentTimeMillis();
		
        Log.d(TAG, "elapsed: STARTUP="+(cur-globalstartTime)+"\tLASTCALL="+(cur-globalstartCall)+"\t"+place );
        globalstartCall=cur;
	}
	
	
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }
    


    public void onScrollStateChanged(AbsListView view, int scrollState) 
    {
    	
    	switch (scrollState) 
        {
        	default:
        	case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL: // ����� ���� �� ����� �������� ������� ��������� ?
        			LogTimeAfterStart("SCROLL_STATE_TOUCH_SCROLL");
       				mBusy = true;
        		    break;
    	    case OnScrollListener.SCROLL_STATE_FLING:
    	    		
    	    		LogTimeAfterStart("SCROLL_STATE_FLING");
    	    		
    	        	//final ListView lst = (ListView ) findViewById(R.id.listView1);
    	        	
    	    		//mBusy = true;
    	    		//break;
        		    
			case OnScrollListener.SCROLL_STATE_IDLE:
					LogTimeAfterStart("SCROLL_STATE_IDLE start");
				
            		mBusy = false;
					
					//int first = view.getFirstVisiblePosition();
					final int count = view.getChildCount();
					
										
					// TODO ���� ���������� ��� � �������� ������???
					// 1 ������� ������(������) ViewHolder`�� ����������� � ����������
					Vector<ViewHolder> ItemHolder = new Vector<ViewHolder>();
					// 2 ������� ���(���������� �����-����) �������
					Set<String> nums=new HashSet<String>();
					
	            	for (int i=0; i<count ; i++)
	            	{
	            		final ViewHolder holder = (ViewHolder) view.getChildAt(i).getTag();
	            		if ( holder!=null && holder.m_State==1 )
	            		{
	            			final String number = (String)holder.fNumber.getText();//���� ����� �������
	            			nums.add(number);
	            			ItemHolder.addElement(holder);
	            			holder.m_State=0;
	            		}
	            	}

					// 3 ��������� ������, ��� ������� ���������� �����-���� � ��������
	            	Map<String, ContactInfo > ret_num_contact=ActLogTableHelper.GetContactInfoMap(nums,getContentResolver());
	            	if(ret_num_contact!=null )
	            	{
	            		for (int i=0; i<ItemHolder.size() ; i++)
	            		{
	            			final ViewHolder holder=ItemHolder.get(i);
	            			final String number = (String)holder.fNumber.getText();//���� ����� �������
	            			final ContactInfo contact=ret_num_contact.get(number);
	            			if(contact!=null)
	            				holder.ShowContactName(contact.TryGetAltName());
	            		}//for (int i=0; i<ItemHolder.size() ; i++)
	            	}//if(ActLogTableHelper.GetContactInfoMap(num_contact,getContentResolver())>0 )

	            	LogTimeAfterStart("SCROLL_STATE_IDLE end");


            break;//case OnScrollListener.SCROLL_STATE_IDLE:
        
        }
    }
	

//---------------------------------------------------------------------------------------	
    @Override public void onCreate(Bundle savedInstanceState) 
    {

    	setContentView(R.layout.main);
    	// Typing here goes to the dialer
    	
    	setDefaultKeyMode(DEFAULT_KEYS_DIALER);
    	final ListView lst = (ListView ) findViewById(R.id.listView1);
    	m_Adapter=new MyListAdapter(this);
    	lst.setAdapter( m_Adapter );
    	lst.setItemsCanFocus(false);

        lst.setOnScrollListener(this);
        
        lst.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				LogTimeAfterStart("OnFocusChangeListener hasFocus="+hasFocus);
				final ListView lst = (ListView ) v;
		    	if(v!=null && hasFocus)
		    		onScrollStateChanged(lst, SCROLL_STATE_IDLE);
			}
        });
        
        lst.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
        		//v.setBackgroundColor(0xFFFF0000);
        		Log.d(TAG, "!!!!!!!!!!onItemClick");
                
            }
        });

        
         
        myHelper = new ActLogTableHelper(this,ActLogTableHelper.m_DBName , null, ActLogTableHelper.m_DBVersion);
        
        m_CallCursor  = myHelper.getReadableDatabase().rawQuery(query_group_by_account, null);
        //m_CallCursor  = myHelper.getReadableDatabase().rawQuery(query_nogroup, null);
        
        
        super.onCreate(savedInstanceState);
        
        LogTimeAfterStart("onCreateEnd");

    }
//---------------------------------------------------------------------------------------
    @Override protected void onStart()
    {
    	m_ScrollToTop = true;
    	super.onStart();
    	LogTimeAfterStart("onStartEnd");
    	
    }
//---------------------------------------------------------------------------------------
    @Override protected void onResume() 
    {
    	super.onResume();
    	
    	LogTimeAfterStart("onResumeEnd");
    }//protected void onResume()
//---------------------------------------------------------------------------------------    
    @Override
    protected void onPause() 
    {
        super.onPause();
        LogTimeAfterStart("onPauseEnd");
    }   
//---------------------------------------------------------------------------------------    
    @Override
    protected void onStop() 
    {
        super.onStop();
        LogTimeAfterStart("onStopEnd");
	}
//---------------------------------------------------------------------------------------    
    @Override
    protected void onDestroy() 
    {
    	LogTimeAfterStart("onDestroyStart");
    	super.onDestroy();

    	SQLiteDatabase db = myHelper.getWritableDatabase();//  Access to database objects
    	db.close();
    	
    	LogTimeAfterStart("onDestroyEnd");
    }    


    public static class ViewHolder  
    {
       
       TextView fNumber;
       TextView fDuration;
       TextView fDateTime;
       ImageView fImg;
       TextView fMsgData;
       ImageView	btnReply;
       
       ViewFlipper mFlipper;
       
      // TextView mContactNameView;
      // TextView fName;
       int m_Pos=-1;
       int m_State=0;// 0 not need update
       int m_Type=0;// 0 not need update
       
       final TextView getNameView()
       {
    	   return ((TextView)mFlipper.getCurrentView());
       }
       
   		public void ShowContactName(String name)
   		{
   			TextView oldview = getNameView();
   			mFlipper.showNext();//if(animation_on)
   			((TextView)mFlipper.getCurrentView()).setText(name);
   			((TextView)mFlipper.getCurrentView()).setTextColor(oldview.getTextColors());
   		  	
   			fNumber.setAnimation(mFlipper.getInAnimation() );
   			fNumber.setVisibility(View.VISIBLE);
   		}//public void ShowContactName(String name)       
       

    }
//---------------------------------------------------------------------------------------
    //static 
    private class MyListAdapter extends BaseAdapter //implements 	//OnClickListener , 
    															//OnLongClickListener
    {
    	private Context 		mContext;
    	private LayoutInflater  mInflater;
    	
    	final OnClickListener			m_BtnReplyClickListener=new OnClickListener(){
    																	@Override
																		public void onClick(View view){
    																		onReplyClick(view);
    																	}
    																};
    	final OnLongClickListener		m_BtnReplyLongClickListener=new OnLongClickListener(){
    																	@Override
    																	public boolean 	onLongClick(View v){
    																		return onLongReplyClick(v);
    																	}
    																};
	    final OnClickListener			m_ItemClickListener=new OnClickListener(){
	    														@Override
																public void onClick(View view)	{
	    															Log.d(TAG, "!!!!!!!!!!onItemClick");												
	    														}
	    													};
//---------------------------------------------------------------------------------------
    	public MyListAdapter(Context context) 
    	{
    		if(context!=null){
            	mContext = context;
	            //mInflater = LayoutInflater.from(context);
            	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
        }

        public int getCount() {
        	if(m_CallCursor!=null)
        		return m_CallCursor.getCount();
        	return 0;
        }
        public Object getItem(int position){
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
        		convertView = (View)mInflater.inflate(R.layout.phonedroidadapterlayout, parent, false);
        		
        		holder = new ViewHolder();
        		
        		holder.fNumber = (TextView) convertView.findViewById(R.id.al_Number);
        		holder.fDuration = (TextView) convertView.findViewById(R.id.al_Duration);
        		holder.fDateTime = (TextView) convertView.findViewById(R.id.al_DateTime);
        		holder.fMsgData=(TextView) convertView.findViewById(R.id.al_Data);
        		holder.fImg = (ImageView) convertView.findViewById(R.id.al_Img);
        		//holder.btnReply = (ImageButton) convertView.findViewById(R.id.btnReply);
        		holder.btnReply = (ImageView) convertView.findViewById(R.id.btnReply);
        		
        		
        		holder.mFlipper = (ViewFlipper) convertView.findViewById(R.id.viewFlipper1);
        		
        		//holder.mContactNameView=(TextView) convertView.findViewById(R.id.ContactNameView);
        		//holder.fName = (TextView) convertView.findViewById(R.id.al_Text);
        		
        		
        		holder.btnReply.setOnClickListener(m_BtnReplyClickListener);
        		holder.btnReply.setOnLongClickListener(m_BtnReplyLongClickListener);
        		
				//holder.mFlipper.getChildAt(0).setOnClickListener(m_ItemClickListener);
        		//holder.mFlipper.getChildAt(1).setOnClickListener(m_ItemClickListener);
        		//holder.fNumber.setOnClickListener(m_ItemClickListener);
        		
        		
        		convertView.setTag(holder);
        		
        	}
        	else
        	{
        		holder = (ViewHolder) convertView.getTag();
        	}
        	
        	holder.m_Pos=position;
/*
    		if(position%2!=0)
    			convertView.setBackgroundColor(ColorDark);
    		else
    			convertView.setBackgroundColor(ColorBlack);
*/
    		
        	if(m_CallCursor.moveToPosition(position) )
        	{
        		
        		
        		String callDate = (String) DateFormat.format("dd MMM.kk:mm", m_CallCursor.getLong(ActLogTableHelper._fdate) );
        		holder.fDateTime.setText(callDate);
        		
        		final String contactNumber= m_CallCursor.getString(ActLogTableHelper._faccount);
        		
        		holder.fNumber.setVisibility(View.INVISIBLE);
        		holder.fNumber.setAnimation(null);
        		holder.fNumber.setText(contactNumber);
        		
        		
        		
        		final TextView name_view = ((TextView)holder.mFlipper.getCurrentView());
        		name_view.setText(contactNumber);//if(animation_on)

  
        		holder.m_Type=m_CallCursor.getInt(ActLogTableHelper._ftype);
                switch(holder.m_Type)
                {
                	default: break;
                	// incoming
                	case ActLogTableHelper.GSM_CALL_INCOMING: 
                			holder.fMsgData.setVisibility(View.GONE );
                			holder.fDuration.setVisibility(View.VISIBLE );

                			holder.fImg.setImageResource(R.drawable.incoming);
                			name_view.setTextColor(ColorIncoming);
                			holder.fNumber.setTextColor(ColorIncoming);
                			holder.fDuration.setTextColor(ColorIncoming);
                			{
                				final long dur =m_CallCursor.getLong(ActLogTableHelper._fdata);
                				holder.fDuration.setText(String.format("%02d:%02d\t", dur/60,dur%60));
                			}
                			break;
                	// outgoing
                	case ActLogTableHelper.GSM_CALL_OUTGOING:
                			holder.fMsgData.setVisibility(View.GONE );
                			final long dur =m_CallCursor.getLong(ActLogTableHelper._fdata);
                			if(dur==0)
                			{
                				//holder.fDuration.setVisibility(View.INVISIBLE);
                				holder.fDuration.setVisibility(View.GONE);
                				holder.fImg.setImageResource(R.drawable.outgoing0);
                				name_view.setTextColor(ColorOutgoing0);
                				holder.fNumber.setTextColor(ColorOutgoing0);
                			}
                			else	
                			{
                				holder.fDuration.setVisibility(View.VISIBLE );
                				holder.fImg.setImageResource(R.drawable.outgoing);
                				name_view.setTextColor(ColorOutgoing);
                				holder.fNumber.setTextColor(ColorOutgoing);
                				holder.fDuration.setTextColor(ColorOutgoing);
               					holder.fDuration.setText(String.format("%02d:%02d\t", dur/60,dur%60));

                			}
                			break;
                	// missed
                	case ActLogTableHelper.GSM_CALL_MISSED:
                			holder.fMsgData.setVisibility(View.GONE );
                			//holder.fDuration.setVisibility(View.INVISIBLE);
                			holder.fDuration.setVisibility(View.GONE);
                			
                			holder.fImg.setImageResource(R.drawable.missed);
                			name_view.setTextColor(ColorMissed);
                			holder.fNumber.setTextColor(ColorMissed);
                			break;
                	// incoming SMS
                	case ActLogTableHelper.MESSAGE_TYPE_INBOX:
                			holder.fMsgData.setVisibility(View.VISIBLE  );
                			holder.fDuration.setVisibility(View.GONE);
           				
                			name_view.setTextColor(ColorIncoming);
                			holder.fNumber.setTextColor(ColorIncoming);
                			
                			holder.fImg.setImageResource(R.drawable.incomingsms);
                			holder.fMsgData.setText(m_CallCursor.getString(ActLogTableHelper._fdata));
                			
                			break;
                	// outgoing SMS		
                	case ActLogTableHelper.MESSAGE_TYPE_OUTBOX:
                	case ActLogTableHelper.MESSAGE_TYPE_QUEUED:
                			name_view.setTextColor(ColorOutgoing0);
                			holder.fNumber.setTextColor(ColorOutgoing0);
                	case ActLogTableHelper.MESSAGE_TYPE_SENT:
            				holder.fMsgData.setVisibility(View.VISIBLE  );
            				holder.fDuration.setVisibility(View.GONE);
            				name_view.setTextColor(ColorOutgoing);
                			holder.fNumber.setTextColor(ColorOutgoing);
            				holder.fImg.setImageResource(R.drawable.outgoingsms);
            				holder.fMsgData.setText(m_CallCursor.getString(ActLogTableHelper._fdata));
            				break;
                	
                }//switch(type)
                
                /*
        		if (!mBusy) 
        		{
        			
        			Log.d("PAINT VIEW", "SCROLL_STATE_LIST"); 
        			holder.m_State=0;
        			final TempContact[] tmp=ActLogTableHelper.GetTempContactNames(new String[]{contactNumber},getContentResolver());
        			if(tmp!=null && tmp.length>0)
        				holder.ShowContactName(tmp[0].TryGetAltName());
        		}
        		else 
        			*/
        		{
        			holder.m_State=1;
        		}
        		
        		LogTimeAfterStart("SET NUM "+ contactNumber+ " POS="+position);
                

        	}//if(m_CallCursor!=null )
        	return convertView;
        }//public View getView(int position, View convertView, ViewGroup parent)

//---------------------------------------------------------------------------------------        

		public void onReplyClick(View view) 
		{
			Log.d(TAG, "onReplyClick");
			
			//View parent=(View)view.getParent();
			//ViewHolder holder = (ViewHolder) parent.getTag();
			ImageView btn = (ImageView) view;
			View parent= (View) btn.getParent() ;
			ViewHolder holder = (ViewHolder) parent.getTag();
 
			          	
			
			if (holder!=null) 
			{
				final String contactNumber=(String) holder.fNumber.getText();
				
				m_CallCursor.moveToPosition(holder.m_Pos);
				final int type=m_CallCursor.getInt(ActLogTableHelper._ftype);
				switch(type)
                {
                	case ActLogTableHelper.GSM_CALL_INCOMING:
                	case ActLogTableHelper.GSM_CALL_OUTGOING:
                	case ActLogTableHelper.GSM_CALL_MISSED:
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
        
       
//---------------------------------------------------------------------------------------
		
		public boolean onLongReplyClick(View view) 
		{
			ImageView btn = (ImageView) view;
			View parent= (View) btn.getParent() ; 
			final ViewHolder holder = (ViewHolder) parent.getTag();          	
			if (holder!=null) 
			{
				
				Intent i = new Intent(mContext, DlgReply.class);
			    i.putExtra("ReplyOnActType", holder.m_Type );
			 	i.putExtra("ContactName", holder.getNameView().getText());
			 	i.putExtra("ContactNumber", holder.fNumber.getText() );
		    	startActivityForResult(i, 0);
		    	
				return true;
			}//if (holder!=null) 
			
			return false;
		}//public boolean onLongClick(View view)
        
    }//private static class MyListAdapter extends BaseAdapter
 //---------------------------------------------------------------------------------------
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
    	Log.d(TAG, "onActivityResult requestCode="+requestCode+" resultCode="+resultCode);
    	
    	if(data!=null && requestCode==0)
		{
			Bundle extras = data.getExtras();
			if (extras != null)
			{
				int SelectedAction = extras.getInt("SelectedAction");
				switch(SelectedAction)
				{
					default: break;
					case DlgReply.Call:
					{
						final String contactNumber = extras.getString("ContactNumber");
						startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + Uri.encode(contactNumber) )));
						break;
					}
					case DlgReply.SMS:
					{
						final String contactNumber = extras.getString("ContactNumber");
						Intent intent = new Intent(Intent.ACTION_VIEW);
	                	//sendIntent.putExtra("sms_body", "smsBody");
	                	//sendIntent.putExtra("address", "phoneNumber1;phoneNumber2;...");
	                	intent.setType("vnd.android-dir/mms-sms");
	                    Uri uri = Uri.parse("sms:"+ contactNumber);
	                    intent.setData(uri);
	                    startActivity(intent);
					}
						
				
				}//switch(SelectedAction)
			}//if (extras != null)
		}//if(data!=null && resultCode==0)
	}//protected void onActivityResult(int requestCode, int resultCode, Intent data)           






    
    
    
    
    
    
}