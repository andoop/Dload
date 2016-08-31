package cn.andoop.android.dloadplugin;

import android.content.Context;

import cn.andoop.android.dloadlib.DLoadActivity;
import cn.andoop.android.dloadlib.interfaces.Idload;


public class DloadImp implements Idload {

	@Override
	public void showList(Context context) {
		// TODO Auto-generated method stub
		DLoadActivity.start(context, "cn.andoop.android.dloadplugin.ui.ListFragment", null);
	}

	
}
