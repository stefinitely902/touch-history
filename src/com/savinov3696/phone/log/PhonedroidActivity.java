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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.savinov3696.phone.log.ActLogTableHelper.TempContact;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ViewFlipper;

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
    
	
	public void ShowContactName(ViewHolder holder,String name)
	{
		TextView oldview = ((TextView)holder.mFlipper.getCurrentView());
		
		holder.mFlipper.showNext();//if(animation_on)
		((TextView)holder.mFlipper.getCurrentView()).setText(name);
		((TextView)holder.mFlipper.getCurrentView()).setTextColor(oldview.getTextColors());
		
		holder.fNumber.setAnimation(holder.mFlipper.getInAnimation() );
		holder.fNumber.setVisibility(View.VISIBLE);
	}
	

    public void onScrollStateChanged(AbsListView view, int scrollState) 
    {
    	
    	switch (scrollState) 
        {
        	default:
    	    case OnScrollListener.SCROLL_STATE_FLING:
    	        	mBusy = true;
            		break;
        	case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL: // может быть во время двигания пальцем загружать ?
        			Log.d("PAINT VIEW","SCROLL_STATE_TOUCH_SCROLL");
        			//	mBusy = true;
        		    //break;
			case OnScrollListener.SCROLL_STATE_IDLE:
					Log.d("PAINT VIEW", "SCROLL_STATE_IDLE");
            		mBusy = false;
					
					//int first = view.getFirstVisiblePosition();
					final int count = view.getChildCount();
					
					//создадим справочник номер-holder элемнтов где надо поискать имена которые надо бы поискать)
					Map<String, ViewHolder > num_name =  new HashMap<String,ViewHolder>();
					// + создадим массив номеров по которым надо искать контакты
	            	for (int i=0; i<count ; i++)
	            	{
	            		final ViewHolder holder = (ViewHolder) view.getChildAt(i).getTag();
	            		if ( holder!=null && holder.m_State==1 )
	            		{
	            			final String number = (String)holder.fNumber.getText();//берём номер контака
	            			num_name.put(number,holder);
	            			holder.m_State=0;
	            		}
	            	}
	            	
	            	if(num_name.size()>0)
	            	{
	            		String[]  account = new String[num_name.size()];
		            	num_name.keySet().toArray(account);
		            	final TempContact[] tmp= ActLogTableHelper.GetTempContactNames(account,getContentResolver());//ищем имя	
		            	if(tmp!=null)
		            		for (int i=0;i<tmp.length;++i) 
		            		{
		            			final ViewHolder holder = num_name.get(tmp[i].m_Number);
	            				ShowContactName(holder,tmp[i].TryGetAltName() );
		            		}//for (int i=0;i<tmp.length;++i)
	            		
	            	}//if(num_name.size()>0)
	            	


            break;//case OnScrollListener.SCROLL_STATE_IDLE:
        
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
       
       TextView fNumber;
       TextView fDuration;
       TextView fDateTime;
       ImageView fImg;
       TextView fMsgData;
       ImageButton	btnReply;
       
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
       
       

    }
//---------------------------------------------------------------------------------------
    //static 
    private class MyListAdapter extends BaseAdapter implements ImageButton.OnClickListener , ImageButton.OnLongClickListener 
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
        		holder.btnReply = (ImageButton) convertView.findViewById(R.id.btnReply);
        		
        		holder.mFlipper = (ViewFlipper) convertView.findViewById(R.id.viewFlipper1);
        		
        		//holder.mContactNameView=(TextView) convertView.findViewById(R.id.ContactNameView);
        		//holder.fName = (TextView) convertView.findViewById(R.id.al_Text);
        		
        		holder.m_Pos=position;
        		holder.btnReply.setOnClickListener(this);
        		holder.btnReply.setOnLongClickListener(this);
        		convertView.setTag(holder);
        	}
        	else
        	{
        		holder = (ViewHolder) convertView.getTag();
        	}

    		if(position%2!=0)
    			convertView.setBackgroundColor(ColorDark);
    		else
    			convertView.setBackgroundColor(ColorBlack);

    		
        	if(m_CallCursor.moveToPosition(position) )
        	{
        		String callDate = (String) DateFormat.format("dd MMM.kk:mm", m_CallCursor.getLong(ActLogTableHelper._fdate) );
        		holder.fDateTime.setText(callDate);
        		
        		final String contactNumber= m_CallCursor.getString(ActLogTableHelper._faccount);
        		
        		holder.fNumber.setVisibility(View.INVISIBLE);
        		holder.fNumber.setAnimation(null);
        		holder.fNumber.setText(contactNumber);
        		
        		Log.d("PAINT VIEW", "SET NUM "+contactNumber);
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
                
        		if (!mBusy) 
        		{
        			
        			Log.d("PAINT VIEW", "SCROLL_STATE_LIST"); 
        			holder.m_State=0;
        			final TempContact[] tmp=ActLogTableHelper.GetTempContactNames(new String[]{contactNumber},getContentResolver());
        			if(tmp!=null && tmp.length>0)
        				ShowContactName(holder,tmp[0].TryGetAltName());
        		}
        		else 
        		{
        			holder.m_State=1;
        		}

                
                

        	}//if(m_CallCursor!=null )
        	return convertView;
        }//public View getView(int position, View convertView, ViewGroup parent)

//---------------------------------------------------------------------------------------        
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
//---------------------------------------------------------------------------------------
		@Override
		public boolean onLongClick(View view) 
		{
			ImageButton btn = (ImageButton) view;
			View parent= (View) btn.getParent() ; 
			ViewHolder holder = (ViewHolder) parent.getTag();          	
			if (holder!=null) 
			{
				final TextView name_view = holder.getNameView();
				//Toast.makeText(mContext,"Long press "+name_view.getText(),Toast.LENGTH_SHORT).show();
				/*Intent result = new Intent();
			       result.setClassName("com.savinov3696.phone.log", "com.savinov3696.phone.log.About");
			       startActivity(result);*/
				
				Intent i = new Intent(mContext, DlgReply.class);
				
			    i.putExtra("ReplyOnActType", holder.m_Type );
			 	i.putExtra("ContactName", name_view.getText());
			 	i.putExtra("ContactNumber", holder.fNumber.getText() );
		    	startActivityForResult(i, 0);
				/*
				final CharSequence[] myitems= {"call","sms"};
				Dialog dlg=new AlertDialog.Builder(PhonedroidActivity.this)
	                .setTitle("title")
	                .setItems(myitems, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                        //String[] items = getResources().getStringArray(myitems);
	                        new AlertDialog.Builder(PhonedroidActivity.this)
	                                .setMessage("You selected: " + which + " , " + myitems[which])
	                                .show();
	                    }
	                })
	                .create();
				dlg.show();
				*/
				return true;
			}//if (holder!=null) 
			
			return false;
		}//public boolean onLongClick(View view)
        
    }//private static class MyListAdapter extends BaseAdapter
 //---------------------------------------------------------------------------------------
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
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