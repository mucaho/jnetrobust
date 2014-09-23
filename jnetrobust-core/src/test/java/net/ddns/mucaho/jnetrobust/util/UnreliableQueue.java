package net.ddns.mucaho.jnetrobust.util;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class UnreliableQueue<T extends Freezable<T>> {
    public interface QueueListener<T> {
        public void notifyLoss(T elem);

        public void notifyDuplicate(T elem);
    }

    private final DelayQueue<UnreliableDelayed> queue = new DelayQueue<UnreliableDelayed>();
    private long minDelay;
    private long maxDelay;
    private float dupChance;
    private float lossChance;
    private final QueueListener<T> listener;

    public UnreliableQueue(QueueListener<T> listener) {
        this(listener, 100, 250, 0f, 0f);
    }

    public UnreliableQueue(QueueListener<T> listener, long minDelay, long maxDelay,
                           float lossChance, float dupChance) {
        this.listener = listener;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.dupChance = dupChance;
        this.lossChance = lossChance;
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean offer(T e) {
        int counter = 0;
        if (Math.random() > lossChance) {
            do {
                queue.offer(new UnreliableDelayed((T) e.clone(), calculateLag()));
                if (counter > 0)
                    listener.notifyDuplicate(e);
                counter++;
            } while (Math.random() < dupChance);
        } else {
            listener.notifyLoss(e);
        }

        return true;
    }

    public synchronized T poll() {
        T out = null;

        UnreliableDelayed delayed = queue.poll();
        if (delayed != null)
            out = delayed.get();

        return out;
    }

    private long calculateLag() {
        return minDelay + (long) (Math.random() * (maxDelay - minDelay));
    }


    public synchronized long getMinDelay() {
        return minDelay;
    }

    public synchronized void setMinDelay(long minDelay) {
        this.minDelay = minDelay;
    }

    public synchronized long getMaxDelay() {
        return maxDelay;
    }

    public synchronized void setMaxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public synchronized float getDupChance() {
        return dupChance;
    }

    public synchronized void setDupChance(float dupChance) {
        this.dupChance = dupChance;
    }

    public synchronized float getLossChance() {
        return lossChance;
    }

    public synchronized void setLossChance(float lossChance) {
        this.lossChance = lossChance;
    }


    private class UnreliableDelayed implements Delayed {
        private final T object;
        private final long finishedTime;

        public UnreliableDelayed(T object, long delay) {
            this.object = object;
            this.finishedTime = System.currentTimeMillis() + delay;
        }

        public T get() {
            return object;
        }

        @Override
        public int compareTo(Delayed other) {
            long thisDelay = this.getDelay(TimeUnit.MILLISECONDS);
            long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
            return (int) (thisDelay - otherDelay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(finishedTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
