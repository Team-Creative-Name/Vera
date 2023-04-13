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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * Advanced Embed paginator. A class that allows for more advanced pagination of embeds. This version supports dynamic page loading,
 * and the ability to show a submenu.
 */
public class AdvancedEmbedPaginator extends PaginatorBase{

    private final ArrayList<MessageEmbed> generatedEmbedList;
    private final ArrayList<Object> pageDataList;
    private final BiConsumer<EmbedBuilder, Object> embedConsumer;

    private final BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer;
    private final BiConsumer<Message, Object> messageSelectConsumer;

    private final boolean hasSelectButton;

    /**
     * Please consider using the AdvancedEmbedPaginatorBuilder to build this object.
     * @param message The message that the paginator is going to modify. Null if this is a slash command.
     * @param commandEvent The commandEvent that the paginator is responding to. Null if this is a message command.
     * @param numberOfPages The number of pages that are in your paginator.
     * @param shouldWrap Determines if the paginator should wrap from the last page to the first.
     * @param userID The id of the discord user who caused the paginator to be created.
     * @param buttonHandler The buttonHandler class that the paginator should use for its buttons.
     * @param pageDataList An arrayList containing your page data.
     * @param pageBuilder A biconsumer that takes in an {@link EmbedBuilder} and an element of the pageDataList to form a page.
     * @param eventSelectConsumer A consumer that is executed when the 'select' button is pressed on a page of the paginator and the paginator is using a slash command.
     * @param messageSelectConsumer A consumer that is executed when the 'select' button is pressed on a page of the paginator and the paginator is using a chat command.
     */
    protected AdvancedEmbedPaginator(Message message, SlashCommandInteractionEvent commandEvent, int numberOfPages, boolean shouldWrap, long userID, ButtonHandler buttonHandler, ArrayList<Object> pageDataList, BiConsumer<EmbedBuilder, Object> pageBuilder, BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer, BiConsumer<Message, Object> messageSelectConsumer){
        super(message, commandEvent, numberOfPages, shouldWrap, userID, buttonHandler);
        this.pageDataList = pageDataList;
        this.eventSelectConsumer = eventSelectConsumer;
        this.messageSelectConsumer = messageSelectConsumer;
        this.generatedEmbedList = new ArrayList<>(Arrays.asList(new MessageEmbed[numberOfPages]));

        embedConsumer = pageBuilder;
        hasSelectButton = eventSelectConsumer != null || messageSelectConsumer != null;

        if(numberOfPages > 1){
            //left, stop, right buttons
            addButton(Button.primary(getFullButtonID("previous"),  Emoji.fromUnicode("\u2B05")));
            addButton(Button.danger(getFullButtonID("stop"), Emoji.fromUnicode("\uD83D\uDDD1")));
            if(hasSelectButton){
                addButton(Button.success(getFullButtonID("success"), "Select This"));
            }
            addButton(Button.primary(getFullButtonID("next"),  Emoji.fromUnicode("\u27A1")));
        }
    }

    @Override
    protected void onButtonClick(ButtonInteractionEvent event) {

        if (event.getUser().getIdLong() != userID) {
            event.reply("You are not the user who created this menu!").setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId().split(":")[2]) {
            case "previous" -> decPageNum();
            case "next" -> incPageNum();
            case "stop" -> {
                destroyMenu(false);
                return;
            }
            case "success" -> {
                event.deferEdit().queue();
                if (hasSelectButton) {
                    if (isCommand) {
                        eventSelectConsumer.accept(commandEvent, pageDataList.get(currentPage));
                    } else {
                        messageSelectConsumer.accept(sentMessage, pageDataList.get(currentPage));
                    }
                }
                //we don't want to continue this paginator after the user has selected something. return
                return;
            }
            default -> {
                return;
            }
        }

        event.deferEdit().queue();
        showPage();

        //now we need to ensure the next pages are generated
        if(getNextPageNum() != -1){
            getMenuEmbed(getNextPageNum());
        }
        if(getPreviousPageNum() != -1){
            getMenuEmbed(getPreviousPageNum());
        }

    }

