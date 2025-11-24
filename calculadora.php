<?php
/**
 * REST API - Calculadora Distribuida
 * AT03 - Sistemas Distribuidos
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

$method = $_SERVER['REQUEST_METHOD'];
$path = isset($_SERVER['PATH_INFO']) ? $_SERVER['PATH_INFO'] : '/';
$recurso = trim($path, '/');

$input = null;
if ($method === 'POST') {
    $json = file_get_contents('php://input');
    $input = json_decode($json, true);
}

try {
    switch ($recurso) {
        case 'soma':
            if ($method === 'POST') {
                echo soma($input);
            } else {
                erro(405, "Metodo nao permitido. Use POST");
            }
            break;
            
        case 'subtracao':
            if ($method === 'POST') {
                echo subtracao($input);
            } else {
                erro(405, "Metodo nao permitido. Use POST");
            }
            break;
            
        case 'multiplicacao':
            if ($method === 'POST') {
                echo multiplicacao($input);
            } else {
                erro(405, "Metodo nao permitido. Use POST");
            }
            break;
            
        case 'divisao':
            if ($method === 'POST') {
                echo divisao($input);
            } else {
                erro(405, "Metodo nao permitido. Use POST");
            }
            break;
            
        case 'expressao':
            if ($method === 'POST') {
                echo calcularExpressao($input);
            } else {
                erro(405, "Metodo nao permitido. Use POST");
            }
            break;
            
        case '':
        case 'info':
            if ($method === 'GET') {
                echo info();
            } else {
                erro(405, "Metodo nao permitido. Use GET");
            }
            break;
            
        default:
            erro(404, "Recurso nao encontrado: $recurso");
    }
    
} catch (Exception $e) {
    erro(500, "Erro interno: " . $e->getMessage());
}

function info() {
    return json_encode([
        'api' => 'Calculadora REST',
        'versao' => '1.0',
        'endpoints' => [
            'POST /soma',
            'POST /subtracao',
            'POST /multiplicacao',
            'POST /divisao',
            'POST /expressao',
            'GET /info'
        ]
    ], JSON_PRETTY_PRINT);
}

function soma($input) {
    validarInput($input, ['a', 'b']);
    $a = floatval($input['a']);
    $b = floatval($input['b']);
    
    return json_encode([
        'resultado' => $a + $b,
        'operacao' => 'soma',
        'expressao' => "$a + $b = " . ($a + $b)
    ]);
}

function subtracao($input) {
    validarInput($input, ['a', 'b']);
    $a = floatval($input['a']);
    $b = floatval($input['b']);
    
    return json_encode([
        'resultado' => $a - $b,
        'operacao' => 'subtracao',
        'expressao' => "$a - $b = " . ($a - $b)
    ]);
}

function multiplicacao($input) {
    validarInput($input, ['a', 'b']);
    $a = floatval($input['a']);
    $b = floatval($input['b']);
    
    return json_encode([
        'resultado' => $a * $b,
        'operacao' => 'multiplicacao',
        'expressao' => "$a * $b = " . ($a * $b)
    ]);
}

function divisao($input) {
    validarInput($input, ['a', 'b']);
    $a = floatval($input['a']);
    $b = floatval($input['b']);
    
    if ($b == 0) {
        erro(400, "Divisao por zero nao permitida");
    }
    
    return json_encode([
        'resultado' => $a / $b,
        'operacao' => 'divisao',
        'expressao' => "$a / $b = " . ($a / $b)
    ]);
}

function calcularExpressao($input) {
    validarInput($input, ['expressao']);
    
    $expressao = $input['expressao'];
    $expressao_limpa = preg_replace('/[^0-9+\-*\/().\s]/', '', $expressao);
    
    if (empty($expressao_limpa)) {
        erro(400, "Expressao invalida");
    }
    
    try {
        $resultado = eval("return $expressao_limpa;");
        
        return json_encode([
            'resultado' => $resultado,
            'operacao' => 'expressao',
            'expressao_original' => $expressao,
            'expressao_calculada' => "$expressao_limpa = $resultado"
        ]);
        
    } catch (Exception $e) {
        erro(400, "Erro ao calcular: " . $e->getMessage());
    }
}

function validarInput($input, $campos_obrigatorios) {
    if (!$input) {
        erro(400, "Body JSON vazio");
    }
    
    foreach ($campos_obrigatorios as $campo) {
        if (!isset($input[$campo])) {
            erro(400, "Campo obrigatorio ausente: $campo");
        }
    }
}

function erro($codigo, $mensagem) {
    http_response_code($codigo);
    echo json_encode([
        'erro' => true,
        'codigo' => $codigo,
        'mensagem' => $mensagem
    ]);
    exit;
}
?>
