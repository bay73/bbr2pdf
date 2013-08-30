package org.bay.bbr2pdf;

import java.io.IOException;

/**
 *
 * @author Andrey Bogdanov <bay73@mail.ru>
 * @version 1.0
 * @since 2013-08-25
 * 
 * Class to process bbr markup and call appropriate method of writer
 * 
 */
public class PageScanner {
    
    private String[] lines;
    private String line;
    private int index;
    private StringBuilder token;
    private PDFWriter writer;

    
    public void scanTo(PDFWriter writer) throws IOException {
        this.writer = writer;
        for(String l:lines){
            this.line = l;
            this.index = 0;
            if(l.length() > 0){
                token = new StringBuilder();
                while(true){
                    char c = this.line.charAt(this.index);
                    if(c=='&'){
                        this.finishToken();
                        this.processTag();
                    }else if(c==' '){
                        this.finishToken();
                        this.processSpace(1);
                        this.index++;
                    }else{
                        token.append(c);
                        this.index++;
                    }
                    if(this.index >= this.line.length()){
                        break;
                    }
                }
                this.finishToken();
            }
            writer.nextRow();
        }
    }

    public void setSource(String[] lines) {
        this.lines = lines;
    }

    private void finishToken() throws IOException {
        if (token.length() > 0) {
            writer.putText(token.toString());
            token = new StringBuilder();
        }
    }

    private void processTag() throws IOException {
        index++;
        char next = line.charAt(index);
        if(next=='<'){
            this.processComplexTag();
        }else if(next >= '0' && next <='9'){
            int count = readInt();
            this.processSimpleTag(count);
        }else{
            this.processSimpleTag(1);
        }
    }

    private void processSpace(int count) {
        writer.skipPosition(count);
    }
    
    private boolean checkTag(String tagName){
        int l = tagName.length();
        if(line.length() > index + l){
            if(line.substring(index, index+l).equalsIgnoreCase(tagName)){
                index+=l;
                return true;
            }
        }
        return false;
    }

    private void processComplexTag() throws IOException {
        if(checkTag("<POS=")){
            float pos = readFloat();
            writer.setPosition(pos);
        }else if(checkTag("<LF>")){
            writer.nextRow();
        }else if(checkTag("<LF=N")){
            writer.restoreRow();
        }else if(checkTag("<LF=")){
            float height = readFloat();
            writer.nextRow(height);
        }else if(checkTag("<HR=")){
            float pos = readFloat();
            writer.drawHR(pos);
        }else if(checkTag("<FONT=")){
            float width = readFloat();
            writer.openArea(width);
        }else if(checkTag("<SMALL=")){
            float width = readFloat();
            writer.openArea(width);
            writer.setFontSize(0.7F);
        }else if(checkTag("<CAPTION=")){
            float width = readFloat();
            writer.openArea(width);
            writer.setFontStyle("B");
        }else if(checkTag("<EMPHASIZED=")){
            float width = readFloat();
            writer.openArea(width);
            writer.setFontStyle("I");
        }else if(checkTag("</FONT")){
            writer.closeArea();
        }else if(checkTag("<LEFT")){
            writer.setAllignment(PDFWriter.ALLIGNMENT_LEFT);
        }else if(checkTag("<RIGHT")){
            writer.setAllignment(PDFWriter.ALLIGNMENT_RIGHT);
        }else if(checkTag("<CENTER")){
            writer.setAllignment(PDFWriter.ALLIGNMENT_CENTER);
        }else if(checkTag("<SIZE=")){
            float height = readFloat();
            writer.setFontSize(height);
        }else if(checkTag("<STYLE=")){
            float height = readFloat();
            StringBuilder style = new StringBuilder();
            char next = line.charAt(index);
            while(next != '>'){
                style.append(next);
                index++;
                next = line.charAt(index);
            }
            writer.setFontStyle(style.toString());
        }
        char next = line.charAt(index);
        while(next != '>'){
            index++;
            next = line.charAt(index);
        }
        index++;
    }

    private void processSimpleTag(int count) throws IOException {
        char tag = line.charAt(index);
        index++;
        if(tag>='a' && tag <='k'){
            writer.processPseudographics(tag, count);
        }else if(tag==' '){
            this.processSpace(count);
        }else{
            token = new StringBuilder();
            while(count-- > 0){
                token.append(tag);
            }
            this.finishToken();
        }
    }
    
    private float readFloat(){
        char next = line.charAt(index);
        float count = 0;
        float decimal = 0;
        while(next >= '0' && next <='9' || next=='.'){
            if(next=='.') decimal = 1/10F;
            else{
                if(decimal==0){
                    count = count*10 + (next - '0');
                }else{
                    count = count + (next - '0') * decimal;
                    decimal /= 10;
                }
            }
            index++;
            next = line.charAt(index);
        }
        return count;
    }
    
    private int readInt(){
        char next = line.charAt(index);
        int count = 0;
        while(next >= '0' && next <='9'){
            count = count*10 + (next - '0');
            index++;
            next = line.charAt(index);
        }
        return count;
    }
}
