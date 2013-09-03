bbr2pdf
=======

Tools to convert bbr markup to pdf file


������ �������������:

// 0) ������ ����������� �������
import org.bay.bbr2pdf.PDFWriter;
import org.bay.bbr2pdf.BBRConverter;

// 1) ����� ������� ��� ������. ������ ����� - �������, ������ - ������
PDFWriter.setFontPaths("C:\\Windows\\Fonts\\lucon.ttf", "C:\\Windows\\Fonts\\courbi.ttf");

// 2) ������������� ����������
BBRConverter converter = new BBRConverter(new PDFWriter());

// 3) �������� ��������� ������. �������� ��� �������� �������� ����������:
// 3�) ���������� � �������� �� + ������������� �������� ������ + ������� ������������� ��������� ������
//     � ���� ������ ��������� ��� ������� ��� ������ �� ���� � ����������� ���������� �����
converter.setSource(Connection, Integer, boolean)

// 3�) ResultSet ���� select LineText, PageNumber, LineNumber, nPartCount from ReportLines order by PageNumber, LineNumber, nPartCount
//     ��������� �������� ���� �� ������, ������ ������ ������
converter.setSource(ResultSet)

// 3�) �����, ���������� ������� �����. ������ ������ ������������ ���� �������� ��� ����������. 
//     ����� �������������� ��������, ������� �������� ������ ����� �� ���� �������������
converter.setSource(Iterable<String[]>)

// 4) ������� ���������� �������� (�������� ����� DesignText � RightMargin �� ReportTemplate � ReadyReport ��������������
//    ���� ��� setSource ������������ Connection, �� ���� ����� �� �����
converter.setReportParams(String, int);

// 5) ��������� ��������� ������. �������� ��������: ��� �����, ���� ��� ����� (OutputStream)
converter.setTarget(String)
converter.setTarget(File)
converter.setTarget(OutputStream)

// 6) ������ �����������
converter.process()

// ������ 3,4,5,6 ��� ������ � ���� �� ���������� BBRConverter ����� ��������� ����������� ����������� ��������� �������.
