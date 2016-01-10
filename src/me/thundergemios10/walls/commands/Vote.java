package me.thundergemios10.walls.commands;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import me.thundergemios10.walls.GameManager;
import me.thundergemios10.walls.MessageManager;
import me.thundergemios10.walls.SettingsManager;


public class Vote implements SubCommand {
    
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return false;
        }
        int game = GameManager.getInstance().getPlayerGameId(player);
        if(game == -1){
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.notinarena", player);
            return true;
        }

        GameManager.getInstance().getGame(GameManager.getInstance().getPlayerGameId(player)).vote(player);

        return true;
    }
    
    @Override
    public String help(Player p) {
        return "/w vote - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.vote", "Votes to start the game");
    }

	@Override
	public String permission() {
		return "walls.arena.vote";
	}
}