package org.daylight.plugins.manhunt.config;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.sound.midi.Patch;

import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.daylight.plugins.manhunt.data.Lines;
import org.daylight.plugins.manhunt.data.References;
import org.daylight.plugins.manhunt.main.Main;
	
public class ConfigWatcher {
	public WatchService ws;
	private Thread configWatchingThread;
//	private Thread configLoadingThread;
	private long lastTimeUpdated = 0L;
//	private boolean needToLoad = false;
	
	public ConfigWatcher() {
		try {
			ws = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			References.pluginConfigDirectory.toPath().register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		if((configWatchingThread!=null && (configWatchingThread.isInterrupted() || !configWatchingThread.isAlive()))
//				|| (configLoadingThread!=null && (configLoadingThread.isInterrupted() || !configLoadingThread.isAlive()))
				) stop();
		configWatchingThread = new Thread(watchingTask);
		configWatchingThread.start();
//		configLoadingThread = new Thread(loadingTask);
//		configLoadingThread.start();
	}
	
	public void stop() {
		configWatchingThread.interrupt();
//		configLoadingThread.interrupt();
	}
	
	private Runnable watchingTask = new Runnable() {
		@Override
		public void run() {
			WatchKey key;
	        try {
				while ((key = ws.take()) != null) {
				    for (WatchEvent<?> event : key.pollEvents()) {
				    	try {
//				    		debug(System.currentTimeMillis() - lastTimeUpdated);
				    		if(lastTimeUpdated != 0L && (System.currentTimeMillis() - lastTimeUpdated <= 20)) continue;
					    	if(event.context().toString().equalsIgnoreCase("mainconfig.yml")) {
					    		Bukkit.getScheduler().runTask(Main.plugin, () -> {
//					    			debug("change");
					    			ConfigOperations.updateAndLoadConfig();
					    		});
					    		lastTimeUpdated = System.currentTimeMillis();
	//				    		needToLoad = true;
					    	}
				    	} catch (IllegalPluginAccessException e) {}
				    }
				    key.reset();
				}
			} catch (InterruptedException e) {}
		}
	};
	
//	private Runnable loadingTask = new Runnable() {
//		@Override
//		public void run() {
//			while (true) {
//				if(needToLoad) {
//					if(System.currentTimeMillis() - lastTimeUpdated >= 100) {
//						ConfigOperations.loadConfig();
//						needToLoad = false;
////						SimpleLogger.debug("reloaded config automatically");
//					}
//				}
//				try {
//					Thread.currentThread().sleep(500);
//				} catch (InterruptedException e) {}
//			}
//		}
//	};
}
