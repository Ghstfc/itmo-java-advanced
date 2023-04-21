package info.kgeorgiy.ja.merkulov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threads;
    private final Queue<Runnable> queue;

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

        public synchronized void setter(int index, T object) {
            result.set(index, object);
            currentNumber++;
            notifyAll();
        }

        public synchronized List<T> asList() throws InterruptedException {
            List<T> res;
            while (currentNumber != finalNumber) {
                wait();
            }
            res = new ArrayList<>(result);
            notifyAll();
            return res;
        }
    }


    public ParallelMapperImpl(int threadsNum) throws InterruptedException {
        if (threadsNum <= 0) {
            throw new InterruptedException("incorrect number of threads");
        }
        threads = new ArrayList<>();
        queue = new ArrayDeque<>();
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


    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Result<R> result = new Result<>(args.size());
        final boolean[] exception = {false};
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            addQueue(() -> {
                try {
                    result.setter(index, f.apply(args.get(index)));
                } catch (RuntimeException e) {
                    exception[0] = true;
                }
            });
        }
        if (exception[0]) {
            throw new RuntimeException("Some problems with running tasks");
        }
        return result.asList();
    }


    private void taskRunner() throws InterruptedException {
        Runnable runnable;
        synchronized (queue) {
            // :NOTE: добиться, чтобы runnable не null
            while (queue.isEmpty()) {
                queue.wait();
            }
            runnable = pollQueue();
            queue.notifyAll();
        }
        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw new RuntimeException("Problems with running task");
        }
    }

    private void addQueue(Runnable task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    private Runnable pollQueue() {
        Runnable runnable = null;
        synchronized (queue) {
            // как-будто бы, если вмсесто if поставить while, то будет грустно,
            // ибо из очереди уйдут все задачи 1 потоку, чего мы не очень хотим
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
            } finally {
                boolean tr = true;
                while (tr) {
                    thread.interrupt();
                    try {
                        thread.join();
                        tr = false;
                    } catch (InterruptedException ignored){}
                }
            }
            // :NOTE: дождаться
        }
    }
}
