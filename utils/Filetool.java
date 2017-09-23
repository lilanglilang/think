package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件工具类
 * 
 * @author jiqinlin
 *
 */
public class Filetool {

	// uuid
	public static String createUUID() {
		return String.valueOf(UUID.randomUUID()).replaceAll("-", "");
	}

	/**
	 * 创建目录
	 * 
	 * @param dir
	 *            目录
	 */
	public static void mkdir(String dir) throws Exception {
		try {
			String dirTemp = dir;
			File dirPath = new File(dirTemp);
			if (!dirPath.exists()) {
				dirPath.mkdir();
			}
		} catch (Exception e) {
			throw new RuntimeException("创建目录操作出错: " + ExceptionUtil.getExceptionMessage(e));
		}
	}

	/**
	 * 新建文件
	 * 
	 * @param fileName
	 *            String 包含路径的文件名 如:E:\phsftp\src\123.txt
	 * @param content
	 *            String 文件内容
	 * 
	 */
	public static void createNewFile(String dir, String fileName, String content) throws Exception {
		try {
			String dirTemp = dir;
			File dirPath = new File(dirTemp);
			if (!dirPath.exists()) {
				dirPath.mkdirs();
			}

			String fileNameTemp = fileName;
			File filePath = new File(dir + "/" + fileNameTemp);
			if (!filePath.exists()) {
				filePath.createNewFile();
			}

			String strContent = content;
			Writer outTxt = new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8");
			outTxt.write(strContent);
			outTxt.flush();
			outTxt.close();
		} catch (Exception e) {
			throw new RuntimeException("新建文件操作出错: " + ExceptionUtil.getExceptionMessage(e));
		}

	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 *            包含路径的文件名
	 */
	public static String delFile(String fileName) throws Exception {
		try {
			String filePath = fileName;
			File delfile = new File(fileName);
			if (delfile.isDirectory()) {
				return "您输入的文件名是一个目录，不是文件，不能删除！";
			}
			if (!delfile.exists()) {
				return "对不起您输入的文件不存在，无法删除！";
			}
			delfile.delete();
		} catch (Exception e) {
			throw new RuntimeException("删除文件操作出错: " + ExceptionUtil.getExceptionMessage(e));
		}
		return "文件删除成功！";
	}

	/**
	 * 删除文件夹
	 * 
	 * @param folderPath
	 *            文件夹路径
	 */
	public static void delFolder(String folderPath) throws Exception {
		try {
			// 删除文件夹里面所有内容
			delAllFile(folderPath);
			String filePath = folderPath;
			File myFilePath = new File(filePath);
			// 删除空文件夹
			myFilePath.delete();
		} catch (Exception e) {
			throw new RuntimeException("删除文件夹操作出错: " + ExceptionUtil.getExceptionMessage(e));
		}
	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * @param path
	 *            文件夹路径
	 */
	public static void delAllFile(String path) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] childFiles = file.list();
		File temp = null;
		for (int i = 0; i < childFiles.length; i++) {
			// File.separator与系统有关的默认名称分隔符
			// 在UNIX系统上，此字段的值为'/'；在Microsoft Windows系统上，它为 '\'。
			if (path.endsWith(File.separator)) {
				temp = new File(path + childFiles[i]);
			} else {
				temp = new File(path + File.separator + childFiles[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + childFiles[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + childFiles[i]);// 再删除空文件夹
			}
		}
	}

	/**
	 * 复制单个文件
	 * 
	 * @param srcFile
	 *            包含路径的源文件 如：E:/phsftp/src/abc.txt
	 * @param dirDest
	 *            目标文件目录；若文件目录不存在则自动创建 如：E:/phsftp/dest
	 * @throws IOException
	 */
	public static void copyFile(String srcFile, String dirDest) throws Exception {
		try {
			FileInputStream in = new FileInputStream(srcFile);
			mkdir(dirDest);
			FileOutputStream out = new FileOutputStream(dirDest + "/" + new File(srcFile).getName());
			int len;
			byte buffer[] = new byte[1024];
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("复制文件操作出错:" + ExceptionUtil.getExceptionMessage(e));
		}
	}

	/**
	 * 复制文件夹
	 * 
	 * @param oldPath
	 *            String 源文件夹路径 如：E:/phsftp/src
	 * @param newPath
	 *            String 目标文件夹路径 如：E:/phsftp/dest
	 * @return boolean
	 */
	public static void copyFolder(String oldPath, String newPath) throws Exception {
		try {
			// 如果文件夹不存在 则新建文件夹
			mkdir(newPath);
			File file = new File(oldPath);
			String[] files = file.list();
			File temp = null;
			for (int i = 0; i < files.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + files[i]);
				} else {
					temp = new File(oldPath + File.separator + files[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
					byte[] buffer = new byte[1024 * 2];
					int len;
					while ((len = input.read(buffer)) != -1) {
						output.write(buffer, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + files[i], newPath + "/" + files[i]);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("复制文件夹操作出错:" + ExceptionUtil.getExceptionMessage(e));
		}
	}

	/**
	 * 移动文件到指定目录
	 * 
	 * @param oldPath
	 *            包含路径的文件名 如：E:/phsftp/src/ljq.txt
	 * @param newPath
	 *            目标文件目录 如：E:/phsftp/dest
	 */
	public static void moveFile(String oldPath, String newPath) throws Exception {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * 移动文件到指定目录，不会删除文件夹
	 * 
	 * @param oldPath
	 *            源文件目录 如：E:/phsftp/src
	 * @param newPath
	 *            目标文件目录 如：E:/phsftp/dest
	 */
	public static void moveFiles(String oldPath, String newPath) throws Exception {
		copyFolder(oldPath, newPath);
		delAllFile(oldPath);
	}

	/**
	 * 移动文件到指定目录，会删除文件夹
	 * 
	 * @param oldPath
	 *            源文件目录 如：E:/phsftp/src
	 * @param newPath
	 *            目标文件目录 如：E:/phsftp/dest
	 */
	public static void moveFolder(String oldPath, String newPath) throws Exception {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}

	/**
	 * 读取数据
	 * 
	 * @param inSream
	 * @param charsetName
	 * @return
	 * @throws Exception
	 */
	public static String readData(String filename) throws Exception {
	    File file=new File(filename);
	    if(!file.exists()){
	    	return "输入的文件路径不对，可以根据相对路径输入(\"src/utils/hello.java\")，也可以更具绝对路径输入！(E:/filedir/filename)";
	    }
		BufferedReader br = null;
		StringBuffer data = new StringBuffer();
		try {
			// 构造BufferedReader对象
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "gbk"));//ide用的是UTF-8然而Windows用的是gbk,所以出现了中文乱码
			// br = new BufferedReader(new FileReader(filename));
			String line = null;
			while ((line = br.readLine()) != null) {
				// 将文本打印到控制台
				data.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭BufferedReader
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data.toString();
	}
	
	/**
	 * 
	 * @param dirname 传入文件夹名称
	 * @return 返回所有的文件名称组成的字符串
	 */
   public static String getAllFileName(String dirname){
	   File file=new File(dirname);
	   File []files=file.listFiles();
	   StringBuffer buffer=new StringBuffer("");
	   for(int i=0;i<files.length;i++){
		 buffer.append(files[i].getName()+",");	    
	   }	  
	   return buffer.toString().substring(0,buffer.toString().length()-1);
   }

}
