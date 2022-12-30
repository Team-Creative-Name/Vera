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
package com.tcn.vera.eventHandlers;

import com.tcn.vera.commands.*;
import com.tcn.vera.utils.VeraUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The command handler for Vera. This class must be constructed via the CommandHandlerBuilder.
 */
public class CommandHandler extends ListenerAdapter {

    //Required for command processing
    private final Set<ChatCommandTemplate> chatCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<SlashCommandTemplate> slashCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<UserContextTemplate> userContextCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<MessageContextTemplate> messageContextCommandSet = ConcurrentHashMap.newKeySet();
    private final ExecutorService commandPool = Executors.newCachedThreadPool(VeraUtils.createThreadFactory("VeraCommandRunner", false));
    private final Logger logger;

    //bot specific information
    private final ArrayList<String> botOwner;
    private final String prefix;

    /**
     * To create an instance of this class, please use the {@link CommandHandlerBuilder}.
     */
    CommandHandler(ArrayList<? extends CommandTemplateBase> commandList, ArrayList<String> botOwner, String prefix){
        logger = LoggerFactory.getLogger("Vera: Command Handler");
        this.botOwner = botOwner;
        this.prefix = prefix;

        for(CommandTemplateBase command: commandList){
            switch (command.getCommandType()){
                case CHAT_COMMAND -> {
                    if(this.chatCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))){
                        this.chatCommandSet.add((ChatCommandTemplate) command);
                    }else{
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a chat command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                case SLASH_COMMAND -> {
                    if(this.slashCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))){
                        this.slashCommandSet.add((SlashCommandTemplate) command);
                    }else{
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a slash command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                case USER_CONTEXT_COMMAND -> {
                    if(this.userContextCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))){
                        this.userContextCommandSet.add((UserContextTemplate) command);
                    }else{
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a user context command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                case CONTEXT_MESSAGE_COMMAND -> {
                    if(this.messageContextCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))){
                        this.messageContextCommandSet.add((MessageContextTemplate) command);
                    }else{
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a message context command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                default -> throw new IllegalArgumentException("Sorry, this command handler is only capable of handling Chat, Slash, and Context commands. " +
                        "If you have created your own command type, you will have to extend this class and add support for it here!");
            }
        }
        logger.info("Registered " + chatCommandSet.size() + " chat command(s).");
        logger.info("Registered " + slashCommandSet.size() + " slash command(s).");
        logger.info("Registered " + userContextCommandSet.size() + " user context menu command(s).");
        logger.info("Registered " + messageContextCommandSet.size() + " message context menu command(s).");
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        if(event.getMessage().getContentRaw().toLowerCase().startsWith(prefix) && !event.getAuthor().isBot()){
            String rawMessage = event.getMessage().getContentRaw();
            String commandName = VeraUtils.getCommandName(rawMessage);

            ChatCommandTemplate command = chatCommandSet.stream().filter(
                   p -> p.getAllCommandNames().contains(commandName)
            ).findFirst().orElse(null);

            if(null != command){
                if(command.isOwnerCommand()){
                    System.out.println("Author ID = " + event.getAuthor().getId());

                    if(botOwner.stream().anyMatch(c -> c.equalsIgnoreCase(event.getAuthor().getId()))) {
                        logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                        executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                    }else{
                        logger.warn(event.getAuthor().getName() + " has attempted to use the \"" + command.getCommandName()
                                + "\" owner chat command without being on the list of owners!");
                    }

                }else{
                    logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                    executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                }
            }

        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        SlashCommandTemplate command = slashCommandSet.stream().filter(
                p -> p.getSlashCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if(null != command){
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeSlashCommand(command, event);
        }

    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event){
        //TODO: implement this
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event){
        //TODO: implement this
    }

    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event){
        //TODO: implement this
    }
    public void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event){
        //TODO: implement this
    }

    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        //TODO: implement this
    }

    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
        UserContextTemplate command = userContextCommandSet.stream().filter(
                p -> p.getUserContextCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if(null != command){
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeUserContextCommand(command, event);
        }
    }
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        MessageContextTemplate command = messageContextCommandSet.stream().filter(
                p -> p.getMessageContextCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if(null != command){
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeMessageContextCommand(command, event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //we need to send slash, userContext, and messageContext commands to discord
        List<CommandData> toadd = new ArrayList<>();

        slashCommandSet.forEach(c -> toadd.add(c.getSlashCommand()));
        userContextCommandSet.forEach(c -> toadd.add(c.getUserContextCommand()));
        messageContextCommandSet.forEach(c -> toadd.add(c.getMessageContextCommand()));

        event.getJDA().updateCommands().addCommands(toadd).queue();
        logger.info("Sent " + slashCommandSet.size() + " slash command(s), " +
                userContextCommandSet.size() + " user context command(s), and " +
                messageContextCommandSet.size() + " message context command(s) to Discord.");

        //we should also check to ensure that there is an owner set. If not, we should be able to get it from JDA
        if(null == botOwner || botOwner.size() < 1){
            event.getJDA().retrieveApplicationInfo().onSuccess(c -> botOwner.add(c.getOwner().getId())).submit();
            logger.warn("No owner IDs were given. New owner ID: " + botOwner.get(0));
        }
    }

    private void executeChatCommand(ChatCommandTemplate template, MessageReceivedEvent event, String messageContent){
        this.commandPool.submit(() ->{
            try{
                template.executeChatCommand(event, event.getMessage(), messageContent);
            }catch (final Exception e){
                logger.error("Error while executing the \"" + template.getCommandName() + "\" chat command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                event.getMessage().reply("Sorry, I was unable to finish executing that command. Please try again later.").queue();
            }
        });
    }

    private void executeSlashCommand(SlashCommandTemplate template, SlashCommandInteractionEvent event){
        this.commandPool.submit(() ->{
            try{
                template.executeSlashCommand(event);
            }catch (final Exception e){
                logger.error("Error while executing the \"" + template.getCommandName() + "\" slash command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                if(event.isAcknowledged()){
                    event.getHook().editOriginal("Sorry, I was unable to finish executing that command. Please try again later.").setActionRow().setEmbeds().queue();
                }else{
                    event.reply("Sorry, I was unable to finish executing that command. Please try again later.").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeUserContextCommand(UserContextTemplate template, UserContextInteractionEvent event){
        try{
            template.executeUserContextCommand(event);
        }catch (Exception e){
            System.out.println("I was made by someone who was too lazy to handle this correctly!");
        }
    }

    private void executeMessageContextCommand(MessageContextTemplate template, MessageContextInteractionEvent event){
        try{
            template.executeMessageContextCommand(event);
        }catch (Exception e){
            System.out.println("I was made by someone who was too lazy to handle this correctly!");
        }
    }
}
