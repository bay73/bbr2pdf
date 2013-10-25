package org.bay.bbr2pdf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        PDFWriter.setFontPaths("C:\\Windows\\Fonts\\lucon.ttf", "C:\\Windows\\Fonts\\courbi.ttf");

        BBRConverter converter = new BBRConverter(new PDFWriter());
        converter.setReportParams("B1211 1 76 ", 87);
        converter.addSource("rep.bbr");
        converter.setTarget("rep.pdf");
        converter.process();

        Connection conn = null;
        try {
            int reportId = 1164516;
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@", "", "");
            converter.setSource(conn, reportId, false);
            converter.setTarget("List.pdf");
            converter.process();
        } catch (Exception ex) {
            Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
