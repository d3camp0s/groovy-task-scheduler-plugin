package com.tsoft.plugins.scheduler.utils

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.util.logging.Logger

class ExcelReader {

    protected static Logger log = Logger.getLogger(ExcelReader.class.getName())
    private static int MAXIMO_FILAS = 10000
    private static int MAXIMO_COLUMNAS = 100

    /**
     * Devuelve una lista de objetos[] a partir del nombre y la hoja del archivo excel
     * @param excelFile
     * @param numSheet
     * @return
     */
    static ArrayList<List<String>> read(File excelFile, int numSheet) {
        int nsheet = numSheet>0? numSheet:0
        ArrayList<List<String>> content = new ArrayList<List<String>>()

        try {
            FileInputStream fis = new FileInputStream(excelFile)
            int maxRows = 0
            // we create an XSSF Workbook object for our XLSX Excel File
            XSSFWorkbook workbook = new XSSFWorkbook(fis)
            XSSFSheet sheet = workbook.getSheetAt(nsheet)
            Iterator<Row> rowIt = sheet.iterator()

            while (rowIt.hasNext() && maxRows<MAXIMO_FILAS) {
                Row row = rowIt.next()
                List<String> cells = new ArrayList<String>()
                int ncols = 0
                // iterate on cells for the current row
                Iterator<Cell> cellIterator = row.cellIterator()
                while (cellIterator.hasNext() && ncols<MAXIMO_COLUMNAS) {
                    Cell cell = cellIterator.next()
                    cells.add(cell.toString())
                    ncols++
                }
                // Guardamos la fila en la lista
                content.add(cells)
                maxRows++
            }

            workbook.close()
            fis.close()

        }catch (FileNotFoundException e){
            throw new Exception(e.getMessage())
        } catch (IOException e) {
            throw new Exception(e.getMessage())
        }

        return content
    }

}
