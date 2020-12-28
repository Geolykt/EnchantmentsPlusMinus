package org.whyisthisnecessary.eps.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.whyisthisnecessary.eps.EnchantMetaWriter;
import org.whyisthisnecessary.eps.Main;

public class EnchantGUI implements Listener, InventoryHolder {

	Inventory gui = Bukkit.createInventory(null, 36, "Enchantments");
	private Main plugin;
	private Player p;
	private String currentgui = "";
	
	public EnchantGUI(Main main){
        this.plugin = main;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        CreatePanes();
    }
	
	public void OpenInventory(Player p, String gui1)
	{
		this.p = p;
        p.openInventory(gui);
        LoadInventory(p, gui1);
	}
	
	public void LoadInventory(Player p, String gui)
	{
		clearInventory();
		@SuppressWarnings("unchecked")
		List<String> l = (List<String>) plugin.config.getList("guis."+gui+".enchants");
        String[] list = new String[l.size()];
        list = l.toArray(list);
        for (String i : list)
        {
        	add(i);
        }
        currentgui = gui;
        ItemStack i = new ItemStack(Material.GOLD_INGOT, 1);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN+"Your Tokens � "+ChatColor.YELLOW+Integer.toString(InternalTokenManager.GetTokens(p.getName())));
        i.setItemMeta(meta);
        this.gui.setItem(4, i);
        this.gui.setItem(31, i);
	}
	
	public void clearInventory()
	{
		for (int i=0;i<gui.getSize();i++)
		{
			if (gui.getItem(i) != null)
			{
			if (!(gui.getItem(i).getType() == Material.BLACK_STAINED_GLASS_PANE))
			gui.clear(i);
			}
		}
	}
	
	@Override
	public Inventory getInventory()
	{
        return gui;
    }
	
	@EventHandler
    public void onpickaxeinventoryClick(final InventoryClickEvent e) {
        if (e.getInventory().getHolder() != null) return;
        if (e.getInventory() != gui) return;
        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();
        Map<Enchantment, Integer> enchs = clickedItem.getItemMeta().getEnchants();
        Enchantment ench = null;
        
        for (Map.Entry<Enchantment, Integer> entry : enchs.entrySet()) {
            ench = entry.getKey();
        }
        
        if (ench == null) return;
        
        if (e.isLeftClick())
        {
            UpgradeItem(ench, p, 1);
        }
        else if (e.isRightClick())
        {
        	UpgradeItem(ench, p, 5);
        }
        else if (e.isShiftClick())
        {
        	UpgradeItem(ench, p, 50);
        }
        
    }

    @EventHandler
    public void onpickaxeinventoryClick(final InventoryDragEvent e) {
        if (e.getInventory() == gui) {
          e.setCancelled(true);
        }
    }
    
    public void add(String name)
    {
    	String cost;
    	Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(name));
    	String displayname = name.substring(0,1).toUpperCase() + name.substring(1);
    	displayname = displayname.replaceAll("_", " ");
    	displayname = WordUtils.capitalizeFully(displayname);
    	Material material = Material.getMaterial(plugin.config.getString("enchants."+name+".upgradeicon"));
    	String desc = plugin.config.getString("enchants."+name+".upgradedesc");
    	Integer maxlevel = (Integer) plugin.config.get("enchants."+enchant.getKey().getKey()+".maxlevel");
        if (!(p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant) >= maxlevel))
        {
        	String method = plugin.config.getString("enchants."+name+".cost.type");
        	cost = Integer.toString(getCost(method, enchant));
        
        }
        else
        {
            cost = "Maxed!";
        }
        ItemStack slot = new ItemStack(Material.BOOK, 1);;
        if (material != null)
        slot = new ItemStack(material, 1);
        else
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Invalid material type for enchantment "+name.toUpperCase()+". Setting to default BOOK.");
        ItemMeta meta = slot.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA+displayname);
        meta.setLore(Arrays.asList(new String[] { "", 
        		ChatColor.GRAY+desc,
        		"",
        		ChatColor.GREEN+"Cost � "+ ChatColor.YELLOW +cost,
        		ChatColor.GREEN+"Max Level � "+ ChatColor.YELLOW +maxlevel,
        		ChatColor.GREEN+"Current Level � "+ ChatColor.YELLOW +p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant),
        		"",
        		ChatColor.GRAY+"� Left-Click to upgrade once",
        		ChatColor.GRAY+"� Right-Click to upgrade 5 times",
        		ChatColor.GRAY+"� Shift-Right-Click to upgrade 50 times",
        		}));
        meta.addEnchant(enchant, 1, true);
        meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
        slot.setItemMeta(meta);
        gui.setItem(gui.firstEmpty(), slot);
    }
    
    public void CreatePanes()
    {
    	ItemStack slot = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
    	ItemMeta meta = slot.getItemMeta();
    	meta.setDisplayName("");
    	slot.setItemMeta(meta);
    	for (int i=0;i<9;i++)
    	gui.setItem(i, slot);
    	gui.setItem(9, slot);
    	gui.setItem(17, slot);
    	gui.setItem(18, slot);
    	gui.setItem(26, slot);
    	for (int i=27;i<36;i++)
        	gui.setItem(i, slot);
    }
    
    public Integer getCost(String type, Enchantment enchant)
    {
    	Integer enchlvl = p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant);
    	if (type.equalsIgnoreCase("manual"))
    	{
    		return plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost."+(enchlvl+1));
    	}
    	else if (type.equalsIgnoreCase("linear"))
    	{
    		Integer startvalue = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost.startvalue");
    		Integer value = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost.value");
    		return (startvalue+value*enchlvl-value);
    	}
    	else if (type.equalsIgnoreCase("exponential"))
    	{
    		Double multi = plugin.config.getDouble("enchants."+enchant.getKey().getKey()+".cost.multi");
    		Integer startvalue = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost.startvalue");
    		return (int)(startvalue*(Math.pow(multi, enchlvl)));
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    public Integer getCost(String type, Enchantment enchant, Integer multi)
    {
    	Integer enchlvl = p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant);
    	if (type.equalsIgnoreCase("manual"))
    	{
    		Integer val = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost."+(enchlvl+1));
    		for (int i=1;i<multi;i++)
    		{
    			val = val + plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost."+(enchlvl+1+i));
    		}
    		return val;
    	}
    	else if (type.equalsIgnoreCase("linear"))
    	{
    		Integer startvalue = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost.startvalue");
    		Integer value = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost.value");
    		return (startvalue+(value*enchlvl-value)*multi);
    	}
    	else if (type.equalsIgnoreCase("exponential"))
    	{
    		Double multi1 = plugin.config.getDouble("enchants."+enchant.getKey().getKey()+".cost.multi");
    		Integer startvalue = plugin.config.getInt("enchants."+enchant.getKey().getKey()+".cost.startvalue");
    		return (int)(startvalue*Math.pow((Math.pow(multi1, enchlvl)), multi));
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    public void UpgradeItem(Enchantment enchant, Player p, Integer multi)
    {
    	String method = plugin.config.getString("enchants."+enchant.getKey().getKey()+".cost.type");
    	Integer cost = (getCost(method, enchant, multi));
    	if (!(p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant)+multi-1 >= plugin.config.getInt("enchants."+enchant.getKey().getKey()+".maxlevel")))
    	{
    		if (InternalTokenManager.GetTokens(p.getName()) >= cost)
            {
	        	p.sendMessage(plugin.translatebukkittext("messages.upgradedpickaxe"));
	        	Integer newvalue = InternalTokenManager.GetTokens(p.getName()) - cost;
	        	InternalTokenManager.SetTokens(p.getName(), newvalue);
	        	p.getInventory().getItemInMainHand().addUnsafeEnchantment(enchant, p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant)+multi);
	        	ItemMeta meta = EnchantMetaWriter.getWrittenEnchantLore(p.getInventory().getItemInMainHand());
	        	p.getInventory().getItemInMainHand().setItemMeta(meta);
	        	clearInventory();
	        	LoadInventory(p, currentgui);
            }
	        else
	        {
	        	p.sendMessage(plugin.translatebukkittext("messages.insufficienttokens"));
	        }
    	}
    	else if ((p.getInventory().getItemInMainHand().getEnchantmentLevel(enchant) >= plugin.config.getInt("enchants."+enchant.getKey().getKey()+".maxlevel")))
    	{
    		p.sendMessage(plugin.translatebukkittext("messages.exceedmaxlvl"));
    	}
    	else
    	{
    		p.sendMessage(plugin.translatebukkittext("messages.exceedmaxlvl"));
    	}

    }
}
