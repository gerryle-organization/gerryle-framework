package com.gerryle.framework.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.fasterxml.jackson.core.format.InputAccessor;

/**
 * 文件系统工具
 * @author Gerryle 2018年2月6日 下午3:41:31
 */
public class FileUtil {
   /****************************************************************/
	public static final String SEPARATOR=File.separator;
	
	public static String standardizeDirPath(String path){
		return standardizeDirPath(new File(path));
	}

	public static String standardizeDirPath(File dir) {
		String formatedPath=dir.getPath();
		return formatedPath.isEmpty()?formatedPath:formatedPath+SEPARATOR;
	}
	
	public static String getChildDirPath(String standardParentPath,String childPath){
	   return standardizeDirPath(standardParentPath+childPath);
	}
	
	public static File getChildFile(String standardParentPath,String childPath){
		return new File(getChildDirPath(standardParentPath, childPath));
	}
	
	public static String getChildFilePath(String standardParentPath,String childPath){
		return standardParentPath+childPath;
	}
	/***************************************************************************************/
	
	public static String toAbsolutePath(String path) throws IOException{
		return new File(path).getCanonicalPath();
	}
	
	/**
	 * 读取配置文件
	 * @param file
	 * @return
	 * @author Gerryle 2018年2月6日 下午3:57:16
	 */
	public static Properties getConfigProperties(File file){
		Properties properties=new Properties();
		try {
			FileInputStream fis=new FileInputStream(file);
			properties.load(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}
	
	public static Properties getConfigProperties(InputStream in){
		Properties properties=new Properties();
		try {
			properties.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}
	
	public static Properties getConfigProperties(BufferedReader br){//处理中文乱码问题
		Properties properties=new Properties();
		try {
		   properties.load(br);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}
	
	/**
	 * 读取配置文件
	 * @param fileName
	 * @return
	 * @author Gerryle 2018年2月6日 下午4:06:27
	 */
	public static Properties getConfigProperties(String fileName){
		
	}
}
