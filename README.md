# Floristería Bloom — API REST (Entrega final)

Sistema para gestionar los **clientes**, **flores**, **pedidos** y **pagos** de la Floristería Bloom (Medellín).
Proyecto de Programación, ITM.

**Integrantes:** Ana Sofía Bolívar Celada · Mariana Buriticá Ospina

## Tecnologías

- Java 21 · Spring Boot · Gradle
- Arquitectura de n capas: `controladores` → `services` → `repositorios`
- SQL Server
- **JDBC** con SQL a mano (Cliente y Flor) y **JPA / Hibernate** con Spring Data JPA (Pedido, Arreglo, Tarjeta y Pago)
- Swagger (springdoc) para documentar la API
- JUnit 5 y Mockito para las pruebas
- Lombok

## Cómo ejecutarlo

1. Tener SQL Server con TCP/IP activado y el modo de autenticación mixto.
2. Ejecutar `sql/bloom_bd.sql` en SSMS (crea la base `bloom`, las tablas, incluida `tarjeta`, y el usuario `bloom_app`).
3. Si tu SQL Server **no** usa el puerto 1433, crea `src/main/resources/application-local.yaml`:

```yaml
bloom:
  db:
    url: "jdbc:sqlserver://localhost:TU_PUERTO;databaseName=bloom;encrypt=true;trustServerCertificate=true"
```

4. Ejecutar:

```bash
./gradlew bootRun
```

La API queda en `http://localhost:8080/bloom`.

## Persistencia: SQL (JDBC) y JPA

| Módulo | Tecnología | Detalle |
|---|---|---|
| Cliente, Flor | JDBC (SQL a mano) | Conexiones cerradas con try-with-resources, columnas explícitas, sin `SELECT *` |
| Pedido, Arreglo, Tarjeta, Pago | JPA / Hibernate | Entidades con `@Entity` y repositorios que extienden `JpaRepository` |

### Relaciones JPA

- **OneToMany / ManyToOne:** un pedido tiene muchos arreglos (`Pedido.arreglos` ↔ `Arreglo.pedido`).
- **OneToOne:** un pedido tiene como máximo una tarjeta (`Pedido.tarjeta` ↔ `Tarjeta.pedido`).
- Las relaciones son `LAZY`; los repositorios usan `@EntityGraph` para traer arreglos y tarjeta cuando se necesitan.
- `@JsonIgnore` en el lado dueño de cada relación evita el bucle infinito en el JSON.
- `Pedido` guarda `idCliente` como número, sin relación JPA, porque Cliente sigue en JDBC.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/clientes/listar` | Lista los clientes activos |
| GET | `/clientes/consultar/{id}` | Consulta un cliente |
| POST | `/clientes/nuevo` | Crea un cliente |
| PUT | `/clientes/actualizar` | Actualiza un cliente |
| DELETE | `/clientes/eliminar/{id}` | Borrado lógico (lo deja inactivo) |
| POST | `/pedidos/nuevo` | Registra un pedido con sus arreglos y tarjeta opcional (transacción) |
| GET | `/pedidos/listar?estado=&idCliente=` | Lista pedidos con filtros opcionales |
| GET | `/pedidos/consultar/{id}` | Consulta un pedido con sus arreglos y su tarjeta |
| PUT | `/pedidos/actualizar` | Edita un pedido (solo si está REGISTRADO) |
| PATCH | `/pedidos/estado?id=&estado=` | Cambia el estado del pedido con reglas |
| PUT | `/pedidos/tarjeta/{id}` | Crea o reemplaza la tarjeta del pedido |
| GET | `/flores/listar` | Flores activas |
| GET | `/flores/consultar/{id}` | Una flor |
| POST | `/flores/nueva` | Crea flor |
| PUT | `/flores/actualizar` | Edita flor |
| DELETE | `/flores/eliminar/{id}` | Borrado lógico |
| PATCH | `/flores/stock?id=&cantidad=` | Suma o resta stock |
| POST | `/pagos/nuevo` | Registra un abono |
| GET | `/pagos/pedido/{idPedido}` | Pagos de un pedido |
| GET | `/pagos/saldo/{idPedido}` | Total, pagado y saldo |

## Reglas de negocio de Pedido

- El cliente debe existir y estar activo.
- La fecha de entrega debe ser futura y el pedido debe tener al menos un arreglo.
- El valor total lo calcula el sistema (suma de los arreglos).
- El pedido, sus arreglos y su tarjeta se guardan en una sola **transacción** (`@Transactional`): todo o nada.
- Flujo de estados: `REGISTRADO → EN_ELABORACION → LISTO → ENTREGADO`. Se puede `CANCELADO` antes de entregar.
  Un pedido entregado o cancelado ya no cambia. Al entregar se registra la fecha y hora reales.

## Tarjeta del pedido

Es una dedicatoria opcional (mensaje y remitente). Cada pedido tiene como máximo una.
El mensaje es obligatorio y puede tener hasta 200 caracteres. No se puede cambiar si el pedido está ENTREGADO o CANCELADO.

## Manejo de errores

Todas las respuestas de error tienen el formato `{"error": "mensaje"}`. Los errores técnicos se registran en el log y al cliente solo le llega un mensaje genérico.

## Estrategia de ramas

`main` (versión estable) · `develop` (integración) · `feature/...` (una por funcionalidad, se une por Pull Request).

## Documentación Swagger

Con la app corriendo: http://localhost:8080/bloom/swagger-ui.html

## Arquitectura y pruebas

Los controllers y services dependen de interfaces (IClienteService, IPedidoService, etc.) y se usa inyección por constructor.

Hay pruebas unitarias de todos los métodos de los services, con mocks (Mockito), en `src/test`. No tocan la base de datos real:

```bash
./gradlew test
```