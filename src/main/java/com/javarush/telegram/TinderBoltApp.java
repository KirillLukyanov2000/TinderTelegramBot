package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "kirs2000_tinder_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7204066660:AAGsWhDwh30vClz7EciCVIg1kJlbOWz0LS0";
    public static final String OPEN_AI_TOKEN = "sk-proj-MbOXHFV6WNdT8WZJMVk3T3BlbkFJbyMlnCvQfYFu7G1Cqx5t";

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            sendTextMessage(loadMessage("main"));

            showMainMenu("главное меню бота", "/start"
                    , "генерация Tinder-профиля \uD83D\uDE0E", "/profile"
                    , "сообщение для знакомства \uD83E\uDD70", "/opener"
                    , "переписка от вашего имени \uD83D\uDE08", "/message"
                    , "переписка со звездами \uD83D\uDD25", "/date"
                    , "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(loadMessage("gpt"));
            return;
        }
        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            String answer = chatGPT.sendMessage(prompt, message);
            sendTextMessage(answer);
            return;
        }
        sendTextMessage("*Hello!*");
        String message2 = getMessageText();
        sendTextButtonsMessage("_You wrote:_ " + message2);
        sendTextButtonsMessage("Choose button: ", "Start", "start"
                , "Stop", "stop");


    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
