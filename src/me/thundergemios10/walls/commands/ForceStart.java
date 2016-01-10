package me.thundergemios10.walls.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import me.thundergemios10.walls.Game;
import me.thundergemios10.walls.GameManager;
import me.thundergemios10.walls.MessageManager;
import me.thundergemios10.walls.SettingsManager;
import me.thundergemios10.walls.MessageManager.PrefixType;



public class ForceStart implements SubCommand {

	MessageManager msgmgr = MessageManager.getInstance();

	public boolean onCommand(Player player, String[] args) {

		if (!player.hasPermission(permission()) && !player.isOp()) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
			return true;
		}
		int game = -1;
		int seconds = 10;
		if(args.length == 2){
			seconds = Integer.parseInt(args[1]);
		}
		if(args.length >= 1){
			game = Integer.parseInt(args[0]);

		}
		else
			game  = GameManager.getInstance().getPlayerGameId(player);
		if(game == -1){
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notingame", player);
			return true;
		}
		if(GameManager.getInstance().getGame(game).getActivePlayers() < 2){
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notenoughtplayers", player);
			return true;
		}


		Game g = GameManager.getInstance().getGame(game);
		if (g.getMode() != Game.GameMode.WAITING && !player.hasPermission("walls.arena.restart")) {
			MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.alreadyingame", player);
			return true;
		}
		g.countdown(seconds);

		msgmgr.sendFMessage(PrefixType.INFO, "game.started", player, "arena-" + game);

		return true;
	}

	@Override
	public String help(Player p) {
		return "/w start - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.forcestart", "Forces the game to start");
	}

	@Override
	public String permission() {
		return "walls.arena.start";
	}
}