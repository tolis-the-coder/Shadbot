package me.shadorc.discordbot.command;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Timer;

import org.json.JSONArray;
import org.json.JSONObject;

import me.shadorc.discordbot.Bot;
import me.shadorc.discordbot.utility.Utils;
import me.shadorc.infonet.Infonet;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class Trivia {

	private static ArrayList <IUser> alreadyAnswered = new ArrayList <> ();
	public static boolean QUIZZ_STARTED = false;

	private static String CORRECT_ANSWER;
	private static IChannel CHANNEL;

	private static final Timer timer = new Timer(30*1000, e -> {
		Bot.sendMessage("Temps écoulé, la bonne réponse était " + CORRECT_ANSWER, CHANNEL);
		Trivia.stop();
	});

	public static void start(IChannel channel) throws MalformedURLException, IOException {
		//Trivia API doc : https://opentdb.com/api_config.php
		String json = Infonet.getHTML(new URL("https://opentdb.com/api.php?amount=1"));
		JSONArray arrayResults = new JSONObject(json).getJSONArray("results");
		JSONObject result = arrayResults.getJSONObject(0);

		String category = result.getString("category");
		String type = result.getString("type");
		String difficulty = result.getString("difficulty");
		String question = result.getString("question");
		String correct_answer = result.getString("correct_answer");

		StringBuilder quizzMessage = new StringBuilder();

		quizzMessage.append("Catégorie : " + category
				+ ", type : " + type
				+ ", difficulté : " + difficulty
				+ "\nQuestion : **" + Utils.convertToUTF8(question) + "**\n");

		if(type.equals("multiple")) {
			JSONArray incorrect_answers = result.getJSONArray("incorrect_answers");

			//Place the correct answer randomly in the list
			int index = Utils.rand(incorrect_answers.length());
			for(int i = 0; i < incorrect_answers.length(); i++) {
				if(i == index) {
					quizzMessage.append("\t- " + Utils.convertToUTF8(correct_answer) + "\n");
				}
				quizzMessage.append("\t- " + Utils.convertToUTF8((String) incorrect_answers.get(i)) + "\n");
			}
		}

		Bot.sendMessage(quizzMessage.toString(), channel);

		Trivia.CORRECT_ANSWER = Utils.convertToUTF8(correct_answer);
		Trivia.CHANNEL = channel;
		Trivia.start();
	}

	public static void checkAnswer(IMessage message) {
		if(alreadyAnswered.contains(message.getAuthor())) {
			Bot.sendMessage("Désolé " + message.getAuthor().getName() + ", tu ne peux plus répondre après avoir donné une mauvaise réponse.", message.getChannel());
		}
		else if(Utils.getLevenshteinDistance(message.getContent().toLowerCase(), Trivia.CORRECT_ANSWER.toLowerCase()) < 2) {
			Bot.sendMessage("Bonne réponse " + message.getAuthor().getName() + " ! Tu gagnes 10 coins.", CHANNEL);
			Utils.gain(message.getAuthor().getName(), 10);
			Trivia.stop();
		}
		else {
			Bot.sendMessage("Mauvaise réponse.", CHANNEL);
			alreadyAnswered.add(message.getAuthor());
		}
	}

	public static void start() {
		Trivia.QUIZZ_STARTED = true;
		timer.start();
	}

	public static void stop() {
		Trivia.QUIZZ_STARTED = false;
		alreadyAnswered.clear();
		timer.stop();
	}
}
