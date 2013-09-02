package org.bay.bbr2pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Andrey Bogdanov <bay73@mail.ru>
 * @version 1.0
 * @since 2013-08-25
 *
 * Base interface for writers
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
    public void nextPage();

    /*
     * add piecs of text
     */
    public void putText(String toString);

    /*
     * positionong
     */
    public void nextRow();

    public void nextRow(float height);

    public void restoreRow();

    public void skipPosition(float count);

    public void setPosition(float pos);

    /*
     * lines
     */
    public void drawHR(float pos);

    public void drawPseudographics(char tag, int count);

    /*
     * Text Formatting Each piece of formatted text is an area Size, style and
     * allignment setting should be after opening area There cannot be
     * pseudographics inside an area
     */
    public void openArea(float width);

    public void closeArea();

    public void setFontSize(float height);

    public void setFontStyle(String style);

    public void setAllignment(int allignment);
}
