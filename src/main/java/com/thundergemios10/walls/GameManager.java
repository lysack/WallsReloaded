package com.thundergemios10.walls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.thundergemios10.walls.Game.GameMode;
import com.thundergemios10.walls.MessageManager.PrefixType;
import com.thundergemios10.walls.api.PlayerLeaveArenaEvent;
import com.thundergemios10.walls.stats.StatsManager;
import com.thundergemios10.walls.util.Kit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class GameManager {

	static GameManager instance = new GameManager();
	private ArrayList < Game > games = new ArrayList < Game > ();
	private Walls p;
	public static HashMap < Integer, HashSet < Block >> openedChest = new HashMap < Integer, HashSet < Block >> ();
	private ArrayList<Kit>kits = new ArrayList<Kit>();
	private HashSet<Player>kitsel = new HashSet<Player>();
	MessageManager msgmgr = MessageManager.getInstance();

	private GameManager() {

	}

	public static GameManager getInstance() {
		return instance;
	}

	public void setup(Walls plugin) {
		p = plugin;
		LoadGames();
		LoadKits();
		for (Game g: getGames()) {
			openedChest.put(g.getID(), new HashSet < Block > ());
		}
	}

	public Plugin getPlugin() {
		return p;
	}

	public void reloadGames() {
		LoadGames();
	}


	public void LoadKits(){
		Set<String> kits1 = SettingsManager.getInstance().getKits().getConfigurationSection("kits").getKeys(false);
		for(String s:kits1){
			kits.add(new Kit(s));
		}
	}

	public void LoadGames() {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		games.clear();
		int no = c.getInt("walls.arenano", 0);
		int loaded = 0;
		int a = 1;
		while (loaded < no) {
			if (c.isSet("walls.arenas." + a + ".x1")) {
				//c.set("walls.arenas."+a+".enabled",c.getBoolean("walls.arena."+a+".enabled", true));
				if (c.getBoolean("walls.arenas." + a + ".enabled")) {
					//SurvivalGames.$(c.getString("walls.arenas."+a+".enabled"));
					//c.set("walls.arenas."+a+".vip",c.getBoolean("walls.arenas."+a+".vip", false));
					Walls.$("Loading Arena: " + a);
					loaded++;
					games.add(new Game(a));
					StatsManager.getInstance().addArena(a);
				}
			}
			a++;
			
		}
		LobbyManager.getInstance().clearAllSigns();
		
	}

	public int getBlockGameId(Location v) {
		for (Game g: games) {
			if (g.isBlockInArena(v)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerGameId(Player p) {
		for (Game g: games) {
			if (g.isPlayerActive(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public int getPlayerSpectateId(Player p) {
		for (Game g: games) {
			if (g.isSpectator(p)) {
				return g.getID();
			}
		}
		return -1;
	}

	public boolean isPlayerActive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isPlayerInactive(Player player) {
		for (Game g: games) {
			if (g.isPlayerActive(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSpectator(Player player) {
		for (Game g: games) {
			if (g.isSpectator(player)) {
				return true;
			}
		}
		return false;
	}

	public void removeFromOtherQueues(Player p, int id) {
		for (Game g: getGames()) {
			if (g.isInQueue(p) && g.getID() != id) {
				g.removeFromQueue(p);
				msgmgr.sendMessage(PrefixType.INFO, "Removed from the queue in arena " + g.getID(), p);
			}
		}
	}
	
    public boolean isOnWall(Location l){
    	for(Game g: getGames()){
    		if(g.isOnWall(l)){
    			return true;
    		}
    	}
    	return false;
    }

	public boolean isInKitMenu(Player p){
		return kitsel.contains(p);
	}

	public void leaveKitMenu(Player p){
		kitsel.remove(p);
	}

	public void openKitMenu(Player p){
		kitsel.add(p);
	}

	
	public void selectKit(Player p, int i) {
		p.getInventory().clear();
		ArrayList<Kit>kits = getKits(p);
		if(i <= kits.size()){
			Kit k = getKits(p).get(i);
			if(k!=null){
				p.getInventory().setContents(k.getContents().toArray(new ItemStack[0]));
			}
		}
		p.updateInventory();

	}

	public int getGameCount() {
		return games.size();
	}

	public Game getGame(int a) {
		//int t = gamemap.get(a);
		for (Game g: games) {
			if (g.getID() == a) {
				return g;
			}
		}
		return null;
	}

	public void removePlayer(Player p, boolean b) {
		for (Game g : games) {
			if (g.getAllPlayers().contains(p)) {
				PlayerLeaveArenaEvent leavearena = new PlayerLeaveArenaEvent(p, g);
				Bukkit.getServer().getPluginManager().callEvent(leavearena);
			}
		}
		getGame(getPlayerGameId(p)).removePlayer(p, b);
	}

	public void removeSpectator(Player p) {
		getGame(getPlayerSpectateId(p)).removeSpectator(p);
	}

	public void disableGame(int id) {
		getGame(id).disable();
	}

	public void enableGame(int id) {
		getGame(id).enable();
	}

	public ArrayList < Game > getGames() {
		return games;
	}

	public GameMode getGameMode(int a) {
		for (Game g: games) {
			if (g.getID() == a) {
				return g.getMode();
			}
		}
		return null;
	}

	public ArrayList<Kit> getKits(Player p){
		ArrayList<Kit>k = new ArrayList<Kit>();
		for(Kit kit: kits){
			if(kit.canUse(p)){
				k.add(kit);
			}
		}
		return k;
	}

	//TODO: Actually make this countdown correctly
	public void startGame(int a) {
		getGame(a).countdown(10);
	}

	public void addPlayer(Player p, int g) {
		Game game = getGame(g);
		if (game == null) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.input",p, "message-No game by this ID exist!");
			return;
		}
		getGame(g).addPlayer(p);
	}

	public void autoAddPlayer(Player pl) {
		ArrayList < Game > qg = new ArrayList < Game > (5);
		for (Game g: games) {
			if (g.getMode() == Game.GameMode.WAITING) qg.add(g);
		}
		//TODO: fancy auto balance algorithm
		if (qg.size() == 0) {
			pl.sendMessage(ChatColor.RED + "No games to join");
			msgmgr.sendMessage(PrefixType.WARNING, "No games to join!", pl);
			return;
		}
		qg.get(0).addPlayer(pl);
	}

	public WorldEditPlugin getWorldEdit() {
		return p.getWorldEdit();
	}

	public void createArenaFromSelection(Player pl) {
		FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
		//SettingsManager s = SettingsManager.getInstance();

		WorldEditPlugin we = p.getWorldEdit();
		Selection sel = we.getSelection(pl);
		if (sel == null) {
			msgmgr.sendMessage(PrefixType.WARNING, "You must make a WorldEdit Selection first!", pl);
			return;
		}
		Location max = sel.getMaximumPoint();
		Location min = sel.getMinimumPoint();

		/* if(max.getWorld()!=SettingsManager.getGameWorld() || min.getWorld()!=SettingsManager.getGameWorld()){
            pl.sendMessage(ChatColor.RED+"Wrong World!");
            return;
        }*/

		int no = c.getInt("walls.arenano") + 1;
		c.set("walls.arenano", no);
		if (games.size() == 0) {
			no = 1;
		} else no = games.get(games.size() - 1).getID() + 1;
		SettingsManager.getInstance().getSpawns().set(("spawns." + no), null);
		c.set("walls.arenas." + no + ".world", max.getWorld().getName());
		c.set("walls.arenas." + no + ".x1", max.getBlockX());
		c.set("walls.arenas." + no + ".y1", max.getBlockY());
		c.set("walls.arenas." + no + ".z1", max.getBlockZ());
		c.set("walls.arenas." + no + ".x2", min.getBlockX());
		c.set("walls.arenas." + no + ".y2", min.getBlockY());
		c.set("walls.arenas." + no + ".z2", min.getBlockZ());
		c.set("walls.arenas." + no + ".enabled", true);

		SettingsManager.getInstance().saveSystemConfig();
		hotAddArena(no);
		pl.sendMessage(ChatColor.GREEN + "Arena ID " + no + " Succesfully added");

	}

	private void hotAddArena(int no) {
		Game game = new Game(no);
		games.add(game);
		StatsManager.getInstance().addArena(no);
		//SurvivalGames.$("game added "+ games.size()+" "+SettingsManager.getInstance().getSystemConfig().getInt("gs-system.arenano"));
	}

	public void hotRemoveArena(int no) {
		for (Game g: games.toArray(new Game[0])) {
			if (g.getID() == no) {
				games.remove(getGame(no));
			}
		}
	}

	public void gameEndCallBack(int id) {
		getGame(id).setRBStatus("clearing chest");
		openedChest.put(id, new HashSet < Block > ());
	}

	public String getStringList(int gid){
		Game g = getGame(gid);
		StringBuilder sb = new StringBuilder();
		Player[][]players = g.getPlayers();

		sb.append(ChatColor.GREEN+"<---------------------[ Alive: "+players[0].length+" ]--------------------->\n"+ChatColor.GREEN+" ");
		for(Player p: players[0]){
			sb.append(p.getName()+",");
		}
		sb.append("\n\n");
		sb.append(ChatColor.RED+  "<---------------------[ Dead: "+players[1].length+" ]---------------------->\n"+ChatColor.GREEN+" ");
		for(Player p: players[1]){
			sb.append(p.getName()+",");
		}
		sb.append("\n\n");

		return sb.toString();
	}
	

    public void setWallFromSelected(Player player, String args) {
      int gameid = Integer.parseInt(args);
      FileConfiguration c = SettingsManager.getInstance().getSystemConfig();

      WorldEditPlugin we = this.p.getWorldEdit();
      Selection sel = we.getSelection(player);
      if (sel == null) {
        player.sendMessage(ChatColor.RED + "You must make a WorldEdit Selection first");
        return;
      }
	  Vector max = sel.getNativeMaximumPoint();
	  Vector min = sel.getNativeMinimumPoint();

      int wallno = c.getInt("walls.arenas." + gameid + ".wallno",0) + 1;
      c.set("walls.arenas." + gameid + ".wallno", wallno);
      
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".world", player.getWorld().getName());
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".x1", max.getBlockX());
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".y1", max.getBlockY());
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".z1", max.getBlockZ());
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".x2", min.getBlockX());
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".y2", min.getBlockY());
      c.set("walls.arenas." + gameid + ".wall." + wallno + ".z2", min.getBlockZ());

      SettingsManager.getInstance().saveSystemConfig();

      getGame(gameid).loadWalls();
      player.sendMessage(ChatColor.GREEN + "Wall added!");
    }


}