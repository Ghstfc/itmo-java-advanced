package info.kgeorgiy.ja.merkulov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threads;
    private final Queue<Runnable> queue;
//    private final List<Object> result;
//    private int finalNumber;
//    private int currentNumber;


    private static class Result<T> {
        private final List<T> result;
        private final int finalNumber;
        private int currentNumber;

        private Result(int number) {
            this.finalNumber = number;
            this.result = new ArrayList<>(number);
            for (int i = 0; i < number; i++) {
                result.add(null);
            }
            this.currentNumber = 0;
        }

        public void setter(int index, T object) {
            // :NOTE: synchronized
            synchronized (result) {
                result.set(index, object);
                currentNumber++;
                result.notifyAll();
            }
        }

        public List<T> asList() throws InterruptedException {
            synchronized (result) {
                List<T> res;
                while (currentNumber != finalNumber) {
                    result.wait();
                }
                res = new ArrayList<>(result);
                result.notifyAll();
                return res;
            }
        }
    }


    public ParallelMapperImpl(int threadsNum) throws InterruptedException {
        if (threadsNum <= 0) {
            throw new InterruptedException("incorrect number of threads");
        }
        threads = new ArrayList<>();
        queue = new ArrayDeque<>();
//        result = new ArrayList<>();
        for (int i = 0; i < threadsNum; i++) {
            threads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        taskRunner();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            threads.get(i).start();
        }
    }

//    private void initRes(int number) {
//        for (int i = 0; i < number; i++) {
//            result.add(null);
//        }
//    }


    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Result<R> result = new Result<>(args.size());
//        initRes(args.size());
//        finalNumber = args.size();
        for (int i = 0; i < args.size(); i++) {
            final int index = i;

            addQueue(() -> result.setter(index, f.apply(args.get(index))));


//            addQueue(() -> set(index, f.apply(args.get(index))));
        }
        return result.asList();
    }
//
//    private <R> List<R> asList() throws InterruptedException {
//        List<R> res;
//        synchronized (result) {
//            while (currentNumber != finalNumber) {
//                result.wait();
//            }
//            res = new ArrayList<>((Collection<? extends R>) result);
//            result.notifyAll();
//        }
//        return res;
//    }
//
//    private <R> void set(int index, R object) {
//        synchronized (result) {
//            result.set(index, object);
//            currentNumber++;
//            result.notifyAll();
//        }
//    }


    private void taskRunner() throws InterruptedException {
        Runnable runnable;
        synchronized (queue) {
            // :NOTE: добиться, чтобы runnable не null
            // :NOTE: перенести while в pollQueue вместо ifа
            while (queue.isEmpty()) {
                queue.wait();
            }
            runnable = pollQueue();
            queue.notifyAll();
        }

        runnable.run();
// :NOTE: обработать exception
    }

    private void addQueue(Runnable task) {
        synchronized (queue) {
            queue.add(task);
            queue.notifyAll(); // :NOTE: нужен ли notify
        }
    }

    private Runnable pollQueue() {
        Runnable runnable = null;
        synchronized (queue) {
            if (!queue.isEmpty()) {
                runnable = queue.poll();
            }
            queue.notifyAll();
        }
        return runnable;
    }


    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
            // :NOTE: дождаться
        }
    }
}
