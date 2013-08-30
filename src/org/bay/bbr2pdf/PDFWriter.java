package org.bay.bbr2pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Andrey Bogdanov <bay73@mail.ru>
 * @version 1.0
 * @since 2013-08-25
 * 
 * Class to create pdf document
 * 
 */
public class PDFWriter {
    public static final int ALLIGNMENT_LEFT = 1;
    public static final int ALLIGNMENT_CENTER = 2;
    public static final int ALLIGNMENT_RIGHT = 3;
    
    // Some magic constants
    public static final float padding = 30F; // page marging outside the text
    public static final float widthmeasure = 72F; // horizontal points per inch
    public static final float fontrelation = 5F/3F; // relation of width and heght of a character
    public static final float heightmeasure = widthmeasure * fontrelation; // relation of width and heght of a character
    public static final float lineWidth = 0.5F; // Ir seems thin lines look better then 1F
    static String normalFontPath = "C:\\Windows\\Fonts\\courbd.ttf";
    static String italicFontPath = "C:\\Windows\\Fonts\\courbi.ttf";
    
    public static void setFontPaths(String normal, String italic){
        normalFontPath = normal;
        italicFontPath = italic;
    }
    
    private float charWidth;
    private float charHeight;
    private float pageWidth;
    private float pageHeight;
    
    private float leftMargin;
    private float topMargin;
    
    private Document document;
    private PdfWriter writer;
    private PdfContentByte content;
    private BaseFont baseFont;
    private BaseFont italicFont;
    
    private float currentX;
    private float currentY;
    
    private Area currentArea;
    
    public class Area{
        private StringBuilder innerText;
        private float startX;
        private float width;
        private float fontSize;
        private String fontStyle;
        private int allignment;
        
        public Area(float width){
            this.innerText = new StringBuilder();
            this.width = width;
            this.startX = currentX;
            this.fontSize = charHeight;
            this.allignment = ALLIGNMENT_LEFT;
        }
        
        void putText(String text){
            innerText.append(text);
        }
        void skipPosition(float count){
            while(count-- > 0){
                innerText.append(" ");
            }
        }
        
        void setFontSize(float size){
            this.fontSize = size;
        }

        void setFontStyle(String style){
            this.fontStyle = style;
        }

        void setAllignment(int allignment){
            this.allignment = allignment;
        }

        void draw(){
            content.beginText();
            BaseFont font = baseFont;
            if(this.fontStyle != null){
                if(this.fontStyle.contains("B"))
                    content.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
                if(this.fontStyle.contains("I"))
                    font = italicFont;
            }
            content.setFontAndSize(font, this.fontSize);
            String text = innerText.toString();
            float textWidth = font.getWidthPoint(text, this.fontSize);
            float left = startX;
            if(this.allignment == ALLIGNMENT_RIGHT){
                left = startX + width - textWidth;
            }else if(this.allignment == ALLIGNMENT_CENTER){
                left = startX + (width - textWidth)/2;
            }
            content.setTextMatrix(left, currentY - this.fontSize + charHeight/6);
            content.showText(innerText.toString());
            content.endText();
            content.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
            currentX = startX + width;
        }
    }
    
    private class sizeChooser{
        private Rectangle bestRect;
        private float bestDiff;
        private float needWidth;
        private float needHeight;
        private void compare(Rectangle rect){
            if(rect.getWidth() >= needWidth && rect.getHeight() >= needHeight){
                float diffW = rect.getWidth() - needWidth;
                float diffH = rect.getHeight() - needHeight;
                if(diffW <= bestDiff && diffH <= bestDiff){
                    bestRect = rect;
                    if (diffW < diffH) bestDiff = diffH;
                    else bestDiff = diffW;
                }
            }
            rect = rect.rotate();
            if(rect.getWidth() >= needWidth && rect.getHeight() >= needHeight){
                float diffW = rect.getWidth() - needWidth;
                float diffH = rect.getHeight() - needHeight;
                if(diffW <= bestDiff && diffH <= bestDiff){
                    bestRect = rect;
                    if (diffW < diffH) bestDiff = diffH;
                    else bestDiff = diffW;
                }
            }
        }
        
        sizeChooser(float width, float height){
            this.needHeight = height;
            this.needWidth = width;
            this.bestDiff = Float.MAX_VALUE;
            compare(PageSize.A0);
            compare(PageSize.A1);
            compare(PageSize.A2);
            compare(PageSize.A3);
            compare(PageSize.A4);
            compare(PageSize.A5);
            compare(PageSize.A6);
            compare(PageSize.A7);
            compare(PageSize.A8);
            compare(PageSize.A9);
            compare(PageSize.A10);
            compare(PageSize.B0);
            compare(PageSize.B1);
            compare(PageSize.B2);
            compare(PageSize.B3);
            compare(PageSize.B4);
            compare(PageSize.B5);
            compare(PageSize.B6);
            compare(PageSize.B7);
            compare(PageSize.B8);
            compare(PageSize.B8);
            compare(PageSize.B10);
            compare(PageSize.EXECUTIVE);
            compare(PageSize.HALFLETTER);
            compare(PageSize.LEGAL);
            compare(PageSize.LETTER);
            compare(PageSize.NOTE);
            compare(PageSize.POSTCARD);
        }
        
        Rectangle getBest(){
            return bestRect;
        }
    }

