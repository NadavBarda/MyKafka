# MyKafka - Computation Graph Web Interface (AP Ex6)

This project implements a web-based visualization interface for computation graphs named **MyKafka**, built as part of the Advanced Programming course (Exercise 6). The application features a custom Java HTTP Server backend that handles dynamic content loading, configuration file uploads, and topic publication, coupled with a clean 3-panel layout.

---

## Background / רקע
The system manages a computational graph consisting of rectangular **Topics** (holding message data values) and circular **Agents** (performing mathematical, logical, or flow operations on topics). Users can upload custom configurations to dynamically construct graphs and publish/subscribe to topics to view output results mapped in real-time.

---

## Design Patterns & Architecture / ארכיטקטורה ודפוסים עיצוביים
The system is built upon core software design patterns to ensure scalability, maintainability, and clean code:
*   **Observer Pattern**: Implemented through the pub/sub architecture. **Agents** act as subscribers (observers) that watch **Topics** (observables) and compute new data dynamically when messages are published.
*   **Singleton Pattern**: Used for the central `TopicManager` to guarantee a single, global registry of all topics and agents across the server.
*   **SOLID Principles**: Emphasized throughout the backend structure, separating the Controller (Servlets handling HTTP requests) from the Model (Computation Graph logic) and the View (HTML/JS output generation).
*   **Strategy Pattern & SOLID Rate Limiter**: 
    The rate limiter system is implemented with clean separation of concerns and pattern compliance:
    *   **Strategy Pattern**: Rate limiting algorithms (e.g., `TokenBucketStrategy`, `FixedWindowStrategy`) implement the common `RateLimitingStrategy` interface, allowing algorithms to be swapped dynamically at runtime using the `RateLimiter` context class.
    *   **Interface Segregation Principle (ISP)**: Rate limiter setter/getter operations are isolated to the `RateLimitedServer` interface, ensuring that base HTTP servers are not forced to implement rate limiting behavior unless required.
    *   **Single Responsibility Principle (SRP)**: Decoupled rate limit configuration mapping from execution. The `RateLimitConfig` class solely manages configuration storage and fallback lookups per URI, while the strategies focus purely on algorithm logic.
    *   **Liskov Substitution Principle (LSP)**: `RateLimitedServer` extends `HTTPServer` so that a rate-limited server can be used interchangeably wherever a standard HTTP server is expected.
    *   **Dependency Inversion Principle (DIP)**: High-level configuration of limits is defined in `Main.java` and injected via `setRateLimiter()`, preventing the server from being tightly coupled to specific rate limiting thresholds.

---

## Advanced Features & Real-Life Flows / פיצ'רים מתקדמים ודוגמה מהעולם האמיתי
To demonstrate capabilities beyond simple mathematical operations, we implemented a **Real-Life Ticket Booking System** pipeline in the `configs` package:
1.  **`configs.OrderAgent`**: Subscribes to `order_requests`. Validates that ticket requests are positive numeric values and forwards validated requests to `validated_orders`.
2.  **`configs.PaymentAgent`**: Subscribes to `validated_orders`. Simulates billing processing (e.g., charging $50 per ticket) and publishes success status to `payment_status`.
3.  **`configs.InventoryAgent`**: Subscribes to `payment_status`. Manages stock levels (default: 50 tickets), decrements inventory upon successful orders, updates `inventory_updates`, and issues detailed booking outcomes to `order_confirmation`.

This demonstrates **non-mathematical stateful processing**, custom text manipulation, and complex multi-agent pipelines.

---

## Installation / התקנה
Ensure you have Java Development Kit (JDK) installed (version 17 or higher recommended).

1. Clone this repository:
   ```bash
   git clone https://github.com/NadavBarda/MyKafka.git
   cd MyKafka
   ```

---

## Running Commands / פקודות להרצה

### Compile the Java files:

**Linux / macOS:**
```bash
javac -d bin -cp "lib/gson-2.10.1.jar" project_biu/Main.java project_biu/configs/*.java project_biu/graph/*.java project_biu/server/*.java project_biu/server/ratelimiter/*.java project_biu/servlets/*.java project_biu/views/*.java
```

**Windows:**
```powershell
javac -d bin -cp "lib/gson-2.10.1.jar" project_biu/Main.java project_biu/configs/*.java project_biu/graph/*.java project_biu/server/*.java project_biu/server/ratelimiter/*.java project_biu/servlets/*.java project_biu/views/*.java
```

### Launch the server:

**Linux / macOS:**
```bash
java -cp "bin:lib/gson-2.10.1.jar" Main
```

**Windows (Command Prompt / PowerShell):**
```powershell
java -cp "bin;lib/gson-2.10.1.jar" Main
```

Open your browser and navigate to: `http://localhost:8080/app/index.html`

---

## Demo Video Requirements / דרישות לסרטון דמו
The submission requires a demo video meeting the following criteria:
*   **Duration**: Max 5 minutes.
*   **Slide 1**: Title / Opening Slide including course details and presenters' names.
*   **Slide 2**: Background Story & Objectives.
*   **Slide 3**: Project Architecture & Design Patterns (SOLID, Strategy, Observer, Singleton).
*   **Live Demo**:
    *   Deploying `ticket_booking.conf` or other configs via the Web Panel.
    *   Publishing messages and demonstrating real-time values propagation across topics and agents.
    *   Highlighting advanced features (Rate Limiting strategies, custom Ticket Booking flow, dynamic graph rendering).
*   **Slide 4 (Summary)**: Wrap-up of key concepts learned during the course and takeaways.

