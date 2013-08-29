package org.bay.bbr2pdf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Andrey Bogdanov <bay73@mail.ru>
 * @version 1.0
 * @since 2013-08-25
 * 
 * Class to test BBR2PDFConverter
 * 
 */
public class TestClass {
    
         
    public static void main(String[] args) throws IOException {
        String[] reportlines = new String[]{
          "This is a sample report",
          "First report line",
          "Second report line",
          "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567",
          "5 &20m&10 &5n",
          "6 &a&10b&3c&d",
          "7 &e&10 &4e",
          "8 &f&10b&3g&h",
          "9 &i&10b&3j&k",
          "10",
          "11",
          "12",
          "13",
          "14",
          "15",
          "16",
          "17",
          "18",
          "19",
          "20",
          "21",
          "22",
          "23",
          "24",
          "25",
          "26",
          "27",
          "28",
          "29",
          "30",
          "31",
          "32",
          "33",
          "34",
          "35",
          "36",
          "37",
          "38",
          "39",
          "40",
          "41",
          "42",
          "43",
          "44",
          "45",
          "46",
          "47",
          "48",
          "49",
          "50",
          "51",
          "52",
          "53",
          "54",
          "55",
          "56",
          "57",
          "58",
          "59",
          "60",
          "61",
          "62",
          "63",
          "64",
          "65",
          "66",
          "67",
          "68",
          "69",
          "70",
          "71",
          "72",
          "73",
          "74",
          "75",
          "76"
        };
        
        List<String> lines1 = Arrays.asList(reportlines);
        BBR2PDFConverter conv1 = new BBR2PDFConverter();
        conv1.setReportParams("B1211 1 76 ", 87);
        conv1.setSource(lines1);
        conv1.setTarget("Hello World.pdf");
        conv1.process();
        
        BBR2PDFConverter conv2 = new BBR2PDFConverter();
        conv2.setReportParams("B1211 1 76 ", 87);
        conv2.setSource("rep.bbr");
        conv2.setTarget("rep.pdf");
        conv2.process();

        Connection conn = null;
        try {
            int reportId= 1164506;
            Class.forName ("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@//host", "user", "password");
            BBR2PDFConverter converter = new BBR2PDFConverter();
            converter.setSource(conn, reportId);
            converter.setTarget("List.pdf");
            converter.process();
        }catch ( SQLException | ClassNotFoundException ex) {
            Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(conn!=null) try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
