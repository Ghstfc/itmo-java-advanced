package info.kgeorgiy.ja.merkulov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threads;
    private final SyncQueue queue;

    private static class SyncQueue {
        private final Queue<Runnable> queue;

        public SyncQueue(Queue<Runnable> queue) {
            this.queue = queue;
        }

        synchronized void add(Runnable task) {
            queue.add(task);
            notifyAll();
        }

        synchronized Runnable poll() throws InterruptedException {
            while (queue.isEmpty())
                wait();
            return queue.poll();
        }

    }

    private static class SyncList<T> {
        private final List<T> result;
        private final int finalNumber;
        private int currentNumber;

        private SyncList(int number) {
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
        queue = new SyncQueue(new ArrayDeque<>());
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
        SyncList<R> result = new SyncList<>(args.size());
        final boolean[] exception = {false};
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            queue.add(() -> {
                try {
                    result.setter(index, f.apply(args.get(index)));
                } catch (RuntimeException e) {
                    exception[0] = true;
                }
            });
        }
        List<R> resultList = result.asList();
        if (exception[0]) {
            throw new RuntimeException("Some problems with running tasks");
        }
        return resultList;
    }


    private void taskRunner() throws InterruptedException {
        Runnable runnable;

        runnable = queue.poll();

        try {
            runnable.run();
        } catch (RuntimeException e) {
            throw new RuntimeException("Problems with running task");
        }
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
                while (true) {
                    thread.interrupt();
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
}
