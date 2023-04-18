package mqttService;

import org.eclipse.paho.client.mqttv3.MqttException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    //status 0 = abwesend & status 1 = anwesend
    public static int status = 0;
    public static int password = 0;
    private static Logger logger;
    private static Properties config;

    private static boolean loadConfig() {
        config = new Properties();
        try {
            config.load(new FileReader("config.properties"));
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading config file",e);
        }
        return false;
    }

    public final static void main(String[] args) throws InterruptedException, MqttException {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        Logger.getGlobal().addHandler(ch);

        logger = Logger.getLogger("main");

        if(!loadConfig()) return;

        logger.info("Config file loaded");

        logger.info("TelegramBot started");

        Mqtt mqttClient = new Mqtt(config.getProperty("mqtt-url"), "runner-13");
        TelegramNotificationBot tnb = new TelegramNotificationBot(config.getProperty("telegram-apikey"),mqttClient);


        try {
            mqttClient.start();
            mqttClient.subscribe("alarmanlage/#");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttClient.addHandler((s, mqttMessage) -> {
            if(s.equals("alarmanlage/status")) {
                status = Integer.parseInt(mqttMessage.toString());
            }

            if(s.equals("alarmanlage/numpad")) {
                password = Integer.parseInt(mqttMessage.toString());
            }
        });


        int lastStatus = status;
        int lastPassword = password;

        while(true) {
            if(lastStatus != status)  {
                System.out.printf("Status changed. Current: "+ status);
                if(status!=0){
                    tnb.alertAlarm();
                }
                //tnb.sendStatusNotificationToAllUsers(status);
                lastStatus = status;
            }
            if(lastPassword != password)  {
                if(password==456){
                    mqttClient.turnOffAlarm();
                    mqttClient.resetPassword();
                }else{
                    //
                }
                //tnb.sendStatusNotificationToAllUsers(status);
                lastStatus = status;
            }
            Thread.sleep(1000l);
        }

    }

}
