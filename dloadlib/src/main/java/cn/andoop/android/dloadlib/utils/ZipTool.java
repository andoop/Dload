package cn.andoop.android.dloadlib.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * zip工具类,用于处理下载模板等操作
 * 
 * @author liaonaibo
 * 
 */
public class ZipTool {

	public boolean execute(String from, String to) {
		int index = from.lastIndexOf("/");
		String absFileName = from.substring(index);
		String baseDir = from.substring(0, index);
		File zipFile = getRealFileName(baseDir, absFileName);
		try {
			upZipFile(zipFile, to);
			return true;
		} catch (ZipException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private int upZipFile(File zipFile, String folderPath) throws ZipException,
			IOException {
		ZipFile zfile = new ZipFile(zipFile);
		Enumeration<? extends ZipEntry> zList = zfile.entries();
		ZipEntry ze = null;
		byte[] buf = new byte[1024];
		File ffolderPath = new File(folderPath);
		if (!ffolderPath.exists()) {
			ffolderPath.mkdir();
		}
		while (zList.hasMoreElements()) {
			ze = (ZipEntry) zList.nextElement();
			if (ze.isDirectory()) {
				String dirstr = folderPath + ze.getName();
				// dirstr.trim();
				dirstr = new String(dirstr.getBytes("utf-8"),"utf-8");
				File f = new File(dirstr);
				f.mkdir();
				continue;
			}
			if (getRealFileName(folderPath, ze.getName()) != null) {
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(getRealFileName(folderPath,
								ze.getName())));
				InputStream is = new BufferedInputStream(
						zfile.getInputStream(ze));
				int readLen = 0;
				while ((readLen = is.read(buf, 0, 1024)) != -1) {
					os.write(buf, 0, readLen);
				}
				is.close();
				os.close();
			}

		}
		zfile.close();
		return 0;
	}

	private File getRealFileName(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		if (dirs.length < 2) {
			File ret1 = new File(baseDir);
			return new File(ret1, dirs[0]);
		}
		File ret = new File(baseDir);
		String substr = null;
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				substr = dirs[i];
				try {
					substr = new String(substr.getBytes("utf-8"), "utf-8");

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ret = new File(ret, substr);

			}
			if (!ret.exists())
				ret.mkdirs();
			substr = dirs[dirs.length - 1];
			try {
				substr = new String(substr.getBytes("utf-8"),"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			ret = new File(ret, substr);
			return ret;
		}
		return ret;
	}
}
