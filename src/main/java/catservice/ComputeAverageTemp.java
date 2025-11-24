package catservice;

import org.eclipse.paho.client.mqttv3.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Serviço CAT - Compute Average Temperature
 * Calcula média dos últimos 120 segundos
 */
public class ComputeAverageTemp {
    
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String TOPICO_TEMP = "temp/#";
    private static final String TOPICO_SPIKE = "alerts/temp_spike";
    private static final String TOPICO_HIGH = "alerts/high_temp";
    
    private static final int JANELA_TEMPO_MS = 120000;  // 120 segundos
    private static final double DIFERENCA_SPIKE = 5.0;   // 5°C
    private static final double LIMITE_TEMP = 200.0;     // 200°C
    
    private static List<Leitura> leituras = new ArrayList<>();
    private static double mediaAnterior = 0;
    private static MqttClient clientePub;
    
    public static void main(String[] args) {
        try {
            MqttClient clienteSub = new MqttClient(BROKER_URL, "CAT_Sub");
            clientePub = new MqttClient(BROKER_URL, "CAT_Pub");
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            
            System.out.println("════════════════════════════════════════════");
            System.out.println("  SERVIÇO CAT - COMPUTE AVERAGE TEMPERATURE");
            System.out.println("════════════════════════════════════════════");
            System.out.println("\nConectando...");
            clienteSub.connect(options);
            clientePub.connect(options);
            System.out.println("✓ CONECTADO!");
            System.out.println("✓ Monitorando: " + TOPICO_TEMP);
            System.out.println("✓ Janela: 120 segundos");
            System.out.println("✓ Limite: " + LIMITE_TEMP + "°C\n");
            System.out.println("────────────────────────────────────────────");
            
            clienteSub.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {
                    System.err.println("Conexão perdida!");
                }
                
                public void deliveryComplete(IMqttDeliveryToken token) {}
                
                public void messageArrived(String topic, MqttMessage message) {
                    try {
                        String payload = new String(message.getPayload());
                        double temp = Double.parseDouble(payload);
                        
                        long agora = System.currentTimeMillis();
                        leituras.add(new Leitura(agora, temp));
                        
                        removerAntigas(agora);
                        double media = calcularMedia();
                        
                        String hora = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        System.out.printf("[%s] %s: %.2f°C | Média: %.2f°C (%d leituras)\n",
                            hora, topic, temp, media, leituras.size());
                        
                        verificarAlertas(media);
                        mediaAnterior = media;
                        
                    } catch (Exception e) {
                        System.err.println("Erro: " + e.getMessage());
                    }
                }
            });
            
            clienteSub.subscribe(TOPICO_TEMP, 1);
            
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void removerAntigas(long tempoAtual) {
        leituras.removeIf(l -> (tempoAtual - l.timestamp) > JANELA_TEMPO_MS);
    }
    
    private static double calcularMedia() {
        if (leituras.isEmpty()) return 0;
        double soma = 0;
        for (Leitura l : leituras) soma += l.temperatura;
        return Math.round((soma / leituras.size()) * 100.0) / 100.0;
    }
    
    private static void verificarAlertas(double mediaAtual) {
        try {
            if (mediaAtual > LIMITE_TEMP) {
                String alerta = String.format("TEMPERATURA ALTA! Média: %.2f°C", mediaAtual);
                publicar(TOPICO_HIGH, alerta);
                System.out.println("  🔥 >>> " + alerta);
            }
            
            if (mediaAnterior > 0) {
                double diferenca = Math.abs(mediaAtual - mediaAnterior);
                if (diferenca > DIFERENCA_SPIKE) {
                    String alerta = String.format("AUMENTO REPENTINO! %.2f°C → %.2f°C", 
                        mediaAnterior, mediaAtual);
                    publicar(TOPICO_SPIKE, alerta);
                    System.out.println("  ⚠️  >>> " + alerta);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar alertas: " + e.getMessage());
        }
    }
    
    private static void publicar(String topico, String texto) {
        try {
            MqttMessage msg = new MqttMessage(texto.getBytes());
            msg.setQos(1);
            clientePub.publish(topico, msg);
        } catch (MqttException e) {
            System.err.println("Erro ao publicar: " + e.getMessage());
        }
    }
    
    static class Leitura {
        long timestamp;
        double temperatura;
        
        Leitura(long timestamp, double temperatura) {
            this.timestamp = timestamp;
            this.temperatura = temperatura;
        }
    }
}
