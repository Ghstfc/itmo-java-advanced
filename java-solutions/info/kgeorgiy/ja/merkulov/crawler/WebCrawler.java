package info.kgeorgiy.ja.merkulov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {


    private final Downloader downloader;
    private final ThreadPoolExecutor downloaders;
    private final ThreadPoolExecutor extractors;

    public WebCrawler(Downloader downloader, int downloaderNumber, int extractorNumber, int perHost) {
        this.downloader = downloader;
        this.downloaders = new ThreadPoolExecutor(1, downloaderNumber, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        this.extractors = new ThreadPoolExecutor(1, extractorNumber, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
    }

    @Override
    public Result download(String url, int depth) {
        // nD -> ext -> nD
        BlockingQueue<String> notDownloaded = new LinkedBlockingDeque<>();
        notDownloaded.add(url);
        BlockingQueue<String> extracted = new LinkedBlockingDeque<>();
        Set<String> alreadyDownloaded = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap<String, IOException> errors = new ConcurrentHashMap<>();

        for (int i = depth; i >= 1; i--) {

            CountDownLatch done = new CountDownLatch(notDownloaded.size());

            while (!notDownloaded.isEmpty()) {
                String s = notDownloaded.poll();
                downloaders.submit(() -> downloader(alreadyDownloaded, extracted, s, errors, done));
            }
            try {
                done.await();
                notDownloaded.addAll(extracted);
                extracted.clear();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new Result(new ArrayList<>(alreadyDownloaded), errors);
    }


    private void downloader(Set<String> alreadyDownloaded, BlockingQueue<String> extracted, String url, ConcurrentHashMap<String,
            IOException> errors, CountDownLatch done) {

        if (alreadyDownloaded.contains(url)) {
            done.countDown();
            return;
        }
        try {
            alreadyDownloaded.add(url);
            Document doc = downloader.download(url);
            extractors.execute(() -> extractor(extracted, doc, errors, url, done));

        } catch (IOException e) {
            done.countDown();
            errors.putIfAbsent(url, e);
        }
    }

    private void extractor(BlockingQueue<String> extracted, Document doc, ConcurrentHashMap<String, IOException> errors,
                           String url, CountDownLatch done) {
        try {
            List<String> urls = doc.extractLinks();
            extracted.addAll(urls);
            done.countDown();
        } catch (IOException e) {
            errors.putIfAbsent(url, e);
            done.countDown();
        }
    }


    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            throw new RuntimeException("Not 5 args");
        }
        for (String arg : args) {
            if (arg == null)
                throw new RuntimeException("Some arguments are null");
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
