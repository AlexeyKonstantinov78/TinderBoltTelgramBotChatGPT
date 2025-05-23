package com.javarush.telegram;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

@Slf4j
public class TinderBoltApp extends MultiSessionTelegramBot {
  private static final Logger log = LoggerFactory.getLogger(TinderBoltApp.class);
  public static String TELEGRAM_BOT_NAME; //TODO: добавь имя бота в кавычках
  public static String TELEGRAM_BOT_TOKEN; //TODO: добавь токен бота в кавычках
  public static String OPEN_AI_TOKEN; //TODO: добавь токен ChatGPT в кавычках
  public static String DEEPSEEK_BOT_NAME; //TODO: добавь имя бота в кавычках
  public static String DEEPSEEK_BOT_TOKEN; //TODO: добавь токен бота в кавычках
  private ChatGPTService chatGPT;
  private DialogMode currentMode = null;
  private ArrayList<String> list = new ArrayList<>();
  private UserInfo me;
  private int questionCount;

  static {
    Properties props = new Properties();
    try (InputStream input = new FileInputStream("config.properties")) {
      props.load(input); // Загружаем файл

      // Получаем значения
      TELEGRAM_BOT_NAME = props.getProperty("telegram.bot.name");
      TELEGRAM_BOT_TOKEN = props.getProperty("telegram.bot.token");
      OPEN_AI_TOKEN = props.getProperty("open.ai.token");
      DEEPSEEK_BOT_TOKEN = props.getProperty("deepseek.ai.token");
      DEEPSEEK_BOT_NAME = props.getProperty("deepseek.ai.name");

    } catch (IOException e) {
      log.error("Ошибка загрузки config.properties: " + e.getMessage());
      //System.err.println("Ошибка загрузки config.properties: " + e.getMessage());
    }
  }

  public TinderBoltApp() {
    super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    this.chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
  }


  @Override
  public void onUpdateEventReceived(Update update) {
    log.info(update.toString());
    String mess = getMessageText();
    // получаем данные от нажатой кнопки
    String key = getCallbackQueryButtonKey();
//    if (update.getMessage().getChat().getType().equals("supergroup")) {
//      log.info("Group mess: " + mess);
//      log.info("Group key: " + key);
//      return;
//    }
//
//    if (update.getMessage() == null && update.getChatMember().getChat().getType().equals("channel")) {
//      log.info("Group mess: " + mess);
//      log.info("Group key: " + key);
//      return;
//    }

    if (currentMode != null) {
      log.info("currentMode: " + currentMode.name());
    } else {
      log.info("currentMode: null");
    }

    if (!mess.isEmpty() && mess != null) {
      // получение сообщений для бота
      log.info("mess: " + mess);
      // System.out.println("mess: " + mess); // аналогично System.out.println(update.getMessage().getText());
      switch (mess) {
        case "/stop" -> {
          stop();
          return;
        }
        case "/start" -> {
          start();
          return;
        }
        case "/profile" -> {
          addProfileTinder();
          return;
        }
        case "/opener" -> {
          openerMessage();
          return;
        }
        case "/message" -> {
          commandMess();
          return;
        }
        case "/date" -> {
          correspondenceStars();
          return;
        }
        case "/gpt" -> {
          chatGptMode();
          return;
        }
        case "/deepseek" -> {
          chatDeepseek();
          return;
        }
      }

      if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
        mainMenu();

        switch (questionCount) {
          case 1:
            me.age = mess;
            questionCount = 2;
            sendTextMessage("Кем вы работаете");
            return;
          case 2:
            me.occupation = mess;
            questionCount = 3;
            sendTextMessage("У вас есть хоби");
            return;
          case 3:
            me.hobby = mess;
            questionCount = 4;
            sendTextMessage("Что вам не нравится в людях");
            return;
          case 4:
            me.annoys = mess;
            questionCount = 5;
            sendTextMessage("Цель знакомства");
            return;
          case 5:
            String answer = "";
            me.goals = mess;
            log.info(me.toString());
            //System.out.println(me.toString());
            String aboutMyself = me.toString();
            Message msg = sendTextMessage("Подождите чат думает...");
            try {
              answer = chatGPT.sendMessage(loadPrompt("profile"), aboutMyself);

            } catch (Exception e) {
              log.error(e.getMessage());
              //System.out.println(e.getMessage());
              updateTextMessage(msg, "Что-то пошло не так");
              return;
            }
            updateTextMessage(msg, answer);
            return;
        }
        return;
      }

      if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
        mainMenu();

        switch (questionCount) {
          case 1:
            me.name = mess;
            questionCount = 2;
            sendTextMessage("Сколько ей лет?");
            return;
          case 2:
            me.age = mess;
            questionCount = 3;
            sendTextMessage("Есть ли хоби");
            return;
          case 3:
            me.hobby = mess;
            questionCount = 4;
            sendTextMessage("Кем она работает?");
            return;
          case 4:
            me.occupation = mess;
            questionCount = 5;
            sendTextMessage("Цель знакомства");
            return;
          case 5:
            me.goals = mess;
            String answer = "";
            log.info(me.toString());
            //System.out.println(me.toString());
            String aboutFreand = mess;
            Message msg = sendTextMessage("Подождите чат думает...");
            try {
              answer = chatGPT.sendMessage(loadPrompt("opener"), aboutFreand);
            } catch (Exception e) {
              log.error(e.getMessage());
              //System.out.println(e.getMessage());
              updateTextMessage(msg, "Что-то пошло не так");
              return;
            }
            updateTextMessage(msg, answer);
            return;
        }
        return;
      }

