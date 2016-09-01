package cn.andoop.android.dloadlib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import cn.andoop.android.dloadlib.MConstans;

/**
 * 资源工具类
 * 
 * @author 黄栋
 *
 */
public class ResourceUtils {
	private Context mContext;
	private String mFilePath;

	public ResourceUtils(Context context) {
		this.mContext = context;
		// data/data/packName/files/
		this.mFilePath = context.getFilesDir().getAbsolutePath()
				+ File.separator;
	}

	/**
	 * 根据文件名获取一个图片资源
	 * 
	 * @param fileName
	 * @return
	 */
	public Bitmap getImageResouce(String fileName) {
		Bitmap bitmap = getImageFromPrivateSpace(fileName);
		if (null != bitmap) {
			Log.e(">>>","get bp success");
		}
		if (null == bitmap) {
			// 获取SDK保存的jar文件名称，获取资源
			String jarPath = mFilePath+ MConstans.jarname;
			bitmap = getImageFromDataFileJar(jarPath, fileName);
		}
		
		if (null == bitmap) {
			// 遍历找到jar包，然后获取资源
			bitmap = getImageFromDataFileJar(fileName);
		}

		return bitmap;
	}

	/**
	 * 从私有空间中获取资源图片
	 * 
	 * @param fileName
	 * @return
	 */
	private Bitmap getImageFromPrivateSpace(String fileName) {
		File mFile = new File(mFilePath + fileName);
		// 若该文件存在
		if (mFile.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(mFilePath + fileName);
			return bitmap;
		}
		return null;
	}

	/**
	 * 通过jar文件中获取资源图片
	 * 
	 * @param jarPath
	 *            jar文件的路径
	 * @param fileName
	 *            文件名 "assets/images/xxx.png"
	 * @return
	 */
	private Bitmap getImageFromDataFileJar(String jarPath, String fileName) {
		JarFile jarfile = null;
		InputStream is = null;
		try {
			jarfile = new JarFile(jarPath);
			if (null != jarfile) {
				ZipEntry zipEntry = jarfile.getEntry(fileName);
				if (null != zipEntry) {
					is = jarfile.getInputStream(zipEntry);
					// 将inputStream流转为bmp对象
					Bitmap bmp = BitmapFactory.decodeStream(is);
					is.close();
					jarfile.close();
					//解压找到资源对应的jar
					new ZipTool().execute(jarPath, mFilePath);
					return bmp;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				is.close();
				jarfile.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 通过默认路径的jar文件获取资源图片
	 * 
	 * @param fileName
	 *            文件名 "res/drawable-mdpi/ic_launcher.png"
	 * @return Bitmap
	 */
	private Bitmap getImageFromDataFileJar(String fileName) {
		String jarPath = iteratorFolder(mFilePath);
		return getImageFromDataFileJar(jarPath, fileName);
	}

	/**
	 * 遍历获取根目录下.jar文件
	 * 
	 * @param path
	 * @return
	 */
	private String iteratorFolder(String path) {
		File file = new File(path);
		if (file.exists()) {
			File[] files = file.listFiles();
			if (files.length == 0) {
				return null;
			} else {
				for (File subFile : files) {
					String subFilePath = subFile.getAbsolutePath();
					if (!subFile.isDirectory()) {
						String prefix = subFilePath.substring(subFilePath
								.lastIndexOf(".") + 1);
						if ("jar".equals(prefix)) {
							return subFilePath;
						}
					}
				}
			}
		} else {
		}
		return null;
	}
}
