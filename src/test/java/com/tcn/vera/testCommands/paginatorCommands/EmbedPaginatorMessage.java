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
package com.tcn.vera.testCommands.paginatorCommands;

import com.tcn.vera.commands.templates.ChatCommandTemplate;
import com.tcn.vera.eventHandlers.ButtonHandler;
import com.tcn.vera.pagination.EmbedPaginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class EmbedPaginatorMessage extends ChatCommandTemplate {

    final ButtonHandler buttonHandler;

    public EmbedPaginatorMessage(ButtonHandler buttonHandler){
        this.buttonHandler = buttonHandler;
        this.commandName = "messagepage";
    }
    @Override
    public void executeChatCommand(MessageReceivedEvent event, Message message, String messageContent) {

        EmbedPaginator paginator = new EmbedPaginator.Builder()
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEmbeds(generateRandomEmbed())
                .setEvent(event)
                .setUserID(event.getAuthor().getIdLong())
                .setButtonHandler(buttonHandler)
                .build();

        paginator.paginate();
    }

    private MessageEmbed generateRandomEmbed(){
        return new EmbedBuilder()
                .setTitle("Random Embed " + Math.floor(Math.random() * 100))
                .setImage("https://source.unsplash.com/random/200x200?sig=" + Math.floor(Math.random() * 1000))
                .setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                        " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris" +
                        " nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit" +
                        " esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt" +
                        " in culpa qui officia deserunt mollit anim id est laborum.")
                .setFooter("This embed was generated randomly. The image was pulled randomly for unsplash.com")
                .build();
    }
}