      if (currentMode == DialogMode.GPT && !isMessageCommand()) {
        String send = "";
        String prompt = loadPrompt("gpt");
        Message msg = sendTextMessage("Подождите чат думает...");
        try {
          send = chatGPT.sendMessage(prompt, mess);
          log.info("Message gpt: " + send);
        } catch (Exception e) {
          log.error(e.getMessage());
          //System.out.println(e.getMessage());
          updateTextMessage(msg, "Что-то пошло не так");
          return;
        }
        updateTextMessage(msg, send);
        mainMenu();
        return;
      }

      if (currentMode == DialogMode.DATE && !isMessageCommand()) {
        String send = "";
        Message msg = sendTextMessage("Подождите чат думает...");
        try {
          send = chatGPT.addMessage(mess);

        } catch (Exception e) {
          log.error(e.getMessage());
          //System.out.println(e.getMessage());
          updateTextMessage(msg, "Что-то пошло не так");
          return;
        }

        updateTextMessage(msg, send);
        mainMenu();
        return;
      }


      if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
        list.add(mess);
        mainMenu();
        return;
      }

      sendTextMessage("*Привет*");
      sendTextMessage("_Как дела?_");
    }

    // сообщение с кнопками
    if (currentMode == null) {
      sendTextButtonsMessage("Выберете режим работы",
              "Старт", "start",
              "Переписка от вашего имени", "message",
              "Переписка со звездами", "date",
              "Стоп", "stop");
    }

    if (!key.isEmpty()) {
      log.info("getCallbackQueryButtonKey(): " + getCallbackQueryButtonKey());
      //System.out.println("getCallbackQueryButtonKey(): " + getCallbackQueryButtonKey());

      switch (key) {
        case "start" -> start();
        case "message" -> commandMess();
        case "date" -> correspondenceStars();
        case "stop" -> stop();
      }

      if (currentMode == DialogMode.DATE && key.startsWith("date_")) {
        sendPhotoMessage(key);
        sendTextMessage("Отличный выбор");
        chatGPT.setPrompt(loadPrompt(key));
      }

      if (currentMode == DialogMode.MESSAGE && key.startsWith("message_")) {
        String promt = loadPrompt(key);
        String userChatHistory = String.join("\n\n", list);
        String answer = "";
        Message msg = sendTextMessage("Подождите чат думает..."); // техническое сообщение
        try {
          answer = chatGPT.sendMessage(promt, userChatHistory);
        } catch (Exception e) {
          log.error(e.getMessage());
          //System.out.println(e.getMessage());
          updateTextMessage(msg, "Что-то пошло не так");
          return;
        }
        updateTextMessage(msg, answer); // изменяет тех сообщение на полученное
        //sendTextMessage(answer);
        list.add(answer);
      }
    }
  }

  private void chatDeepseek() {
    currentMode = DialogMode.DEEPSEEK;
//    sendPhotoMessage("deepseek");
//    String text = loadMessage("deepseek");
    sendTextMessage("DeepSeek");
  }

  private void addProfileTinder() {
    currentMode = DialogMode.PROFILE;
    me = new UserInfo();
    questionCount = 1;
    sendPhotoMessage("profile");
    sendTextMessage(loadMessage("profile"));
    sendTextMessage("Сколько вам лет");
    mainMenu();
  }

  private void openerMessage() {
    currentMode = DialogMode.OPENER;
    me = new UserInfo();
    questionCount = 1;
    sendPhotoMessage("opener");
    sendTextMessage(loadMessage("opener"));
    sendTextMessage("Имя девушки?");
    mainMenu();
  }

  private void commandMess() {
    currentMode = DialogMode.MESSAGE;
    sendPhotoMessage("message");
    sendTextButtonsMessage("Пришлите в чат вашу переписку",
            "Следующее сообщение", "message_next",
            "Пригласить на свидание", "message_date");

  }

  private void correspondenceStars() {
    currentMode = DialogMode.DATE;
    sendPhotoMessage("date");
    String text = loadMessage("date");
    sendTextMessage(text);
    sendTextButtonsMessage("Выберите девушку",
            "Ариана Гранде", "date_grande",
            "Марго Робби", "date_robbie",
            "Зендея", "date_zendaya",
            "Райан Гослинг", "date_gosling",
            "Том Харди", "date_hardy"
    );
    mainMenu();
  }

  public void start() {
    currentMode = DialogMode.MAIN;
    sendTextMessage("Это Alex Bot");
    sendPhotoMessage("main");
    String text = loadMessage("main");
    sendTextMessage(text);
    mainMenu();
  }

  public void stop() {
    currentMode = null;
    mainMenu();
    //log.info("currentMode = null");
    //System.out.println(currentMode);
  }

  public void chatGptMode() {
    currentMode = DialogMode.GPT;
    sendPhotoMessage("gpt");
    String text = loadMessage("gpt");
    sendTextMessage(text);
  }

  public void mainMenu() {
    showMainMenu(
            "Начало", "/start",
            "генерация Tinder-профиля \uD83D\uDE0E", "/profile",
            "сообщение для знакомства \uD83E\uDD70", "/opener",
            "переписка от вашего имени \uD83D\uDE08", "/message",
            "переписка со звездами \uD83D\uDD25", "/date",
            "задать вопрос чату GPT \uD83E\uDDE0", "/gpt",
            "задать вопрос чату Deepseek \uD83E\uDDE0", "/deepseek",
            "Стоп", "/stop"
    );
  }

  public static void main(String[] args) throws TelegramApiException {
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    telegramBotsApi.registerBot(new TinderBoltApp());
  }
}
