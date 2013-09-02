package org.bay.bbr2pdf;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrey Bogdanov <bay73@mail.ru>
 * @version 1.0
 * @since 2013-08-25
 *
 * Class to convert BBR markup data to pdf-file
 *
 */
public class BBRConverter {
    
    /*
     * Private staff
     */
    private PageScanner scanner;
    private CommonWriter writer;
    private int leftMargin;
    private int rightMargin;
    private int topMargin;
    private int bottomMargin;
    private int cpi;
    private int width;
    private int height;
    private List<String[]> pages;
    private OutputStream outStream;

    public BBRConverter(CommonWriter writer) {
        this.pages = new ArrayList<>();
        this.writer = writer;
    }

    /**
     * Main method which start conversion
     */
    public void process() throws IOException {
        width = rightMargin + leftMargin;
        height = topMargin + bottomMargin;
        writer.setPageSize(width, height, cpi);
        writer.open(outStream);
        writer.setMargins(leftMargin, topMargin);
        int pageCount = pages.size();
        for (int p = 0; p < pageCount; p++) {
            writer.nextPage();
            scanner = new PageScanner();
            scanner.setSource(pages.get(p));
            scanner.scanTo(writer);
        }
        writer.finish();
    }
    
    /*
     * setters
     */
    public void setCPI(int cpi) {
        this.cpi = cpi;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    /**
     * Set parameters of report from bbr description string The string has the
     * next format: BDDOTTLLHHH where B - is only a symbol DD - number of
     * character per inch O - page orientation 1 - portrait, 2 - landscape TT -
     * top margin LL - left margin HHH - page height
     *
     */
    public void setReportParams(String designText, int rightMargin) {
        setRightMargin(rightMargin);
        setLeftMargin(new Integer(designText.substring(4, 6).trim()));
        setTopMargin(new Integer(designText.substring(6, 8).trim()));
        setBottomMargin(new Integer(designText.substring(8).trim()));
        setCPI(new Integer(designText.substring(1, 3).trim()));
    }

    /**
     * Set data source for reading bbr-data it could be array, file or
     * sql-connection to oracle database
     */
    public void setSource(String[] lines) {
        pages.add(lines);
    }

    public void setSource(List<String> lines) {
        String[] reportlines = new String[lines.size()];
        reportlines = lines.toArray(reportlines);
        setSource(reportlines);
    }

    public void setSource(String fileName) throws FileNotFoundException, IOException {
        setSource(new File(fileName));
    }

    public void setSource(File file) throws FileNotFoundException, IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            setSource(lines);
        } finally {
            reader.close();
        }
    }

    public void setSource(Connection connection, Integer reportId, boolean bTemp) throws IOException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT RR.PageCount, RR.RightMargin, RT.DesignText "
                    + " FROM " + (bTemp ? "ReadyReport_Tmp" : "ReadyReport") + " RR, ReportTemplate RT"
                    + " WHERE RR.Classified = :1 and RR.ReportProc = RT.ReportProc(+)");
            ps.setInt(1, reportId);
            rs = ps.executeQuery();
            if (rs.next()) {
                int rM = rs.getInt(2);
                String designText = rs.getString(3);
                setReportParams(designText, rM);
            }
            rs.close();
            ps.close();
            ps = connection.prepareStatement(
                    "SELECT LineText, PageNumber, LineNumber, nPartCount"
                    + " FROM " + (bTemp ? "ReportLines_Tmp" : "ReportLines") + " "
                    + " WHERE Report = :1 ORDER BY PageNumber, LineNumber, nPartCount");
            ps.setInt(1, reportId);
            rs = ps.executeQuery();
            setSource(rs);
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void setSource(ResultSet rs) throws SQLException {
        ArrayList<String> lines = new ArrayList<>();
        int lastpage = -1;
        int lastrow = -1;
        StringBuilder str = new StringBuilder();
        while (rs.next()) {
            int page = rs.getInt(2);
            if (page != lastpage) {
                lines.add(str.toString());
                setSource(lines);
                lines = new ArrayList<>();
                str = new StringBuilder();
            }
            int row = rs.getInt(3);
            if (row != lastrow) {
                lines.add(str.toString());
                str = new StringBuilder();
            }
            str.append(rs.getString(1));
            lastrow = row;
            lastpage = page;
        }
        lines.add(str.toString());
        setSource(lines);
    }

    /**
     * Set target for writing pdf it could be file or stream
     */
    public void setTarget(String fileName) throws IOException {
        File file = new File(fileName);
        setTarget(file);
    }

    public void setTarget(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        setTarget(new FileOutputStream(file));
    }

    public void setTarget(OutputStream stream) {
        outStream = stream;
    }
}
