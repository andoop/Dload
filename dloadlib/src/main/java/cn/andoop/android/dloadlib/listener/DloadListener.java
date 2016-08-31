package cn.andoop.android.dloadlib.listener;

/**
 * 
 * @author 黄栋
 *
 *插件加载状态监听
 */
public interface DloadListener {

	void onStart();
	void onSuccess();
	void onFail(String err);
	void onProgress(int persent);
	
}
