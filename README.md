# CommandAPI

# Javadocs
[master](https://jitpack.io/com/github/DiscordBolt/CommandAPI/master-SNAPSHOT/javadoc/index.html)  
[v2](https://jitpack.io/com/github/DiscordBolt/CommandAPI/v2-SNAPSHOT/javadoc/index.html)

# Initialization
```java
CommandManager commandManager = new CommandManager(discordClient, "my.toplevel.package");
```

# Registering Commands
Methods annotated with `@BotCommand` are automatically registered.  
Commands created via extending `CustomCommand` must be registered by hand.  
```java
commandManager.registerCommand(new HelpCommand());
```

# Setting Command Prefixes
Commands by default are prefixed by `!` but can be cutomized per guild.  
Prefixes are not persistent and must be set on restart.  
```java
commandManager.setCommandPrefix(guildID, "$");
```

# Examples

### `@BotCommand` Annotation Example
These methods must be `public` and `static` in order to be registered.   
They will be found automatically and regestered when you create `CommandManager`  
```java
@BotCommand(command = "ping", description = "ping", usage = "ping", module = "misc")
public static void ping(CommandContext context) {
    context.replyWith("Pong!").subscribe();
}
```

### `CustomCommand` Class Example
```java
public class HelpCommand extends CustomCommand {

    private static String[] command = {"help"};
    private static String description = "View all commands";
    private static String usage = "help";
    private static String module = "Command API";

    public HelpCommand() {
        super(command, description, usage, module);
        super.setAliases("h");
    }

    @Override
    public void execute(CommandContext cc) {
        cc.replyWith("Command List:").subscribe();
    }
}
```
Or with no details for the !Help command:
```java
public class HelpCommand extends CustomCommand {

    private static String[] command = {"help"};

    public HelpCommand() {
        super(command);
        super.setAliases("h");
    }

    @Override
    public void execute(CommandContext cc) {
        cc.replyWith("Command List:").subscribe();
    }
}
```
