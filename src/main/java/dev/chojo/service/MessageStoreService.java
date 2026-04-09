/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.EncryptedContent;
import dev.chojo.crypto.policy.KeyRotationPolicy;
import dev.chojo.crypto.processing.Encryptor;
import dev.chojo.crypto.processing.StringEncryptor;
import dev.chojo.crypto.processing.model.BytesProcessInput;
import dev.chojo.crypto.processing.model.BytesProcessResult;
import dev.chojo.crypto.processing.wrapper.RSAAlgorithmWrapper;
import dev.chojo.data.dao.GuildSettings;
import dev.chojo.data.repository.GuildSettingsRepository;
import dev.chojo.data.repository.MessageRepository;
import dev.chojo.data.snapshot.EncryptedMessage;
import dev.chojo.data.snapshot.MessageSnapshot;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.slf4j.Logger;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class MessageStoreService {
    private static final Logger log = getLogger(MessageStoreService.class);
    private final Cache<Long, StringEncryptor> encryptors =
            CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    // The backfilling queue that stores messages for later processing
    private final ArrayBlockingQueue<MessageSnapshot> backfill = new ArrayBlockingQueue<>(10000);
    // High priority queue that stores messages for immediate processing
    private final ArrayBlockingQueue<MessageSnapshot> cache = new ArrayBlockingQueue<>(10000);
    private final JsonMapper jsonMapper = new JsonMapper();
    private final Configuration configuration;
    private final CryptoService cryptoService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final MessageRepository messageRepository;

    // TODO probably should be a thread pool at some point
    private final ScheduledExecutorService runner = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public MessageStoreService(
            Configuration configuration,
            CryptoService cryptoService,
            GuildSettingsRepository guildSettingsRepository,
            MessageRepository messageRepository) {
        this.cryptoService = cryptoService;
        this.guildSettingsRepository = guildSettingsRepository;
        this.configuration = configuration;
        this.messageRepository = messageRepository;
        runner.execute(this::processLoop);
    }

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
        if (!guildSettingsRepository.get(snapshot.guildId()).crypto().hasPublicKey()) {
            // Do not store messages without a public key for encryption
            return;
        }
        String content = jsonMapper.writeValueAsString(snapshot.content());
        EncryptedContent encrypted = getEncryptor(snapshot.guildId()).encrypt(content);
        EncryptedMessage encryptedMessage = EncryptedMessage.create(encrypted, snapshot);
        messageRepository.storeMessage(encryptedMessage);
    }

    private StringEncryptor getEncryptor(long guildId) {
        try {
            return encryptors.get(guildId, () -> createEncryptor(guildId));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private StringEncryptor createEncryptor(long guildId) {
        GuildSettings guildSettings = guildSettingsRepository.get(guildId);
        RSAAlgorithmWrapper rsaAlgorithmWrapper = guildSettings.crypto().publicKey();
        Encryptor<BytesProcessInput, BytesProcessResult> rsaEncryptor = new Encryptor<>(rsaAlgorithmWrapper);
        KeyRotationPolicy keyRotationPolicy = new KeyRotationPolicy(
                configuration.main().crypto().rotationInterval(), () -> new Encryptor<>(cryptoService.randomAESKey()));
        return new StringEncryptor(rsaEncryptor, keyRotationPolicy);
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

    public long oldestKnownMessage(Channel channel) {
        return 0;
    }
}
