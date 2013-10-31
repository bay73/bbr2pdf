/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bay.bbr2pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 *
 * @author a.bogdanov
 */
public class TXTWriter implements CommonWriter{
    
    private PrintStream out;
    private int currentX;

    private Area currentArea;

    public class Area {

        private StringBuilder innerText;
        private int width;
        private int allignment;

        public Area(int width) {
            this.innerText = new StringBuilder();
            this.width = width;
            this.allignment = CommonWriter.ALLIGNMENT_LEFT;
        }

        void putText(String text) {
            innerText.append(text);
        }

        void skipPosition(float count) {
            while (count-- > 0) {
                innerText.append(" ");
            }
        }

        void setAllignment(int allignment) {
            this.allignment = allignment;
        }

        void draw() {
            String text = innerText.toString();
            int leftMargin = 0;
            if(this.allignment == CommonWriter.ALLIGNMENT_RIGHT){
                leftMargin = width - text.length();
            }else if (this.allignment == CommonWriter.ALLIGNMENT_CENTER) {
                leftMargin = (width - text.length()) / 2;
            }
            if(leftMargin < 0) leftMargin = 0;
            int rightMargin = width - text.length() - leftMargin;
            if(rightMargin < 0) rightMargin = 0;
            StringBuilder space = new StringBuilder();
            while (space.length() < leftMargin) {
                space.append(' ');
            }
            out.print(space.toString());
            out.print(text);
            space = new StringBuilder();
            while (space.length() < rightMargin) {
                space.append(' ');
            }
            out.print(space.toString());
            currentX += width;
        }
    }
    public void open(OutputStream outStream) throws IOException {
        this.out = new PrintStream(outStream, true, "Cp1251");
        currentX = 0;
    }

    public void finish() throws IOException {
        this.out.flush();
    }

    public void setPageSize(int width, int height, int cpi) throws IOException {
    }

    public void setMargins(int leftMargin, int topMargin) throws IOException {
    }

    public void nextPage() {
        if (currentArea != null) {
            closeArea();
        }
        currentX = 0;
    }

    public void putText(String text) {
        if (currentArea == null) {
            this.out.print(text);
            currentX += text.length();
        } else {
            currentArea.putText(text);
        }
    }

    public void nextRow() {
        this.out.println();
        currentX = 0;
    }

    public void nextRow(float height) {
        this.nextRow();
    }

    public void restoreRow() {
        this.nextRow();
    }

    public void skipPosition(float count) {
        if (currentArea == null) {
            StringBuilder space = new StringBuilder();
            while (space.length() < count) {
                space.append(' ');
            }
            this.out.print(space.toString());
            currentX += count;
        } else {
            currentArea.skipPosition(count);
        }

    }

    public void setPosition(float pos) {
        if(currentX < pos)
            this.skipPosition(pos - currentX);
    }

    public void drawHR(float pos) {
        if(currentX < pos){
            StringBuilder space = new StringBuilder();
            while (space.length() < pos - currentX) {
                space.append('-');
            }
            this.out.print(space.toString());
            currentX = (int)pos;
        }
    }

    public void drawPseudographics(char tag, int count) {
        int counter = count;
        StringBuilder space = new StringBuilder();
        if (tag == CommonWriter.PG_TAG_HORIZONTAL) {
            while (counter-- > 0) {
                space.append('-');
            }
        } else if (tag == CommonWriter.PG_TAG_VERTICAL) {
            while (counter-- > 0) {
                space.append('|');
            }
        }else {
            while (counter-- > 0) {
                space.append('+');
            }
        }
        this.out.print(space.toString());
        currentX += count;
    }

    public void openArea(float width) {
        currentArea = new Area(Math.round(width));
    }

    public void closeArea() {
        currentArea.draw();
        currentArea = null;
    }

    public void setFontSize(float height) {
    }

    public void setFontStyle(String style) {
    }

    public void setAllignment(int allignment) {
        if (currentArea != null) {
            currentArea.setAllignment(allignment);
        }
    }
    
}
