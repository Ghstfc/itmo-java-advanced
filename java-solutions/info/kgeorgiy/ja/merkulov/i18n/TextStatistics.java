package info.kgeorgiy.ja.merkulov.i18n;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class TextStatistics {

    private static final String TAB = "\t";
    private static final String DOTS = " : ";
    private static final String EOL = System.lineSeparator();
    private static final String LBR = " (";
    private static final String RBR = ")";


    public static void main(String[] args) {

        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Illegal args");
        }

        if (!args[1].equals("en") && !args[1].equals("ru")) {
            throw new IllegalArgumentException("Illegal locale");
        }


        try {
            String text = Files.readString(Path.of(args[2]), StandardCharsets.UTF_8);
            Locale inputLocale = new Locale(args[0]);
            StatsCalculator calculator = new StatsCalculator(inputLocale, text);
            calculator.calculateStats("word");
            calculator.calculateStats("sent");
            calculator.calculateStats("");


            ResourceBundle bundle = null;
            Locale outputLocale = new Locale(args[1]);
            if (outputLocale.getLanguage().equals("ru")) {
                bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.merkulov.i18n.UsageResourceBundle_ru");
            } else if (outputLocale.getLanguage().equals("en")) {
                bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.merkulov.i18n.UsageResourceBundle_en");
            }

            // :NOTE: try-with-resources
            BufferedWriter writer = new BufferedWriter(new FileWriter(args[3], StandardCharsets.UTF_8));
            DateStats dateStats = calculator.getDateStats();
            NumberStats numberStats = calculator.getNumberStats();
            NumberStats moneyStats = calculator.getMoneyStats();
            TextStats sentenceStats = calculator.getSentenceStats();
            TextStats wordStats = calculator.getWordStats();
            // writing whole info
            writeWholeInfo(args, bundle, writer, dateStats, numberStats, moneyStats, sentenceStats, wordStats);
            writeSentenceStats(bundle, writer, sentenceStats);
            writeWordStat(bundle, writer, wordStats);
            writeNumberStats(bundle, writer, numberStats);
            writeMoneyStats(bundle, writer, moneyStats);
            writeDateStats(bundle, writer, dateStats);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Unknown file: " + e.getMessage());
        }


    }

    private static void writeDateStats(ResourceBundle bundle, BufferedWriter writer, DateStats dateStats) throws IOException {
        writer.write(
                bundle.getString("dateStats") + EOL +
                        TAB + bundle.getString("dateNumber") + DOTS + dateStats.count + EOL +
                        TAB + bundle.getString("minDate") + DOTS + dateStats.minDate.toString() + EOL +
                        TAB + bundle.getString("maxDate") + DOTS + dateStats.maxDate.toString() + EOL +
                        TAB + bundle.getString("averageLengthDate") + DOTS + dateStats.average + EOL

        );
    }

    private static void writeMoneyStats(ResourceBundle bundle, BufferedWriter writer, NumberStats moneyStats, String type) throws IOException {
        writer.write(
                bundle.getString("moneyStats") + EOL +
                        TAB + bundle.getString("moneyNumber") + DOTS + moneyStats.count + EOL +
                        TAB + bundle.getString("minMoney") + DOTS + moneyStats.minValue + EOL +
                        TAB + bundle.getString(type + "max") + DOTS + moneyStats.maxValue + EOL +
                        TAB + bundle.getString(type + "average") + DOTS + moneyStats.averageNumber + EOL + EOL
        );
    }

    private static void writeNumberStats(ResourceBundle bundle, BufferedWriter writer, NumberStats numberStats) throws IOException {
        writer.write(
                bundle.getString("numberStats") + EOL +
                        TAB + bundle.getString("numbersNumber") + DOTS + numberStats.count + EOL +
                        TAB + bundle.getString("minNumber") + DOTS + numberStats.minValue + EOL +
                        TAB + bundle.getString("maxNumber") + DOTS + numberStats.maxValue + EOL +
                        TAB + bundle.getString("averageNumber") + DOTS + numberStats.averageNumber + EOL + EOL
        );
    }

    private static void writeWordStat(ResourceBundle bundle, BufferedWriter writer, TextStats wordStats) throws IOException {
        writer.write(
                bundle.getString("wordStats") + EOL +
                        TAB + bundle.getString("wordsNumber") + DOTS + wordStats.numberSegments + EOL +
                        TAB + bundle.getString("minLexicWord") + DOTS + wordStats.minLexic + EOL +
                        TAB + bundle.getString("maxLexicWord") + DOTS + wordStats.maxLexic + EOL +
                        TAB + bundle.getString("minLengthWord") + DOTS + wordStats.minSegment.length() + LBR + wordStats.minSegment + RBR + EOL +
                        TAB + bundle.getString("maxLengthWord") + DOTS + wordStats.maxSegment.length() + LBR + wordStats.maxSegment + RBR + EOL +
                        TAB + bundle.getString("averageLengthWord") + DOTS + wordStats.averageLength + EOL + EOL
        );
    }

    private static void writeSentenceStats(ResourceBundle bundle, BufferedWriter writer, TextStats sentenceStats) throws IOException {
        writer.write(
                bundle.getString("sentenceStats") + EOL +
                        TAB + bundle.getString("sentenceNumber") + DOTS + sentenceStats.numberSegments + EOL +
                        TAB + bundle.getString("minLexicSentence") + DOTS + sentenceStats.minLexic + EOL +
                        TAB + bundle.getString("maxLexicSentence") + DOTS + sentenceStats.maxLexic + EOL +
                        TAB + bundle.getString("minLengthSentence") + DOTS + sentenceStats.minSegment.length() + LBR + sentenceStats.minSegment + RBR + EOL +
                        TAB + bundle.getString("maxLengthSentence") + DOTS + sentenceStats.maxSegment.length() + LBR + sentenceStats.maxSegment + RBR + EOL +
                        TAB + bundle.getString("averageLengthSentence") + DOTS + sentenceStats.averageLength + EOL + EOL
        );
    }

    private static void writeWholeInfo(String[] args, ResourceBundle bundle, BufferedWriter writer, DateStats dateStats, NumberStats numberStats,
                                       NumberStats moneyStats, TextStats sentenceStats, TextStats wordStats) throws IOException {
        writer.write(
                bundle.getString("analyzed") + DOTS + args[2] + EOL +
                        bundle.getString("wholeStats") + EOL +
                        TAB + bundle.getString("sentenceNumber") + DOTS + sentenceStats.numberSegments + EOL +
                        TAB + bundle.getString("wordsNumber") + DOTS + wordStats.numberSegments + EOL +
                        TAB + bundle.getString("numbersNumber") + DOTS + numberStats.count + EOL +
                        TAB + bundle.getString("moneyNumber") + DOTS + moneyStats.count + EOL +
                        TAB + bundle.getString("dateNumber") + DOTS + dateStats.count + EOL + EOL
        );
    }

}
