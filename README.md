AT03 - Sistema de Monitoramento Industrial com MQTT e Calculadora REST
Disciplina: CKP7500 - Sistemas Distribuídos e Redes de Comunicação
Instituição: Universidade Federal do Ceará (UFC)
Aluno: Leonardo Quezado de Meneses
Matrícula: 584270
Período: 2025.1

Descrição do Projeto
Este projeto implementa dois paradigmas de comunicação em sistemas distribuídos:

Sistema MQTT: Monitoramento industrial baseado no paradigma Publish-Subscribe utilizando o protocolo MQTT. O sistema simula o monitoramento de uma caldeira industrial através de sensores de temperatura que publicam leituras a cada 60 segundos. Um serviço de processamento (CAT - Compute Average Temperature) calcula médias móveis e dispara alertas quando condições críticas são detectadas. Um serviço de alarmes monitora e exibe notificações em tempo real.
Sistema REST/HTTP: Calculadora distribuída com operações matemáticas remotas, incluindo política de retry para tolerância a falhas.


📹 Demonstrações em Vídeo

Sistema MQTT: [INSERIR LINK DO YOUTUBE/DRIVE]


📊 Documentação

Relatório Técnico Completo: docs/RELATORIO_AT03.pdf
Capturas Wireshark: docs/wireshark/

mqtt_publish.png - Análise de tráfego MQTT
http_post.png - Análise de tráfego HTTP




Arquitetura do Sistema MQTT
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
Arquitetura do Sistema REST/HTTP
┌──────────────────────┐                    ┌──────────────────────┐
│  Cliente REST        │   POST /soma       │  Servidor PHP        │
│  (Java)              │   {a: 10, b: 15}   │  (REST API)          │
│                      │  ─────────────►    │                      │
│  ┌────────────────┐  │                    │  ┌────────────────┐  │
│  │  Política de   │  │                    │  │  Validação     │  │
│  │  RETRY         │  │                    │  │  JSON          │  │
│  │  (3x, 2s)      │  │  JSON Response     │  └────────────────┘  │
│  └────────────────┘  │  ◄─────────────    │                      │
│                      │  {resultado: 25}   │                      │
└──────────────────────┘                    └──────────────────────┘

Componentes MQTT
1. Sensores de Temperatura (sensor/SensorTemperatura.java)
Sensores simulados que publicam temperaturas aleatórias entre 180°C e 220°C.
Características:

Intervalo de publicação: 60 segundos (conforme especificação)
Tópico MQTT: temp/sensor{N} onde N é o identificador do sensor
QoS: 1 (entrega garantida pelo menos uma vez)
Implementação: Java + Eclipse Paho MQTT

Executar:
bashmvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="1"
2. Sensor Real (Smartphone)
Sensor físico implementado através de dispositivo móvel utilizando o aplicativo IoT MQTT Panel.
Configuração:

Broker: 192.168.0.14:1883
Tópico: temp/sensor_real
Tipo: Text Publisher
QoS: 1

3. Serviço CAT - Compute Average Temperature (catservice/ComputeAverageTemp.java)
Serviço responsável por processar leituras de temperatura e disparar alertas.
Funcionalidades:

Subscreve em temp/# (wildcard para todos os sensores)
Calcula média móvel dos últimos 120 segundos
Detecta e publica dois tipos de alertas:

Temperatura Alta: média superior a 200°C (publica em alerts/high_temp)
Aumento Repentino: diferença entre médias consecutivas superior a 5°C (publica em alerts/temp_spike)



Algoritmo de Janela Deslizante:
java// Remove leituras antigas (fora da janela de 120s)
leituras.removeIf(l -> (tempoAtual - l.timestamp) > JANELA_TEMPO_MS);

// Calcula média aritmética
double soma = 0;
for (Leitura l : leituras) soma += l.temperatura;
double media = soma / leituras.size();
Executar:
bashmvn exec:java -Dexec.mainClass="catservice.ComputeAverageTemp"
4. Serviço de Alarmes (alarmservice/AlarmService.java)
Monitora tópicos de alertas e exibe notificações formatadas.
Características:

