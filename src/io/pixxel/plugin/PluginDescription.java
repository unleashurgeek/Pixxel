package io.pixxel.plugin;

import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ImmutableDescriptor;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;



public final class PluginDescription {
	private static final Yaml yaml = new Yaml(new SafeConstructor());
	
	// ---- PluginDescription Vars ----
	private String name = null;
	private String main = null;
	private List<String> depend = null;
	private List<String> softDepend = null;
	private List<String> loadBefore = null;
	private String version = null;
	private Map<String, Map<String, Object>> commands = null;
	private String description = null;
	private List<String> authors = null;
	private String website = null;
	private String prefix  = null;
	private List<Permission> permissions = null;
	private Map<?, ?> simplePermissions = null;
	private PermissionDefault defaultPerm = PermissionDefault.OP;
	
	public PluginDescription(final InputStream stream) {
		loadMap(asMap(yaml.load(stream)));
	}
	
	public PluginDescription(final String pluginName, final String pluginVersion, final String mainClass) {
		name = pluginName;
		version = pluginVersion;
		main = mainClass;
	}
	
	public void save(Writer writer) {
		yaml.dump(saveMap(), writer);
	}
	
	private void loadMap(Map<?, ?> map) {
		try {
			name = map.get("name").toString();
			
			if (!name.matches("^[A-Za-z0-9 _.-]+$")) {
				System.out.println("Name in plugin.yml contains invalid characters!");
			}
		} catch (NullPointerException e) {
			System.out.println("Name is Null!");
		} 
		
		try {
			version = map.get("version").toString();
		} catch (NullPointerException e) {
			System.out.println("Version is not defined!");
		}
		
		try {
			main = map.get("main").toString();
			if (main.startsWith("io.pixxel")) {
				System.out.println("Package can not start with io.pixxel!");
			}
		} catch (NullPointerException e) {
			System.out.println("Main is Null!");
		}
		
		if (map.get("commands") != null) {
			Map<String, Map<String, Object>> commandsMap = new HashMap<>();
			
			try {
				for (Map.Entry<?, ?> command : ((Map<?, ?>) map.get("commands")).entrySet()) {
					Map<String, Object> commandMap = new HashMap<>();
					if (command.getValue() != null) {
						for (Map.Entry<?, ?> commandEntry : ((Map<?, ?>) command.getValue()).entrySet()) {
							if (commandEntry.getValue() instanceof Iterable) {
								List<Object> commandSubList = new ArrayList<>();
								for (Object commandSubListItem : (Iterable<?>) commandEntry.getValue()) {
									if (commandSubListItem != null) {
										commandSubList.add(commandSubListItem);
									}
								}
								
								commandMap.put(commandEntry.getKey().toString(), commandSubList);
							} else if (commandEntry.getValue() != null) {
								commandMap.put(commandEntry.getKey().toString(), commandEntry.getValue());
							}
						}
					}
					commandsMap.put(command.getKey().toString(), commandMap);
				}
			} catch (ClassCastException e) {
				System.out.println("Commands are of wrong type");
			}
			
			commands = commandsMap;
		}
		
		if (map.get("website") != null) {
			website = map.get("website").toString();
		}
		
		if (map.get("description") != null) {
			description = map.get("description").toString();
		}
		
		if (map.get("prefix") != null) {
			prefix = map.get("prefix").toString();
		}
		
		if(map.get("depend") != null) {
			List<String> dependent = new ArrayList<String>();
			try {
				for (Object dependency : (Iterable<?>) map.get("depend")) {
					dependent.add(dependency.toString());
				}
			} catch (ClassCastException e) {
				System.out.println("Depend is not a string!");
			} catch (NullPointerException e) {
				System.out.println("Invalid Dependency Format");
			}
			depend = dependent;
		}
		
		if(map.get("softdepend") != null) {
			List<String> dependent = new ArrayList<String>();
			try {
				for (Object dependency : (Iterable<?>) map.get("softdepend")) {
					dependent.add(dependency.toString());
				}
			} catch (ClassCastException e) {
				System.out.println("softdepend is not a string!");
			} catch (NullPointerException e) {
				System.out.println("Invalid Soft-Dependency Format");
			}
			softDepend = dependent;
		}
		
		if (map.get("loadBefore") != null) {
			List<String> loadBeforeList = new ArrayList<String>();
			try {
				for (Object dependency : (Iterable<?>) map.get("loadBefore")) {
					loadBeforeList.add(dependency.toString());
				}
			} catch (ClassCastException e) {
				System.out.println("loadbefore is not a string!");
			} catch (NullPointerException e) {
				System.out.println("Invalid load-before Format");
			}
			loadBefore = loadBeforeList;
		}
		
		if (map.get("authors") != null) {
			List<String> authorsMap = new ArrayList<>();
			try {
				for (Object o : (Iterable<?>) map.get("authors")) {
					authorsMap.add(o.toString());
				}
			} catch (ClassCastException e) {
				System.out.println("Authors are the wrong type");
			} catch (NullPointerException e) {
				System.out.println("Authors are improperly defined!");
			}
			authors = authorsMap;
		} else if (map.get("author") != null) {
			authors = Arrays.asList(new String[]{map.get("author").toString()});
		} else {
			authors = Arrays.asList(new String[]{"Kyle Bartz"});
		}
		
		if (map.get("default-permission") != null) {
			try {
				defaultPerm = PermissionDefault.getByName(map.get("default-permission").toString());
			} catch (ClassCastException e) {
				System.out.println("Default-permission is of wrong type");
			} catch (IllegalArgumentException e) {
				System.out.println("default-permission is not a valid choice");
			}
		}
		
		try {
			simplePermissions = (Map<?, ?>) map.get("permissions");
		} catch (ClassCastException e) {
			System.out.println("permissions are of the wrong type");
		}
	}
	