    public PDFWriter(int width, int height, int cpi) throws IOException{
        try {
            charWidth = widthmeasure/cpi;
            charHeight = heightmeasure/cpi;
            pageWidth = width*charWidth + 2*padding;
            pageHeight = height*charHeight + 2*padding;
            leftMargin = 0;
            topMargin = 0;
//            baseFont = FontFactory.getFont("Lucida Console", BaseFont.IDENTITY_H).getBaseFont();
            baseFont = BaseFont.createFont(normalFontPath, BaseFont.IDENTITY_H, true);
            italicFont = BaseFont.createFont(italicFontPath, BaseFont.IDENTITY_H, true);
        } catch (DocumentException ex) {
            throw new IOException(ex);
        }
    }
    
    void setMargins(int leftMargin, int topMargin){
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
    }

    void nextPage() throws IOException {
        if(currentArea!=null)
            closeArea();
        document.newPage();
        currentX = padding + leftMargin * this.charWidth;
        currentY = this.pageHeight - padding - topMargin * this.charHeight;
        currentArea = null;
    }
    
    void open(OutputStream stream) throws IOException {
        try {
            document = new Document();
            Rectangle rect = new sizeChooser(pageWidth, pageHeight).getBest();
            document.setPageSize(rect);
            writer = PdfWriter.getInstance(document, stream);
            document.open();
            content = writer.getDirectContent();
        } catch (DocumentException ex) {
            throw new IOException(ex);
        }
    }

    void finish() throws IOException {
        document.close();
    }
    
    void openArea(float width){
        currentArea = new Area(width * charWidth);
    }
    
    void setFontSize(float height){
        if(currentArea!=null && height > 0){
            currentArea.setFontSize(height * charHeight);
        }
    }
    void setFontStyle(String style){
        if(currentArea!=null){
            currentArea.setFontStyle(style);
        }
    }

    void setAllignment(int allignment){
        if(currentArea!=null){
            currentArea.setAllignment(allignment);
        }
    }

    void closeArea(){
        currentArea.draw();
        currentArea = null;
    }
    
    void putText(String text) throws IOException {
        if(currentArea==null){
            content.beginText();
            content.setFontAndSize(baseFont, charHeight);
            content.setTextMatrix(currentX, currentY - this.charHeight * 5/6);
            content.showText(text);
            content.endText();
            currentX += text.length() * this.charWidth;
        }else{
            currentArea.putText(text);
        }
    }

    void setPosition(float position) {
        this.currentX = padding + (position + leftMargin) * this.charWidth;
    }

    void nextRow() {
        this.currentY -= this.charHeight;
        this.currentX = padding + leftMargin * this.charWidth;
    }

    void nextRow(float height) {
        this.currentY -= height / fontrelation * this.charHeight;
        this.currentX = padding + leftMargin * this.charWidth;
    }

    void restoreRow() {
        int rows = (int)((this.pageHeight - padding - topMargin * this.charHeight - currentY)/this.charHeight) + 1;
        this.currentY = this.pageHeight - padding - (rows + topMargin) * this.charHeight;
        this.currentX = padding + leftMargin * this.charWidth;
    }

    void skipPosition(float count) {
        if(currentArea==null){
            currentX += count * this.charWidth;
        }else{
            currentArea.skipPosition(count);
        }
    }

    void drawHR(float position) {
        content.moveTo(currentX, currentY - this.charHeight/2);
        content.lineTo(padding + (position + leftMargin) * this.charWidth, currentY - this.charHeight/2);
        content.stroke();
        this.currentX = padding + (position + leftMargin) * this.charWidth;
    }

    void processPseudographics(char tag, int count) throws IOException {
        content.setLineWidth(lineWidth);
        int counter = count;
        if(tag=='a'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.lineTo( currentX + this.charWidth*(counter+1), currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='b'){
            content.moveTo(currentX, currentY - this.charHeight/2);
            content.lineTo(currentX + count*this.charWidth, currentY - this.charHeight/2);
            content.stroke();
        }else if(tag=='c'){
            content.moveTo(currentX, currentY - this.charHeight/2);
            content.lineTo(currentX + count*this.charWidth, currentY - this.charHeight/2);
            content.stroke();
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='d'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.lineTo( currentX + this.charWidth*counter, currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='e'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.stroke();
            }
        }else if(tag=='f'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.stroke();
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.lineTo( currentX + this.charWidth*(counter+1), currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='g'){
            content.moveTo(currentX, currentY - this.charHeight/2);
            content.lineTo(currentX + count*this.charWidth, currentY - this.charHeight/2);
            content.stroke();
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.stroke();
            }
        }else if(tag=='h'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.stroke();
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.lineTo( currentX + this.charWidth*counter, currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='i'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.lineTo( currentX + this.charWidth*(counter+1), currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='j'){
            content.moveTo(currentX, currentY - this.charHeight/2);
            content.lineTo(currentX + count*this.charWidth, currentY - this.charHeight/2);
            content.stroke();
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.stroke();
            }
        }else if(tag=='k'){
            while(counter-- > 0){
                content.moveTo(currentX + this.charWidth*counter + this.charWidth/2, currentY);
                content.lineTo(currentX + this.charWidth*counter + this.charWidth/2, currentY - this.charHeight/2);
                content.lineTo( currentX + this.charWidth*counter, currentY - this.charHeight/2);
                content.stroke();
            }
        }

        this.skipPosition(count);
    }
}
