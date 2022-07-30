package com.softawii.capivara.listeners;

import com.softawii.capivara.core.DroneManager;
import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.utils.Utils;
import com.softawii.curupira.annotations.*;
import com.softawii.curupira.exceptions.MissingPermissionsException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

@IGroup(name = "Voice", description = "Voice", hidden = false)
public class VoiceGroup {

    private static final Logger LOGGER = LogManager.getLogger(VoiceGroup.class);

    @ISubGroup(name = "Dynamic", description = "Dynamic")
    public static class Dynamic extends ListenerAdapter {

        public static VoiceManager voiceManager;
        public static DroneManager droneManager;

        // region constants
        public static final String configModal = "voice-dynamic-config-modal";
        public static final String generateConfigModal = "voice-dynamic-generate-config-modal";

        public static final String droneConnect       = "voice-dynamic-drone-private-public";
        public static final String droneVisibility    = "voice-dynamic-drone-visibility";
        public static final String droneLimit         = "voice-dynamic-drone-limit";
        public static final String droneName          = "voice-dynamic-drone-name";

        public static final String droneInvite        = "voice-dynamic-drone-channel";
        public static final String droneKick          = "voice-dynamic-drone-kick";
        public static final String droneBan           = "voice-dynamic-drone-ban";

        // endregion

        //region Discord Commands

        /**
         * This command is used to put a 'Voice Dynamic Master' channel in a category
         *
         * @param event : SlashCommandInteractionEvent
         */
        @ICommand(name = "set", description = "Set dynamic voice channels to the selected category", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "category", description = "Category to add dynamic voice chat!", required = true, type = OptionType.CHANNEL)
        @SuppressWarnings({"ConstantConditions", "unused"})
        public static void set(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            // region Verify and Decode
            GuildChannelUnion channel = event.getOption("category").getAsChannel();

            if (channel.getType() != ChannelType.CATEGORY) {
                LOGGER.debug(method + " : error : non category : " + channel);
                event.reply("You need to pass a category!").queue();
                return;
            }

            // endregion

            Category category = channel.asCategory();

            //  Manager (business rule)
            try {
                VoiceHive hive = voiceManager.setDynamicCategory(category);

                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();

                VoiceChannel hiveChannel = guild.getVoiceChannelById(hive.getVoiceId());

                MessageEmbed embed = Utils.simpleEmbed("Voice Hive Initialized",
                        "Just click in " + hiveChannel.getAsMention() + " to create a dynamic channel!", Color.GREEN);

                event.replyEmbeds(embed).addActionRow(Button.primary(generateConfigModal + ":" + hiveChannel.getParentCategoryIdLong(), "Open Settings")).setEphemeral(true).queue();
            } catch (ExistingDynamicCategoryException e) {
                LOGGER.debug(method + " : error : " + e.getMessage());

                VoiceHive hive;
                try {
                    hive = voiceManager.find(category);
                } catch (KeyNotFoundException ex) {
                    // Wait, this is a bug!
                    LOGGER.debug(method + " : error : " + ex.getMessage());
                    return;
                }
                // Reply -> Never NULL (Guild Command)
                Guild        guild       = event.getGuild();
                VoiceChannel hiveChannel = guild.getVoiceChannelById(hive.getVoiceId());
                MessageEmbed embed = Utils.simpleEmbed("The current category is already a hive of voice channels!",
                        "Just click in " + hiveChannel.getAsMention() + " to create a dynamic channel!", Color.RED);
                event.replyEmbeds(embed).queue();
            }

            LOGGER.debug(method + " : end");
        }