	private Map<String, Object> saveMap() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("name", name);
		map.put("main", main);
		map.put("version", version);
		map.put("default-permission", defaultPerm.toString());
		
		 if (commands != null) {
			 map.put("command", commands);
	     }
	    
		 if (depend != null) {
	         map.put("depend", depend);
	     }
	     
	     if (softDepend != null) {
	    	 map.put("softdepend", softDepend);
	     }
	     
	     if (website != null) {
	    	 map.put("website", website);
	     }
	     
	     if (description != null) {
	         map.put("description", description);
	     }
	     
	     if (authors.size() == 1) {
	    	 map.put("author", authors.get(0));
	     } else if (authors.size() > 1) {
	         map.put("authors", authors);
	     }

	     if (prefix != null) {
	    	 map.put("prefix", prefix);
	     }

	     return map;
	}
	
	
	private Map<?, ?> asMap(Object object) {
		if (object instanceof Map) {
			return (Map<?, ?>) object;
		}
		
		System.out.println("Plugin.yml is not properly formated!");
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getMain() {
		return main;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<String> getAuthors() {
		return authors;
	}
	
	public String getWebsite() {
		return website;
	}
	
	public List<String> getDepend() {
		return depend;
	}
	
	public List<String> getSoftDepend() {
		return softDepend;
	}
	
	public List<String> getLoadBefore() {
		return loadBefore;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public Map<String, Map<String, Object>> getCommands() {
		return commands;
	}
	
	public List<Permission> getPermissions() {
		if (permissions == null) {
			if (simplePermissions == null) {
				permissions = new ArrayList<>();
			} else {
				permissions = Permission.loadPermissions(simplePermissions, "Permission node '%s' in plugin description file for " + getFullName() + " is invalid", defaultPerm);
			}
		}
		return permissions;
	}
	
	public PermissionsDefault getPermissionDefault() {
		return defaultPerm;
	}
	
	public String getFullName() {
		return name + " v" + version;
	}
	
	
}
