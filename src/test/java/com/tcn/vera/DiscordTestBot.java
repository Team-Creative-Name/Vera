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

import com.tcn.vera.eventHandlers.ButtonHandler;
import com.tcn.vera.eventHandlers.CommandHandler;
import com.tcn.vera.eventHandlers.CommandHandlerBuilder;
import com.tcn.vera.testCommands.chatCommands.BasicChatCommand;
import com.tcn.vera.testCommands.messageContext.BasicMessageContextCommand;
import com.tcn.vera.testCommands.paginatorCommands.EmbedPaginatorCommand;
import com.tcn.vera.testCommands.paginatorCommands.EmbedPaginatorMessage;
import com.tcn.vera.testCommands.slashCommands.*;
import com.tcn.vera.testCommands.userContext.BasicUserContextCommand;
import com.tcn.vera.testCommands.userContext.UserContextPing;
import com.tcn.vera.testCommands.userContext.UserContextStringSelectInterface;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordTestBot {


    public static void main(String[] args) {

        JDA discordBot = JDABuilder.createDefault(Secrets.discordToken).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        ButtonHandler buttonHandler = new ButtonHandler();

        CommandHandler veraHandler = new CommandHandlerBuilder()
                .addOwner(Secrets.ownerID)
                .addCommand(new BasicChatCommand())
                .addCommand(new BasicSlashCommand())
                .addCommand(new BasicUserContextCommand())
                .addCommand(new BasicMessageContextCommand())
                .addCommand(new AutocompleteSlashCommand())
                .addCommand(new ModalSlashCommand())
                .addCommand(new EntitySelectSlashCommand())
                .addCommand(new UserContextPing())
                .addCommand(new StringSelectSlashCommand())
                .addCommand(new UserContextStringSelectInterface())
                .addCommand(new ButtonSlashCommand())
                .addCommand(new EmbedPaginatorCommand(buttonHandler))
                .addCommand(new EmbedPaginatorMessage(buttonHandler))
                .changePrefix("!")
                .addButtonHandler(buttonHandler)
                .build();


        discordBot.addEventListener(veraHandler);

        System.out.println("All ready");
    }

}
