# Order Service

## Descrição
Microsserviço **orquestrador** responsável pela criação e gerenciamento de pedidos. Coordena a comunicação entre Product Service, Inventory Service e Payment Service para processar pedidos de forma completa.

**Fluxo de criação de pedido:**
1. Consulta produtos no Product Service
2. Verifica disponibilidade no Inventory Service  
3. Cria pedido com status CRIADO
4. Solicita pagamento ao Payment Service
5. Atualiza status para PAGO ou CANCELADO
6. Se aprovado, dá baixa no estoque via Inventory Service

## Pré-requisitos
- JDK 21
- Maven 3.8+
- MongoDB rodando na porta 27017
- **Todos os outros serviços devem estar rodando** (Product, Inventory, Payment)

## Como Executar

1. Navegue até o diretório do serviço:
```bash
cd order-service
```

2. Execute o serviço:
```bash
mvn spring-boot:run
```

**Importante:** Certifique-se que os serviços nas seguintes portas estão ativos:
- Product Service: 8081
- Inventory Service: 8083
- Payment Service: 8084

## Porta Utilizada
**8085**

## Endpoints

- `POST /order` - Criar novo pedido (orquestra todo o fluxo)
- `GET /order/{id}` - Consultar pedido por ID

## Banco de Dados
- Database: `order_db`
- Collection: `orders`
