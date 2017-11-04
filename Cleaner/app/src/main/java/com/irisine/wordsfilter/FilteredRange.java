package com.irisine.wordsfilter;

/**
 * 用于表示文本过滤过程中一个被过滤的连续文本区间的类。
 */
public class FilteredRange {

    /**
     * @param from 区间的起始位置索引。
     * @param to 区间的结束位置的索引。
     */
    public FilteredRange(int from, int to) {
        m_from = from;
        m_to = to;
    }

    public int getFrom() {
        return m_from;
    }

    public void setFrom(int from) {
        m_from = from;
    }

    public int getTo() {
        return m_to;
    }

    public void setTo(int to) {
        m_to = to;
    }

    /**
     * 区间起始位置的索引。
     */
    private int m_from;

    /**
     * 区间结束位置的索引。
     */
    private int m_to;
}
