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
    public static final String TELEGRAM_BOT_TOKEN = "";
    public static final String OPEN_AI_TOKEN = "";

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> messageList = new ArrayList<>();

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        //START MODE
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

        //GPT MODE
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(loadMessage("gpt"));
            return;
        }
        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            Message serviceMessage = sendTextMessage("Chat GPT is updating info...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(serviceMessage, answer);
            return;
        }

        //DATE MODE
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");

            sendTextButtonsMessage(loadMessage("date"),
                    "Ariana Grande", "date_grande"
                    , "Margo Robbi", "date_robbie"
                    , "Zendaya", "date_zendaya"
                    , "Ryan Gosling", "date_gosling"
                    , "Tom Hardy", "date_hardy");
            return;
        }
        if (currentMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Good choice! Try to appoint a date in 5 messages ❤\uFE0F");
                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }
            Message serviceMessage = sendTextMessage("Waiting an answer...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(serviceMessage, answer);
            return;
        }

        //MESSAGE MODE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage(loadMessage("message"),
                    "Next message", "message_next"
                    , "Invite on a date", "message_date"
            );
            return;
        }
        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String chatHistory = String.join("\n\n", messageList);
                Message serviceMessage = sendTextMessage("Chat GPT is updating info...");
                String answer = chatGPT.sendMessage(loadPrompt(query), chatHistory);
                updateTextMessage(serviceMessage, answer);
                return;
            }
            messageList.add(message);
            return;

        }

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
