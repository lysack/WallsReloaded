package com.thundergemios10.walls.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.thundergemios10.walls.GameManager;
import com.thundergemios10.walls.MessageManager;
import com.thundergemios10.walls.SettingsManager;



public class Spectate implements SubCommand{

    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        
        if(args.length == 0){
            if(GameManager.getInstance().isSpectator(player)){
                GameManager.getInstance().removeSpectator(player);
                return true;
            }
            else{
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Game ID");
                return true;
            }
        }
        if(SettingsManager.getInstance().getSpawnCount(Integer.parseInt(args[0])) == 0){
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.nospawns", player);
            return true;
        }
        if(GameManager.getInstance().isPlayerActive(player)){
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.specingame", player);
            return true;
        }
        GameManager.getInstance().getGame(Integer.parseInt(args[0])).addSpectator(player);
        return true;
    }

    public String help(Player p) {
        return "/w spectate <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.spectate", "Spectate a running arena");
    }

	public String permission() {
		return "walls.arena.spectate";
	}

}
