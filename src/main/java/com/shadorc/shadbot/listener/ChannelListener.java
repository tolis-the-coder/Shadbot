package com.shadorc.shadbot.listener;

import com.shadorc.shadbot.core.setting.Setting;
import com.shadorc.shadbot.data.database.DBGuild;
import com.shadorc.shadbot.data.database.DatabaseManager;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import reactor.core.publisher.Mono;

import java.util.List;

public class ChannelListener {

    public static Mono<Void> onTextChannelDelete(TextChannelDeleteEvent event) {
        return Mono.fromRunnable(() -> {
            final DBGuild dbGuild = DatabaseManager.getInstance().getDBGuild(event.getChannel().getGuildId());
            final List<Long> allowedTextChannelIds = dbGuild.getAllowedTextChannels();
            // If the channel was an allowed channel...
            if (allowedTextChannelIds.remove(event.getChannel().getId().asLong())) {
                // ...update settings to remove the deleted one
                dbGuild.setSetting(Setting.ALLOWED_TEXT_CHANNELS, allowedTextChannelIds);
            }
        });
    }

}