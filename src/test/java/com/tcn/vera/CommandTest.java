/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022 Thomas Wessel and the rest of Team Creative Name
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
package com.tcn.vera;

import com.tcn.vera.commands.CommandTemplate;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandTest extends CommandTemplate {

    public CommandTest(){
        this.aliases = new String[]{"one", "two", "three"};
        this.commandName = "commandtest";
    }


    @Override
    public void executeChatCommand(MessageReceivedEvent event, Message message, String content) {
        event.getMessage().reply("Yes, a command exists!").queue();
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        event.reply("YES! slash commands seem to work!").queue();
    }
}