Subscreve em alerts/# (todos os alertas)
Identifica tipo de alerta pelo tópico
Exibe informações formatadas: timestamp, tipo, detalhes

Executar:
bashmvn exec:java -Dexec.mainClass="alarmservice.AlarmService"

Componentes REST/HTTP
1. Cliente REST (http/ClienteREST.java)
Cliente Java com política de retry para tolerância a falhas.
Política de Retry:

3 tentativas máximas
2 segundos de delay entre tentativas
Retry apenas em erros 5xx (servidor)
Abort em erros 4xx (cliente)

Executar:
bashmvn exec:java -Dexec.mainClass="http.ClienteREST"
2. Servidor REST (calculadora.php)
API REST em PHP para operações matemáticas.
Endpoints:

POST /soma, /subtracao, /multiplicacao, /divisao
POST /expressao (avaliação de expressões completas)
GET /info

Executar:
bashphp -S localhost:8000

Protocolo MQTT
Tópicos Utilizados
TópicoTipoDescriçãotemp/sensor1PublishTemperaturas do sensor 1temp/sensor2PublishTemperaturas do sensor 2temp/sensor_realPublishTemperaturas do sensor real (smartphone)alerts/high_tempPublishAlertas de temperatura altaalerts/temp_spikePublishAlertas de aumento repentino
Quality of Service (QoS)
Todos os componentes utilizam QoS 1 (entrega garantida pelo menos uma vez), garantindo que:

Mensagens não sejam perdidas em caso de falhas temporárias
Broker confirme recebimento antes de descartar mensagem
Equilíbrio entre confiabilidade e performance


Instalação e Configuração
1. Instalar Mosquitto
Ubuntu/Debian:
bashsudo apt update
sudo apt install mosquitto mosquitto-clients
sudo systemctl start mosquitto
sudo systemctl enable mosquitto
2. Configurar Mosquitto para Conexões Externas
Editar arquivo de configuração:
bashsudo nano /etc/mosquitto/mosquitto.conf
```

Adicionar ao final:
```
listener 1883
allow_anonymous true
Reiniciar serviço:
bashsudo systemctl restart mosquitto
3. Liberar Firewall (se aplicável)
bashsudo ufw allow 1883/tcp
4. Instalar PHP
bashsudo apt install php php-cli
5. Clonar Repositório e Compilar
bashgit clone [URL_DO_REPOSITORIO]
cd AT03-SD
mvn clean install

Execução do Sistema
Ordem de Inicialização - Sistema MQTT
Terminal 1 - Serviço de Alarmes:
bashmvn exec:java -Dexec.mainClass="alarmservice.AlarmService"
Terminal 2 - Serviço CAT:
bashmvn exec:java -Dexec.mainClass="catservice.ComputeAverageTemp"
Terminal 3 - Sensor 1:
bashmvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="1"
Terminal 4 - Sensor 2:
bashmvn exec:java -Dexec.mainClass="sensor.SensorTemperatura" -Dexec.args="2"
Smartphone - Sensor Real:

Abrir IoT MQTT Panel
Conectar ao broker (IP do PC, porta 1883)
Publicar temperaturas manualmente no tópico temp/sensor_real

Sistema REST/HTTP
Terminal 1 - Servidor:
bashphp -S localhost:8000
Terminal 2 - Cliente:
bashmvn exec:java -Dexec.mainClass="http.ClienteREST"
```

---

## Análise com Wireshark

Capturas de tráfego de rede para análise de overhead dos protocolos:

- **MQTT:** `docs/wireshark/mqtt_publish.png` (~89 bytes)
- **HTTP:** `docs/wireshark/http_post.png` (~463 bytes)

Análise detalhada disponível no relatório técnico.

---

## Estrutura do Projeto
```
AT03-SD/
├── src/main/java/
│   ├── sensor/
│   │   └── SensorTemperatura.java
│   ├── catservice/
│   │   └── ComputeAverageTemp.java
│   ├── alarmservice/
│   │   └── AlarmService.java
│   └── http/
│       └── ClienteREST.java
├── calculadora.php
├── pom.xml
├── README.md
└── docs/
    ├── RELATORIO_AT03.pdf
    └── wireshark/
        ├── mqtt_publish.png
        └── http_post.png
