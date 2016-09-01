package cn.andoop.android.dloadlib;

import android.content.Context;
import android.util.Log;



import java.io.File;

import cn.andoop.android.dloadlib.listener.DloadListener;
import cn.andoop.android.dloadlib.utils.DloadUtils;
import dalvik.system.DexClassLoader;

/**
 * 插件管理、加载、维护
 * @author 黄栋
 *
 */
public class DexExcutor {

	public static DexExcutor INSTANCE;
	private Context context;
	private DloadListener dloadListener;
	private String filepath;
	private DexClassLoader dexClassLoader;
	private DexExcutor(Context context){
		this.context = context;
		filepath=context.getFilesDir().getAbsolutePath();
	}
	public static DexExcutor getInstance(Context context){
		
	  if(INSTANCE==null){
		  synchronized (Dload.class) {
			  if(INSTANCE==null){
				  INSTANCE=new DexExcutor(context);
			  }
			
		}
	  }
	  return INSTANCE;
	}
	
	public void init(DloadListener dloadListener){
		this.dloadListener = dloadListener;
		if(dloadListener!=null)
			dloadListener.onStart();
		
		//检查插件是否存在
		File file = new File(filepath+File.separator+MConstans.jarname);
		if(file.exists()){
			//存在，加载插件
			loadPlugin();
		}else{
			//不存在，则将插件从asset或者服务端加载到本地
			downloadPlugin();
		}
	}
	
	private void downloadPlugin() {
		// TODO Auto-generated method stub
		DloadUtils.downloadPlugin(context,"plugin.jar",filepath+File.separator+MConstans.jarname,new DloadUtils.Downloadlistener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				loadPlugin();
			}
			
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgress(int progress) {
				// TODO Auto-generated method stub
				if(dloadListener!=null){
					dloadListener.onProgress(progress);
				}
			}
			
			@Override
			public void onFail(String err) {
				// TODO Auto-generated method stub
				if(dloadListener!=null){
					dloadListener.onFail(err);
				}
			}
		});
		
	}
	/**
	 * 加载插件
	 */
	private void loadPlugin() {
		
		try {
			dexClassLoader = new DexClassLoader(filepath+File.separator+MConstans.jarname, filepath,null, context.getClassLoader());
			if(dloadListener!=null){
				dloadListener.onProgress(100);
				dloadListener.onSuccess();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(dloadListener!=null)
				dloadListener.onFail(e.getMessage());
		}
	}
	
	/**
	 * 根据类名获取一个实例
	 * 
	 * @param className
	 *            例:cn.aow.android.impl.DImplement
	 * @return Object
	 */
	public Object newInstance(String className) {
		try {
			if (null != dexClassLoader) {
				Class<?> dynamic_class = dexClassLoader.loadClass(className);
				return dynamic_class.newInstance();
			}
		} catch (ClassNotFoundException e) {
			Log.e(">>>", "newInstance:" + e.getMessage());
		} catch (InstantiationException e) {
			Log.e(">>>", "newInstance:" + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(">>>", "newInstance:" + e.getMessage());
		}
		return null;
	}

	
}
