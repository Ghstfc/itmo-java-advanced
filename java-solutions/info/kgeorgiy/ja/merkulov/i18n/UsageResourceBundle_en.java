package info.kgeorgiy.ja.merkulov.i18n;

import java.util.ListResourceBundle;

public class UsageResourceBundle_en extends ListResourceBundle {
    
    private static final Object[][] CONTENTS = {
            {"analyzed", "Analized file"},
            {"unique", "Unique"},

            {"wholeStats", "Whole statistics"},
            {"wordsNumber", "Number of words"},
            {"numbersNumber", "Number of numbers"},
            {"moneyNumber", "Number of amount of money"},
            {"dateNumber", "Number of dates"},

            {"sentenceStats", "Statistics of sentences"},
            {"sentenceNumber", "Number of sentences"},
            {"minLexicSentence", "Minimal sentence"},
            {"maxLexicSentence", "Maximum sentence"},
            {"minLengthSentence", "Minimal length of sentence"},
            {"maxLengthSentence", "Maximal length of sentence"},
            {"averageLengthSentence", "Average length of sentence"},

            {"wordStats", "Statistics of words"},
            {"minLexicWord", "Minimum of words"},
            {"maxLexicWord", "Maximal of words"},
            {"minLengthWord", "Minimum length of words"},
            {"maxLengthWord", "Maximal length of words"},
            {"averageLengthWord", "Average length of words"},


            {"numberStats", "Statistics of words"},
            {"minNumber", "Minimum number"},
            {"maxNumber", "Maximal number"},
            {"averageNumber", "Average number"},


            {"moneyStats", "Statistics суммам денег"},
            {"minMoney", "Minimum amount of money"},
            {"maxMoney", "Maximal amount of money"},
            {"averageMoney", "Average amount of money"},

            {"dateStats", "Statistics датам"},
            {"minDate", "Minimum date"},
            {"maxDate", "Maximal date"},
            {"averageLengthDate", "Average length of dates"},
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
