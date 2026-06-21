# Computation Graph Web Interface - Advanced Programming Ex6

This project implements a web-based visualization interface for computation graphs, built as part of the Advanced Programming course (Exercise 6). The application features a custom Java HTTP Server backend that handles dynamic content loading, config file uploads, and topic publication, coupled with a clean 3-panel iframe front-end layout.

## Background
The system manages a computation graph consisting of rectangular **Topics** (holding data values) and circular **Agents** (performing operations on topics). Users can upload custom configurations to dynamically construct graphs and publish/subscribe to topics to view output results mapped in real-time.

## Project Structure
*   `html_files/`: Frontend static assets.
    *   `index.html` / `index.css`: Multi-iframe layout orchestrator.
    *   `form.html` / `form.css`: Control panel containing Configuration Upload and Publish forms.
    *   `graph.html` / `graph.css` / `graph.js`: HTML5 Canvas visualization for rendering the computation graph.
    *   `temp.html` / `temp.css`: Empty state placeholder.
*   `src/`: Java source code implementing the HTTP Server, Servlets, and backend logic.

## Installation
Ensure you have Java Development Kit (JDK) installed (version 17 or higher recommended).

1. Clone this repository:
   ```bash
   git clone <repository-url>
   cd ex6
   ```
2. Compile the Java files:
   ```bash
   javac -d bin src/*.java
   ```

## Running the Application
To launch the backend server:
```bash
java -cp bin Main
```
The server will start listening on `http://localhost:8080/`. You can connect via any browser by navigating to `http://localhost:8080/app/index.html`.

## Demo Video
The project includes a 5-minute overview video demonstrating:
*   Project introduction with course details and developer info.
*   The architecture and design patterns used (SOLID principles).
*   Live demonstration of loading a `.conf` file to render the graph.
*   Publishing messages to topics and witnessing value updates.
*   Summary of key learning points.
