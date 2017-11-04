package com.irisine.wordsfilter;

import java.util.ArrayList;

/**
 * FilterResult
 * 用于表示文本过滤结果的类。
 */
public class FilterResult {

    /**
     * @param resultString 文本过滤结果文本。
     * @param resultRanges 文本过滤过程中被过滤的文本区间集合。
     */
    public FilterResult(String resultString, ArrayList<FilteredRange> resultRanges) {
        m_resultString = resultString;
        m_resultRanges = resultRanges;
    }

    /**
     * 文本过滤结果文本。
     */
    private String m_resultString = null;


    /**
     * 文本过滤过程中被过滤的文本区间集合。
     */
    private ArrayList<FilteredRange> m_resultRanges = null;

    public String getResultString() {
        return m_resultString;
    }

    public void setResultString(String resultString) {
        m_resultString = resultString;
    }

    public ArrayList<FilteredRange> getResultRanges() {
        return m_resultRanges;
    }

    public void setResultRanges(ArrayList<FilteredRange> resultRanges) {
        m_resultRanges = resultRanges;
    }
}
