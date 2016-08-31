package cn.andoop.android.dload;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cn.andoop.android.dloadlib.Dload;
import cn.andoop.android.dloadlib.listener.DloadListener;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dload.getInstance(this).init(new DloadListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Log.e(">>>","sccess");
			}
			
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				Log.e(">>>","start");
			}
			
			@Override
			public void onProgress(int persent) {
				// TODO Auto-generated method stub
				Log.e(">>>","progress>"+persent+"%");
			}
			
			@Override
			public void onFail(String err) {
				// TODO Auto-generated method stub
				Log.e(">>>","fail>"+err);
			}
		});
    }

  
    public void showList(View view){
    	Dload.getInstance(this).showList();
    }
}
