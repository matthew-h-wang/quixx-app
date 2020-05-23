package quixxapp;

import java.util.ArrayList;

/**
 * Represents the state of a Game (?? Maybe just the initial joining of a Game?)
 */
public class Game {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 8;
    private final int maxPlayers;
    private Player host;
    private String gameCode;
    private ArrayList<Player> players;

    public Game(String gameCode, int maxPlayers, String hostName) throws IllegalArgumentException {
        this.gameCode = gameCode;
        if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS)
            throw new IllegalArgumentException("max players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
        this.maxPlayers = maxPlayers;
        this.host = new Player(hostName);
        players = new ArrayList<Player>();
        players.add(host);
    }

    public Player getHost(){
        return host;
    }

    public boolean isReady(){
        return players.size() >= MIN_PLAYERS && players.size() <= maxPlayers;
    }

    public boolean isFull(){
        return players.size() >= maxPlayers;
    }
    /**
     * Attempts to add a new player to the game.
     * If game is full, or another player already exists with the same name
     */
    public Player addPlayer(String name) throws IllegalArgumentException {
        if (isFull())
            throw new IllegalArgumentException("Game is already full!");
        Player player = new Player(name);
        if (players.contains(player))
            throw new IllegalArgumentException("Game already has player with name " + name);
        
        players.add(player);
        return player;
    
    }

    /**
     * Attempts to remove a player from the game.
     * If successful, returns Player that was removed.
     * Cannot remove host;
     */
    public Player removePlayer(String name){
        Player player = new Player(name);
        if (player.equals(host) || !players.contains(player))
            return null;
        else {
            return players.remove(players.indexOf(player));
        }
    }

    class Player {
        private String name;
        private Player(String name){    
            this.name = name;
        }
    
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Player)) return false;
            Player that = (Player) other;
            return this.name.equals(that.name);
        }

        public boolean isHost(){
            return this == getHost();
        }

        public String getGameCode(){
            return gameCode;
        }

        // class PlayerRunner implements Runnable {
        //     final Socket socket;
        //     final Scanner input;
        //     final PrintWriter output;
        
        //     public PlayerRunner(Socket socket, Scanner input, PrintWriter output) {
        //         this.socket = socket;
        //         this.input = input;
        //         this.output = output;
        //     }
        
        //     @Override
        //     public void run() {
        //         // TODO Auto-generated method stub
        
        //     }
            
    }
    

}


// class GameRunner implements Runnable {
//     Game game;
//     @Override
//     public void run() {
        
        
//     }
// }