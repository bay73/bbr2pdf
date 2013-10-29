/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bay.bbr2pdf;

import java.io.IOException;
import java.sql.*;

/**
 *
 * @author a.bogdanov
 */
public class Unloader {
    public static void ProcessQueue(Connection connection, String root) throws SQLException, IOException{
        PreparedStatement ps = null;
        ResultSet rs = null;
        BBRConverter pdfConverter = new BBRConverter(new PDFWriter());
        BBRConverter txtConverter = new BBRConverter(new TXTWriter());
        try {
            ps = connection.prepareStatement(
                    "SELECT RQ.Classified, RQ.ProcName, RQ.ParamString, RQ.FileName, RT.IsTemp, RT.DesignText, RQ.FileType "
                    + " FROM DMR_ReportQueue RQ, ReportTemplate RT"
                    + " WHERE RT.ReportProc = RQ.ProcName AND decode(Status,null,1,null) = 1");
            rs = ps.executeQuery();
            while (rs.next()) {
                int queueId = rs.getInt(1);
                String procName = rs.getString(2);
                String paramString = rs.getString(3);
                String fileName = rs.getString(4);
                boolean bTemp = (rs.getInt(5)==1);
                String designText = rs.getString(6);
                String fileType = rs.getString(7);
                CallableStatement cs = null;
                try{
                    BBRConverter converter = txtConverter;
                    if(fileType.equalsIgnoreCase("PDF")) 
                        converter = pdfConverter;
                    int reportId = execReport(connection, procName, paramString, bTemp, designText);
                    converter.setSource(connection, reportId, bTemp);
                    converter.setTarget(root + "/" + fileName);
                    converter.process();
                    removeReport(connection, reportId, bTemp);
                    saveLog(connection, queueId, null);
                    connection.commit();
                } catch (Exception ex){
                    connection.rollback();
                    saveLog(connection, queueId, ex.getMessage());
                    connection.commit();
                } finally{
                    if( cs!=null){
                        cs.close();
                    }
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }
    
    public static int execReport(Connection connection, String procName, String paramString, boolean bTemp, String designText) throws SQLException{
        CallableStatement cs = null;
        try {
            if(bTemp){
                cs = connection.prepareCall("begin REPORT.SetUseTemp(1); end;");
            }else{
                cs = connection.prepareCall("begin REPORT.SetUseTemp(0); end;");
            }
            cs.execute();
            cs.close();
            cs = connection.prepareCall("begin " + procName + "( " + paramString + ", " + designText.substring(8).trim() + ", 255, 0); end;");
            cs.execute();
            cs.close();
            cs = connection.prepareCall("begin REPORT.get_last_id( :1 ); end;");
            cs.registerOutParameter(1, java.sql.Types.INTEGER);
            cs.execute();
            return cs.getInt(1);
        } finally {
            if (cs != null) {
                cs.close();
            }
        }
    }

    public static void removeReport(Connection connection, int reportId, boolean bTemp) throws SQLException{
        CallableStatement cs = null;
        try {
            if(bTemp){
                cs = connection.prepareCall("begin ClearReportTemp( :1, 0 ); end;");
            }else{
                cs = connection.prepareCall("begin DeleteReadyReportEngine( :1, 0 ); end;");
            }
            cs.setInt(1, reportId);
            cs.execute();
        } finally {
            if (cs != null) {
                cs.close();
            }
        }
    }
    
    public static void saveLog(Connection connection, int queueId, String message) throws SQLException{
        CallableStatement cs = null;
        try {
            if(message != null){
                cs = connection.prepareCall("UPDATE DMR_ReportQueue SET Status = 1, Message = substr(:1, 1, 2000), Processed = sysdate WHERE Classified = :2");
                cs.setString(1, message);
                cs.setInt(2, queueId);
            }else{
                cs = connection.prepareCall("UPDATE DMR_ReportQueue SET Status = 0, Processed = sysdate WHERE Classified = :1");
                cs.setInt(1, queueId);
            }
            cs.execute();
        } finally {
            if (cs != null) {
                cs.close();
            }
        }
        
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        String connectionString = "";
        String user = "";
        String password = "";
        String root = "";
        for(String arg: args){
            String value[] = arg.split("=");
            if (value.length>1){
                if(value[0].equalsIgnoreCase("user")){
                    user = value[1];
                }
                if(value[0].equalsIgnoreCase("password") || value[0].equalsIgnoreCase("pwd")){
                    password = value[1];
                }
                if(value[0].equalsIgnoreCase("connection") || value[0].equalsIgnoreCase("tns")){
                    connectionString = value[1];
                }
                if(value[0].equalsIgnoreCase("path") || value[0].equalsIgnoreCase("root")){
                    root = value[1];
                }
            }
        }
        if(connectionString.length()==0){
            System.out.println("Database Connection string is not specified. Add tns=<jdbc string> to the call.");
            System.exit(1);
        }
        if(user.length()==0){
            System.out.println("User name for database connection is not specified. Add user=<user> to the call.");
            System.exit(1);
        }
        if(password.length()==0){
            System.out.println("Password for database connection is not specified. Add pwd=<password> to the call.");
            System.exit(1);
        }
        PDFWriter.setFontPaths("Fonts/plain.ttf", "Fonts/italic.ttf");
        Connection connection = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(connectionString, user, password);
            Unloader.ProcessQueue(connection, root);
        } catch(Exception ex){
            System.out.println("Error occured while executing:");
            ex.printStackTrace(System.out);
            System.exit(1);
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
