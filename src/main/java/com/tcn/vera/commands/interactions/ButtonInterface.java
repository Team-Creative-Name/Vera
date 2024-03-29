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
package com.tcn.vera.commands.interactions;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * This interface allows the addition of buttons to a {@link com.tcn.vera.commands.templates.SlashCommandTemplate Slash Command}
 * or a {@link com.tcn.vera.commands.templates.ChatCommandTemplate Chat Command}.
 */
public interface ButtonInterface {

    /**
     * The code to be executed when the user presses a button. Every button defined by the command should be handled here.
     * <br><br>
     * Vera provides a {@link com.tcn.vera.utils.VeraUtils#getButtonName(ButtonInteractionEvent)} function that can be used to
     * determine the name of the button that was pressed.
     * <br><br>
     * We recommend using something similar to the following code:
     * <blockquote><pre>
     *     public void executeButton(ButtonInteractionEvent event) {
     *
     *         event.deferEdit().queue();
     *
     *         switch (VeraUtils.getButtonName(event)) {
     *             case "buttonOne" -> {
     *                 event.getMessage().editMessage("You pressed the first button!").setComponents().queue();
     *             }
     *             case "buttonTwo" -> {
     *                 event.getMessage().editMessage("You pressed the second button").setComponents().queue();
     *             }
     *         }
     *     }
     *   </pre></blockquote>
     *
     * @param event The {@link ButtonInteractionEvent} sent by discord
     */
    void executeButton(ButtonInteractionEvent event);

    /**
     * The class ID of the button used to identify which class registered the command. This string must be unique
     * to each command but has no other requirements. We recommend using the name of the class.
     *
     * @return The class ID of the button
     */
    String getButtonClassID();

}
