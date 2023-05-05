package info.kgeorgiy.ja.merkulov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {


    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;

    public WebCrawler(Downloader downloader, int downloaderNumber, int extractorNumber, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaderNumber);
        this.extractors = Executors.newFixedThreadPool(extractorNumber);
    }

    @Override
    public Result download(String url, int depth) {
        // nD -> ext -> nD
        ConcurrentLinkedQueue<String> nextLayer = new ConcurrentLinkedQueue<>();
        nextLayer.add(url);
        ConcurrentLinkedQueue<String> extracted = new ConcurrentLinkedQueue<>();
        Set<String> alreadyDownloaded = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap<String, IOException> errors = new ConcurrentHashMap<>();

        // :NOTE: Phaser need here

        for (int i = depth; i >= 1; i--) {

            Phaser phaser = new Phaser(1);
            final int curDepth = i;
            while (!nextLayer.isEmpty()) {
                String s = nextLayer.poll();
                if (!alreadyDownloaded.contains(s) && !errors.containsKey(s)) {
                    phaser.register();
                    downloaders.submit(() -> downloader(alreadyDownloaded, extracted, s, errors, phaser, curDepth));
                    alreadyDownloaded.add(s);
                }
            }
            phaser.arriveAndAwaitAdvance();
            nextLayer.addAll(extracted);
            extracted.clear();
        }
        return new Result(new ArrayList<>(alreadyDownloaded), errors);
    }


    private void downloader(Set<String> alreadyDownloaded, ConcurrentLinkedQueue<String> extracted,
                            String url, ConcurrentHashMap<String, IOException> errors, Phaser done, int curDepth) {

        try {
            Document doc = downloader.download(url);
            alreadyDownloaded.add(url);
            if (curDepth == 1) {
                done.arrive();
                return;
            }
            extractors.submit(() -> extractor(extracted, doc, done));
        } catch (IOException e) {
            alreadyDownloaded.remove(url);
            errors.putIfAbsent(url, e);
            done.arrive();
        } finally {
//            done.arrive();
        }
    }

    private void extractor(ConcurrentLinkedQueue<String> extracted, Document doc,
                           Phaser done) {
        try {
            List<String> urls = doc.extractLinks();
            extracted.addAll(urls);
        } catch (IOException ignored) {
        } finally {
            done.arrive();
        }
    }


    @Override
    public void close() {
        // https://docs.oracle.com/en/java/javase/20/docs/api/java.base/java/util/concurrent/ExecutorService.html
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    public static void main(String[] args) {
        // :NOTE: WebCrawler url [depth [downloads [extractors [perHost]]]]
        if (args.length != 5) {
            throw new RuntimeException("Not 5 args");
        }
        for (String arg : args) {
            if (arg == null)
                throw new RuntimeException("Some arguments are null");
        }
        for (int i = 1; i < args.length; i++) {
            try {
                Integer.parseInt(args[i]);
            } catch (RuntimeException e) {
                throw new RuntimeException("There are not numbers in params");
            }
        }
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(1), Integer.parseInt(args[2]),
                Integer.parseInt(args[3]), Integer.parseInt(args[4]))) {
            Result res = crawler.download(args[0], Integer.parseInt(args[1]));
            System.err.println(res.getDownloaded());
            System.err.println(res.getErrors().keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
