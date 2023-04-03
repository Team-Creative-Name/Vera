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

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;

/**
 * Enables a command to accept a {@link ModalInteractionEvent} from JDA. In order for Vera to match the modal in the event
 * to the one supplied by your command, the getModal command *must* return the modal your command generates.
 * <p>
 * Please note that only slash and context commands have the ability to generate and execute modal commands. Do not try to
 * implement this interface with a chat command as it WILL NOT work.
 */
public interface ModalInterface {

    /**
     * The code that executes when the user submits the modal.
     *
     * @param event The {@link ModalInteractionEvent} fired by discord when the user attempts to submit a modal
     * @implNote Discord requires a modal to be acknowledged in order for the modal to close via the submit button. Be sure to do so even if
     * you have no intention on acting upon the modal.
     * <p>
     * If your command fails to complete execution (crashes), Vera will attempt to handle the event to ensure that the user is not
     * stuck with a broken modal.
     */
    void executeModal(ModalInteractionEvent event);

    /**
     * In order for the {@link com.tcn.vera.eventHandlers.CommandHandler} to match a modal event to this command, it must
     * be able to see it. Please ensure that the modal returned by this method is the same as the one that you used as a
     * response to the Discord interaction event.
     * <p>
     * WARNING: If this is null or is not the same modal as you used to respond to discord, the command WILL FAIL.
     *
     * @return The modal sent to discord as a reply
     */
    Modal getModal();

}
