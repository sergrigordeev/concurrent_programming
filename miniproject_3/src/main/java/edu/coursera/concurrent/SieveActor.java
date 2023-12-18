package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;
import java.util.ArrayList;
import java.util.List;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * <p>
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     * <p>
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        SieveActorActor actor = new SieveActorActor();
        PCDP.finish(() -> {
                    actor.send(2);
                    for (int i = 3; i <= limit; i += 2) {
                        actor.send(i);
                    }
                    actor.send(0);
                }
        );
        int result = 0;
        SieveActorActor current = actor;
        while (current != null) {
            result += current.getCount();
            current = current.getNext();
        }
        return result;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        public static final int BUFFER = 1_000;
        private int count;
        private final List<Integer> localPrimes = new ArrayList<>();
        private SieveActorActor next;

        /**
         * Process a single message sent to this actor.
         * <p>
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            Integer n = (Integer) msg;
            if (n <= 0) {
                if (next != null) {
                    next.send(n);
                }
            } else {
                if (isPrime(n)) {
                    if (localPrimes.size() < BUFFER) {
                        count++;
                        localPrimes.add(n);
                    } else {
                        if (next == null) {
                            next = new SieveActorActor();
                        }
                        next.send(n);
                    }
                }
            }

        }

        private boolean isPrime(int n) {
            for (int lp : localPrimes) {
                if (n % lp == 0) {
                    return false;
                }
            }
            return true;
        }

        public SieveActorActor getNext() {
            return next;
        }

        public int getCount() {
            return count;
        }
    }
}