        @ICommand(name = "unset", description = "Unset dynamic voice channels to the selected category", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "category", description = "Category to add dynamic voice chat!", required = true, type = OptionType.CHANNEL)
        @SuppressWarnings({"ConstantConditions", "unused"})
        public static void unset(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            // region Verify and Decode
            GuildChannelUnion channel = event.getOption("category").getAsChannel();

            if (channel.getType() != ChannelType.CATEGORY) {
                LOGGER.debug(method + " : error : non category : " + channel);
                event.reply("You need to pass a category!").queue();
                return;
            }

            // endregion

            Category category = channel.asCategory();

            //  Manager (business rule)
            try {
                voiceManager.unsetDynamicCategory(category);

                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();

                MessageEmbed embed = Utils.simpleEmbed("Voice Hive Uninitialized",
                        "The dynamic voice channels are now unset!", Color.GREEN);

                event.replyEmbeds(embed).queue();
            } catch (KeyNotFoundException e) {
                LOGGER.debug(method + " : error : " + e.getMessage());

                // Reply -> Never NULL (Guild Command)
                Guild guild = event.getGuild();
                MessageEmbed embed = Utils.simpleEmbed("The current category is not a hive of voice channels!",
                        "You need to set a dynamic voice channel first!", Color.RED);
                event.replyEmbeds(embed).queue();
            }

            LOGGER.debug(method + " : end");
        }

        @ICommand(name = "list", description = "List all dynamic voice channels categories")
        @SuppressWarnings({"ConstantConditions", "unused"})
        public static void list(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            long  guildId = event.getGuild().getIdLong();
            Guild guild   = event.getGuild();

            List<VoiceHive> hives = voiceManager.findAllByGuildId(guildId);

            StringBuilder sb = new StringBuilder();
            for (VoiceHive hive : hives) {
                VoiceChannel hiveChannel = guild.getVoiceChannelById(hive.getVoiceId());
                Category     parent      = hiveChannel.getParentCategory();

                if (hiveChannel == null || parent == null) {
                    LOGGER.debug(method + " : error : hive channel not found : " + hive.getVoiceId());
                    continue;
                }

                sb.append(hiveChannel.getParentCategory().getAsMention())
                        .append(" : ")
                        .append(hiveChannel.getAsMention())
                        .append("\n");
            }
            String message = sb.toString();

            MessageEmbed embed = Utils.simpleEmbed("Voice Hives", message, Color.GREEN);
            event.replyEmbeds(embed).queue();

            LOGGER.debug(method + " : end");
        }

        @ICommand(name = "config", description = "Configure dynamic voice channels", permissions = {Permission.ADMINISTRATOR})
        @IArgument(name = "category", description = "Category to configure your dynamic voice chat!", required = true, type = OptionType.CHANNEL)
        @SuppressWarnings({"unused"})
        public static void config(SlashCommandInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            // region Verify and Decode
            GuildChannelUnion channel = event.getOption("category").getAsChannel();

            if (channel.getType() != ChannelType.CATEGORY) {
                LOGGER.debug(method + " : error : non category : " + channel);
                event.reply("You need to pass a category!").queue();
                return;
            }

            // endregion

            Category category = channel.asCategory();

            //  Manager (business rule)
            Modal configModal = voiceManager.getConfigModal(category, Dynamic.configModal);

            if(configModal == null) {
                LOGGER.debug(method + " : error : config modal not found : " + category);
                event.reply("This category is not a dynamic voice channel!").queue();
                return;
            }

            event.replyModal(configModal).queue();
            LOGGER.debug(method + " : end");
        }

        //endregion

        // region Discord Modals, Buttons and Menus

        @IButton(id=generateConfigModal)
        public static void generateConfig(ButtonInteractionEvent event) {
            LOGGER.debug("generateConfig : start");
            String[] args = event.getComponentId().split(":");

            if(args.length != 2) {
                LOGGER.debug("generateConfig : error : invalid component id : " + event.getComponentId());
                return;
            }

            long categoryId = Long.parseLong(args[1]);
            Category category = event.getGuild().getCategoryById(categoryId);

            if(category == null) {
                LOGGER.debug("generateConfig : error : category not found : " + categoryId);
                return;
            }

            Modal configModal = voiceManager.getConfigModal(category, Dynamic.configModal);

            event.replyModal(configModal).queue();
        }

