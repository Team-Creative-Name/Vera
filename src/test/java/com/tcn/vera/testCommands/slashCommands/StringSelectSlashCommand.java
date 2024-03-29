/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2023 Thomas Wessel and the rest of Team Creative Name
 *
 *
 * This library is licensed under the GNU Lesser General Public License v2.1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 *
 * For more information, please check out the original repository of this project on github
 * https://github.com/Team-Creative-Name/Vera
 */
package com.tcn.vera.testCommands.slashCommands;

import com.tcn.vera.commands.templates.SlashCommandTemplate;
import com.tcn.vera.commands.interactions.StringSelectInterface;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class StringSelectSlashCommand extends SlashCommandTemplate implements StringSelectInterface {

    StringSelectMenu menu = StringSelectMenu.create("bob2").addOption("test1", "test1").addOption("test2", "test2").build();

    public StringSelectSlashCommand(){
        this.commandName = "string-select-test";
        this.help = "A command to test String selections";
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        event.reply("Please select the user you'd like the ping").addComponents(ActionRow.of(menu)).queue();

    }

    @Override
    public void executeStringSelectInteraction(StringSelectInteractionEvent event) {
        event.reply("You chose: " + event.getInteraction()).queue();
    }

    @Override
    public StringSelectMenu getMenu() {
        return menu;
    }
}
