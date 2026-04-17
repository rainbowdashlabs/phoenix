/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package dev.chojo.commands.slash;

import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.configuration.elements.sub.Crypto;
import dev.chojo.crypto.CryptoService;
import dev.chojo.crypto.exceptions.CryptoException;
import dev.chojo.crypto.processing.wrapper.AsymAlgorithmWrapper;
import dev.chojo.crypto.serialization.PlainAsymAlgorithmWrapper;
import dev.chojo.data.dao.GuildSettings;
import dev.chojo.data.repository.GuildSettingsRepository;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.annotations.interactions.Modal;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;
import io.github.kaktushose.jdac.dispatching.events.interactions.ModalEvent;
import io.github.kaktushose.jdac.message.placeholder.Entry;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Interaction("setup")
public class Setup {

    private final CryptoService cryptoService;
    private final GuildSettingsRepository settingsRepository;
    private final Configuration configuration;

    @Inject
    public Setup(CryptoService cryptoService, GuildSettingsRepository settingsRepository, Configuration configuration) {
        this.cryptoService = cryptoService;
        this.settingsRepository = settingsRepository;
        this.configuration = configuration;
    }

    @Command(value = "encryption new", desc = "Setup encryption for the guild")
    public void onEncryptionNew(CommandEvent event) {
        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
        if (guildSettings.crypto().hasPublicKey()) {
            event.reply("commands-slash-setup-encryption-new-message-error-keyexists");
            return;
        }
        event.deferReply();
        Crypto crypto = configuration.main().crypto();
        KeyPair keyPair = cryptoService.generateRSAKeyPair();
        AsymAlgorithmWrapper asymAlgorithmWrapper =
                new AsymAlgorithmWrapper(keyPair.getPrivate(), crypto.asymmetricCipher());
        AsymAlgorithmWrapper asymAlgorithmWrapperPublic =
                new AsymAlgorithmWrapper(keyPair.getPublic(), crypto.asymmetricCipher());
        guildSettings.crypto().setPublicKey(PlainAsymAlgorithmWrapper.wrap(asymAlgorithmWrapperPublic));
        event.reply("commands-slash-setup-encryption-new-message-success");
        MessageCreateData build = new MessageCreateBuilder()
                .setContent("commands-slash-setup-encryption-new-message-success")
                .addFiles(FileUpload.fromData(
                        PlainAsymAlgorithmWrapper.wrap(asymAlgorithmWrapper)
                                .key()
                                .getBytes(StandardCharsets.UTF_8),
                        "key.pem"))
                .build();
        event.reply(build);
    }

    @Command(value = "encryption upload", desc = "Setup encryption for the guild")
    public void onEncryptionUpload(CommandEvent event) {
        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
        if (guildSettings.crypto().hasPublicKey()) {
            event.reply("commands-slash-setup-encryption-upload-message-error-keyexists");
            return;
        }
        String label = "Upload your own RSA public key. This key must have a key length of %d"
                .formatted(configuration.main().crypto().asymmetricKeySize());
        event.replyModal(
                "onPublicKeyModal",
                List.of(
                        TextDisplay.of(label),
                        Label.of("Upload your public key here", TextInput.of("pubkey", TextInputStyle.PARAGRAPH))));
    }

    @Modal("Upload public key")
    public void onPublicKeyModal(ModalEvent event) {
        event.deferReply();
        ModalMapping pubkey = event.value("pubkey");
        if (pubkey.getAsString().isEmpty()) {
            event.reply("commands-slash-setup-encryption-upload-message-error-nokey");
            return;
        }

        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
        if (guildSettings.crypto().hasPublicKey()) {
            event.reply("commands-slash-setup-encryption-upload-message-error-keyexists");
            return;
        }

        PlainAsymAlgorithmWrapper plainWrapper = new PlainAsymAlgorithmWrapper(
                pubkey.getAsString(), configuration.main().crypto().asymmetricCipher());
        AsymAlgorithmWrapper unwrap;
        try {
            unwrap = plainWrapper.unwrap();
        } catch (CryptoException e) {
            event.reply("commands-slash-setup-encryption-upload-message-error-invalidkey");
            return;
        }

        if (!(unwrap.key() instanceof RSAPublicKey publicKey)) {
            event.reply("commands-slash-setup-encryption-upload-message-error-invalidkey");
            return;
        }

        int expectedKeySize = configuration.main().crypto().asymmetricKeySize();
        int actualKeySize = publicKey.getModulus().bitLength();
        if (actualKeySize != expectedKeySize) {
            event.reply(
                    "commands-slash-setup-encryption-upload-message-error-keysize",
                    Entry.entry("expected", expectedKeySize),
                    Entry.entry("actual", actualKeySize));
            return;
        }

        if (guildSettings.crypto().setPublicKey(plainWrapper)) {
            event.reply("commands-slash-setup-encryption-upload-message-success");
        } else {
            event.reply("commands-slash-setup-encryption-upload-message-error-keyexists");
        }
    }

    @Command(value = "encryption delete", desc = "Delete encryption keys for the guild")
    public void onEncryptionDelete(CommandEvent event) {
        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
        guildSettings.crypto().clearPublicKey();
        event.reply("commands-slash-setup-encryption-delete-message-success");
    }
}
