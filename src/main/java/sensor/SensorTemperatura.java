package sensor;

import org.eclipse.paho.client.mqttv3.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Sensor de Temperatura - Caldeira Industrial
 * Publica temperaturas a cada 60 segundos
 */
public class SensorTemperatura {
    
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final double TEMP_MIN = 180.0;
    private static final double TEMP_MAX = 220.0;
    private static final int INTERVALO_MS = 60000; // 60 segundos
    
    private static String CLIENT_ID;
    private static String TOPICO;
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java sensor.SensorTemperatura <numero>");
            System.err.println("Exemplo: java sensor.SensorTemperatura 1");
            System.exit(1);
        }
        
        String numero = args[0];
        CLIENT_ID = "SensorTemp" + numero;
        TOPICO = "temp/sensor" + numero;
        
        executarSensor();
    }
    
    private static void executarSensor() {
        try {
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            
            System.out.println("════════════════════════════════════════════");
            System.out.println("  SENSOR DE TEMPERATURA - CALDEIRA");
            System.out.println("════════════════════════════════════════════");
            System.out.println("\nConectando ao broker...");
            client.connect(options);
            System.out.println("✓ CONECTADO!");
            System.out.println("✓ Cliente: " + CLIENT_ID);
            System.out.println("✓ Tópico: " + TOPICO);
            System.out.println("✓ Intervalo: 60 segundos\n");
            System.out.println("────────────────────────────────────────────");
            
            int contador = 0;
            
            while (true) {
                contador++;
                
                double temp = TEMP_MIN + (TEMP_MAX - TEMP_MIN) * random.nextDouble();
                temp = Math.round(temp * 100.0) / 100.0;
                
                MqttMessage msg = new MqttMessage();
                msg.setPayload(String.valueOf(temp).getBytes());
                msg.setQos(1);
                
                client.publish(TOPICO, msg);
                
                String hora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                System.out.printf("[%s] Leitura #%d: %.2f°C\n", hora, contador, temp);
                
                Thread.sleep(INTERVALO_MS);
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
