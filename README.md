# AT03 - Sistema de Monitoramento Industrial com MQTT

**Disciplina:** CKP7500 - Sistemas Distribuídos e Redes de Comunicação  
**Instituição:** Universidade Federal do Ceará (UFC)  
**Aluno:** Leonardo Quezado de Meneses    
**Matrícula:** 584270
**Período:** 2025.1

---

## Descrição do Projeto

Este projeto implementa um sistema de monitoramento industrial baseado no paradigma Publish-Subscribe utilizando o protocolo MQTT.
O sistema simula o monitoramento de uma caldeira industrial através de sensores de temperatura que publicam leituras a cada 60 segundos.
Um serviço de processamento (CAT - Compute Average Temperature) calcula médias móveis e dispara alertas quando condições críticas são detectadas. 
Um serviço de alarmes monitora e exibe notificações em tempo real.

---

## Arquitetura do Sistema
```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Sensor 1    │  │  Sensor 2    │  │  Sensor N    │  │ Sensor Real  │
│  (Java)      │  │  (Java)      │  │  (Java)      │  │ (Smartphone) │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │                 │
       │            publish temp/sensor*                     │
       └─────────────────┼─────────────────┼─────────────────┘
                         ↓                 ↓
                ┌─────────────────────────────────┐
                │   Broker MQTT (Mosquitto)       │
                │   Porta: 1883                   │
                └──────────┬──────────────────────┘
                           │ subscribe
                  ┌────────┴────────┐
                  ↓                 ↓
         ┌─────────────────┐  ┌──────────────────┐
         │  Serviço CAT    │  │  Serviço Alarms  │
         │  (Calcula       │  │  (Exibe          │
         │   médias e      │  │   notificações)  │
         │   publica       │  │                  │
         │   alertas)      │  │                  │
         └─────────────────┘  └──────────────────┘
```

---

## Componentes

### 1. Sensores de Temperatura (sensor/SensorTemperatura.java)

Sensores simulados que publicam temperaturas aleatórias entre 180°C e 220°C.

**Características:**
- Intervalo de publicação: 60 segundos (conforme especificação)
- Tópico MQTT: `temp/sensor{N}` onde N é o identificador do sensor
- QoS: 1 (entrega garantida pelo menos uma vez)
- Implementação: Java + Eclipse Paho MQTT

**Executar:**
```bash
mvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="1"
```

### 2. Sensor Real (Smartphone)

Sensor físico implementado através de dispositivo móvel utilizando o aplicativo IoT MQTT Panel.

**Configuração:**
- Broker: 192.168.0.14:1883
- Tópico: `temp/sensor_real`
- Tipo: Text Publisher
- QoS: 1

### 3. Serviço CAT - Compute Average Temperature (catservice/ComputeAverageTemp.java)

Serviço responsável por processar leituras de temperatura e disparar alertas.

**Funcionalidades:**
- Subscreve em `temp/#` (wildcard para todos os sensores)
- Calcula média móvel dos últimos 120 segundos
- Detecta e publica dois tipos de alertas:
  1. **Temperatura Alta:** média superior a 200°C (publica em `alerts/high_temp`)
  2. **Aumento Repentino:** diferença entre médias consecutivas superior a 5°C (publica em `alerts/temp_spike`)

**Algoritmo de Janela Deslizante:**
```java
// Remove leituras antigas (fora da janela de 120s)
leituras.removeIf(l -> (tempoAtual - l.timestamp) > JANELA_TEMPO_MS);

// Calcula média aritmética
double soma = 0;
for (Leitura l : leituras) soma += l.temperatura;
double media = soma / leituras.size();
```

**Executar:**
```bash
mvn exec:java -Dexec.mainClass="catservice.ComputeAverageTemp"
```

### 4. Serviço de Alarmes (alarmservice/AlarmService.java)

Monitora tópicos de alertas e exibe notificações formatadas.

**Características:**
- Subscreve em `alerts/#` (todos os alertas)
- Identifica tipo de alerta pelo tópico
- Exibe informações formatadas: timestamp, tipo, detalhes

**Executar:**
```bash
mvn exec:java -Dexec.mainClass="alarmservice.AlarmService"
```

---

## Protocolo MQTT

### Tópicos Utilizados

| Tópico | Tipo | Descrição |
|--------|------|-----------|
| `temp/sensor1` | Publish | Temperaturas do sensor 1 |
| `temp/sensor2` | Publish | Temperaturas do sensor 2 |
| `temp/sensor_real` | Publish | Temperaturas do sensor real (smartphone) |
| `alerts/high_temp` | Publish | Alertas de temperatura alta |
| `alerts/temp_spike` | Publish | Alertas de aumento repentino |

### Quality of Service (QoS)

Todos os componentes utilizam **QoS 1** (entrega garantida pelo menos uma vez), garantindo que:
- Mensagens não sejam perdidas em caso de falhas temporárias
- Broker confirme recebimento antes de descartar mensagem
- Equilíbrio entre confiabilidade e performance


## Instalação e Configuração

### 1. Instalar Mosquitto

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mosquitto mosquitto-clients
sudo systemctl start mosquitto
sudo systemctl enable mosquitto
```

### 2. Configurar Mosquitto para Conexões Externas

Editar arquivo de configuração:
```bash
sudo nano /etc/mosquitto/mosquitto.conf
```

Adicionar ao final:
```
listener 1883
allow_anonymous true
```

Reiniciar serviço:
```bash
sudo systemctl restart mosquitto
```

### 3. Liberar Firewall (se aplicável)
```bash
sudo ufw allow 1883/tcp
```

### 4. Clonar Repositório e Compilar
```bash
git clone [URL_DO_REPOSITORIO]
cd AT03-SD
mvn clean install
```

---

## Execução do Sistema

### Ordem de Inicialização

1. **Terminal 1 - Serviço de Alarmes:**
```bash
mvn exec:java -Dexec.mainClass="alarmservice.AlarmService"
```

2. **Terminal 2 - Serviço CAT:**
```bash
mvn exec:java -Dexec.mainClass="catservice.ComputeAverageTemp"
```

3. **Terminal 3 - Sensor 1:**
```bash
mvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="1"
```

4. **Terminal 4 - Sensor 2:**
```bash
mvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="2"
```

5. **Smartphone - Sensor Real:**
   - Abrir IoT MQTT Panel
   - Conectar ao broker (IP do PC, porta 1883)
   - Publicar temperaturas manualmente no tópico `temp/sensor_real`

