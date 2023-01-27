/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022-23 Thomas Wessel and the rest of Team Creative Name
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
package com.tcn.vera.testCommands.userContext;

import com.tcn.vera.commands.UserContextTemplate;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class BasicUserContextCommand extends UserContextTemplate {

    public BasicUserContextCommand() {
        this.commandName = "usercontextcommandexample";
        this.help = "Tests the ability to use use context commands";
    }

    @Override
    public void executeUserContextCommand(UserContextInteractionEvent event) {
        event.reply("This is an example response for the User Context command!").queue();
    }
}
