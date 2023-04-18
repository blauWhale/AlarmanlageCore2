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
// methode zum telegram msg becho sobald sich de wert gänderet het
//    public void sendStatusNotificationToAllUsers(int status) {
//        System.out.println(users);
//        for(Long user: users) {
//            SendMessage reply = new SendMessage(user, "Status changed. Current: "+ status);
//            bot.execute(reply);
//        }
//    }

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
            if(message.startsWith("/help")) {
                SendMessage reply = new SendMessage(update.message().chat().id(), "Use /subscribe to subscribe to temperature updates. Use /unsubscribe to leave");
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
                            "You are already subscribed the temperature notifications!");
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
            if(message.startsWith("/off")) {
                try {
                    mqttClient.turnOffAlarm();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
            if(message.startsWith("/status")) {
                SendMessage reply;
                System.out.println(Main.status);
                if (Main.status == 0){
                    reply = new SendMessage(update.message().chat().id(),
                            "The alarm is off");
                }else{
                    reply = new SendMessage(update.message().chat().id(),
                            "The alarm is on");
                }
                bot.execute(reply);
            }
        }

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