        @IModal(id = configModal)
        public static void configModal(ModalInteractionEvent event) {
            String method = new Throwable().getStackTrace()[0].getMethodName();
            LOGGER.debug(method + " : start");

            String strCategoryId = event.getModalId().split(":")[1];
            long   categoryId = Long.parseLong(strCategoryId);
            VoiceHive voiceHive;
            try {
                Category category = event.getGuild().getCategoryById(categoryId);

                if(category == null) {
                    LOGGER.debug(method + " : error : category not found : " + categoryId);
                    event.reply("Category not found!").setEphemeral(true).queue();
                    return;
                }
                voiceHive = voiceManager.setConfigModal(event, category);
            } catch (KeyNotFoundException e) {
                event.reply("This category is not a dynamic voice channel!").setEphemeral(true).queue();
                return;
            } catch(Exception e) {
                LOGGER.debug(method + " : error : " + e.getMessage());
                event.reply("An error occurred while configuring the dynamic voice channel!").setEphemeral(true).queue();
                return;
            }

            try {
                event.replyEmbeds(voiceHive.show(event.getGuild())).queue();
            } catch(Exception e) {
                LOGGER.debug(method + " : error : " + e.getMessage());
                event.reply("update!!").setEphemeral(true).queue();
            }

            LOGGER.debug(method + " : end");
        }

        @IButton(id=droneName)
        public static void renameDroneButton(ButtonInteractionEvent event) {
            MessageChannelUnion channel = event.getChannel();

            Modal modal = null;
            try {
                modal = droneManager.checkRenameDrone(event.getGuild(), channel, event.getMember(), droneName);
            } catch (KeyNotFoundException e) {
                event.reply("This channel is not a temporary channel!").queue();
            } catch (MissingPermissionsException e) {
                event.reply("You don't have the required permissions to rename this channel!").queue();
            }

            event.replyModal(modal).queue();
        }

        @IModal(id=droneName)
        public static void renameDroneModal(ModalInteractionEvent event) {
            try {
                String newName = droneManager.renameDrone(event);
                event.reply("Channel renamed!").setEphemeral(true).queue();

                if(event.getChannelType() == ChannelType.VOICE) {
                    voiceManager.createControlPanel(event.getChannel().asVoiceChannel(), event.getMember(), newName);
                }
            } catch(Exception e) {
                event.reply("An error occurred while renaming the channel!").setEphemeral(true).queue();
            }
        }

        //endregion


        //region Voice Events

        /**
         * This event is used to verify if the user is in a drone voice channel, if so, it will check to delete the drone
         *
         * @param event : VoiceChannelCreateEvent
         */
        @Override
        public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
            VoiceChannel joined = (VoiceChannel) event.getChannelJoined();
            VoiceChannel left   = (VoiceChannel) event.getChannelLeft();
            Member       member = event.getMember();

            if (joined != null) voiceManager.checkToCreateTemporary(joined, member);
            if (left != null) voiceManager.checkToDeleteTemporary(left, member);
        }

        @Override
        public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
            if (event.getChannel().getType() == ChannelType.CATEGORY) {
                Category category = event.getChannel().asCategory();
                if (voiceManager.isDynamicCategory(category)) {
                    try {
                        voiceManager.unsetDynamicCategory(category);
                    } catch (KeyNotFoundException e) {
                        // Ok... it's not a dynamic category
                    }
                }
            }


            if (event.getChannel().getType() == ChannelType.VOICE) {
                VoiceChannel channel = event.getChannel().asVoiceChannel();
                // Checking if it is a drone voice channel
                voiceManager.checkToDeleteTemporary(channel, null);

                // Checking if it is a hive voice channel
                Category hive        = channel.getParentCategory();
                long     snowflakeId = channel.getIdLong();

                if (hive != null) {
                    voiceManager.checkToDeleteHive(hive, snowflakeId);
                }
            }
        }

        @Override
        public void onChannelUpdateParent(@NotNull ChannelUpdateParentEvent event) {
            if (event.getChannelType() == ChannelType.VOICE) {
                VoiceChannel hive         = event.getChannel().asVoiceChannel();
                Category     hiveCategory = event.getOldValue();

                if (hiveCategory != null) {
                    try {
                        VoiceHive dbHive = voiceManager.find(hiveCategory);

                        if (dbHive.getVoiceId() == hive.getIdLong()) {
                            hive.getManager().setParent(hiveCategory).queue(q -> {
                            }, e -> {
                            });
                        }

                    } catch (KeyNotFoundException e) {
                        // The category is not a hive, just ignore it...
                    }
                }
            }
        }

        //endregion
    }
}
