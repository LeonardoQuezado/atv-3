package alarmservice;

import org.eclipse.paho.client.mqttv3.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço de Alarmes
 * Monitora e exibe alertas
 */
public class AlarmService {
    
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String TOPICO_ALERTAS = "alerts/#";
    private static int totalAlertas = 0;
    
    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER_URL, "AlarmService");
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            
            System.out.println("════════════════════════════════════════════");
            System.out.println("  SERVIÇO DE ALARMES");
            System.out.println("════════════════════════════════════════════");
            System.out.println("\nConectando...");
            client.connect(options);
            System.out.println("✓ CONECTADO!");
            System.out.println("✓ Monitorando: " + TOPICO_ALERTAS + "\n");
            System.out.println("════════════════════════════════════════════");
            System.out.println("Aguardando alertas...\n");
            
            client.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {
                    System.err.println("\n[!] Conexão perdida!");
                }
                
                public void deliveryComplete(IMqttDeliveryToken token) {}
                
                public void messageArrived(String topic, MqttMessage message) {
                    totalAlertas++;
                    
                    String hora = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    String alerta = new String(message.getPayload());
                    
                    String tipo, emoji;
                    if (topic.contains("temp_spike")) {
                        tipo = "AUMENTO REPENTINO";
                        emoji = "⚠️ ";
                    } else if (topic.contains("high_temp")) {
                        tipo = "TEMPERATURA ALTA";
                        emoji = "🔥";
                    } else {
                        tipo = "ALERTA";
                        emoji = "❗";
                    }
                    
                    System.out.println("\n╔════════════════════════════════════════════╗");
                    System.out.println("║ " + emoji + " ALERTA #" + totalAlertas + " - " + tipo);
                    System.out.println("╠════════════════════════════════════════════╣");
                    System.out.println("║ Hora  : " + hora);
                    System.out.println("║ Tópico: " + topic);
                    System.out.println("║ " + alerta);
                    System.out.println("╚════════════════════════════════════════════╝\n");
                }
            });
            
            client.subscribe(TOPICO_ALERTAS, 1);
            
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
