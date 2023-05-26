package info.kgeorgiy.ja.merkulov.i18n;

import java.text.*;
import java.util.*;

public class StatsCalculator {
    private final Locale locale;
    private final String text;

    private final TextStats sentenceStats = new TextStats();
    private final TextStats wordStats = new TextStats();
    private final NumberStats numberStats = new NumberStats();
    private final NumberStats moneyStats = new NumberStats();
    private final DateStats dateStats = new DateStats();


    public StatsCalculator(Locale locale, String text) {
        this.locale = locale;
        this.text = text;
    }


    public void calculateStats(String partition) {
        HashSet<String> uniqueParts = new HashSet<>();
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        BreakIterator iterator;
        switch (partition) {
            case "word":
                iterator = BreakIterator.getWordInstance(locale);
                iterator.setText(text);
                processText(iterator, uniqueParts, collator, wordStats);
                break;
            case "sent":
                iterator = BreakIterator.getSentenceInstance(locale);
                iterator.setText(text);
                processText(iterator, uniqueParts, collator, sentenceStats);
                break;
            default:
                iterator = BreakIterator.getWordInstance(locale);
                iterator.setText(text);
                numbersProcess(iterator);
                break;
        }

    }


    private void processText(BreakIterator iterator, HashSet<String> uniqueSet, Collator collator, TextStats stats) {
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String segment = text.substring(start, end);
            if (segment.replaceAll("\\s", "").isEmpty() || segment.replaceAll("[\\p{Punct}&&[^']]", "").isEmpty()) {
                continue;
            }
            segment = segment.replaceAll("\\s", " ");
//            segment = segment.toLowerCase(locale);
            if (stats.maxSegment == null) {
                uniqueSet.add(segment);
                stats.maxSegment = segment;
                stats.minSegment = segment;
                stats.maxLexic = segment;
                stats.minLexic = segment;
                stats.numberSegments++;
                stats.summaryLength += segment.length();
                continue;
            }

            stats.numberSegments++;
            stats.summaryLength += segment.length();
            uniqueSet.add(segment);
            if (stats.maxSegment.length() < segment.length()) {
                stats.maxSegment = segment;
            }
            if (stats.minSegment.length() > segment.length()) {
                stats.minSegment = segment;
            }
            if (collator.compare(stats.maxLexic, segment) > 0) {
                stats.maxLexic = segment;
            }
            if (collator.compare(stats.minLexic, segment) < 0) {
                stats.minLexic = segment;
            }
            stats.summaryLength += segment.length();
        }
        stats.averageLength = (float) stats.summaryLength / stats.numberSegments;
        stats.numberDifferentSegments = uniqueSet.size();
    }

    private void numbersProcess(BreakIterator iterator) {
        List<Number> numbers = new ArrayList<>();
        List<Date> dates = new ArrayList<>();
        List<Number> money = new ArrayList<>();

        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(locale);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String segment = text.substring(start, end);
            if (segment.replaceAll("\\s", "").isEmpty()) {
                continue;
            }
            try {
                Date date = dateFormat.parse(segment);
                dates.add(date);
                continue;
            } catch (ParseException ignored) {
            }
            try {
                Number number = numberFormat.parse(segment);
                numbers.add(number);
                continue;
            } catch (ParseException ignored) {
            }
            try {
                Number currency = moneyFormat.parse(segment);
                money.add(currency);
            } catch (ParseException ignored) {
            }
        }
        getNumbersStats(money, moneyStats);
        getNumbersStats(numbers, numberStats);
        getDateStats(dates);
    }

    private void getDateStats(List<Date> dates) {
        if (dates.isEmpty()) {
            dateStats.maxDate = new Date(0);
            dateStats.minDate = new Date(0);
            return;
        }
        HashSet<Date> unique = new HashSet<>();
        for (Date date : dates) {
            unique.add(date);
            if (dateStats.count == 0) {
                dateStats.count++;
                dateStats.maxDate = date;
                dateStats.minDate = date;
                dateStats.average += date.toString().length();
                continue;
            }
            dateStats.count++;
            if (dateStats.maxDate.after(date)) {
                dateStats.maxDate = date;
            }
            if (dateStats.minDate.before(date)) {
                dateStats.minDate = date;
            }
            dateStats.average += date.toString().length();
        }
        dateStats.unique = unique.size();
        dateStats.average = dateStats.average / dateStats.count;
    }


    private void getNumbersStats(List<Number> money, NumberStats stats) {
//        stats.symbol = Currency.getInstance(locale).getSymbol();
        HashSet<Double> unique = new HashSet<>();

        if (money.isEmpty()) {
            stats.averageNumber = 0;
            stats.differentNumbers = 0;
            stats.maxValue = 0;
            stats.minValue = 0;
            stats.count = 0;
            return;
        }

        for (Number number : money) {
            double value = number.doubleValue();
            unique.add(value);
            if (stats.count == 0) {
                stats.minValue = value;
                stats.maxValue = value;
                stats.sum += value;
                stats.count++;
                continue;
            }
            if (stats.maxValue < value) {
                stats.maxValue = value;
            }
            if (stats.minValue > value) {
                stats.minValue = value;
            }
            stats.sum += value;
            stats.count++;
        }
        stats.differentNumbers = unique.size();
        stats.averageNumber = stats.sum / stats.count;
    }

    public TextStats getSentenceStats() {
        return sentenceStats;
    }

    public TextStats getWordStats() {
        return wordStats;
    }

    public NumberStats getNumberStats() {
        return numberStats;
    }

    public NumberStats getMoneyStats() {
        return moneyStats;
    }

    public DateStats getDateStats() {
        return dateStats;
    }


}
