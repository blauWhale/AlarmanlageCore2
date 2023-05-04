package mqttService;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.eclipse.paho.client.mqttv3.MqttException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramNotificationBot
extends Thread implements UpdatesListener {

    private final TelegramBot bot;
    private final List<Long> users = Collections.synchronizedList(new ArrayList<Long>());
    private final Mqtt mqttClient;

    public TelegramNotificationBot(String botToken, Mqtt mqttClient) {
        bot = new TelegramBot(botToken);
        bot.setUpdatesListener(this);
        this.mqttClient=mqttClient;
    }

    public void sendStatusNotificationToAllUsers(int status) {
        System.out.println(users);
        for(Long user: users) {
            SendMessage reply = new SendMessage(user, "Status changed. Current: "+ status);
            bot.execute(reply);
        }
    }

    public void alertAlarm(){
        for(Long user: users) {
            SendMessage reply = new SendMessage(user, "The Alarm has been triggered!");
            bot.execute(reply);
        }
    }

    @Override
    public int process(List<Update> updates) {
        for(Update update: updates) {
            if(update.message() == null) continue;
            String message = update.message().text();
            if(message == null) continue;
            if (message.startsWith("/help")) {
                SendMessage reply = new SendMessage(update.message().chat().id(),
                        "Use /subscribe to subscribe to alarm updates.\n" +
                                "Use /unsubscribe to stop receiving updates.\n" +
                                "Use /disarm to disarm the alarm.\n" +
                                "Use /arm to arm the alarm.\n" +
                                "Use /testTrigger to test the alarm.\n" +
                                "Use /status to check the current status.\n" +
                                "Use /alarm to check if there is an alarm.\n" +
                                "Use /addPassword <password> to add a password (4-digit number).");
                bot.execute(reply);
            }

            if(message.startsWith("/subscribe")) {
                if(!users.contains(update.message().chat().id())) {
                    users.add(update.message().chat().id());
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "Welcome! Use /unsubscribe to stop getting notifications.");
                    bot.execute(reply);
                }else{
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You are already subscribed the alarm notifications!");
                    bot.execute(reply);
                }
            }
            if(message.startsWith("/unsubscribe")) {
                if(users.contains(update.message().chat().id())) {
                    users.remove(update.message().chat().id());
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "Byebye!");
                    bot.execute(reply);
                }else{
                    SendMessage reply = new SendMessage(update.message().chat().id(),
                            "You cannot unsubscribe something you've never subscribed to.");
                    bot.execute(reply);
                }
            }
            if(message.startsWith("/disarm")) {
                try {
                    mqttClient.turnOffAlarm();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            if(message.startsWith("/arm")) {
                try {
                    mqttClient.armAlarm();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            if(message.startsWith("/testTrigger")) {
                try {
                    mqttClient.testAlarm();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }


            if(message.startsWith("/status")) {
                SendMessage reply;
                System.out.println(Main.status);
                if (Main.status == 0){
                    reply = new SendMessage(update.message().chat().id(),
                            "The Status is: Abwesend");
                }else{
                    reply = new SendMessage(update.message().chat().id(),
                            "The Status is: Anwesend");
                }
                bot.execute(reply);
            }

            if(message.startsWith("/alarm")) {
                SendMessage reply;
                System.out.println(Main.status);
                if (Main.alarm == 1){
                    reply = new SendMessage(update.message().chat().id(),
                            "Alarm!");
                }else{
                    reply = new SendMessage(update.message().chat().id(),
                            "Kein Alarm");
                }
                bot.execute(reply);
            }

            if (message.startsWith("/addPassword")) {
                try {
                    Integer parameter = Integer.parseInt(message.replace("/addPassword ", ""));

                    // Check if the parameter is a 4-digit number
                    if (parameter < 1000 || parameter > 9999) {
                        throw new IllegalArgumentException("Invalid password format. Please enter a 4-digit number.");
                    }

                    Main.passwords.add(parameter);
                    System.out.println(Main.passwords);
                    SendMessage reply = new SendMessage(update.message().chat().id(), "Passwort gespeichert");
                    bot.execute(reply);
                } catch (NumberFormatException e) {
                    // Handle the case when the input is not a valid integer
                    SendMessage reply = new SendMessage(update.message().chat().id(), "Invalid input. Please enter a valid 4-digit number.");
                    bot.execute(reply);
                } catch (IllegalArgumentException e) {
                    // Handle the case when the input is not a 4-digit number
                    SendMessage reply = new SendMessage(update.message().chat().id(), e.getMessage());
                    bot.execute(reply);
                }
            }

        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
