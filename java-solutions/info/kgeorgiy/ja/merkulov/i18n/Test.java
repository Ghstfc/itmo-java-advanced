package info.kgeorgiy.ja.merkulov.i18n;

import org.junit.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class Test {

    private static final String SENTENCE = "As the countdown to the opening day continued, " +
            "excitement filled the air in Metropolis.";

    private static final String NUMBER = "1 2 3 4 100 10 1";

    private static final String MONEY = "$1 $2 $5 $10 $10";

    private static final String DATE = "26.05.2023 25.05.2023";

    @org.junit.Test
    public void test1() {
        StatsCalculator calculator = new StatsCalculator(Locale.US, NUMBER);
        calculator.calculateStats("num");
        NumberStats stats = calculator.getNumberStats();
        Assert.assertEquals(stats.count, 7);
        Assert.assertEquals(stats.maxValue, 100, 0.0001);
        Assert.assertEquals(stats.minValue, 1, 0.0001);
        Assert.assertEquals(stats.averageNumber, (float) (1 + 2 + 3 + 4 + 100 + 10 + 1) / 7, 0.0001);
        Assert.assertEquals(stats.differentNumbers, 6);
    }

    @org.junit.Test
    public void test2() {
        StatsCalculator calculator = new StatsCalculator(Locale.US, MONEY);
        calculator.calculateStats("num");
        NumberStats stats = calculator.getMoneyStats();
        Assert.assertEquals(stats.count, 5);
        Assert.assertEquals(stats.maxValue, 10, 0.0001);
        Assert.assertEquals(stats.minValue, 1, 0.0001);
        Assert.assertEquals(stats.averageNumber, (float) (1 + 2 + 5 + 10 + 10) / 5, 0.0001);
        Assert.assertEquals(stats.differentNumbers, 4);
    }

    @org.junit.Test
    public void test3() {
        StatsCalculator calculator = new StatsCalculator(new Locale("ru", "RU"), MONEY);
        calculator.calculateStats("num");
        NumberStats stats = calculator.getMoneyStats();
        Assert.assertNotEquals(stats.count, 5);
        Assert.assertNotEquals(stats.maxValue, 10, 0.0001);
        Assert.assertNotEquals(stats.minValue, 1, 0.0001);
        Assert.assertNotEquals(stats.averageNumber, (float) (1 + 2 + 5 + 10 + 10) / 5, 0.0001);
        Assert.assertNotEquals(stats.differentNumbers, 4);
    }

    @org.junit.Test
    public void test4() {
        StatsCalculator calculator = new StatsCalculator(Locale.US, SENTENCE);
        calculator.calculateStats("sent");
        TextStats stats = calculator.getSentenceStats();
        Assert.assertEquals(SENTENCE, stats.minLexic);
        Assert.assertEquals(SENTENCE, stats.maxLexic);
        Assert.assertEquals(SENTENCE.length(), stats.maxSegment.length());
        Assert.assertEquals(SENTENCE, stats.maxSegment);
        Assert.assertEquals(SENTENCE.length(), stats.minSegment.length());
        Assert.assertEquals(SENTENCE, stats.minSegment);
        Assert.assertEquals(SENTENCE.length(), stats.averageLength, 0.0001);
        Assert.assertEquals(1, stats.numberSegments);
    }

    @org.junit.Test
    public void test5() throws ParseException {
        DateFormat dateFormat;
        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("ru", "RU"));
        Date min = dateFormat.parse("26.05.2023");
        Date max = dateFormat.parse("25.05.2023");

        StatsCalculator calculator = new StatsCalculator(new Locale("ru", "RU"), DATE);
        calculator.calculateStats("num");
        DateStats stats = calculator.getDateStats();
        Assert.assertEquals(2, stats.count);
        Assert.assertEquals(2, stats.unique);
        Assert.assertEquals(max.toString(), stats.maxDate.toString());
        Assert.assertEquals(min.toString(), stats.minDate.toString());
    }


}
