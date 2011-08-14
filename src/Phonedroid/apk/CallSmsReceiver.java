package Phonedroid.apk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallSmsReceiver extends BroadcastReceiver 
{
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

		      Log.d("DEBUG", "OFFHOOK");

		    break;

		    case TelephonyManager.CALL_STATE_RINGING:

		      Log.d("DEBUG", "RINGING");

		    break;

		    }
		  } 

	}	
	@Override
	public void onReceive(Context context, Intent intent)
	{
	    MyPhoneStateListener phoneListener=new MyPhoneStateListener();

	    TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

	    Log.d("DEBUG", "onReceive");
	    
	    telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	}

	

}
