package com.savinov3696.phone.log;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public class DlgReply extends Activity 
{
	public int 		m_ReplyOnActType=0;
	public String	m_ContactName;
	public String	m_ContactNumber;
	
	final static int Call=1;
	final static int SMS=2;
	
	public int		m_SelectedAction=0;
//---------------------------------------------------------------------------------------	
	@Override
	protected void onCreate(Bundle savedInstanceState)
   	{
      	setContentView(R.layout.dlgreply);
      	
		final TableRow TableRow1 = (TableRow) findViewById(R.id.tableRow1);
		final TableRow TableRow2 = (TableRow) findViewById(R.id.tableRow2);
        
        TableRow1.setOnTouchListener(new TableRow.OnTouchListener()
										{
											public boolean onTouch(View v, MotionEvent event) 
											{
												return MakeCall();
											}
        								} );

        TableRow2.setOnTouchListener(new TableRow.OnTouchListener()
										{
											public boolean onTouch(View v, MotionEvent event) 
											{
												return MakeSMS();
											}
        								} );
        								
        						
      
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
		{
			m_ReplyOnActType = extras.getInt("ReplyOnActType");
			m_ContactName = extras.getString("ContactName");
			m_ContactNumber = extras.getString("ContactNumber");
			
			if(m_ContactNumber!=null)
			{
				final TextView number = (TextView) findViewById(R.id.dlgreply_number);
				final TextView name = (TextView) findViewById(R.id.dlgreply_name);
				 number.setText(m_ContactNumber);
				    
				if(m_ContactName!=null && !m_ContactName.contentEquals(m_ContactNumber) ) 
				{
					setTitle(m_ContactName);
					name.setText(m_ContactName);
					number.setVisibility(View.VISIBLE);
					//number.setTextColor(0xFFFF0000);
				}
				else
				{
					setTitle(m_ContactNumber);
					name.setText(m_ContactNumber);
					
					number.setVisibility(View.GONE);
					//number.setTextColor(0xFFFF00FF);
				}
			

			}//if(m_ContactNumber!=null)
   		}//if (extras != null)
		
		super.onCreate(savedInstanceState);
	}//protected void onCreate(Bundle savedInstanceState)
//---------------------------------------------------------------------------------------	
	public boolean MakeCall()
	{
		m_SelectedAction=Call;
		      	
		final TableRow TableRow1 = (TableRow) findViewById(R.id.tableRow1);
		
		TableRow1.setSelected(true);
        
		finish();
		return true;
	}
//---------------------------------------------------------------------------------------	
	public boolean MakeSMS()
	{
		m_SelectedAction=SMS;
		
		final TableRow TableRow2 = (TableRow) findViewById(R.id.tableRow2);
		TableRow2.setSelected(true);
		
		finish();
		return true;		
	}
//---------------------------------------------------------------------------------------	
	@Override
	public void finish() 
	{
		Intent data = new Intent();
		data.putExtra("ContactName", m_ContactName);
		data.putExtra("ContactNumber", m_ContactNumber);
		data.putExtra("SelectedAction", m_SelectedAction);
		setResult(m_SelectedAction, data);
		super.finish();
	}	
		
	
	
	
	
	
	
}//public class DlgReply extends Activity 