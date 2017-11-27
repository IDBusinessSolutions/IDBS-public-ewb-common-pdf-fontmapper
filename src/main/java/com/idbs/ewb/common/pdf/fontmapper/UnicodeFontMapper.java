package com.idbs.ewb.common.pdf.fontmapper;


/*
 * Copyright 2004 by Paulo Soares.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */


/*
 * The class is a based on com.lowagie.text.pdfDefaultFontMapper in the OpenPDF
 * library. It differs from that class in that it has another constructor
 * providing the ability to accept different encoding. The awtToPdf() method is
 * also modified to use IDENTITY_H encoding if the provided font is of type TTF
 * or TTC.
 */

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FontMapper;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Default class to map awt fonts to BaseFont.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class UnicodeFontMapper implements FontMapper
{

    /**
     * A representation of BaseFont parameters.
     */
    @SuppressWarnings("serial")
    public static class BaseFontParameters implements Serializable
    {
        /**
         * The font name.
         */
        public final String fontName;

        /**
         * The encoding for that font.
         */
        public final String encoding;

        /**
         * The embedding for that font.
         */
        public final boolean embedded;

        /**
         * Whether the font is cached of not.
         */
        public final boolean cached;

        /**
         * Constructs default BaseFont parameters.
         *
         * @param fontName the font name or location
         */
        public BaseFontParameters(String fontName)
        {
            this(fontName, BaseFont.CP1252);
        }

        public BaseFontParameters(String fontName, String encoding)
        {
            this.fontName = fontName;
            this.encoding = encoding;
            embedded = BaseFont.EMBEDDED;
            cached = BaseFont.CACHED;
        }
    }

    /**
     * Maps aliases to names.
     */
    private Map<Object, String> aliases = new HashMap<>();

    /**
     * Maps names to BaseFont parameters.
     */
    private Map<String, BaseFontParameters> mapper = new HashMap<>();

    /**
     * Returns a BaseFont which can be used to represent the given AWT Font
     *
     * @param font the font to be converted
     * @return a BaseFont which has similar properties to the provided Font
     */
    public BaseFont awtToPdf(Font font)
    {
        try
        {
            BaseFontParameters p = getBaseFontParameters(font.getFontName());
            if (p != null)
            {
                if (p.fontName.contains(".ttf") || p.fontName.contains(".TTF") || p.fontName.contains(".ttc")
                        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        || p.fontName.contains(".TTC")) //$NON-NLS-1$
                {
                    p = new BaseFontParameters(p.fontName, BaseFont.IDENTITY_H);
                }
                return BaseFont.createFont(p.fontName, p.encoding, p.embedded, p.cached, null, null);
            }
            String fontKey = null;
            String logicalName = font.getName();

            switch (logicalName.toLowerCase())
            {
            case "dialoginput"://$NON-NLS-1$
            case "courier"://$NON-NLS-1$
            case "monospaced":
                if (!font.isItalic())
                {
                    fontKey = !font.isBold() ? BaseFont.COURIER : BaseFont.COURIER_BOLD;
                }
                else
                {
                    fontKey = !font.isBold() ? BaseFont.COURIER_OBLIQUE : BaseFont.COURIER_BOLDOBLIQUE;
                }
                break;
            case "serif"://$NON-NLS-1$
            case "timesroman"://$NON-NLS-1$
                if (!font.isItalic())
                {
                    fontKey = !font.isBold() ? BaseFont.TIMES_ROMAN : BaseFont.TIMES_BOLD;
                }
                else
                {
                    fontKey = !font.isBold() ? BaseFont.TIMES_ITALIC : BaseFont.TIMES_BOLDITALIC;
                }
                break;
            default:
                if (!font.isItalic())
                {
                    fontKey = !font.isBold() ? BaseFont.HELVETICA : BaseFont.HELVETICA_BOLD;
                }
                else
                {
                    fontKey = !font.isBold() ? BaseFont.HELVETICA_OBLIQUE : BaseFont.HELVETICA_BOLDOBLIQUE;
                }
                break;
            }
            return BaseFont.createFont(fontKey, BaseFont.CP1252, false);
        }
        catch (Exception e)
        {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Returns an AWT Font which can be used to represent the given BaseFont
     *
     * @param font the font to be converted
     * @param size the desired point size of the resulting font
     * @return a Font which has similar properties to the provided BaseFont
     */

    public Font pdfToAwt(BaseFont font, int size)
    {
        String names[][] = font.getFullFontName();
        if (names.length == 1)
        {
            return new Font(names[0][3], 0, size);
        }
        String name10 = null;
        String name3x = null;
        for (int k = 0; k < names.length; ++k)
        {
            String name[] = names[k];
            if (name[0].equals("1") && name[1].equals("0")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                name10 = name[3];
            }
            else
            {
                if (name[2].equals("1033"))
                { //$NON-NLS-1$
                    name3x = name[3];
                    break;
                }
            }
        }
        String finalName = name3x;
        if (finalName == null)
        {
            finalName = name10;
        }
        if (finalName == null)
        {
            finalName = names[0][3];
        }
        return new Font(finalName, 0, size);
    }

    /**
     * Maps a name to a BaseFont parameter.
     *
     * @param awtName    the name
     * @param parameters the BaseFont parameter
     */
    public void putName(String awtName, BaseFontParameters parameters)
    {
        mapper.put(awtName, parameters);
    }

    /**
     * Maps an alias to a name.
     *
     * @param alias   the alias
     * @param awtName the name
     */
    public void putAlias(String alias, String awtName)
    {
        aliases.put(alias, awtName);
    }

    /**
     * Looks for a BaseFont parameter associated with a name.
     *
     * @param name the name
     * @return the BaseFont parameter or <CODE>null</CODE> if not found.
     */
    public BaseFontParameters getBaseFontParameters(String name)
    {
        String alias = (String)aliases.get(name);
        if (alias == null)
        {
            return (BaseFontParameters)mapper.get(name);
        }
        BaseFontParameters p = (BaseFontParameters)mapper.get(alias);
        if (p == null)
        {
            return (BaseFontParameters)mapper.get(name);
        }
        else
        {
            return p;
        }
    }

    /**
     * Inserts the names in this map.
     *
     * @param allNames the returned value of calling {@link BaseFont#getAllFontNames(String, String, byte[])}
     * @param path     the full path to the font
     */
    public void insertNames(Object allNames[], String path)
    {
        String names[][] = (String[][])allNames[2];
        String main = null;
        for (int k = 0; k < names.length; ++k)
        {
            String name[] = names[k];
            if (name[2].equals("1033"))
            { //$NON-NLS-1$
                main = name[3];
                break;
            }
        }
        if (main == null)
        {
            main = names[0][3];
        }
        BaseFontParameters p = new BaseFontParameters(path);
        mapper.put(main, p);
        for (int k = 0; k < names.length; ++k)
        {
            aliases.put(names[k][3], main);
        }
        aliases.put(allNames[0], main);
    }

    /**
     * Inserts all the fonts recognized by iText in the
     * <CODE>directory</CODE> into the map. The encoding
     * will be <CODE>BaseFont.CP1252</CODE> but can be
     * changed later.
     *
     * @param dir the directory to scan
     * @return the number of files processed
     */
    public int insertDirectory(String dir)
    {
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory())
        {
            return 0;
        }
        File files[] = file.listFiles();
        if (files == null)
        {
            return 0;
        }
        int count = 0;

        Object allNames[];
        String fontType;
        String fontNames[];
        String path;

        for (int k = 0; k < files.length; ++k)
        {

            path = files[k].getPath();
            fontType = path.substring(path.length() - 3).toLowerCase();

            try
            {
                if (fontType.equals("ttf") || fontType.equals("otf") || fontType.equals("afm"))
                { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    allNames = BaseFont.getAllFontNames(path, BaseFont.CP1252, null);
                    insertNames(allNames, path);
                    ++count;
                }
                else
                {
                    if (fontType.equals("ttc"))
                    { //$NON-NLS-1$
                        fontNames = BaseFont.enumerateTTCNames(path);
                        for (int j = 0; j < fontNames.length; ++j)
                        {
                            String nt = path + "," + j; //$NON-NLS-1$
                            allNames = BaseFont.getAllFontNames(nt, BaseFont.CP1252, null);
                            insertNames(allNames, nt);
                        }
                        ++count;
                    }
                }
            }
            catch (Exception e)
            {
            }
        }
        return count;
    }

    public Map<String, BaseFontParameters> getMapper()
    {
        return mapper;
    }

    public Map<Object, String> getAliases()
    {
        return aliases;
    }
}


