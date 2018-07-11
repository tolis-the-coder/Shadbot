package me.shadorc.shadbot.command.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import discord4j.core.spec.EmbedCreateSpec;
import me.shadorc.shadbot.api.wikipedia.WikipediaPage;
import me.shadorc.shadbot.api.wikipedia.WikipediaResponse;
import me.shadorc.shadbot.core.command.AbstractCommand;
import me.shadorc.shadbot.core.command.CommandCategory;
import me.shadorc.shadbot.core.command.Context;
import me.shadorc.shadbot.core.command.annotation.Command;
import me.shadorc.shadbot.core.command.annotation.RateLimited;
import me.shadorc.shadbot.utils.DiscordUtils;
import me.shadorc.shadbot.utils.NetUtils;
import me.shadorc.shadbot.utils.StringUtils;
import me.shadorc.shadbot.utils.Utils;
import me.shadorc.shadbot.utils.command.Emoji;
import me.shadorc.shadbot.utils.embed.EmbedUtils;
import me.shadorc.shadbot.utils.embed.HelpBuilder;
import me.shadorc.shadbot.utils.message.LoadingMessage;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@RateLimited
@Command(category = CommandCategory.UTILS, names = { "wiki", "wikipedia" })
public class WikiCmd extends AbstractCommand {

	@Override
	public Mono<Void> execute(Context context) {
		final String arg = context.requireArg();

		LoadingMessage loadingMsg = new LoadingMessage(context.getClient(), context.getChannelId());

		try {
			// Wiki api doc https://en.wikipedia.org/w/api.php?action=help&modules=query%2Bextracts
			final URL url = new URL(String.format("https://en.wikipedia.org/w/api.php?"
					+ "format=json"
					+ "&action=query"
					+ "&titles=%s"
					+ "&redirects=true"
					+ "&prop=extracts"
					+ "&explaintext=true"
					+ "&exintro=true"
					+ "&exsentences=5",
					NetUtils.encode(arg)));

			WikipediaResponse wikipedia = Utils.MAPPER.readValue(url, WikipediaResponse.class);
			Map<String, WikipediaPage> pages = wikipedia.getQuery().getPages();
			final String pageId = pages.keySet().toArray()[0].toString();
			final WikipediaPage page = pages.get(pageId);

			if("-1".equals(pageId) || page.getExtract() == null) {
				return context.getAuthorName()
						.flatMap(username -> loadingMsg.send(String.format(Emoji.MAGNIFYING_GLASS + " (**%s**) No Wikipedia resultats found for `%s`",
								username, arg)))
						.then();
			}

			final String extract = StringUtils.truncate(page.getExtract(), DiscordUtils.DESCRIPTION_CONTENT_LIMIT);

			return context.getAuthorAvatarUrl()
					.map(avatarUrl -> EmbedUtils.getDefaultEmbed()
							.setAuthor(String.format("Wikipedia: %s", page.getTitle()),
									String.format("https://en.wikipedia.org/wiki/%s", page.getTitle().replace(" ", "_")),
									avatarUrl)
							.setDescription(extract))
					.flatMap(loadingMsg::send)
					.then();

		} catch (IOException err) {
			loadingMsg.stopTyping();
			throw Exceptions.propagate(err);
		}
	}

	@Override
	public Mono<EmbedCreateSpec> getHelp(Context context) {
		return new HelpBuilder(this, context)
				.setDescription("Show Wikipedia description for a search.")
				.addArg("search", false)
				.build();
	}

}
