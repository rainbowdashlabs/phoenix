/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.service;

import dev.chojo.data.snapshot.MessageSnapshot;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageStoreService {
    private static final Logger log = getLogger(MessageStoreService.class);
    // The backfilling queue that stores messages for later processing
    private final ArrayBlockingQueue<MessageSnapshot> backfill = new ArrayBlockingQueue<>(10000);
    // High priority queue that stores messages for immediate processing
    private final ArrayBlockingQueue<MessageSnapshot> cache = new ArrayBlockingQueue<>(10000);

    // TODO probably should be a thread pool at some point
    private final ScheduledExecutorService runner = Executors.newSingleThreadScheduledExecutor();

    public void processLoop() {
        while (!cache.isEmpty()) {
            process(cache.poll());
        }

        // Process the backfill queue as long as we have space in the cache
        while (!backfill.isEmpty() && cache.remainingCapacity() > cache.size()) {
            process(backfill.poll());
        }

        // Reschedule
        if (cache.isEmpty() && backfill.isEmpty()) {
            runner.schedule(this::processLoop, 1, TimeUnit.SECONDS);
        } else {
            runner.execute(this::processLoop);
        }
    }

    private void process(MessageSnapshot snapshot) {
        // TODO: Store message in database
    }

    /**
     * Store a message for later processing. This queue will be processed after the cache queue.
     *
     * @param message the message to store
     */
    public void store(MessageSnapshot message) {
        backfill.offer(message);
    }

    /**
     * Store a message with high priority. This queue will be processed before the backfill queue.
     *
     * @param message the message to store
     */
    public void storeImmediately(MessageSnapshot message) {
        cache.offer(message);
    }
}
