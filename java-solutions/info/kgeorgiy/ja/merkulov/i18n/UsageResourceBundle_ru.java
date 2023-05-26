package info.kgeorgiy.ja.merkulov.i18n;

import java.util.ListResourceBundle;

public class UsageResourceBundle_ru extends ListResourceBundle {


    private static final Object[][] CONTENTS = {
            {"analyzed", "Анализируемый файл"},
            {"unique", "Различных"},

            {"wholeStats", "Сводная статистика"},
            {"wordsNumber", "Число слов"},
            {"numbersNumber", "Число чисел"},
            {"moneyNumber", "Число упоминаемых денег"},
            {"dateNumber", "Число дат"},

            {"sentenceStats", "Статистика по предложениям"},
            {"sentenceNumber", "Число предложений"},
            {"minLexicSentence", "Минимальное предложение"},
            {"maxLexicSentence", "Максимальное предложение"},
            {"minLengthSentence", "Минимальная длина предложения"},
            {"maxLengthSentence", "Минимальная длина предложения"},
            {"averageLengthSentence", "Средняя длина предложения"},

            {"wordStats", "Статистика по словам"},
            {"wordNumber", "Число слов"},
            {"minLexicWord", "Минимальное слово"},
            {"maxLexicWord", "Максимальное слово"},
            {"minLengthWord", "Минимальная длина слова"},
            {"maxLengthWord", "Максимальная длина слова"},
            {"averageLengthWord", "Средняя длина слова"},


            {"numberStats", "Статистика по числам"},
            {"numberNumber", "Число чисел"},
            {"minNumber", "Минимальное число"},
            {"maxNumber", "Максимальное слово"},
            {"averageNumber", "Среднее число"},


            {"moneyStats", "Статистика по суммам денег"},
            {"moneyNumber", "Число сумм"},
            {"minMoney", "Минимальная сумма"},
            {"maxMoney", "Максимальная сумма"},
            {"averageMoney", "Средняя сумма"},

            {"dateStats", "Статистика по датам"},
            {"dateNumber", "Число дат"},
            {"minDate", "Минимальная дата"},
            {"maxDate", "Максимальная дата"},
            {"averageLengthDate", "Средняя длина дат"},
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
