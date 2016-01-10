package me.thundergemios10.walls.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import me.thundergemios10.walls.LobbyManager;



public class KeepLobbyLoadedEvent implements Listener{
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e){
        LobbyManager.getInstance();
		if(LobbyManager.lobbychunks.contains(e.getChunk())){
            e.setCancelled(true);
        }
        //System.out.println("Chunk unloading");
    }

}
