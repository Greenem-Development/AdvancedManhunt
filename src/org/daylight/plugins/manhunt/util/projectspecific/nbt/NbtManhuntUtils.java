package org.daylight.plugins.manhunt.util.projectspecific.nbt;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;

public class NbtManhuntUtils {
	private static final String OWN_SECTION_NAME = "advanced_manhunt";
	
	private static <E extends Enum<E>> E enumLookup(Class<E> e, String str) {
		if(str == null) return null;
		try {
			return Enum.valueOf(e, str);
		} catch (IllegalArgumentException e1) {
			return null;
		}
	}
	
	public static <E extends Enum<E>> E getManhuntEnumValue(Class<E> e, ItemStack item, String key) {
		String typeString = getManhuntStringValue(item, key);
		return enumLookup(e, typeString);
	}
	
	public static String getManhuntStringValue(ItemStack item, String key) {
		final AtomicReference<String> reference = new AtomicReference<String>();
		NBT.get(item, nbt -> {
		    ReadableNBT ownNbt = nbt.getCompound(OWN_SECTION_NAME);
		    if(ownNbt == null) return;
		    reference.set(ownNbt.getString(key));
		});
		if(reference.get() == null) return null;
		return reference.get();
	}
	
	public static Boolean getManhuntBooleanValue(ItemStack item, String key) {
		final AtomicReference<Boolean> reference = new AtomicReference<Boolean>();
		NBT.get(item, nbt -> {
		    ReadableNBT ownNbt = nbt.getCompound(OWN_SECTION_NAME);
		    if(ownNbt == null) return;
		    reference.set(ownNbt.getBoolean(key));
		});
		if(reference.get() == null) return null;
		return reference.get();
	}
	
	public static void setManhuntStringValue(ItemStack item, String key, Object value) {
		setManhuntStringValue(item, key, value.toString());
	}
	
	public static void setManhuntStringValue(ItemStack item, String key, String value) {
		NBT.modify(item, nbt -> {
			ReadWriteNBT ownNbt = nbt.getOrCreateCompound(OWN_SECTION_NAME);
			ownNbt.setString(key, value);
		});
	}
	
	public static void setManhuntBooleanValue(ItemStack item, String key, boolean value) {
		NBT.modify(item, nbt -> {
			ReadWriteNBT ownNbt = nbt.getOrCreateCompound(OWN_SECTION_NAME);
			ownNbt.setBoolean(key, value);
		});
	}
	
	public static boolean isManhuntItem(ItemStack item) {
		final AtomicReference<Boolean> reference = new AtomicReference<Boolean>();
		NBT.get(item, nbt -> {
		    reference.set(nbt.getCompound(OWN_SECTION_NAME) != null);
		});
		return reference.get();
	}
	
	public static ItemStack keepOnlyNameAndSection(ItemStack oldItem) {
		if(oldItem.getType() == null) return oldItem;
//		final AtomicReference<ReadableNBT> reference = new AtomicReference<ReadableNBT>();
//		final Map<String, Object> oldData = new HashMap<>();
		
		ItemStack newItem = new ItemStack(oldItem.getType());
		newItem.setAmount(oldItem.getAmount());
		
		ItemMeta newMeta = newItem.getItemMeta();
		newMeta.setDisplayName(oldItem.getItemMeta().getDisplayName());
		newItem.setItemMeta(newMeta); // setting old name on new item
		
		NBT.get(oldItem, nbt -> {
			ReadableNBT ownNbt = nbt.getCompound(OWN_SECTION_NAME);
			
			NBT.modify(newItem, nbt2 -> {
				nbt2.getOrCreateCompound(OWN_SECTION_NAME).mergeCompound(ownNbt);
			});
			
//			ownNbt.toString();
//			for(String key : ownNbt.getKeys()) {
//				NBTType type = ownNbt.getType(key);
//				switch (type) {
//					case NBTTagString: {
//						oldData.put(key, ownNbt.getString(key));
//						break;
//					}
//					case NBTTagInt: {
//						oldData.put(key, ownNbt.getInteger(key));
//						break;
//					}
//					default: {
//						log("Unknown nbt type " + type.toString() + " in function keepOnlyNameAndSection!");
//						break;
//					}
//				}
//			}
		});
		
		return newItem;
	}
}