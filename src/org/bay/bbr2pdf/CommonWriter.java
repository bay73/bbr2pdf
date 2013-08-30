/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bay.bbr2pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author a.bogdanov
 */
public interface CommonWriter {

    /*
     * allignment constants
     */
    public static final int ALLIGNMENT_LEFT = 1;
    public static final int ALLIGNMENT_CENTER = 2;
    public static final int ALLIGNMENT_RIGHT = 3;
    /*
     * pseudographic constants
     */
    public static final char PG_TAG_TOPLEFT = 'a';
    public static final char PG_TAG_HORIZONTAL = 'b';
    public static final char PG_TAG_TOP = 'c';
    public static final char PG_TAG_TOPRIGHT = 'd';
    public static final char PG_TAG_VERTICAL = 'e';
    public static final char PG_TAG_LEFT = 'f';
    public static final char PG_TAG_CROSS = 'g';
    public static final char PG_TAG_RIGHT = 'h';
    public static final char PG_TAG_BOTTOMLEFT = 'i';
    public static final char PG_TAG_BOTTOM = 'j';
    public static final char PG_TAG_BOTTOMRIGHT = 'k';

    /*
     * global settings
     */
    public void open(OutputStream outStream) throws IOException;

    public void finish() throws IOException;

    /*
     * global settings
     */
    public void setPageSize(int width, int height, int cpi) throws IOException;

    public void setMargins(int leftMargin, int topMargin) throws IOException;

    /*
     * switch pages
     */
    public void nextPage() throws IOException;

    /*
     * add piecs of text
     */
    public void putText(String toString) throws IOException;

    /*
     * positionong
     */
    public void nextRow() throws IOException;

    public void nextRow(float height) throws IOException;

    public void restoreRow() throws IOException;

    public void skipPosition(float count) throws IOException;

    public void setPosition(float pos) throws IOException;

    /*
     * lines
     */
    public void drawHR(float pos) throws IOException;

    public void drawPseudographics(char tag, int count) throws IOException;

    /*
     * Text Formatting Each piece of formatted text is an area Size, style and
     * allignment setting should be after opening area There cannot be
     * pseudographics inside an area
     */
    public void openArea(float width) throws IOException;

    public void closeArea() throws IOException;

    public void setFontSize(float height) throws IOException;

    public void setFontStyle(String style) throws IOException;

    public void setAllignment(int allignment) throws IOException;
}
