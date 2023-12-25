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
package com.tcn.vera.pagination;

import com.tcn.vera.eventHandlers.ButtonHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;

import java.util.ArrayList;
import java.util.List;

//This paginator has support for both messages and interaction events

/**
 * The base class for all Vera paginators. This class should not be used directly, but instead extended by other classes.
 */
public abstract class PaginatorBase {

    protected final Message message;
    protected Message sentMessage;

    protected final SlashCommandInteractionEvent commandEvent;
    protected final long userID;

    protected final int numberOfPages;

    protected final boolean shouldWrap;

    protected final boolean isCommand;
    protected final ButtonHandler buttonHandler;
    protected int currentPage;
    protected List<Button> buttonList = new ArrayList<>();


    protected PaginatorBase(Message message, Message sentMessage, SlashCommandInteractionEvent commandEvent, int numberOfPages, boolean shouldWrap, long userID, ButtonHandler buttonHandler) {
        this.message = message;
        this.sentMessage = sentMessage;
        this.commandEvent = commandEvent;
        this.numberOfPages = numberOfPages;
        this.shouldWrap = shouldWrap;
        this.userID = userID;
        this.buttonHandler = buttonHandler;

        if (message == null && sentMessage == null && commandEvent != null ) {
            isCommand = true;
        } else if ((message != null || sentMessage != null) && commandEvent == null) {
            isCommand = false;
        } else {
            throw new IllegalArgumentException("This paginator cannot be in response to both a message and a command at the same time!");
        }

        buttonHandler.registerButtonSet(getButtonID(), this::onButtonClick);
    }

    /**
     * The method called when a button is pressed. This method should be overridden by the child class.
     *
     * @param event The {@link ButtonInteractionEvent} that was fired.
     */
    protected abstract void onButtonClick(ButtonInteractionEvent event);

    /**
     * The method called when a page is shown. This method should be overridden by the child class.
     *
     * @param pageNum The page number that should be shown.
     * @return The embed that should be shown.
     */
    protected abstract MessageEmbed getMenuEmbed(int pageNum);

    /**
     * The method called when a page is shown. If a different implementation is needed, it is ok to override this method
     * as long as everything is covered.
     */
    protected void showPage(){
        if (isCommand) {
            WebhookMessageEditAction<Message> messageEdit = commandEvent.getHook().editOriginalComponents().setEmbeds(getMenuEmbed(currentPage));
            messageEdit = !buttonList.isEmpty() ? messageEdit.setActionRow(buttonList) : messageEdit.setComponents();
            messageEdit.queue();

        } else {
            if(sentMessage == null){
                MessageCreateAction messageEdit = message.getChannel().sendMessageEmbeds(getMenuEmbed(currentPage));
                messageEdit = !buttonList.isEmpty() ? messageEdit.setActionRow(buttonList) : messageEdit.setComponents();
                sentMessage = messageEdit.complete();

            }else{
                MessageEditAction messageEdit =  sentMessage.editMessageEmbeds(getMenuEmbed(currentPage));
                messageEdit = !buttonList.isEmpty() ? messageEdit.setActionRow(buttonList) : messageEdit.setComponents();
                messageEdit.queue();
            }
        }
    }

    /**
     * Shows the paginator at a specific page number.
     *
     * @param pageNum The page number that should be first presented to the user.
     */
    public void paginate(int pageNum) {
        if (pageNum < 1) {
            currentPage = 1;
        } else if (pageNum > numberOfPages) {
            currentPage = numberOfPages;
        }
        showPage();
    }

    /**
     * Shows the paginator on the first page.
     */
    public void paginate() {
        paginate(1);
    }

    /**
     * Used to generate the ID of a button via the message ID, discord user ID, and the name of the button. While Vera
     * would be capable of matching buttons without all of these factors, we can ensure unique buttons with all of this information.
     *
     * @param buttonName The name of the button
     * @return A String representing the ID of the button. This should be used as the first parameter when creating a button.
     */
    public String getFullButtonID(String buttonName) {
        return getButtonID() + ":" + buttonName;
    }

    /**
     * Used to generate the base ID of a button via the object hash and the discord user ID.
     *
     * @return A string representing the base ID of the button.
     */
    public String getButtonID() {
        return this.hashCode() + ":" + userID;
    }

    /**
     * Calculates the next page number. Returns -1 if there is no next page.
     * @return The next page number
     */
    protected int getNextPageNum(){
        if (currentPage == numberOfPages - 1 && shouldWrap) {
            return 0;
        } else if (currentPage < numberOfPages - 1) {
            return currentPage + 1;
        } else {
            return -1;
        }
    }

    protected void incPageNum() {
        if(getNextPageNum() != -1){
            currentPage = getNextPageNum();
        }
    }

    /**
     * Calculates the previous page number. Returns -1 if there is no previous
     * @return The previous page number.
     */
    protected int getPreviousPageNum(){
        if (currentPage == 0 && shouldWrap) {
            return numberOfPages - 1;
        } else if (currentPage > 0) {
            return currentPage - 1;
        } else {
            return -1;
        }
    }

    protected void decPageNum() {
        if(getPreviousPageNum() != -1){
            currentPage = getPreviousPageNum();
        }
    }

