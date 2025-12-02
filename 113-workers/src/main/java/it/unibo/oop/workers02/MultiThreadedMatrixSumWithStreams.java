package it.unibo.oop.workers02;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is an implementation using streams.
 *
 */
public class MultiThreadedMatrixSumWithStreams implements SumMatrix {
    private final int nthread;

    /**
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedMatrixSumWithStreams(final int nthread) {
        this.nthread = nthread;
    }

    /**
     * Computes the sum of all elements in the matrix using a multithreaded approach.
     *
     * @param matrix the matrix to sum.
     * @return the total sum of the elements.
     */
    @Override
    public double sum(final double[][] matrix) {
        final List<Double> list = Arrays.stream(matrix)
            .flatMapToDouble(Arrays::stream)
            .boxed()
            .collect(Collectors.toList());

        final int size = list.size() % nthread + list.size() / nthread;
        /*
         * Build a stream of workers
         */
        return IntStream
            .iterate(0, start -> start + size)
            .limit(nthread)
            .mapToObj(start -> new Worker(list, start, size))
            .peek(Thread::start)
            .peek(MultiThreadedMatrixSumWithStreams::joinUninterruptibly)
            .mapToDouble(Worker::getResult)
            .sum();
    }

    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Worker extends Thread {
        private final List<Double> list;
        private final int startpos;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         *
         * @param list
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final List<Double> list, final int startpos, final int nelem) {
            super();
            this.list = list;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = startpos; i < list.size() && i < startpos + nelem; i++) {
                this.res += this.list.get(i);
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }
    }
}
