package org.daylight.plugins.manhunt.coreDependent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class AuthorsBaiscVer {
	public static void authors(CommandSender sender) {
//		String discordLinkText = "a";
		String curseforgeLinkText = "https://www.curseforge.com/minecraft/bukkit-plugins/advanced-manhunt";
		String line = "-------------------------------------------------------------------------------";
		String endLine = "\n" + ChatColor.RED + line;
		
		String part1 = ChatColor.RED + line + ChatColor.RESET + "\n";
		part1 += ChatColor.WHITE + "" + ChatColor.BOLD + "Info about Advanced Manhunt plugin" + ChatColor.RESET + "\n";
		part1 += ChatColor.WHITE + "Author: " + ChatColor.RED + "" + ChatColor.BOLD + "Greenem" + ChatColor.RESET + "\n";
		part1 += ChatColor.GRAY + "Social links: \n" + ChatColor.RESET;
		part1 += ChatColor.WHITE + "Discord server: " + ChatColor.RESET;
		
//		String discordPartText = ChatColor.GRAY + "" + ChatColor.BOLD + discordLinkText;
		
		String part2 = ChatColor.GRAY + "\n" + "Download links: " + ChatColor.RESET + "\n";
		part2 += ChatColor.WHITE + "CurseForge: " + ChatColor.RESET;
		
		String curseforgePartText = ChatColor.GRAY + "" + ChatColor.BOLD + curseforgeLinkText;
		
//		sender.sendMessage(part1 + discordPartText + part2 + curseforgePartText + endLine);
		sender.sendMessage(part1 + part2 + curseforgePartText + endLine);

		//		clickable.setHoverEvent(new HoverEvent()); // to do later, show that player's name
	}
}
