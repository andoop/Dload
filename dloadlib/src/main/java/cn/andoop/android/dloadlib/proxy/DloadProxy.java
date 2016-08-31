package cn.andoop.android.dloadlib.proxy;

import android.content.Context;
import android.widget.Toast;

import cn.andoop.android.dloadlib.DexExcutor;
import cn.andoop.android.dloadlib.interfaces.Idload;


/**
 * 
 * @author 黄栋
 *  动态插件的代理，代理插件行为，实际调用插件方法
 */
public class DloadProxy implements Idload {
  
	@Override
	public void showList(Context context) {
		Idload newInstance = (Idload) DexExcutor.getInstance(context).newInstance("cn.andoop.android.dloadplugin.DloadImp");
		if(newInstance==null){

			Toast.makeText(context, "插件还没有加载好", Toast.LENGTH_SHORT).show();
			return;
		}
		newInstance.showList(context);
	}
}
