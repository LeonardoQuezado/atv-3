package http;

import java.net.*;
import java.io.*;
import org.json.*;

/**
 * Cliente REST - Calculadora Distribuída
 * Implementa política de RETRY conforme requisito
 */
public class ClienteREST {
    
    private static final String BASE_URL = "http://localhost:8000/calculadora.php";
    private static final int MAX_TENTATIVAS = 3;
    private static final int DELAY_MS = 2000;
    
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("  CLIENTE REST - CALCULADORA DISTRIBUIDA");
        System.out.println("===============================================\n");
        
        testarOperacoes();
    }
    
    private static void testarOperacoes() {
        System.out.println("1. SOMA: 10 + 15");
        System.out.println(soma(10, 15) + "\n");
        
        System.out.println("2. SUBTRACAO: 20 - 5");
        System.out.println(subtracao(20, 5) + "\n");
        
        System.out.println("3. MULTIPLICACAO: 4 * 5");
        System.out.println(multiplicacao(4, 5) + "\n");
        
        System.out.println("4. DIVISAO: 10 / 2");
        System.out.println(divisao(10, 2) + "\n");
        
        System.out.println("5. EXPRESSAO: (10 + 15) * 2");
        System.out.println(expressao("(10+15)*2") + "\n");
        
        System.out.println("6. TESTE ERRO: 10 / 0");
        System.out.println(divisao(10, 0) + "\n");
    }
    
    public static String soma(double a, double b) {
        JSONObject json = new JSONObject();
        json.put("a", a);
        json.put("b", b);
        return requisicaoREST("POST", "/soma", json.toString());
    }
    
    public static String subtracao(double a, double b) {
        JSONObject json = new JSONObject();
        json.put("a", a);
        json.put("b", b);
        return requisicaoREST("POST", "/subtracao", json.toString());
    }
    
    public static String multiplicacao(double a, double b) {
        JSONObject json = new JSONObject();
        json.put("a", a);
        json.put("b", b);
        return requisicaoREST("POST", "/multiplicacao", json.toString());
    }
    
    public static String divisao(double a, double b) {
        JSONObject json = new JSONObject();
        json.put("a", a);
        json.put("b", b);
        return requisicaoREST("POST", "/divisao", json.toString());
    }
    
    public static String expressao(String expr) {
        JSONObject json = new JSONObject();
        json.put("expressao", expr);
        return requisicaoREST("POST", "/expressao", json.toString());
    }
    
    /**
     * Faz requisicao REST com politica de RETRY
     * ESTE E O REQUISITO PRINCIPAL (1 ponto)
     */
    private static String requisicaoREST(String metodo, String recurso, String jsonBody) {
        
        // Loop de tentativas
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                System.out.println("  -> Tentativa " + tentativa + "/" + MAX_TENTATIVAS);
                
                // Cria URL
                URL url = new URL(BASE_URL + recurso);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                // Configura requisicao
                conn.setRequestMethod(metodo);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                // Envia JSON
                if (jsonBody != null && !jsonBody.isEmpty()) {
                    OutputStream os = conn.getOutputStream();
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.close();
                }
                
                // Le resposta
                int codigoResposta = conn.getResponseCode();
                
                InputStream is;
                if (codigoResposta >= 200 && codigoResposta < 300) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }
                
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
                StringBuilder resposta = new StringBuilder();
                String linha;
                while ((linha = br.readLine()) != null) {
                    resposta.append(linha.trim());
                }
                br.close();
                conn.disconnect();
                
                // Verifica sucesso
                if (codigoResposta >= 200 && codigoResposta < 300) {
                    System.out.println("  [OK] Sucesso! HTTP " + codigoResposta);
                    return resposta.toString();
                } else {
                    System.err.println("  [ERRO] HTTP " + codigoResposta);
                    
                    // Erro 4xx (cliente) - nao retry
                    if (codigoResposta >= 400 && codigoResposta < 500) {
                        System.err.println("  [!] Erro do cliente, abortando retries");
                        return resposta.toString();
                    }
                    
                    // Erro 5xx (servidor) - retry
                    throw new IOException("HTTP " + codigoResposta);
                }
                
            } catch (Exception e) {
                System.err.println("  [ERRO] " + e.getMessage());
                
                // Se nao e a ultima tentativa, aguarda e tenta novamente
                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        System.out.println("  [WAIT] Aguardando " + (DELAY_MS/1000) + "s para retry...");
                        Thread.sleep(DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.err.println("  [FAIL] Todas as tentativas falharam!");
                }
            }
        }
        
        return "{\"erro\": true, \"mensagem\": \"Falha apos " + MAX_TENTATIVAS + " tentativas\"}";
    }
}
