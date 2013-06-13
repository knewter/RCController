package com.isotope11.rccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;
import android.os.AsyncTask;

public class MainActivity extends Activity {

  protected final String TAG = MainActivity.class.toString();
  protected ScriptingContainer mRubyContainer;
  
  double mLeft = 0;
  double mRight = 0;
  
  protected TextView mLeftValue;
  protected TextView mRightValue;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setupJRuby();
    
	final Activity lol = this;
	
	TimerTask task = new TimerTask() {
		public void run(){
			TankSetter task = new TankSetter();
			task.execute();
		}
	};
	Timer timer = new Timer();
	timer.schedule(task, 0, 50);
	
    final VerticalSeekBar leftStick = (VerticalSeekBar) findViewById(R.id.leftStick);
    final VerticalSeekBar rightStick = (VerticalSeekBar) findViewById(R.id.rightStick);
    mLeftValue = (TextView) findViewById(R.id.leftValue);
    mRightValue = (TextView) findViewById(R.id.rightValue);

    leftStick.setProgress(50);
    mLeftValue.setText(Double.toString(0));
    leftStick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        double stickValue = mapStickValueToMotorValue(i);
        if(stickValue != 0.0){
          mLeft = stickValue;	
        }
        mLeftValue.setText(Double.toString(stickValue));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // NOTE: This should happen, but it moves the thumb to the bottom for some inexplicable reason
        leftStick.setProgress(50);
        mLeft = 0.01;
        mLeftValue.setText(Double.toString(mapStickValueToMotorValue(50)));
      }
    });
    
    rightStick.setProgress(50);
    mRightValue.setText(Double.toString(0));
    rightStick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        double stickValue = mapStickValueToMotorValue(i);
        if(stickValue != 0.0){
          mRight = stickValue;	
        }
        mRightValue.setText(Double.toString(stickValue));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // NOTE: This should happen, but it moves the thumb to the bottom for some inexplicable reason
        rightStick.setProgress(50);
        mRight = 0.01;
        mRightValue.setText(Double.toString(mapStickValueToMotorValue(50)));
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  protected double mapStickValueToMotorValue(int stickValue) {
    double middle = 50;
    double middleMapped = (double) stickValue - middle;
    return middleMapped / middle;
  }
  
  protected double getLeft(){
	return mLeft;
  }
	  
  protected double getRight(){
	return mRight;
  }
  
  protected void setLeft(double val){
	    mLeft = val;
	  }
		
  protected void setRight(double val){
	    mRight = val;
	  }
		
  private void setupJRuby(){
	System.setProperty("jruby.bytecode.version", "1.5");
	mRubyContainer = new ScriptingContainer();
	List<String> loadPaths = new ArrayList<String>();
	loadPaths.add("jruby.home/lib/ruby/shared");
	loadPaths.add("jruby.home/lib/ruby/1.8");
	mRubyContainer.setLoadPaths(loadPaths);
  }
  
	private class TankSetter extends AsyncTask<Object, Void, String> {

		@Override
		protected String doInBackground(Object... arg0) {
			double left = getLeft();
			double right = getRight();
			if(left != 0.0 && right != 0.0){
				String rubyDrbClient = "unless($tank);" +
						"require 'drb/drb';" +
						"SERVER_URI='druby://192.168.1.82:8787';" +
						"DRb.start_service;" +
						"$tank = DRbObject.new_with_uri(SERVER_URI);" +
						"end;" +
						"$tank.set("
						+ Double.toString(left)
						+ ", "
						+ Double.toString(right)
						+ ")";
				mRubyContainer.runScriptlet(rubyDrbClient);
			}
			Log.d(TAG, "Complete");
			return "nope";
		}

		@Override
		protected void onPostExecute(String result) {
			
		}

	}
}