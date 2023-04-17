package com.tcn.vera.commands.builtin;

import com.tcn.vera.commands.templates.ChatCommandTemplate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;

public class chatHelpCommand extends ChatCommandTemplate {
    private final Set<ChatCommandTemplate> commandList;
    private final String prefix;

    public chatHelpCommand(Set<ChatCommandTemplate> commandList, String prefix) {
        this.commandName = "help";
        this.aliases = new String[]{"h", "commands", "cmds"};
        this.help = "Shows this help message containing all chat commands and their descriptions";
        this.isOwnerCommand = false;

        this.commandList = commandList;
        this.prefix = prefix;
    }


    @Override
    public void executeChatCommand(MessageReceivedEvent event, Message message, String messageContent) {
        //if there are no chat commands, return
        if (commandList.isEmpty()) {
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Chat Command Help:")
                .setColor(0x00ff00);

        commandList.forEach(command -> embedBuilder.addField(prefix + command.getCommandName(), command.getCommandHelp(), false));

        message.reply("").addEmbeds(embedBuilder.build()).queue();

    }
}
