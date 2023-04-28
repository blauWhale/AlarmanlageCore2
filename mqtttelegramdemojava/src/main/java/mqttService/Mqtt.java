package mqttService;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mqtt implements MqttCallback {

    private final String broker;
    private final String clientId;
    private final String username;
    private final String password;
    private final int qos = 2;

    public Mqtt(String broker, String clientId) {
        this(broker, clientId, null, null);
    }

    public Mqtt(String broker, String clientId, String username, String password) {
        this.broker = broker;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.logger = Logger.getLogger("Mqtt");
    }

    private MqttClient client;

    Logger logger;
    private Thread controlThread;
    private volatile boolean isRunning;

    private ArrayList<BiConsumer<String, MqttMessage>> consumers = new ArrayList<>();


    public void sendMessage(String topic, String content) throws MqttException {
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        client.publish(topic, message);
        logger.info(topic + " = " + message);
    }

    public void subscribe(String topic) throws MqttException {
        client.subscribe(topic);
    }

    public void addHandler(BiConsumer<String, MqttMessage> consumer) {
        consumers.add(consumer);
    }

    public void start() throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        client = new MqttClient(broker, clientId, persistence);
        client.setCallback(this);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        if(username != null) {
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
        }
        connOpts.setCleanSession(true);
        logger.info("Connecting to broker: "+broker);
        client.connect(connOpts);
        logger.info("Connected");
        isRunning = true;
        controlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(isRunning) {
                        if(!client.isConnected()) {
                            client.reconnect();
                        }
                        Thread.sleep(1000);
                    }
                }catch(InterruptedException | MqttException e) {
                    logger.log(Level.SEVERE, "", e);
                }
            }
        });
        controlThread.start();

    }

    public void stop() throws MqttException {
        isRunning = false;
        client.disconnect();
        logger.info("Disconnected");
    }
    public void turnOffAlarm() throws MqttException {
        MqttMessage statusMessage = new MqttMessage("1".getBytes());
        client.publish("alarmanlage/status", statusMessage);

        MqttMessage alarmMessage = new MqttMessage("0".getBytes());
        client.publish("alarmanlage/alarm", alarmMessage);

        MqttMessage screenledMessage = new MqttMessage("off".getBytes());
        client.publish("alarmanlage/screenled", screenledMessage);

        MqttMessage ledMessage = new MqttMessage("0".getBytes());
        client.publish("alarmanlage/sideled", ledMessage);
    }

    public void armAlarm() throws MqttException {
        if(!(Main.motion==1)){ //Alarm can't be armed if motion is detected
            MqttMessage message = new MqttMessage("0".getBytes());
            client.publish("alarmanlage/status", message);
        }

    }

    public void testAlarm() throws MqttException {
            MqttMessage message = new MqttMessage("1".getBytes());
            client.publish("alarmanlage/alarm", message);
    }

    public void resetPassword() throws MqttException {
        MqttMessage message = new MqttMessage("0".getBytes());
        client.publish("alarmanlage/numpad", message);
    }


    @Override
    public void connectionLost(Throwable cause) {
        logger.log(Level.SEVERE, "", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        logger.log(Level.FINE, "Receveid: " + topic + " " + message.toString());
        for(BiConsumer<String, MqttMessage> consumer: consumers) {
            consumer.accept(topic, message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void publish(String topic, String message) throws MqttException {
        client.publish(topic, new MqttMessage(message.getBytes(StandardCharsets.UTF_8)));
    }
}
