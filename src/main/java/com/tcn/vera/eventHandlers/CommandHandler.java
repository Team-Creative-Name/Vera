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
package com.tcn.vera.eventHandlers;

import com.tcn.vera.commands.*;
import com.tcn.vera.interactions.*;
import com.tcn.vera.utils.CommandCache;
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
import java.util.function.Consumer;

/**
 * The command handler for Vera. This class must be constructed via the CommandHandlerBuilder.
 */
public class CommandHandler extends ListenerAdapter {

    //Required for command processing
    private final Set<ChatCommandTemplate> chatCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<SlashCommandTemplate> slashCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<UserContextTemplate> userContextCommandSet = ConcurrentHashMap.newKeySet();
    private final Set<MessageContextTemplate> messageContextCommandSet = ConcurrentHashMap.newKeySet();

    //keeps track of the last 100 buttons created by the bot. Allows us to respond to commands with unique buttons IDs with every command creation
    private final CommandCache<String, Consumer<? super ButtonInteractionEvent>> buttons = new CommandCache<>(100);
    private final ExecutorService commandPool = Executors.newCachedThreadPool(VeraUtils.createThreadFactory("VeraCommandRunner", false));
    private final Logger logger;

    //bot specific information
    private final ArrayList<String> botOwner;
    private final String prefix;

