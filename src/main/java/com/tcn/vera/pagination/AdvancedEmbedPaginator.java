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
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A Pagainator that is capable of displaying embeds in a more advanced way than the {@link EmbedPaginator}. This paginator
 * allows for dynamic content loading, page caching, and the ability to add a select button which can be used to do anything
 * via a consumer.
 * <p>
 * While the constructor is public, it is recommended that you use the {@link AdvancedEmbedPaginator.Builder} to build this
 * paginator as it is quite complex and has many options. For more information on how to construct this paginator, please
 * see the builder's javadoc.
 */
public class AdvancedEmbedPaginator extends PaginatorBase{

    private final ArrayList<MessageEmbed> generatedEmbedList;
    private final ArrayList<Object> pageDataList;
    private final BiConsumer<EmbedBuilder, Object> embedConsumer;

    private final BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer;
    private final BiConsumer<Message, Object> messageSelectConsumer;

    private final boolean hasSelectButton;

    private final boolean addPageNum;

    /**
     * Please consider using the AdvancedEmbedPaginatorBuilder to build this object.
     * <p>
     * This paginator is capable of being used with both slash commands and message commands.
     *
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
     * @param addPageNum Determines if the paginator should add the page number to the footer of the embed when its is being generated and no footer is present.
     */
    protected AdvancedEmbedPaginator(Message message, SlashCommandInteractionEvent commandEvent, int numberOfPages, boolean shouldWrap, long userID, ButtonHandler buttonHandler, ArrayList<Object> pageDataList, ArrayList<MessageEmbed> embedList, BiConsumer<EmbedBuilder, Object> pageBuilder, BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer, BiConsumer<Message, Object> messageSelectConsumer, boolean addPageNum){
        super(message, commandEvent, numberOfPages, shouldWrap, userID, buttonHandler);

        this.pageDataList = pageDataList;
        this.eventSelectConsumer = eventSelectConsumer;
        this.messageSelectConsumer = messageSelectConsumer;
        this.generatedEmbedList = embedList;
        this.addPageNum = addPageNum;

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
        } else if (hasSelectButton) {
            addButton(Button.danger(getFullButtonID("stop"), Emoji.fromUnicode("\uD83D\uDDD1")));
            addButton(Button.success(getFullButtonID("success"), "Select This"));
        }
    }

    @Override
    protected void onButtonClick(ButtonInteractionEvent event) {

        if (event.getUser().getIdLong() != userID) {
            event.reply("You are not the user who created this menu!").setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();

        switch (event.getComponentId().split(":")[2]) {
            case "previous" -> decPageNum();
            case "next" -> incPageNum();
            case "stop" -> {
                destroyMenu(false);
                return;
            }
            case "success" -> {
                enterSubMenu();
                //we don't want to continue this paginator after the user has selected something. return
                return;
            }
            default -> {
                return;
            }
        }


        showPage();

        //now we need to ensure the next pages are generated
        if(getNextPageNum() != -1){
            getMenuEmbed(getNextPageNum());
        }
        if(getPreviousPageNum() != -1){
            getMenuEmbed(getPreviousPageNum());
        }

    }

    private void enterSubMenu(){
        //depending on how this page was created, we either need to send a embedBuilder or the pageData object
        Object toSend;
        if(pageDataList.get(currentPage) == null){
            toSend = getMenuEmbed(currentPage);
        } else {
            toSend = pageDataList.get(currentPage);
        }
        if (hasSelectButton) {
            if (isCommand) {
                eventSelectConsumer.accept(commandEvent, toSend);
            } else {
                messageSelectConsumer.accept(sentMessage, toSend);
            }
        }
    }

    protected MessageEmbed getMenuEmbed(int pagenum){
        if(generatedEmbedList.size() - 1 >= pagenum && null != generatedEmbedList.get(pagenum)){
            return generatedEmbedList.get(pagenum);
        }

        //executed only if the page hasn't been generated yet
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedConsumer.accept(embedBuilder, pageDataList.get(pagenum));

        //if the embed doesn't have a footer, add one that shows the page number if the user wants it
        if(addPageNum && embedBuilder.build().getFooter() == null){
            embedBuilder.setFooter("Page " + (pagenum + 1) + " of " + numberOfPages);
        }

        generatedEmbedList.set(pagenum, embedBuilder.build());

        return embedBuilder.build();
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

    /**
     * The builder class for the AdvancedEmbedPaginator.
     * <p>
     * The advancedEmbedPaginator is a paginator that allows you to use a biconsumer to build your pages as they are needed instead of all at once.
     * This is useful if you have a lot of pages or if the pages are costly to generate. Simply add your page data to the pageDataList via the {@link #addPageData(Object)} method
     * and then whenever it is needed, the biconsumer will be called the page will be generated. This paginator attempts to keep at least one page ahead of the current page generated
     * so a user never has to wait for a page to be generated.
     * <p>
     * Submenus are handled by biconsumers as well. If you want to use a submenu, you must pass in a biconsumer that will be executed when the user selects the page.
     * These are type sensitive. Only pass a eventSelectConsumer if you are using a slash command, and only pass a messageSelectConsumer if you are using a chat command.
     * Adding a submenu consumer will automatically add a select button to the paginator.
     *
     */
    public static class Builder extends PaginatorBase.Builder<AdvancedEmbedPaginator.Builder, AdvancedEmbedPaginator> {
        private BiConsumer<EmbedBuilder, Object> embedConsumer;
        private final ArrayList<Object> pageDataList = new ArrayList<>();

        private final ArrayList<MessageEmbed> generatedEmbedList = new ArrayList<>();

        private BiConsumer<SlashCommandInteractionEvent, Object> eventSelectConsumer = null;

        private BiConsumer<Message, Object> messageSelectConsumer = null;

        private boolean addPageNum = true;

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

            return new AdvancedEmbedPaginator(message, commandEvent, pageDataList.size(), shouldWrap, userID, buttonHandler, pageDataList, generatedEmbedList, embedConsumer, eventSelectConsumer, messageSelectConsumer, addPageNum);
        }

        @Override
        protected boolean runAdditionalChecks() {
            //an embed consumer is required if there are pages that need to be generated
            if (embedConsumer == null && !pageDataList.isEmpty()) {
                throw new IllegalArgumentException("You must provide a embed consumer!");
            }

            //there has to be at least one pageDataList object or a pre-generated embed
            if (pageDataList.isEmpty() && generatedEmbedList.isEmpty()) {
                throw new IllegalArgumentException("You must provide at least one page of data! This can be in the form of a pre-generated embed or a pageData object.");
            }

            if (eventSelectConsumer != null && messageSelectConsumer != null) {
                throw new IllegalArgumentException("This paginator cannot have both a message and event select consumer! " +
                        "Please provide only the one that matches the type of paginator you are using.");
            }
            return true;
        }

        /**
         * Adds an object to the pageData list. This function can be called multiple times to add multiple items to the list.
         * If you want to add a large number of items, consider adding them as an arrayList instead with {@link AdvancedEmbedPaginator.Builder#addPageData(List pageDataArray)}
         * @param pageData The object you would like to add to the page data list
         * @return This Builder
         */
        public AdvancedEmbedPaginator.Builder addPageData(Object pageData){
            pageDataList.add(pageData);
            //ensure the generated embed list is the same size as the pageDataList
            generatedEmbedList.add(null);
            return this;
        }

        /**
         * Adds a List of objects to the pageData list. This function can be called multiple times to add multiple items to the list.
         * @param pageData The List you would like to add to the page data list.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder addPageDataList(List<?> pageData){
            pageDataList.addAll(pageData);
            //ensure the generated embed list is the same size as the pageDataList
            pageData.forEach(o -> generatedEmbedList.add(null));
            return this;
        }

        /**
         * Adds a messageEmbed to the next free spot in the page list.
         * @param embed The pre-made embed you would like to add to the page list.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder addEmbed(MessageEmbed embed){
            generatedEmbedList.add(embed);
            //ensure that we dont overwrite any data in the pageDataList
            pageDataList.add(null);
            return this;
        }

        /**
         * Adds a list of messageEmbeds to the next free spots in the page list.
         * @param embeds The pre-made embeds you would like to add to the page list.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder addEmbeds(List<MessageEmbed> embeds){
            generatedEmbedList.addAll(embeds);
            //ensure that we dont overwrite any data in the pageDataList
            embeds.forEach(o -> pageDataList.add(null));
            return this;
        }


        /**
         * Sets the embed consumer for the paginator. This is the function that will be called to generate the embeds for each page.
         * <p>
         * The consumer will be called once for each page and then the embed will be cached.
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
         * <p>
         * The second parameter of the consumer is the information that was passed in when the page was added. Example: If the page
         * was originally added as a MessageEmbed, then you will get a MessageEmbed object back.
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
         * <p>
         * The second parameter of the consumer is the information that was passed in when the page was added. Example: If the page
         * was originally added as a MessageEmbed, then you will get a MessageEmbed object back.
         * 
         * @param messageSelectConsumer The consumer that will be called when the user selects the paginator.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder setMessageSelectConsumer(BiConsumer<Message, Object> messageSelectConsumer) {
            this.messageSelectConsumer = messageSelectConsumer;
            return this;
        }

        /**
         * Determines if Vera should add the page number to the footer of a generated embed. This will not overwrite any footer that is already set,
         * and it does not apply to any pre-made embeds that are added to the paginator. The added footer will be in the format of "Page x of y".
         * <p>
         * Default value: true
         * @param addPageNum Whether or not the paginator should add the page number to the footer of the embed.
         * @return This Builder.
         */
        public AdvancedEmbedPaginator.Builder addPageCounter(boolean addPageNum) {
            this.addPageNum = addPageNum;
            return this;
        }
    }
}
