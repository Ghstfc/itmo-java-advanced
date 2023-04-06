package info.kgeorgiy.ja.merkulov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {


    private <T> T comparingImpl(int threadsNum, List<? extends T> values, Function<List<? extends T>, T> calculationOnParts,
                                Function<List<? extends T>, T> resultingCalculation) throws InterruptedException {
        List<List<? extends T>> parts = partition(threadsNum, values);

        List<Thread> threads = new ArrayList<>();
        // shared list
        List<T> result = new ArrayList<>();

        mainCalc(calculationOnParts, parts, threads, result);

        // interruption checking
        interruptionChecking(threads);

        return resultingCalculation.apply(result);
    }

    private <T> boolean matchingImpl(int threadsNum, List<? extends T> values, Function<List<? extends T>, Boolean> calculationOnParts,
                                     Function<List<? extends Boolean>, Boolean> resultingCalculation) throws InterruptedException {

        List<List<? extends T>> parts = partition(threadsNum, values);

        List<Thread> threads = new ArrayList<>();
        // shared list
        List<Boolean> result = new ArrayList<>();

        mainCalc(calculationOnParts, parts, threads, result);

        // interruption checking
        interruptionChecking(threads);

        return resultingCalculation.apply(result);
    }

    private void interruptionChecking(List<Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            if (thread.isInterrupted())
                throw new InterruptedException("Thread №" + thread.getId() + " was interrupted by unknown problem");
        }
    }

    private <T> Integer countImpl(int threadsNum, List<? extends T> values,
                                  Function<List<? extends T>, Long> calculationOnParts,
                                  Function<List<? extends Long>, Integer> resultingCalculation)
            throws InterruptedException {

        List<List<? extends T>> parts = partition(threadsNum, values);

        List<Thread> threads = new ArrayList<>();
        // shared list
        List<Long> result = new ArrayList<>();

        mainCalc(calculationOnParts, parts, threads, result);

        // interruption checking
        interruptionChecking(threads);

        return resultingCalculation.apply(result);
    }


    private <T> List<List<? extends T>> partition(int threadsNum, List<? extends T> values) {
        List<List<? extends T>> parts = new ArrayList<>();
        int size = values.size();
        int partitionSize = size / threadsNum;
        int remainder = size % threadsNum;
        for (int i = 0; i < size; i += partitionSize) {
            if (size - (i + partitionSize) == remainder) {
                parts.add(values.subList(i, i + partitionSize + remainder));
                break;
            } else {
                parts.add(values.subList(i, i + partitionSize));
            }
        }
        return parts;
    }


    private <T, R> void mainCalc(Function<List<? extends T>, R> calculationOnParts, List<List<? extends T>> parts,
                                 List<Thread> threads, List<R> result) {
        for (int i = 0; i < parts.size(); i++) {
            result.add(null);
        }
        for (int i = 0; i < parts.size(); i++) {
            final int index = i;
            threads.add(new Thread(() -> result.set(index, calculationOnParts.apply(parts.get(index)))));
            threads.get(index).start();
        }
        // waiting for all processes ending
        boolean isProcessing;
        do {
            isProcessing = false;
            for (Thread t : threads) {
                if (t.isAlive()) {
                    isProcessing = true;
                    break;
                }
            }
        } while (isProcessing);
    }


    @Override
    public <T> T maximum(int threadsNum, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return comparingImpl(threadsNum, values,
                list -> list.stream().max(comparator).get(),
                list -> list.stream().max(comparator).get()

        );
    }

    @Override
    public <T> T minimum(int threadsNum, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return comparingImpl(threadsNum, values,
                list -> list.stream().min(comparator).get(),
                list -> list.stream().min(comparator).get()

        );
    }


    @Override
    public <T> boolean all(int threadsNum, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return matchingImpl(threadsNum, values,
                list -> list.stream().allMatch(predicate),
                booleans -> booleans.stream().allMatch(Boolean::booleanValue)
        );
    }


    @Override
    public <T> boolean any(int threadsNum, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return matchingImpl(threadsNum, values,
                list -> list.stream().anyMatch(predicate),
                booleans -> booleans.stream().anyMatch(Boolean::booleanValue)
        );
    }

    @Override
    public <T> int count(int threadsNum, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return countImpl(threadsNum, values,
                list -> list.stream().filter(predicate).count(),
                longs -> longs.stream().mapToInt(Long::intValue).sum()
        );
    }
}
