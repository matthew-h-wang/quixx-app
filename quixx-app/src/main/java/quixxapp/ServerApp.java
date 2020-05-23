package quixxapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ServerApp listens for client connections and fires GameMatching handler threads to 
 * create and match Games and Players.
 */
public class ServerApp {
    public static final int PORT = 58989;
    public static final int GAME_CODE_LENGTH = 5;
    public static ConcurrentHashMap<String, Game> games;

    public static ExecutorService threadPool; 
    public static Random codeGen;

    private static enum C_ARGS {CREATE , HOST_NAME, MAX_PLAYERS, PUBLIC_PRIVATE, JOIN, PLAYERNAME, OPT_GAMCODE};
    private static final C_ARGS[] createCommandArgs = {C_ARGS.CREATE, C_ARGS.HOST_NAME, C_ARGS.MAX_PLAYERS, C_ARGS.PUBLIC_PRIVATE};
    private static final C_ARGS[] joinCommandArgs = {C_ARGS.JOIN, C_ARGS.PLAYERNAME, C_ARGS.OPT_GAMCODE};


    public static void main(String[] args) throws IOException {
        //TODO: Add multigame handling, with codes.
        try (ServerSocket listener = new ServerSocket(PORT)) {
            games = new ConcurrentHashMap<String, Game>();
            codeGen = new Random();

            System.out.println("Quixx Server is Running...");
            threadPool = Executors.newFixedThreadPool(200);
            while (true) {
                threadPool.execute(new GameMatching(listener.accept()));
            }
        }
    }

    static synchronized String generateGameCode(){
        String code;
        do {
            code = "";
            for (int i = 0; i < GAME_CODE_LENGTH; i++) {
                int c = codeGen.nextInt(36);
                if (c < 10)
                    code += c;
                else 
                    code += (char) ('A' + (c -10));
            } 
        }
        while (games.containsKey(code));

        return code;
    }

    /**
     * 
     * @param commandArgs should fit pattern of createCommandArgs
     * @return host Player
     */
    private static synchronized Game.Player createGame(String[] commandArgs) throws IllegalArgumentException {
        try {
            String gameCode = generateGameCode();
            //TODO handle commandArgs better
            Game game = new Game(gameCode, Integer.parseInt(commandArgs[2]), commandArgs[1]);
            games.put(gameCode, game);
            return game.getHost(); 
        } catch (NumberFormatException | IndexOutOfBoundsException e){
            throw new IllegalArgumentException("Valid CREATE command args are: CREATE " + Arrays.toString(createCommandArgs).replaceAll(",", " "));
        }
    }

    /**
     * 
     * @param commandArgs should fit pattern of joinCommandArgs
     * @return Player of joined game
     */
    private static synchronized Game.Player joinGame(String[] commandArgs) throws IllegalArgumentException {
        try {
            //TODO handle private/public rooms
            String gameCode = commandArgs[2];
            //TODO handle commandArgs better
            if (!games.containsKey(gameCode))
                throw new IllegalArgumentException("No open game with code " + gameCode);
            Game game = games.get(gameCode);
            return game.addPlayer(commandArgs[1]); 
        } catch (IndexOutOfBoundsException e){
            throw new IllegalArgumentException("Valid JOIN command args are: JOIN " + Arrays.toString(joinCommandArgs).replaceAll(",", " "));
        }
    }
    
    static class GameMatching implements Runnable {
        Socket socket;

        public GameMatching(Socket socket){
            this.socket = socket;
        }
    
        @Override
        public void run() {
            try{
                Scanner input = new Scanner(socket.getInputStream());
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Client connection from " + socket.getInetAddress());

                Game.Player player = null; 
                output.println("Welcome to the Quixx Server!");
                output.println("Would you like to CREATE a new game or JOIN?");
                while (input.hasNextLine()) {
                    String[] commandArgs = input.nextLine().split(" ");
                    if (commandArgs[0].equals(C_ARGS.CREATE.toString())) {
                        try {
                            player = createGame(commandArgs);
                            output.println("Successfully created a game with code " + player.getGameCode());
                            System.out.println(socket.getInetAddress() + " successfully created a game with code " + player.getGameCode());
                            break;
                        } catch(IllegalArgumentException e){
                            output.println(e.getMessage());
                        }                        
                    } else if (commandArgs[0].equals(C_ARGS.JOIN.toString())) {
                        try { 
                            player = joinGame(commandArgs);
                            output.println("Successfully joined the game with code " + player.getGameCode());
                            System.out.println(socket.getInetAddress() + " successfully joined a game with code " + player.getGameCode());
                            break;
                        } catch(IllegalArgumentException e){
                            output.println(e.getMessage());
                        }
                    } else {
                        output.println("Valid commands include: \n" +
                        "\tCREATE " + Arrays.toString(createCommandArgs).replaceAll(",", " ") +
                        "\tJOIN " + Arrays.toString(joinCommandArgs).replaceAll(",", " ") );
                    }
                }
                                
                if (player != null){
                    //continue this thread, instead of starting a new one.
                    //PlayerRunner is now responsible to close the thread.
                    // player.new PlayerRunner(socket, input, output).run();

                    //TEMP
                    while (input.hasNextLine()) {
                        if(input.nextLine().startsWith("/quit"))
                            break;
                    }

                    //should we start new one instead? 
                    //threadPool.execute(game.new PlayerRunner(socket, input, output));
                }
                //else {
                    //no successful player set up, end connection;
                    System.out.println("Closing Client connection from " + socket.getInetAddress());
                
                        socket.close();
                //}
                         
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
                try{
                    System.out.println("Closing Client connection from " + socket.getInetAddress());
                    socket.close();
                }
                catch (IOException e2) {
                }
            }
        }    
    }
}




