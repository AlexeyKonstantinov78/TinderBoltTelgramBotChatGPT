package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TinderBoltApp extends MultiSessionTelegramBot {
  public static String TELEGRAM_BOT_NAME; //TODO: добавь имя бота в кавычках
  public static String TELEGRAM_BOT_TOKEN; //TODO: добавь токен бота в кавычках
  public static String OPEN_AI_TOKEN; //TODO: добавь токен ChatGPT в кавычках

  static {
    Properties props = new Properties();
    try (InputStream input = new FileInputStream("config.properties")) {
      props.load(input); // Загружаем файл

      // Получаем значения
      TELEGRAM_BOT_NAME = props.getProperty("telegram.bot.name");
      TELEGRAM_BOT_TOKEN = props.getProperty("telegram.bot.token");
      OPEN_AI_TOKEN = props.getProperty("open.ai.token");

    } catch (IOException e) {
      System.err.println("Ошибка загрузки config.properties: " + e.getMessage());
    }
  }

  public TinderBoltApp() {
    super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
  }

  @Override
  public void onUpdateEventReceived(Update update) {
    //TODO: основной функционал бота будем писать здесь
    String mess = getMessageText();

    if (!mess.isEmpty()) {
      // получение сообщений для бота
      System.out.println("mess: " + mess); // аналогично System.out.println(update.getMessage().getText());
      if (mess.equals("/start")) {
        start();
        return;
      }
      sendTextMessage("*Привет*");
      sendTextMessage("_Как дела?_");
    }

    // сообщение с кнопками
    sendTextButtonsMessage("Выберете режим работы", "Старт", "start", "Стоп", "stop");
    // получаем данные от нажатой кнопки
    String key = getCallbackQueryButtonKey();

    if (!key.isEmpty()) {
      System.out.println("getCallbackQueryButtonKey(): " + getCallbackQueryButtonKey());
      if (key.equals("start")) {
        start();
      }
    }
  }

  public void start() {
    sendTextMessage("Это Alex Bot");
    sendPhotoMessage("main");
    String text = loadMessage("main");
    sendTextMessage(text);
  }

  public static void main(String[] args) throws TelegramApiException {
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    telegramBotsApi.registerBot(new TinderBoltApp());
  }

}
