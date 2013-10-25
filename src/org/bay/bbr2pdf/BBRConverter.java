package org.bay.bbr2pdf;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
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
    private Iterable<String[]> pages;
    private OutputStream outStream;

    public BBRConverter(CommonWriter writer) {
        this.writer = writer;
    }

    /**
     * Main method which makes conversion
     */
    public void process() throws IOException {
        try{
            start();
            for (String[] page : pages) {
                writer.nextPage();
                processPage(page);
            }
        }finally{
            finish();
        }
    }

    public void start() throws IOException {
        width = rightMargin + leftMargin;
        height = topMargin + bottomMargin;
        writer.setPageSize(width, height, cpi);
        writer.open(outStream);
        writer.setMargins(leftMargin, topMargin);
    }

    public void finish() throws IOException {
        writer.finish();
        if(pages instanceof ResultSetReader){
            ResultSet rs = ((ResultSetReader)pages).rs;
            if(rs!=null){
                try {
                    rs.getStatement().close();
                    rs.close();
                } catch (SQLException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }

    public void processPage(String[] page) throws IOException {
        scanner = new PageScanner();
        scanner.setSource(page);
        scanner.scanTo(writer);
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
    public void clearSource() {
        pages = null;
    }

    public void addSource(String[] lines) {
        if (pages == null) {
            pages = new ArrayList<String[]>();
        }
        if (pages instanceof ArrayList) {
            ((ArrayList) pages).add(lines);
        }
    }

    public void addSource(List<String> lines) {
        String[] reportlines = new String[lines.size()];
        reportlines = lines.toArray(reportlines);
        addSource(reportlines);
    }

    public void addSource(String fileName) throws FileNotFoundException, IOException {
        addSource(new File(fileName));
    }

    public void addSource(File file) throws FileNotFoundException, IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            addSource(lines);
        }finally{
            if(reader != null)
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
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
        ps = connection.prepareStatement(
                "SELECT LineText, PageNumber, LineNumber, nPartCount"
                + " FROM " + (bTemp ? "ReportLines_Tmp" : "ReportLines") + " "
                + " WHERE Report = :1 ORDER BY PageNumber, LineNumber, nPartCount");
        ps.setInt(1, reportId);
        rs = ps.executeQuery();
        setSource(rs);
    }

    public void setSource(ResultSet rs) throws SQLException {
        this.pages = new ResultSetReader(rs);
    }

    public void setSource(Iterable<String[]> pageList) {
        this.pages = pageList;
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
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        setTarget(new FileOutputStream(file));
    }

    public void setTarget(OutputStream stream) {
        outStream = stream;
    }

    /*
     * Helper class to iterate ResultSet page by Page
     */
    private class ResultSetReader implements Iterable {

        private ResultSet rs = null;

        public ResultSetReader(ResultSet rs) {
            this.rs = rs;
        }

        private class ResultSetIterator implements Iterator {

            private int prevpage = -1;
            private int prevrow = -1;
            private boolean isFinished = false;
            private StringBuilder str = new StringBuilder();

            @Override
            public boolean hasNext() {
                return !isFinished;
            }

            @Override
            public String[] next() {
                try {
                    ArrayList<String> lines = new ArrayList<String>();
                    while (rs.next()) {
                        int page = rs.getInt(2);
                        int row = rs.getInt(3);
                        if (page != prevpage && prevpage > 0) {
                            lines.add(str.toString());
                            String[] reportlines = new String[lines.size()];
                            reportlines = lines.toArray(reportlines);
                            str = new StringBuilder();
                            str.append(rs.getString(1));
                            prevrow = row;
                            prevpage = page;
                            return reportlines;
                        }
                        if (row != prevrow && prevrow > 0) {
                            lines.add(str.toString());
                            str = new StringBuilder();
                        }
                        str.append(rs.getString(1));
                        prevrow = row;
                        prevpage = page;
                    }
                    lines.add(str.toString());
                    String[] reportlines = new String[lines.size()];
                    reportlines = lines.toArray(reportlines);
                    isFinished = true;
                    rs.getStatement().close();
                    rs.close();
                    return reportlines;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported.");
            }
        }

        @Override
        public Iterator iterator() {
            return new ResultSetIterator();
        }
    }
}
