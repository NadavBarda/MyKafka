# MyKafka - Computation Graph Web Interface (AP Ex6)

This project implements a web-based visualization interface for computation graphs named **MyKafka**, built as part of the Advanced Programming course (Exercise 6). The application features a custom Java HTTP Server backend that handles dynamic content loading, configuration file uploads, and topic publication, coupled with a clean 3-panel layout.


## Design Patterns & Architecture
The system is built upon core software design patterns to ensure scalability, maintainability, and clean code:
*   **Observer Pattern**: Implemented through the pub/sub architecture. **Agents** act as subscribers (observers) that watch **Topics** (observables) and compute new data dynamically when messages are published.
*   **Singleton Pattern**: Used for the central `TopicManager` to guarantee a single, global registry of all topics and agents across the server.
*   **SOLID Principles**: Emphasized throughout the backend structure, separating the Controller (Servlets handling HTTP requests) from the Model (Computation Graph logic) and the View (HTML/JS output generation).
*   **Strategy Pattern & SOLID Rate Limiter**: 
    The rate limiter system is implemented with clean separation of concerns and pattern compliance:
    *   **Strategy Pattern**: Rate limiting algorithms (e.g., `TokenBucketStrategy`, `FixedWindowStrategy`) implement the common `RateLimitingStrategy` interface, allowing algorithms to be swapped dynamically at runtime using the `RateLimiter` context class.
    *   **Interface Segregation Principle (ISP)**: Rate limiter setter/getter operations are isolated to the `RateLimitedServer` interface, ensuring that base HTTP servers are not forced to implement rate limiting behavior unless required.
    *   **Single Responsibility Principle (SRP)**: Decoupled rate limit configuration mapping from execution. The `RateLimitConfig` class solely manages configuration storage and fallback lookups per URI (like tighter polling rules for `/api/graph`), while the strategies focus purely on algorithm logic.
    *   **Liskov Substitution Principle (LSP)**: `RateLimitedServer` extends `HTTPServer` so that a rate-limited server can be used interchangeably wherever a standard HTTP server is expected.
    *   **Dependency Inversion Principle (DIP)**: High-level configuration of limits is defined in `Main.java` and injected via `setRateLimiter()`, preventing the server from being tightly coupled to specific rate limiting thresholds.

## Background
The system manages a computation graph consisting of rectangular **Topics** (holding data values) and circular **Agents** (performing mathematical or logical operations on topics). Users can upload custom configurations to dynamically construct graphs and publish/subscribe to topics to view output results mapped in real-time.

## Project Structure
*   `html_files/`: Frontend static assets.
    *   `index.html` / `index.css`: Multi-iframe layout orchestrator.
    *   `form.html` / `form.css`: Control panel containing Configuration Upload and Publish forms.
    *   `graph.html` / `graph.css` / `graph.js`: HTML5 Canvas visualization for rendering the computation graph.
    *   `temp.html` / `temp.css`: Empty state placeholder.
*   `project_biu/`: Java source code implementing the HTTP Server, Servlets, and backend logic.

## Installation
Ensure you have Java Development Kit (JDK) installed (version 17 or higher recommended).

1. Clone this repository:
   ```bash
   git clone https://github.com/NadavBarda/MyKafka.git
   cd MyKafka
   ```
2. Compile the Java files:

   **Linux / macOS:**
   ```bash
   javac -d bin -cp "lib/gson-2.10.1.jar" project_biu/Main.java project_biu/configs/*.java project_biu/graph/*.java project_biu/server/*.java project_biu/server/ratelimiter/*.java project_biu/servlets/*.java project_biu/views/*.java
   ```

   **Windows:**
   ```powershell
   javac -d bin -cp "lib/gson-2.10.1.jar" project_biu/Main.java project_biu/configs/*.java project_biu/graph/*.java project_biu/server/*.java project_biu/server/ratelimiter/*.java project_biu/servlets/*.java project_biu/views/*.java
   ```

## Running the Application
To launch the backend server:

**Linux / macOS:**
```bash
java -cp "bin:lib/gson-2.10.1.jar" Main
```

**Windows (Command Prompt / PowerShell):**
```powershell
java -cp "bin;lib/gson-2.10.1.jar" Main
```

The server will start listening on `http://localhost:8080/`. You can connect via any browser by navigating to `http://localhost:8080/app/index.html`.

## Demo Video
The project includes a 5-minute overview video demonstrating:
*   Project introduction with course details and developer info.
*   The architecture and design patterns used (SOLID principles, Observer, Singleton).
*   Live demonstration of loading a `.conf` file to render the graph.
*   Publishing messages to topics and witnessing value updates.
*   Summary of key learning points.
