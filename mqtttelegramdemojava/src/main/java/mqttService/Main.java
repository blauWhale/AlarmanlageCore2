package mqttService;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    //status 0 = abwesend & status 1 = anwesend
    public static int status = 0;
    public static int motion = 0;
    public static int numpad = 0;
    public static int alarm = 0;
    private static Logger logger;
    private static Properties config;
    public static Integer masterPassword=1234;
    public static ArrayList<Integer> passwords = new ArrayList<>();


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
        passwords.add(masterPassword);
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
            mqttClient.publish("alarmanlage/masterPassword","1234");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttClient.addHandler((s, mqttMessage) -> {
            if(s.equals("alarmanlage/status")) {
                status = Integer.parseInt(mqttMessage.toString());
            }

            if(s.equals("alarmanlage/motion")) {
                motion = Integer.parseInt(mqttMessage.toString());
            }

//            if(s.equals("alarmanlage/masterPassword")) {
//                masterPassword = Integer.parseInt(mqttMessage.toString());
//            }
            if(s.equals("alarmanlage/numpad")) {
                numpad = Integer.parseInt(mqttMessage.toString());
            }

            if(s.equals("alarmanlage/alarm")) {
                alarm = Integer.parseInt(mqttMessage.toString());
            }
        });


        int lastStatus = status;

        while(true) {

            if(lastStatus != status)  {
                if(motion!=0 && status==0){
                    tnb.alertAlarm();
                }
                //tnb.sendStatusNotificationToAllUsers(status);
                lastStatus = status;
            }
            int password = Integer.parseInt(String.valueOf(numpad));
            if(passwords.contains(password))  {
                System.out.println(password);
                System.out.println(masterPassword);
                mqttClient.turnOffAlarm();
                mqttClient.resetPassword();
                //tnb.sendStatusNotificationToAllUsers(status);
                lastStatus = status;
            }
            if(alarm==1){
                tnb.alertAlarm();
            }

            Thread.sleep(1000l);
        }

    }

}
