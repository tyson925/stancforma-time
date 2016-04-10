package hu.stancforma.excel

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.*


public class CreateExcel(){

    public fun creatXls(){
        //Blank workbook
        val workbook = XSSFWorkbook();

        //Create a blank sheet
        val sheet = workbook.createSheet("Employee Data");
        //val data = TreeMap <String, Any[]>();

    }

    public fun writeWorkBook(workbook : XSSFWorkbook){

    }


}

fun main(args: Array<String>) {

}

