package cn.andoop.android.dloadlib.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * 简单工具类
 * @author 黄栋
 *
 */
public class DloadUtils {
	
	public interface Downloadlistener{
		void onStart();
		void onProgress(int progress);
		void onFail(String err);
		void onSuccess();
	}

	public static void downloadPlugin(Context context,String srcfile,String destfile,
			Downloadlistener dloadListener) {
		
		AssetManager assetManager = context.getAssets();
		try {
			InputStream inputStream = assetManager.open(srcfile);
			FileOutputStream fileOutputStream = new FileOutputStream(new File(destfile));
		
			byte[] buf = new byte[1024];
			int ch = -1;
			int total = -1;
			while ((ch = inputStream.read(buf)) != -1) {
			
				total += ch; // total = total + len1
				if(dloadListener!=null){
					dloadListener.onProgress((int) ((total * 100) / 10000));
				}
				fileOutputStream.write(buf, 0, ch);
			}
			
			inputStream.close();
			fileOutputStream.close();
			
			if(dloadListener!=null){
				dloadListener.onSuccess();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(dloadListener!=null){
				dloadListener.onFail(e.getMessage());
			}
		}
		
		
	}

	
}
