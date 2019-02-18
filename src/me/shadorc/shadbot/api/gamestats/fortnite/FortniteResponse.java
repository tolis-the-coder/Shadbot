package me.shadorc.shadbot.api.gamestats.fortnite;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import reactor.util.annotation.Nullable;

public class FortniteResponse {

	@Nullable
	@JsonProperty("error")
	private String error;
	@JsonProperty("stats")
	private Stats stats;

	public Optional<String> getError() {
		return Optional.ofNullable(this.error);
	}

	public Stats getStats() {
		return this.stats;
	}

}
