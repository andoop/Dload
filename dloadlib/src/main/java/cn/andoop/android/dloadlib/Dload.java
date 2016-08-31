package cn.andoop.android.dloadlib;

import android.content.Context;

import cn.andoop.android.dloadlib.listener.DloadListener;
import cn.andoop.android.dloadlib.proxy.DloadProxy;

public class Dload {

	private Context context;
	private DloadProxy dloadProxy;
	
	public static Dload INSTANCE;
	private Dload(Context context){
		this.context = context;
	}
	public static Dload getInstance(Context context){
		
	  if(INSTANCE==null){
		  synchronized (Dload.class) {
			  if(INSTANCE==null){
				  INSTANCE=new Dload(context);
			  }
			
		}
	  }
	  return INSTANCE;
	}
	/**
	 * 初始化状态，例如：加载插件
	 */
	public void init(DloadListener dloadListener){
		DexExcutor.getInstance(context).init(dloadListener);
		dloadProxy=new DloadProxy();
	}
	/**
	 * 打开列表，最终会调用插件功能
	 */
	public void showList(){
		dloadProxy.showList(context);
	}
}
