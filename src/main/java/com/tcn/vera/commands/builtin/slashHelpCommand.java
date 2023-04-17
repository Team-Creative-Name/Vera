package com.tcn.vera.commands.builtin;

import com.tcn.vera.commands.templates.SlashCommandTemplate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Set;

public class slashHelpCommand extends SlashCommandTemplate {
    private final Set<SlashCommandTemplate> commandList;

    public slashHelpCommand(Set<SlashCommandTemplate> commandList) {
        this.commandName = "help";
        this.help = "Shows this help message containing all slash commands and their descriptions";

        this.commandList = commandList;

    }
    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        //discord forces us to acknowledge the slash command, so we have to reply even if we don't have any commands
        if (commandList.isEmpty()) {
            event.reply("Sorry, this bot does not support any slash commands").queue();
            return;
        }

        event.deferReply().queue();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Slash Command Help:")
                .setColor(0x00ff00);

        commandList.forEach(command -> embedBuilder.addField(command.getCommandName(), command.getCommandHelp(), false));

        event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
    }
}
