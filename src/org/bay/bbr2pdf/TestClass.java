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

        BBRConverter conv2 = new BBRConverter(new PDFWriter());
        conv2.setReportParams("B1211 1 76 ", 87);
        conv2.setSource("rep.bbr");
        conv2.setTarget("rep.pdf");
        conv2.process();

        Connection conn = null;
        try {
            int reportId = 1164506;
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@//host", "user", "password");
            BBRConverter converter = new BBRConverter(new PDFWriter());
            converter.setSource(conn, reportId, false);
            converter.setTarget("List.pdf");
            converter.process();
        } catch (SQLException | ClassNotFoundException ex) {
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
