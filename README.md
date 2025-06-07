# BattleShips

## Project Description
This project implements the game **BattleShips** (Lodě). It is a classic strategy game for two players where they take turns guessing the positions of each other's ships on a grid. The game is implemented as a server application that allows two players to connect and interact.

The server manages game sessions, facilitates communication between players, and enforces game rules.

---

## Compilation and Build of Server-side
The project is built using **Java 17** and **Maven**. Follow these steps to compile and build the project:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/krystofjelinek/battleshipsServer.git
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

## Deviations from specifications


1. **Comunication protocol**:
   - Server doesn't send `OK` or `ERR` message after receiving a command. Server responds with following messages only:
     - `SUCCESS` - command was executed successfully
     - `FAILURE` - command was not executed successfully or expected another command
     - `HIT` - bombing was successful - target was hit
     - `MISS` - bombing was unsuccessful - target was not hit
     - `QUIT` - client should quit the game
     - `WIN` - client won the game
     - `LOST` - client lost the game
     - `TURN` - notifies client that it is his turn
     - `PONG` - responds to ping command
     
   - Some commands are not implemented:
     - `SUNK` - notifies client that a ship was sunk
     - `READY` - notifies client that the game is ready to start (bombing phase)
     
   - Some commands were changed:
     - `PLACE` - now looks like this: 
       ```
       PLACE <x> <y> <shipShape> <rotatiton
       ```
       where `<shipShape>` is one of `SIX_SHAPE`, `BLOCK_SHAPE`, `FOUR_SHAPE` or `TWO_SHAPE` and `<rotation>` is either `0` (horiznatal) or `1` (vertical).
       
     - `BOMB` - server responds with `HIT`, `MISS` or `FAILURE`.
       ```
       BOMB <x> <y>
       ```
