package 自定义包名;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import com.packege.utils.ChineseToEnglish;
public class ExcelHelPer {
	String sql="INSERT INTO test(KW_System,KW_KeyWord,KW_KeyValue,KW_ProvName,KW_State,KW_PINYIN,KW_UpdateTime) VALUES(?,?,?,?,?,?,?)";
	DBHelper helper=new DBHelper(sql);//引入数据库帮助类
	private String readExcelFilePro(String filename) {	   
		StringBuffer result = new StringBuffer();
		String fileToBeRead = filename;
		Map<String, String> map = new HashMap<String,String>();
		// 创建对Excel工作簿文件的引用
		HSSFWorkbook workbook = null;
		try {
			workbook = new HSSFWorkbook(new FileInputStream(fileToBeRead));

			// 创建对工作表的引用。
			HSSFSheet sheet = workbook.getSheetAt(0);
			// HSSFSheet sheet = workbook.getSheet("Sheet1");
			// 便利所有单元格，读取单元格
			int row_num = sheet.getLastRowNum();
			for (int i = 2; i <= row_num; i++) {
				HSSFRow r = sheet.getRow(i);
				int cell_num = r.getLastCellNum();
				r.getCell(2).setCellType(Cell.CELL_TYPE_STRING);//设置读取字符的类型，这里我只遍历了行，根据我的需要提取的数据
				r.getCell(7).setCellType(Cell.CELL_TYPE_STRING);
				r.getCell(11).setCellType(Cell.CELL_TYPE_STRING);
				r.getCell(12).setCellType(Cell.CELL_TYPE_STRING);
				r.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
				map.put("row", r.getCell(0).getStringCellValue());
				map.put("KW_System", "指定微博");
				map.put("KW_KeyWord", r.getCell(1).getStringCellValue());
				map.put("KW_KeyValue", r.getCell(7).getStringCellValue());
				map.put("KW_ProvName", "#".equals(r.getCell(11).getStringCellValue()+"#"+r.getCell(12).getStringCellValue())?"":r.getCell(11).getStringCellValue()+"#"+r.getCell(12).getStringCellValue());
				map.put("KW_State", "1");
				map.put("KW_PINYIN",ToEnglish(r.getCell(11).getStringCellValue()+r.getCell(12).getStringCellValue()));
				map.put("KW_UpdateTime",getCurrentTime());				
				inserDbta(map);//插入到数据库	
				result.append("\n");
			}
			try {
				helper.pst.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					helper.pst.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("文件没找到 : " + e);
		} catch (IOException e) {
			System.out.println("已运行IO异常: " + e);
		}
		return result.toString();

	}
    private void inserDbta(Map map) {
		// TODO Auto-generated method stub	  
	   try{
		   helper.pst.setString(1,map.get("KW_System").toString());
		   helper.pst.setString(2,map.get("KW_KeyWord").toString());
		   helper.pst.setString(3,map.get("KW_KeyValue").toString());
		   helper.pst.setString(4,map.get("KW_ProvName").toString());
		   helper.pst.setString(5,map.get("KW_State").toString());
		   helper.pst.setString(6,map.get("KW_PINYIN").toString());
		   helper.pst.setString(7,map.get("KW_UpdateTime").toString());
		   helper.pst.addBatch();
	   }catch(Exception e){
		   e.printStackTrace();
	   }
	   
	}
	//遍历完整的行列
	public String readExcelFile(String filename) {  
	        StringBuffer result = new StringBuffer();  
	        String fileToBeRead = filename;  
	        Map map=new HashMap<>();
	        // 创建对Excel工作簿文件的引用  
	        HSSFWorkbook workbook = null;  
	        try {  
	            workbook = new HSSFWorkbook(new FileInputStream(fileToBeRead));  
	  
	            // 创建对工作表的引用。  
	            HSSFSheet sheet = workbook.getSheetAt(0);  
	            // HSSFSheet sheet = workbook.getSheet("Sheet1");  
	            // 便利所有单元格，读取单元格  
	            int row_num = sheet.getLastRowNum();  
	            for (int i = 2; i <= row_num; i++) {  
	                HSSFRow r = sheet.getRow(i);  
	                int cell_num = r.getLastCellNum();  
	                for (int j = 0; j < cell_num; j++) {  
	                    //System.out.println(r.getCell((short)j).getCellType());  
	                    if(r.getCell(j).getCellType() == 1){  
	                        result.append(r.getCell(j).getStringCellValue());
	                        
	                    }else{  
	                        result.append(r.getCell(j).getNumericCellValue());  
	                    } 	                 
	                    result.append("\t");  
	                }  
	                result.append("\n");  
	            }  
	        } catch (FileNotFoundException e) {  
	            System.out.println("文件没找到 : " + e);  
	        } catch (IOException e) {  
	            System.out.println("已运行IO异常: " + e);  
	        }  
	        return result.toString();  
	  
	    }  
    private String getCurrentTime(){
    	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:MM");
    	Calendar calendar=Calendar.getInstance();
    	return format.format(calendar.getTime());
    	
    }
    private String ToEnglish(String name){
    	ChineseToEnglish cte=new ChineseToEnglish();
    	return cte.getPingYin(name);    	
    }
	public static void main(String[] args) {
        ExcelHelPer helPer=new ExcelHelPer(); 
        String resPro = helPer.readExcelFilePro("E:/helllo.xls");          
	}
}