    private MessageEmbed getMenuEmbed(int pagenum){
        if(generatedEmbedList.size() - 1 >= pagenum && null != generatedEmbedList.get(pagenum)){
            return generatedEmbedList.get(pagenum);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedConsumer.accept(embedBuilder, pageDataList.get(pagenum));

        //if the embed doesnt have a footer, add one that shows the page number
        if(embedBuilder.build().getFooter() == null){
            embedBuilder.setFooter("Page " + (pagenum + 1) + " of " + numberOfPages);
        }

        generatedEmbedList.add(pagenum, embedBuilder.build());

        return embedBuilder.build();
    }

    @Override
    protected void showPage() {
        if (isCommand) {
            commandEvent.getHook().editOriginalComponents().setEmbeds(getMenuEmbed(currentPage)).setActionRow(buttonList).queue();
        } else {
            if(sentMessage == null){
                sentMessage = message.getChannel().sendMessageEmbeds(getMenuEmbed(currentPage)).setActionRow(buttonList).complete();
            }else{
                sentMessage.editMessageEmbeds(getMenuEmbed(currentPage)).setActionRow(buttonList).queue();
            }
        }
    }

    @Override
    public void paginate() {
        paginate(1);

        //now we need to ensure the next pages are generated
        if(getNextPageNum() != -1){
            getMenuEmbed(getNextPageNum());
        }
        if(getPreviousPageNum() != -1){
            getMenuEmbed(getPreviousPageNum());
        }
    }

    public static class Builder extends PaginatorBase.Builder<AdvancedEmbedPaginator.Builder, AdvancedEmbedPaginator> {
        private BiConsumer<EmbedBuilder, Object> embedConsumer;
        private final ArrayList<Object> pageDataList = new ArrayList<>();

        private BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer = null;

        private BiConsumer<Message, Object> messageSelectConsumer = null;

        /**
         * Builds the paginator. This method will throw an IllegalArgumentException if the paginator is not valid.
         *
         * @return A new EmbedPaginator object.
         */
        @Override
        public AdvancedEmbedPaginator build() {
            //validate stuff
            if (!runChecks()) {
                throw new IllegalArgumentException("Cannot build, invalid arguments!");
            }

            //calculate the number of pages
            return new AdvancedEmbedPaginator(message, commandEvent, pageDataList.size(), shouldWrap, userID, buttonHandler,pageDataList, embedConsumer, eventSelectConsumer, messageSelectConsumer);
        }

        @Override
        protected boolean runAdditionalChecks() {
            if (embedConsumer == null) {
                throw new IllegalArgumentException("You must provide a embed consumer!");
            }

            if (pageDataList.isEmpty()) {
                throw new IllegalArgumentException("You must provide at least one page of data!");
            }

            if (eventSelectConsumer != null && messageSelectConsumer != null) {
                throw new IllegalArgumentException("This paginator cannot have both a message and event select consumer! " +
                        "Please provide only the one that matches the type of paginator you are using.");
            }
            return true;
        }

        /**
         * Adds an object to the pageData list. This function can be called multiple times to add multiple items to the list.
         * If you want to add a large number of items, consider adding them as an arrayList instead with {@link AdvancedEmbedPaginator.Builder#addPageData(ArrayList pageDataArray)}
         * @param pageData The object you would like to add to the page data list
         * @return This Builder
         */
        public AdvancedEmbedPaginator.Builder addPageData(Object pageData){
            pageDataList.add(pageData);
            return this;
        }

        /**
         * Adds an arrayList of objects to the pageData list. This function can be called multiple times to add multiple items to the list.
         * @param pageData The arrayList you would like to add to the page data list.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder addPageData(ArrayList<Object> pageData){
            pageDataList.add(pageData);
            return this;
        }


        /**
         * Sets the embed consumer for the paginator. This is the function that will be called to generate the embeds for each page.
         * @param embedBuilder The embed consumer that will be used to generate the embeds for each page.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder setEmbedConsumer(BiConsumer<EmbedBuilder, Object> embedBuilder) {
            embedConsumer = embedBuilder;
            return this;
        }

        /**
         * Sets the consumer that will be called when the user uses the select button on the paginator.
         * This consumer will only be called if the paginator is a command paginator.
         * Do not use this function if you are using a message paginator.
         *
         * @param eventSelectConsumer The consumer that will be called when the user selects the paginator.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder setEventSelectConsumer(BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer){
            this.eventSelectConsumer = eventSelectConsumer;
            return this;
        }

        /**
         * Sets the consumer that will be called when the user uses the select button on the paginator.
         * This consumer will only be called if the paginator is a message paginator.
         * Do not use this function if you are using a command paginator.
         * @param messageSelectConsumer The consumer that will be called when the user selects the paginator.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder setMessageSelectConsumer(BiConsumer<Message, Object> messageSelectConsumer) {
            this.messageSelectConsumer = messageSelectConsumer;
            return this;
        }
    }
}