    /**
     * Adds a button to the paginator. Please note that there is a hard limit of 5 buttons per paginator. If more are added,
     * an exception will be thrown.
     *
     * @param toAdd The button to be added.
     */
    protected void addButton(Button toAdd) {
        if (buttonList.size() < 5) {
            buttonList.add(toAdd);
        } else {
            throw new IllegalArgumentException("You cannot have more than 5 buttons on a message!");
        }
    }

    /**
     * Destroys the paginator. This method should be called when the paginator is no longer needed.
     *
     * @param deleteMessage Whether or not the message should be deleted.
     */
    protected void destroyMenu(boolean deleteMessage) {
        if (deleteMessage && isCommand) {
            commandEvent.getHook().deleteOriginal().queue();
        } else if (deleteMessage && sentMessage != null) {
            sentMessage.delete().queue();
        } else if (isCommand) {
            commandEvent.getHook().editOriginalComponents().setComponents().queue();
        } else if (sentMessage != null) {
            sentMessage.editMessageComponents().setComponents().queue();
        }
        //if there is no sent message there is no message to delete
    }

    //** public getter methods for submenu creation **//

    /**
     * Gets the original message that triggered the paginator. This will be null if the paginator was responding to a command
     * or if the paginator was created with a previously sent message.
     * @return The original message that triggered the paginator.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Gets the message that the paginator is using to display the pages. This will be null if the paginator was created in response to a command.
     * @return The message that the paginator is using to display the pages.
     */
    public Message getSentMessage() {
        return sentMessage;
    }

    /**
     * Gets the event that triggered the paginator. This will be null if the paginator was created with a chat command or sent message
     * @return The Slash Command Event that triggered the paginator.
     */
    public SlashCommandInteractionEvent getSlashCommandEvent() {
        return commandEvent;
    }

    /**
     * Gets the id of the user who triggered the paginator.
     * @return The id of the user who triggered the paginator.
     */
    public long getUserID() {
        return userID;
    }

    /**
     * Gets the button handler that the paginator is using.
     * @return The button handler that the paginator is using.
     */
    public ButtonHandler getbuttonHandler() {
        return buttonHandler;
    }

    @SuppressWarnings("unchecked")
    protected abstract static class Builder<T extends Builder<T, V>, V extends PaginatorBase> {
        protected boolean shouldWrap = true;
        protected Message message;
        protected Message sentMessage;
        protected SlashCommandInteractionEvent commandEvent;

        protected long userID = -1;

        protected ButtonHandler buttonHandler;

        /**
         * builds the paginator object. This method should be overridden by the child class.
         *
         * @return The paginator object.
         */
        public abstract V build();

        protected boolean runChecks() {
            if (null == buttonHandler) {
                throw new IllegalStateException("A paginator MUST have a button handler!");
            }

            if(userID == -1){
                if(commandEvent != null){
                    userID = commandEvent.getUser().getIdLong();
                }else if(message != null){
                    userID = message.getAuthor().getIdLong();
                }else{
                    throw new IllegalArgumentException("The paginator must be passed a user ID if it is not responding to an event!");
                }
            }


            return runAdditionalChecks();
        }

        /**
         * This method should be overridden by the child class to add additional checks.
         *
         * @return Whether or not the additional checks passed.
         */
        protected abstract boolean runAdditionalChecks();

        /**
         * Sets whether or not the paginator should wrap around when it reaches the end of the pages.
         *
         * @param shouldWrap Whether or not the paginator should wrap around.
         * @return The builder.
         */
        public final T wrapPages(boolean shouldWrap) {
            this.shouldWrap = shouldWrap;
            return (T) this;
        }

        /**
         * Sets the user ID of the user who should be able to interact with the paginator.
         *
         * @param userID The user ID of the user who should be able to interact with the paginator.
         * @return The builder.
         */
        public final T setUserID(long userID) {
            this.userID = userID;
            return (T) this;
        }

        /**
         * Sets the event that triggered the paginator.
         *
         * @param event The event that triggered the paginator.
         * @return The builder.
         */
        public final T setEvent(SlashCommandInteractionEvent event) {
            this.commandEvent = event;
            return (T) this;
        }

        /**
         * Sets the event that triggered the paginator.
         *
         * @param event The event that triggered the paginator.
         * @return The builder.
         */
        public final T setEvent(MessageReceivedEvent event) {
            this.message = event.getMessage();
            return (T) this;
        }

        /**
         * Sets the message that the paginator should use instead of creating a new one. Any text in the message will remain, but the embeds and buttons will be replaced.
         * This will override the event if it is set.
         * @param message The message that the paginator should use instead of creating a new one.
         * @return The builder.
         */
        public final T setSentMessage(Message message) {
            this.sentMessage = message;
            return (T) this;
        }

        /**
         * Sets the button handler that should be used by the paginator. If this is not set, the paginator will not work.
         *
         * @param buttonHandler The button handler that should be used by the paginator.
         * @return The builder.
         */
        public final T setButtonHandler(ButtonHandler buttonHandler) {
            this.buttonHandler = buttonHandler;
            return (T) this;
        }

    }
}
