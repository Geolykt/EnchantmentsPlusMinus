package org.whyisthisnecessary.eps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.whyisthisnecessary.eps.internal.InternalTokenManager;
import org.whyisthisnecessary.eps.internal.Metrics;
import org.whyisthisnecessary.eps.internal.PayTokensCaller;
import org.whyisthisnecessary.eps.internal.PlaceholderAPIHook;
import org.whyisthisnecessary.eps.internal.ScrapCaller;
import org.whyisthisnecessary.eps.internal.GetTokensCaller;
import org.whyisthisnecessary.eps.internal.EnchantGUICaller;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
    
	private Main plugin;
	private File DataFolder;
	private File PackFolder;
	private File LangFile;
	private File userstore;
	public FileConfiguration config;
	private FileConfiguration usconfig;
	private FileConfiguration lang;
	private List<String> list;
	public List<Material> fortuneapply = new ArrayList<Material>(Arrays.asList());
	private List<Plugin> packs = new ArrayList<Plugin>(Arrays.asList());
	
	@Override
	public void onEnable() 
	{
    	plugin = this;
   
    	saveDefaultConfig();

	    Bukkit.getPluginManager().registerEvents(this, plugin);
		DataFolder = new File(getDataFolder(), "data");
        if (!DataFolder.exists()) {
            DataFolder.mkdirs();
         }
        LangFile = new File(getDataFolder(), "lang.yml");
        if (!LangFile.exists())
        {
            try {
				LangFile.createNewFile();
		        copyFile("/lang.yml", LangFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        lang = YamlConfiguration.loadConfiguration(LangFile);
        
        PackFolder = new File(getDataFolder(), "packs");
        
        userstore = new File(DataFolder, "usernamestore.yml");
	    if (!userstore.exists()) {
	        userstore.getParentFile().mkdirs();
	        try {
				userstore.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	     }
	    usconfig = YamlConfiguration.loadConfiguration(userstore);
	    config = plugin.getConfig();
	    EnchantHandler.config = config;
	    EnchantHandler.lang = lang;
	    EnchantHandler.configFile = new File(getDataFolder(), "config.yml");
	    EnchantHandler.langFile = new File(getDataFolder(), "lang.yml");
	    
	    plugin.getCommand("eps").setExecutor(this);
	    plugin.getCommand("tokens").setExecutor(new GetTokensCaller(this));
    	plugin.getCommand("enchants").setExecutor(new EnchantGUICaller(this));
    	plugin.getCommand("scrap").setExecutor(new ScrapCaller(this));
    	plugin.getCommand("paytokens").setExecutor(new PayTokensCaller(this));
    	
    	new InternalTokenManager(this);
    	
    	if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
    	new PlaceholderAPIHook();
    	
    	int pluginId = 9735;
        new Metrics(this, pluginId);
    	
    	list = config.getStringList("misc.applyfortuneon");
    	for (String i : list)
    	fortuneapply.add(Material.getMaterial(i));
    	
    	if (!PackFolder.exists()) {
            PackFolder.mkdirs();
			downloadPack("AutoUpdater", "https://github.com/dsdd/EnchantmentsPlusMinus/raw/main/Packs/AutoUpdater.jar");
			downloadPack("TokenRewards", "https://github.com/dsdd/EnchantmentsPlusMinus/raw/main/Packs/TokenRewards.jar");
        }
    	else
    	{
    		File[] files = PackFolder.listFiles();
    	    if (files != null)
    	    {
    	    	for (File file : files)
    	    	{
    	    		if (file.getName().equalsIgnoreCase("PickaxePack.jar") || file.getName().equalsIgnoreCase("PVPPack.jar"))
    	    		{
    	    			file.delete();
    	    		}
    	    		else {
					try {
						Plugin plugin1 = Bukkit.getPluginManager().loadPlugin(file);
						Bukkit.getPluginManager().enablePlugin(plugin1);
						packs.add(plugin1);
					} catch (Exception e) {
					}
    	    	}
    	    	}

    	    }
    	}
    	new BuiltInPackParser(this);
	}
	
	@Override
	public void onDisable()
	{
		for (Plugin pl : packs)
		{
			Bukkit.getPluginManager().disablePlugin(pl);
		}
	}
    
    @EventHandler
	public void onJoin(PlayerJoinEvent e) 
	{
		Player p = e.getPlayer();

		UUID uuid = p.getUniqueId();
		String stringuuid = uuid.toString();
			
		File datafile = new File(DataFolder, stringuuid+".yml");
	    if (!datafile.exists()) {
	        datafile.getParentFile().mkdirs();
	        try {
				datafile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	     }
	        
	    FileConfiguration dfconfig = YamlConfiguration.loadConfiguration(datafile);
	        
		addDefault(dfconfig, "uuid", stringuuid);
		addDefault(dfconfig, "player-name", p.getName());
		addDefault(dfconfig, "tokens", 0);
		usconfig.set(p.getName(), stringuuid);
		
		try {
			dfconfig.save(datafile);
			usconfig.save(userstore);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (p.isOp())
		{
			p.sendMessage(ChatColor.YELLOW + "Check for new updates here: https://www.spigotmc.org/resources/enchantments.86901/");
		}
	}
	
	private void addDefault(FileConfiguration config, String key, Object value)
	{
		if (config.get(key) == null)
		{
			config.set(key, value);
		} 
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (args.length == 0)
		{
			sender.sendMessage(ChatColor.RED + "Usage:");
			sender.sendMessage(ChatColor.RED + "/eps reload");
			sender.sendMessage(ChatColor.RED + "/eps settokens [plr] [amount]");
			sender.sendMessage(ChatColor.RED + "/eps changetokens [plr] [amount]");
			sender.sendMessage(ChatColor.RED + "/eps enchant [enchant] [lvl]");
			sender.sendMessage(ChatColor.RED + "/eps reloadpack [packname]");
			return false;
		}
		
		if (args[0].equalsIgnoreCase("reload"))
		{
			String perm = "eps.admin.reload";
			if (!sender.hasPermission(perm))
			{
				sender.sendMessage(translatebukkittext("insufficientpermission"));
				return false;
			}
			reloadConfig();
			config = plugin.getConfig();
			usconfig = YamlConfiguration.loadConfiguration(userstore);
			lang = YamlConfiguration.loadConfiguration(LangFile);
			EnchantHandler.config = config;
		    EnchantHandler.lang = lang;
		    EnchantHandler.configFile = new File(getDataFolder(), "config.yml");
		    EnchantHandler.langFile = new File(getDataFolder(), "lang.yml");
			File[] files = PackFolder.listFiles();
    	    if (files != null)
    	    {
    	    	for (File file : files)
    	    	{
					try {
						String filename = file.getName().replaceFirst("[.][^.]+$", "");
						Plugin plugin1 = getPluginByName(filename);
						Bukkit.getPluginManager().disablePlugin(plugin1);
						Bukkit.getPluginManager().enablePlugin(plugin1);
					} catch (Exception e) {
					}
    	    	}

    	    }
			list = config.getStringList("misc.applyfortuneon");
			for (String i : list)
	    	fortuneapply.add(Material.getMaterial(i));
			sender.sendMessage(translatebukkittext("messages.reloadconfig"));
			return false;
		}
		
		if (args[0].equalsIgnoreCase("settokens"))
		{
			String perm = "eps.admin.settokens";
			if (!sender.hasPermission(perm))
			{
				sender.sendMessage(translatebukkittext("insufficientpermission"));
				return false;
			}
			if (args.length < 3)
			{
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /eps settokens [player] [amount]"));
				return true;
			}
			try
			{
			InternalTokenManager.SetTokens(args[1],Integer.parseInt(args[2]));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSet "+args[1]+"'s tokens to "+Integer.parseInt(args[2])));
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /eps settokens [player] [amount]"));
			}
			return false;
		}
		
		if (args[0].equalsIgnoreCase("changetokens"))
		{
			String perm = "eps.admin.changetokens";
			if (!sender.hasPermission(perm))
			{
				sender.sendMessage(translatebukkittext("insufficientpermission"));
				return false;
			}
			if (args.length < 3)
			{
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /eps changetokens [player] [amount]"));
				return true;
			}
			try
			{
			InternalTokenManager.ChangeTokens(args[1],Integer.parseInt(args[2]));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aChanged "+args[1]+"'s tokens by "+Integer.parseInt(args[2])));
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /eps changetokens [player] [amount]"));
			}
			return false;
		}
		
		if (args[0].equalsIgnoreCase("enchant"))
		{
			String perm = "eps.admin.enchant";
			
			if (!(sender instanceof Player))
			{
				sender.sendMessage(translatebukkittext("messages.invalidplayertype"));
				return true;
			}
			
			Player p = (Player) sender;
			
			if (p.hasPermission(perm))
	        {
				if (args.length < 3)
				{
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cUsage: /eps enchant [enchant] [lvl]"));
					return true;
				}
				if (p.getInventory().getItemInMainHand().getAmount() > 0)
				{
					p.getInventory().getItemInMainHand().addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(args[1])), Integer.parseInt(args[2]));
					ItemMeta meta = EnchantMetaWriter.getWrittenEnchantLore(p.getInventory().getItemInMainHand());
		        	p.getInventory().getItemInMainHand().setItemMeta(meta);
					return true;
				}
				else
				{
					p.sendMessage(translatebukkittext("messages.invaliditem"));
					return false;
				}
	        }
			else
			{
				p.sendMessage(translatebukkittext("messages.insufficientpermission"));
				return false;
			}
		}
		if (args[0].equalsIgnoreCase("loadpack"))
		{
			if (sender.hasPermission("eps.admin.loadpack"))
			{
			try {
			    Plugin pl1 = Bukkit.getPluginManager().loadPlugin(new File(PackFolder, args[1]+".jar"));
			    Bukkit.getPluginManager().enablePlugin(pl1);
			    packs.add(pl1);
			    sender.sendMessage(ChatColor.GREEN + "Loaded pack!");
			    }
			    catch (Exception e)
			    {
			    	e.printStackTrace();
			    }
			}
			else
			{
				sender.sendMessage(translatebukkittext("messages.insufficientpermission"));
				return false;
			}
		}
		if (args[0].equalsIgnoreCase("reloadpack"))
		{
			if (sender.hasPermission("eps.admin.reloadpack"))
			{
			try {
		    Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);	
		    Bukkit.getPluginManager().disablePlugin(pl);
		    Plugin pl1 = Bukkit.getPluginManager().loadPlugin(getJarFile(pl));
		    Bukkit.getPluginManager().enablePlugin(pl1);
		    sender.sendMessage(ChatColor.GREEN + "Reloaded pack!");
		    }
		    catch (Exception e)
		    {
		    	e.printStackTrace();
		    }
		    }
			else
			{
				sender.sendMessage(translatebukkittext("messages.insufficientpermission"));
				return false;
			}
		}
		
		return false;
	}
	
	public String translatebukkittext(String text)
	{
		return ChatColor.translateAlternateColorCodes('&',lang.getString("prefix")) + ChatColor.translateAlternateColorCodes('&', lang.getString(text));
	}
	
	private File downloadFile(String localFileName, String fromUrl) { try {
	    File localFile = new File(localFileName);
	    if (!localFile.exists()) {
	    	localFile.createNewFile();
	    }
	    URL url = new URL(fromUrl);
	    OutputStream out = new BufferedOutputStream(new FileOutputStream(localFileName));
	    URLConnection conn = url.openConnection();
	    ((HttpURLConnection) conn).setRequestMethod("GET"); 
	    conn.setRequestProperty("User-Agent", "  Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
	    conn.connect();
	    InputStream in = conn.getInputStream();
	    byte[] buffer = new byte[16384];

	    int numRead;
	    while ((numRead = in.read(buffer)) != -1) {
	        out.write(buffer, 0, numRead);
	    }
	    if (in != null) {
	        in.close();
	    }
	    if (out != null) {
	        out.close();
	    }
	    out.flush();
	    
	    return localFile;   } catch (Exception e){return null;}
	}
	
	private void copyFile(String str, File dest) {
		try {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = this.getClass().getResourceAsStream(str);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[16384];
	        int length;
	        while ((length = is.read(buffer)) != -1) {
	            os.write(buffer, 0, length);
	        }
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	    finally {
	        is.close();
	        os.close();
	    }
	}
	catch(IOException e) {}
	}
	
	private static Plugin getPluginByName(String name) {
	    for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
	      if (name.equalsIgnoreCase(plugin.getName()))
	        return plugin; 
	    } 
	    return null;
	  }
	
	private void downloadPack(String name, String url)
	{
		File file = downloadFile(PackFolder.getPath()+"/"+name+".jar", url); try{        
			Plugin plugin1 = Bukkit.getPluginManager().loadPlugin(file);
			Bukkit.getPluginManager().enablePlugin(plugin1); } catch (Exception e) {}
	}
	
	private File getJarFile(Plugin pl) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		JavaPlugin plugin = (JavaPlugin) getServer().getPluginManager().getPlugin(pl.getName());
		Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
		getFileMethod.setAccessible(true);
		File file = (File) getFileMethod.invoke(plugin);
		return file;
	}
}
