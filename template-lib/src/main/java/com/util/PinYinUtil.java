package com.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @Author 佘忠新
 * @Date 2022/6/27 11:36
 * @Version 1.0
 */
public class PinYinUtil {


    /**
     * 中文转拼音首字母大写
     *
     * @param str str
     * @return {@link String}
     */
    public static String converterToFirstSpell(String str) {
        StringBuilder pinyinName = new StringBuilder();
        char[] nameChar = str.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        //转大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char characters : nameChar) {
            String string = String.valueOf(characters);
            if (string.matches("[\\u4e00-\\u9fa5]")) {
                try {
                    String[] mPinyinArray = PinyinHelper.toHanyuPinyinStringArray(characters, defaultFormat);
                    pinyinName.append(mPinyinArray[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName.append(characters);
            }
        }
        return pinyinName.toString();
    }


}