    /**
     * To create an instance of this class, please use the {@link CommandHandlerBuilder}.
     */
    CommandHandler(ArrayList<? extends CommandTemplateBase> commandList, ArrayList<String> botOwner, String prefix) {
        logger = LoggerFactory.getLogger("Vera: Command Handler");
        this.botOwner = botOwner;
        this.prefix = prefix;

        //split the commandList into their respective global hashmaps. This allows us to get specific command types easier
        for (CommandTemplateBase command : commandList) {
            switch (command.getCommandType()) {
                case CHAT_COMMAND -> {
                    if (this.chatCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))) {
                        this.chatCommandSet.add((ChatCommandTemplate) command);
                    } else {
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a chat command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                case SLASH_COMMAND -> {
                    if (this.slashCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))) {
                        this.slashCommandSet.add((SlashCommandTemplate) command);
                    } else {
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a slash command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                case USER_CONTEXT_COMMAND -> {
                    if (this.userContextCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))) {
                        this.userContextCommandSet.add((UserContextTemplate) command);
                    } else {
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a user context command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                case CONTEXT_MESSAGE_COMMAND -> {
                    if (this.messageContextCommandSet.stream().map(CommandTemplateBase::getCommandName).noneMatch(c -> command.getCommandName().equalsIgnoreCase(c))) {
                        this.messageContextCommandSet.add((MessageContextTemplate) command);
                    } else {
                        throw new IllegalArgumentException("A command with the name \"" + command.getCommandName()
                                + "\" has already been registered as a message context command. Command names must be unique, lowercase and alphanumeric");
                    }
                }
                default ->
                        throw new IllegalArgumentException("Sorry, this command handler is only capable of handling Chat, Slash, and Context commands. " +
                                "If you have created your own command type, you will have to extend this class and add support for it here!");
            }

            //aside from splitting our types out, we also need to put all buttons into their cache
            if(command instanceof ButtonInterface buttonInterface){
                buttons.add(buttonInterface.getButtonClassID(), buttonInterface::executeButton);
            }
        }
        logger.info("Registered " + chatCommandSet.size() + " chat command(s).");
        logger.info("Registered " + slashCommandSet.size() + " slash command(s).");
        logger.info("Registered " + userContextCommandSet.size() + " user context menu command(s).");
        logger.info("Registered " + messageContextCommandSet.size() + " message context menu command(s).");
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //we need to send slash, userContext, and messageContext commands to discord
        List<CommandData> toAdd = new ArrayList<>();

        slashCommandSet.forEach(c -> toAdd.add(c.getSlashCommand()));
        userContextCommandSet.forEach(c -> toAdd.add(c.getUserContextCommand()));
        messageContextCommandSet.forEach(c -> toAdd.add(c.getMessageContextCommand()));

        event.getJDA().updateCommands().addCommands(toAdd).queue();
        logger.info("Sent " + slashCommandSet.size() + " slash command(s), " +
                userContextCommandSet.size() + " user context command(s), and " +
                messageContextCommandSet.size() + " message context command(s) to Discord.");

        //we should also check to ensure that there is an owner set. If not, we should be able to get it from JDA
        if (null == botOwner || botOwner.isEmpty()) {
            event.getJDA().retrieveApplicationInfo().onSuccess(c -> botOwner.add(c.getOwner().getId())).submit();
            logger.warn("No owner IDs were given. New owner ID: " + botOwner.get(0));
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().toLowerCase().startsWith(prefix) && !event.getAuthor().isBot()) {
            String rawMessage = event.getMessage().getContentRaw();
            String commandName = VeraUtils.getCommandName(rawMessage);

            ChatCommandTemplate command = chatCommandSet.stream().filter(
                    p -> p.getAllCommandNames().contains(commandName)
            ).findFirst().orElse(null);

            if (null != command) {
                if (command.isOwnerCommand()) {
                    if (botOwner.stream().anyMatch(c -> c.equalsIgnoreCase(event.getAuthor().getId()))) {
                        logger.debug(event.getAuthor().getName() + " has used the \"" + command.getCommandName() + "\" chat command");
                        executeChatCommand(command, event, rawMessage.substring(commandName.length() + 1));
                    } else {
                        logger.warn(event.getAuthor().getName() + " has attempted to use the \"" + command.getCommandName()
                                + "\" owner chat command without being on the list of owners!");
                    }
                } else {
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

        if (null != command) {
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeSlashCommand(command, event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        SlashCommandTemplate command = slashCommandSet.stream().filter(
                p -> p.getSlashCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (command instanceof AutoCompleteInterface autoCompleteInstance) {
            logger.debug(event.getUser().getName() + " is using autocomplete on \"" + command.getCommandName() + "\"");
            executeAutoCompleteInteraction(autoCompleteInstance, event);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Consumer<? super ButtonInteractionEvent> callback = buttons.find(buttonIdentifier -> event.getComponentId().startsWith(buttonIdentifier));

        if (callback == null) {
            event.reply("This button is no longer functional, please try running the command again.").setEphemeral(true).queue();
            return;
        }

        executeButtonInteraction(callback, event);
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        for (UserContextTemplate command : userContextCommandSet) {
            if (command instanceof StringSelect stringSelectInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(stringSelectInstance.getMenu().getId())) {
                    executeStringSelectInteraction(stringSelectInstance, event);
                    return;
                }
            }
        }

        for (MessageContextTemplate command : messageContextCommandSet) {
            if (command instanceof StringSelect stringSelectInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(stringSelectInstance.getMenu().getId())) {
                    executeStringSelectInteraction(stringSelectInstance, event);
                    return;
                }
            }
        }

        for (SlashCommandTemplate command : slashCommandSet) {
            if (command instanceof StringSelect stringSelectInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(stringSelectInstance.getMenu().getId())) {
                    executeStringSelectInteraction(stringSelectInstance, event);
                    return;
                }
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event) {
        for (UserContextTemplate command : userContextCommandSet) {
            if (command instanceof EntitySelect entitySelectInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(entitySelectInstance.getMenu().getId())) {
                    executeEntitySelectInteraction(entitySelectInstance, event);
                    return;
                }
            }
        }

        for (MessageContextTemplate command : messageContextCommandSet) {
            if (command instanceof EntitySelect entitySelectInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(entitySelectInstance.getMenu().getId())) {
                    executeEntitySelectInteraction(entitySelectInstance, event);
                    return;
                }
            }
        }

        for (SlashCommandTemplate command : slashCommandSet) {
            if (command instanceof EntitySelect entitySelectInstance) {
                if (event.getSelectMenu().getId().equalsIgnoreCase(entitySelectInstance.getMenu().getId())) {
                    executeEntitySelectInteraction(entitySelectInstance, event);
                    return;
                }
            }
        }
    }

    /**
     * Called by JDA whenever a ModalInteractionEvent fires on the event bus. Modals are supported by the Slash, userContext,
     * and messageContext templates, and we check every registered command of those types to see if they are responsible for
     * the event. If a match is found, we pass the event to the command and allow it to handle it further.
     *
     * @param event The {@link ModalInteractionEvent that JDA sent to Vera.}
     * @implNote This method loops through every registered command that supports
     */
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        for (MessageContextTemplate command : messageContextCommandSet) {
            if (command instanceof ModalInterface modalInstance) {
                if (modalInstance.getModal().getId().equals(event.getModalId())) {
                    executeModalInteraction(modalInstance, event);
                    return;
                }
            }
        }

        for (UserContextTemplate command : userContextCommandSet) {
            if (command instanceof ModalInterface modalInstance) {
                if (modalInstance.getModal().getId().equals(event.getModalId())) {
                    executeModalInteraction(modalInstance, event);
                    return;
                }
            }
        }

        for (SlashCommandTemplate command : slashCommandSet) {
            if (command instanceof ModalInterface modalInstance) {
                if (modalInstance.getModal().getId().equals(event.getModalId())) {
                    executeModalInteraction((ModalInterface) command, event);
                    return;
                }
            }
        }
    }

    @Override
    public void onUserContextInteraction(@Nonnull UserContextInteractionEvent event) {
        UserContextTemplate command = userContextCommandSet.stream().filter(
                p -> p.getUserContextCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (null != command) {
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeUserContextCommand(command, event);
        }
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        MessageContextTemplate command = messageContextCommandSet.stream().filter(
                p -> p.getMessageContextCommand().getName().equalsIgnoreCase(event.getFullCommandName())
        ).findFirst().orElse(null);

        if (null != command) {
            logger.debug(event.getUser().getName() + " has used the \"" + command.getCommandName() + "\" slash command");
            executeMessageContextCommand(command, event);
        }
    }

    private void executeChatCommand(ChatCommandTemplate template, MessageReceivedEvent event, String messageContent) {
        this.commandPool.submit(() -> {
            try {
                template.executeChatCommand(event, event.getMessage(), messageContent);
            } catch (final Exception e) {
                logger.error("Error while executing the \"" + template.getCommandName() + "\" chat command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                event.getMessage().reply("Sorry, I was unable to finish executing that command. Please try again later.").queue();
            }
        });
    }

    private void executeSlashCommand(SlashCommandTemplate template, SlashCommandInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeSlashCommand(event);
            } catch (final Exception e) {
                logger.error("Error while executing the \"" + template.getCommandName() + "\" slash command! \n" +
                        "Exception: " + e.getLocalizedMessage());
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to finish executing that command. Please try again later.").setActionRow().setEmbeds().queue();
                } else {
                    event.reply("Sorry, I was unable to finish executing that command. Please try again later.").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeAutoCompleteInteraction(AutoCompleteInterface template, CommandAutoCompleteInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeAutocomplete(event);
            } catch (final Exception e) {
                //we don't really care if this breaks tbh... I'll just log this
                logger.error("Unable to autocomplete the \"" + event.getFullCommandName() + "\" slash command");
            }
        });
    }

    private void executeButtonInteraction(Consumer<? super ButtonInteractionEvent> consumer, ButtonInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
               consumer.accept(event);
            }catch (final Exception e) {
                logger.error("Button interaction failed! Button ID: " + event.getId());
            }
        });
    }

    private void executeStringSelectInteraction(StringSelect template, StringSelectInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeStringSelectInteraction(event);
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeEntitySelectInteraction(EntitySelect template, EntitySelectInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeEntitySelectInteraction(event);
            } catch (Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeModalInteraction(ModalInterface template, ModalInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeModal(event);
            } catch (Exception e) {
                //this one is very important to catch. The modal will not close unless it gets handled.
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command. Please try again later").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeUserContextCommand(UserContextTemplate template, UserContextInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeUserContextCommand(event);
            } catch (final Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }

    private void executeMessageContextCommand(MessageContextTemplate template, MessageContextInteractionEvent event) {
        this.commandPool.submit(() -> {
            try {
                template.executeMessageContextCommand(event);
            } catch (final Exception e) {
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal("Sorry, I was unable to execute that command").queue();
                } else {
                    event.reply("Sorry, I was unable to execute that command. Please try again later").setEphemeral(true).queue();
                }
            }
        });
    }
}
