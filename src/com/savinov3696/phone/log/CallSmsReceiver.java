package com.savinov3696.phone.log;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallSmsReceiver extends BroadcastReceiver 
{
	/*
	public class MyPhoneStateListener extends PhoneStateListener 
	{

		public void onCallStateChanged(int state,String incomingNumber)
		  {

		  switch(state)
		  {

		    case TelephonyManager.CALL_STATE_IDLE:

		      Log.d("DEBUG", "IDLE");

		    break;

		    case TelephonyManager.CALL_STATE_OFFHOOK:

		      Log.d("DEBUG", "OFFHOOK исходящий дозвон ");

		    break;

		    case TelephonyManager.CALL_STATE_RINGING:

		      Log.d("DEBUG", "RINGING входящий дозвон");

		    break;

		    }
		  } 

	}
	*/	
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    //MyPhoneStateListener phoneListener=new MyPhoneStateListener();

	    TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

	    //Log.d("DEBUG", "BroadcastReceiver onReceive");
	    
	    //telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	    
	    //int call_state = telephony.getCallState();
	    
	    
	    
	    Bundle extras = intent.getExtras();
		if (extras != null && !extras.isEmpty() )
		{
			String str_state = extras.getString(TelephonyManager.EXTRA_STATE);
			String ext_nums = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			
			Log.d("DEBUG", "EXTRA_STATE="+str_state+" EXTRA_INCOMING_NUMBER="+ ext_nums );
					
		}//if (extras != null)

	
	}

	

}
