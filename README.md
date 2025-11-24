# AT03 - Paradigmas de Comunicação em Sistemas Distribuídos

**UFC - CKP7500** | Leonardo Quezado de Meneses | 584270 | 2025.1

## Descrição

Implementação comparativa de dois paradigmas de comunicação:
- **MQTT (Publish-Subscribe):** Monitoramento de caldeira com sensores de temperatura
- **REST/HTTP (Requisição-Resposta):** Calculadora distribuída com retry

**Documentação:** `AT03-SD/Relatorio.pdf | **Vídeo:** AT03-SD/videos | **Wireshark:** `AT03-SD/wireshark

## Executar

### Configuração Inicial
```bash
# Instalar dependências
sudo apt install mosquitto mosquitto-clients openjdk-11-jdk maven php

# Configurar Mosquitto (/etc/mosquitto/mosquitto.conf)
listener 1883
allow_anonymous true

# Reiniciar
sudo systemctl restart mosquitto

# Compilar projeto
cd AT03-SD && mvn clean install
```

### Sistema MQTT (4 terminais)
```bash
# Terminal 1: Alarmes
mvn exec:java -Dexec.mainClass="alarmservice.AlarmService"

# Terminal 2: CAT
mvn exec:java -Dexec.mainClass="catservice.ComputeAverageTemp"

# Terminal 3: Sensor 1
mvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="1"

# Terminal 4: Sensor 2
mvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="2"

# Sensor Real: IoT MQTT Panel → 192.168.0.14:1883 → temp/sensor_real
```

### Sistema REST/HTTP (2 terminais)
```bash
# Terminal 1: Servidor
php -S localhost:8000

# Terminal 2: Cliente
mvn exec:java -Dexec.mainClass="http.ClienteREST"
```

## Comparação

| Aspecto | MQTT | REST/HTTP |
|---------|------|-----------|
| Modelo | Pub-Sub (1:N) | Req-Resp (1:1) |
| Acoplamento | Desacoplado | Acoplado |
| Overhead | ~89 bytes | ~463 bytes |
