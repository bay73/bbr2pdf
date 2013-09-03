bbr2pdf
=======

Tools to convert bbr markup to pdf file


Пример использования:

// 0) Импорт необходимых классов
import org.bay.bbr2pdf.PDFWriter;
import org.bay.bbr2pdf.BBRConverter;

// 1) Выбор шрифтов для печати. Первый шрифт - обычный, второй - курсив
PDFWriter.setFontPaths("C:\\Windows\\Fonts\\lucon.ttf", "C:\\Windows\\Fonts\\courbi.ttf");

// 2) Инициализация конвертора
BBRConverter converter = new BBRConverter(new PDFWriter());

// 3) Указание источника данных. Возможно три варианта указания исчточника:
// 3а) соединение с Афинской БД + идентификатор готового отчета + признак использования временных таблиц
//     в этом случае конвертор сам считает все данные из базы и постранично отрендерит отчет
converter.setSource(Connection, Integer, boolean)

// 3б) ResultSet вида select LineText, PageNumber, LineNumber, nPartCount from ReportLines order by PageNumber, LineNumber, nPartCount
//     конвертор выполнит цикл по набору, склеит нужные строки
converter.setSource(ResultSet)

// 3в) Набор, содержащий массивы строк. Каждый массив представляет одну страницу для рендеринга. 
//     Будет использоваться итератор, поэтому готовить данные можно по мере необходимости
converter.setSource(Iterable<String[]>)

// 4) Задание параметров страницы (значение полей DesignText и RightMargin из ReportTemplate и ReadyReport соответственно
//    Если для setSource используется Connection, то этот вызов не нужен
converter.setReportParams(String, int);

// 5) Установки приемника данных. Возможны варианты: имя файла, файл или поток (OutputStream)
converter.setTarget(String)
converter.setTarget(File)
converter.setTarget(OutputStream)

// 6) Запуск конвертации
converter.process()

// Пункты 3,4,5,6 для одного и того же экземпляра BBRConverter можно повторять многократно конвертируя несколько отчетов.
