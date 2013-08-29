package org.bay.bbr2pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPatternPainter;
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
    // Some magic constants
    public static final float padding = 31.2F;
    public static final float widthmeasure = 72F;
    public static final float heightmeasure = 120.3F;
    public static final float lineWidth = 0.5F;
    
    private float charWidth;
    private float charHeight;
    private float pageWidth;
    private float pageHeight;
    
    private float leftMargin;
    private float topMargin;
    
    private Document document;
    private PdfWriter writer;
    private PdfContentByte content;
    private BaseFont font;
    
    private float currentX;
    private float currentY;
    
    public PDFWriter(int width, int height, int cpi) throws IOException{
        try {
            charWidth = widthmeasure/cpi;
            charHeight = heightmeasure/cpi;
            pageWidth = width*charWidth + 2*padding;
            pageHeight = height*charHeight + 2*padding;
            leftMargin = 0;
            topMargin = 0;
            font = BaseFont.createFont("C:\\Windows\\Fonts\\lucon.ttf", BaseFont.IDENTITY_H, true);
        } catch (DocumentException ex) {
            throw new IOException(ex);
        }
    }
    
    void setMargins(int leftMargin, int topMargin){
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
    }

    void nextPage() throws IOException {
        document.newPage();
        currentX = padding + leftMargin * this.charWidth;
        currentY = this.pageHeight - padding - topMargin * this.charHeight;
    }

    void open(OutputStream stream) throws IOException {
        try {
            document = new Document();
            Rectangle rect = new Rectangle(pageWidth, pageHeight);
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

    void putText(String text) throws IOException {
            content.beginText();
            content.setFontAndSize(font, charHeight);
            content.setTextMatrix(currentX, currentY - this.charHeight);
            content.showText(text);
            content.endText();
            currentX += text.length() * this.charWidth;
    }

    void setPosition(int position) {
        this.currentX = padding + (position + leftMargin) * this.charWidth;
    }

    void nextRow() {
        this.currentY -= this.charHeight;
        this.currentX = padding + leftMargin * this.charWidth;
    }

    void nextRow(float height) {
        this.currentY -= height * this.charHeight;
        this.currentX = padding + leftMargin * this.charWidth;
    }

    void skipPosition(int count) {
        currentX += count * this.charWidth;
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
