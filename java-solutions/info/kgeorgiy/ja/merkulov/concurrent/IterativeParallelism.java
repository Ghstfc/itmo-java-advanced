package info.kgeorgiy.ja.merkulov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP {

    private final ParallelMapper parallelMapper;

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    public IterativeParallelism() {
        this.parallelMapper = null;
    }

    private <T, R> R mainImpl(int threadsNum, List<? extends T> values, Function<Stream<? extends T>, R> calculationOnParts,
                              Function<List<? extends R>, R> resultingCalculation) throws InterruptedException {
        List<R> result = new ArrayList<>();
        List<Stream<? extends T>> parts = partition(threadsNum, values);
        if (parallelMapper == null) {
            mainCalc(calculationOnParts, parts, result);
        } else {
            result = parallelMapper.map(calculationOnParts, parts);
        }
        return resultingCalculation.apply(result);
    }


    private <T> List<Stream<? extends T>> partition(int threadsNum, List<? extends T> values) {
        List<Stream<? extends T>> parts = new ArrayList<>();
        int size = values.size();
        int partitionSize = size / threadsNum;
        int remainder = size % threadsNum;
        for (int i = 0; i < size; i += partitionSize) {
            parts.add(values.subList(i, i + partitionSize + (remainder > 0 ? 1 : 0)).stream());
            i += remainder > 0 ? 1 : 0;
            remainder--;
        }
        return parts;
    }


    private <T, R> void mainCalc(Function<Stream<? extends T>, R> calculationOnParts, List<Stream<? extends T>> parts, List<R> result) throws InterruptedException {
        for (int i = 0; i < parts.size(); i++) {
            result.add(null);
        }
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            final int index = i;
            threads.add(new Thread(() -> result.set(index, calculationOnParts.apply(parts.get(index)))));
            threads.get(index).start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new InterruptedException("Some threads was interrupted by unknown problem " + e.getMessage());
            }
        }

    }


    @Override
    public <T> T maximum(int threadsNum, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return mainImpl(threadsNum, values,
                list -> list.max(comparator).get(),
                list -> list.stream().max(comparator).get()
        );
    }

    @Override
    public <T> T minimum(int threadsNum, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threadsNum, values, comparator.reversed());
    }


    @Override
    public <T> boolean all(int threadsNum, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mainImpl(threadsNum, values,
                list -> list.allMatch(predicate),
                booleans -> booleans.stream().allMatch(Boolean::booleanValue)
        );
    }


    @Override
    public <T> boolean any(int threadsNum, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mainImpl(threadsNum, values,
                list -> list.anyMatch(predicate),
                booleans -> booleans.stream().anyMatch(Boolean::booleanValue)
        );
    }

    @Override
    public <T> int count(int threadsNum, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return mainImpl(threadsNum, values,
                list -> (int) list.filter(predicate).count(),
                longs -> longs.stream().mapToInt(Integer::intValue).sum()
        );
    }
}
