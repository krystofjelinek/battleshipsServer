# BattleShips

## Project Description
This project implements the game **BattleShips** (Lodě). It is a classic strategy game for two players where they take turns guessing the positions of each other's ships on a grid. The game is implemented as a server application that allows two players to connect and interact.

The server manages game sessions, facilitates communication between players, and enforces game rules.

---

## Compilation and Build
The project is built using **Java 17** and **Maven**. Follow these steps to compile and build the project:

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd BattleShips
   ```

2. **Compile and build**:
   Run the following command to build the project and create an executable JAR file:
   ```bash
   mvn clean package
   ```

   The resulting JAR file will be located in the `target` directory under the name `BattleShips-1.0.jar`.

3. **Run the server**:
   Start the server using:
   ```bash
   java -jar target/BattleShips-1.0.jar
   ```

   If no port is provided as an argument, the server will use the default port specified in the `config.properties` file.

---

## Unresolved Issues
- **Input validation**: The server currently lacks sufficient validation for client inputs, which may lead to unexpected behavior.
- **Incomplete game rules**: Some advanced game mechanics (e.g., special ship types) are not implemented.
- **Exception handling**: Certain parts of the code do not handle exceptions properly, which may cause the server to crash in unexpected situations.
- **Dependency on `config.properties`**: The server cannot start if this file is missing or misconfigured.

---

## Deviations from Requirements
- **No graphical interface**: The project is implemented as a console application, even if the requirements may have specified a graphical interface.
- **Two-player limitation**: The server supports only two simultaneous connections, which may differ from the requirements if more players were expected.
- **No game state persistence**: The game does not support saving and loading ongoing sessions.

---

This project is still under development, and some features may be added in future versions.