package com.irisine.wordsfilter;

import org.apache.commons.text.StrBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Filter;

/**
 * 一个用于对文本进行简单过滤的单例类。
 */
public enum SimpleWordsFilter {
    /**
     * 单例类的实例。
     */
    Instance;

    /**
     * @param orgString 将要被过滤的字符串。
     * @param wordList 用于过滤字符串的单词列表，所有被过滤的单词都会被变成相应长度的“*”序列。
     * @param sort 过滤单词列表是否需要排序，如果为true将会对该列表进行按单词长度降序排序；否则不进行排序。排序与否将会在某些情况下影响过滤的准确性。如果已经做了降序排序那么就设置为false。
     * @return 表示过滤结果的FilterResult对象
     * @see FilterResult
     */
    public FilterResult filter(String orgString, ArrayList<String> wordList, boolean sort) {

        if(sort) {
            wordList.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.length() - o1.length();
                }
            });
        }

        StrBuilder orgBuilder = new StrBuilder(orgString);
        StrBuilder holder = new StrBuilder();

        FilterResult filterResult = new FilterResult(null, null);

        ArrayList<FilteredRange> ranges = new ArrayList<>();

        int last = wordList.size();
        for (int j = 0; j < last; ++j) {
            String str = wordList.get(j);
            if(orgBuilder.contains(str)) {
                if(str.length() != holder.length()) {
                    holder.clear();
                    for (int i = 0; i < str.length(); i++) {
                        holder.append('*');
                    }
                }
                orgBuilder.replaceAll(str, holder.toString());
            }
        }

        filterResult.setResultString(orgBuilder.toString());
        filterResult.setResultRanges(new ArrayList<FilteredRange>());

        int from = 0;
        int to = 0;
        String strTmp = filterResult.getResultString();
        ArrayList<FilteredRange> rangesTmp = filterResult.getResultRanges();
        for (int i = 0; i < strTmp.length(); ++i) {
            if (strTmp.charAt(i) == '*') {
                ++to;
            }
            else {
                if(to > from) {
                    rangesTmp.add(new FilteredRange(from, to - 1));
                }
                from = ++to;
            }
        }

        if(strTmp.endsWith("*")) {
            rangesTmp.add(new FilteredRange(from, to));
        }

        return filterResult;
    }
}
