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

