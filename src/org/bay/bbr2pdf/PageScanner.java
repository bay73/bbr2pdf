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
            int count = 0;
            while(next >= '0' && next <='9'){
                count = count*10 + (next - '0');
                index++;
                next = line.charAt(index);
            }
            this.processSimpleTag(count);
        }else{
            this.processSimpleTag(1);
        }
    }

    private void processSpace(int count) {
        writer.skipPosition(count);
    }

    private void processComplexTag() {
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
    
}
