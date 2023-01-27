package com.tcn.vera.testCommands.userContext;

import com.tcn.vera.commands.UserContextTemplate;
import com.tcn.vera.interactions.StringSelect;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class UserContextStringSelect extends UserContextTemplate implements StringSelect {

    StringSelectMenu menu = StringSelectMenu.create("bob2").addOption("test1", "test1").addOption("test2", "test2").build();


    public UserContextStringSelect(){
        this.commandName = "test";
    }

    @Override
    public void executeUserContextCommand(UserContextInteractionEvent event) {
        event.reply("Please select the user you'd like the ping").addComponents(ActionRow.of(menu)).queue();
    }

    @Override
    public void executeStringSelectInteraction(StringSelectInteractionEvent event) {
        event.reply("You chose: " + event.getInteraction().toString()).queue();
    }

    @Override
    public StringSelectMenu getMenu() {
        return menu;
    }
}
