package com.tcn.vera.testCommands.slashCommands;

import com.tcn.vera.commands.SlashCommandTemplate;
import com.tcn.vera.interactions.ButtonInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;


public class ButtonSlashCommand extends SlashCommandTemplate implements ButtonInterface {

    public ButtonSlashCommand(){
        this.commandName = "button-test";
        this.help = "This command tests buttonnnnnssss!";
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        event.reply("Press a button and I'll do something (if this works)")
                .addActionRow(
                        Button.primary(getButtonClassID() + ":primary", "Primary Button"),
                        Button.success(getButtonClassID() + ":success", "Success Button"),
                        Button.secondary(getButtonClassID() + ":secondary", "Secondary Button"),
                        Button.danger(getButtonClassID() + ":danger", "DANGER BUTTON")
                ).queue();
    }

    @Override
    public void executeButton(ButtonInteractionEvent event) {
        event.getMessage().editMessage("This button goes away now").setComponents().queue();
    }

    @Override
    public String getButtonClassID() {
        return "ButtonSlashCommand";
    }

}
