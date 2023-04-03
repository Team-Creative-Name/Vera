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
import com.tcn.vera.commands.interactions.ButtonInterface;
import com.tcn.vera.utils.VeraUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;


public class ButtonSlashCommand extends SlashCommandTemplate implements ButtonInterface {

    public ButtonSlashCommand(){
        this.commandName = "button-test";
        this.help = "This command tests buttons!";
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        event.reply("Press a button and I'll tell you what was pressed.")
                .addActionRow(
                        Button.primary(getButtonClassID() + ":primary", "Primary Button"),
                        Button.success(getButtonClassID() + ":success", "Success Button"),
                        Button.secondary(getButtonClassID() + ":secondary", "Secondary Button"),
                        Button.danger(getButtonClassID() + ":danger", "DANGER BUTTON")
                ).queue();
    }

    @Override
    public void executeButton(ButtonInteractionEvent event) {

        event.deferEdit().queue();

        switch (VeraUtils.getButtonName(event)) {
            case "primary" -> {
                event.getMessage().editMessage("You pressed the primary button").setComponents().queue();
            }
            case "success" -> {
                event.getMessage().editMessage("You pressed the success button").setComponents().queue();
            }
            case "secondary" -> {
                event.getMessage().editMessage("You pressed the secondary button").setComponents().queue();
            }
            case "danger" -> {
                event.getMessage().editMessage("You pressed the danger button").setComponents().queue();
            }
        }
    }

    @Override
    public String getButtonClassID() {
        return "ButtonSlashCommand";
    }

}
