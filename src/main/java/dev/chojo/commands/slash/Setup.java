package dev.chojo.commands.slash;

import com.google.inject.Inject;
import dev.chojo.configuration.Configuration;
import dev.chojo.crypto.CryptoService;
import dev.chojo.data.dao.GuildSettings;
import dev.chojo.data.repository.GuildSettingsRepository;
import io.github.kaktushose.jdac.annotations.interactions.Command;
import io.github.kaktushose.jdac.annotations.interactions.Interaction;
import io.github.kaktushose.jdac.dispatching.events.interactions.CommandEvent;

@Interaction("setup")
public class Setup {

    private final CryptoService service;
    private final GuildSettingsRepository settingsRepository;
    private final Configuration configuration;

    @Inject
    public Setup(CryptoService service, GuildSettingsRepository settingsRepository, Configuration configuration) {
        this.service = service;
        this.settingsRepository = settingsRepository;
        this.configuration = configuration;
    }

    @Command(value = "encryption new", desc = "Setup encryption for the guild")
    public void onEncryptionNew(CommandEvent event) {
        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
        if(guildSettings.crypto().hasPublicKey()){
            event.reply("commands-slash-setup-encryption-new-message-error-keyexists");
            return;
        }
        event.reply("commands-slash-setup-encryption-new-success");
    }

    @Command(value = "encryption upload", desc = "Setup encryption for the guild")
    public void onEncryptionUpload(CommandEvent event) {
        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
    }

    @Command(value = "encryption delete", desc = "Delete encryption keys for the guild")
    public void onEncryptionDelete(CommandEvent event) {
        GuildSettings guildSettings = settingsRepository.get(event.getGuild());
    }


}
