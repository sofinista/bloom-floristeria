# Floristería Bloom — API REST (Entrega 1)

Sistema para gestionar los **clientes** y **pedidos** de la Floristería Bloom (Medellín).
Proyecto de Programación, ITM.

**Integrantes:** Ana Sofía Bolívar Celada · Mariana Buriticá Ospina

## Tecnologías

- Java 21 · Spring Boot · Gradle
- Arquitectura de n capas: `controladores` → `services` → `repositorios`
- SQL Server con **JDBC** y consultas SQL (sin ORM)
- Lombok

## Cómo ejecutarlo

1. Tener SQL Server con TCP/IP activado y el modo de autenticación mixto.
2. Ejecutar `sql/bloom_bd.sql` en SSMS (crea la base `bloom`, las tablas y el usuario `bloom_app`).
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

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/clientes/listar` | Lista los clientes activos |
| GET | `/clientes/consultar/{id}` | Consulta un cliente |
| POST | `/clientes/nuevo` | Crea un cliente |
| PUT | `/clientes/actualizar` | Actualiza un cliente |
| DELETE | `/clientes/eliminar/{id}` | Borrado lógico (lo deja inactivo) |
| POST | `/pedidos/nuevo` | Registra un pedido con sus arreglos (transacción) |
| GET | `/pedidos/listar?estado=&idCliente=` | Lista pedidos con filtros opcionales |
| GET | `/pedidos/consultar/{id}` | Consulta un pedido con sus arreglos |
| PUT | `/pedidos/actualizar` | Edita un pedido (solo si está REGISTRADO) |
| PATCH | `/pedidos/estado?id=&estado=` | Cambia el estado del pedido con reglas |

## Reglas de negocio de Pedido

- El cliente debe existir y estar activo.
- La fecha de entrega debe ser futura y el pedido debe tener al menos un arreglo.
- El valor total lo calcula el sistema (suma de los arreglos).
- El pedido y sus arreglos se guardan en una sola **transacción** (todo o nada).
- Flujo de estados: `REGISTRADO → EN_ELABORACION → LISTO → ENTREGADO`. Se puede `CANCELADO` antes de entregar.
  Un pedido entregado o cancelado ya no cambia. Al entregar se registra la fecha y hora reales.

## Estrategia de ramas

`main` (versión estable) · `develop` (integración) · `feature/...` (una por funcionalidad, se une por Pull Request).