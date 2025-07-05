package net.minecraft.world.scores;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ScoreboardSaveData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String FILE_ID = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardSaveData(Scoreboard p_166101_) {
        this.scoreboard = p_166101_;
    }

    public ScoreboardSaveData load(CompoundTag p_166103_, HolderLookup.Provider p_330463_) {
        this.loadObjectives(p_166103_.getList("Objectives", 10), p_330463_);
        this.scoreboard.loadPlayerScores(p_166103_.getList("PlayerScores", 10), p_330463_);
        if (p_166103_.contains("DisplaySlots", 10)) {
            this.loadDisplaySlots(p_166103_.getCompound("DisplaySlots"));
        }

        if (p_166103_.contains("Teams", 9)) {
            this.loadTeams(p_166103_.getList("Teams", 10), p_330463_);
        }

        return this;
    }

    private void loadTeams(ListTag p_83525_, HolderLookup.Provider p_329597_) {
        for (int i = 0; i < p_83525_.size(); i++) {
            CompoundTag compoundtag = p_83525_.getCompound(i);
            String s = compoundtag.getString("Name");
            PlayerTeam playerteam = this.scoreboard.addPlayerTeam(s);
            Component component = Component.Serializer.fromJson(compoundtag.getString("DisplayName"), p_329597_);
            if (component != null) {
                playerteam.setDisplayName(component);
            }

            if (compoundtag.contains("TeamColor", 8)) {
                playerteam.setColor(ChatFormatting.getByName(compoundtag.getString("TeamColor")));
            }

            if (compoundtag.contains("AllowFriendlyFire", 99)) {
                playerteam.setAllowFriendlyFire(compoundtag.getBoolean("AllowFriendlyFire"));
            }

            if (compoundtag.contains("SeeFriendlyInvisibles", 99)) {
                playerteam.setSeeFriendlyInvisibles(compoundtag.getBoolean("SeeFriendlyInvisibles"));
            }

            if (compoundtag.contains("MemberNamePrefix", 8)) {
                Component component1 = Component.Serializer.fromJson(compoundtag.getString("MemberNamePrefix"), p_329597_);
                if (component1 != null) {
                    playerteam.setPlayerPrefix(component1);
                }
            }

            if (compoundtag.contains("MemberNameSuffix", 8)) {
                Component component2 = Component.Serializer.fromJson(compoundtag.getString("MemberNameSuffix"), p_329597_);
                if (component2 != null) {
                    playerteam.setPlayerSuffix(component2);
                }
            }

            if (compoundtag.contains("NameTagVisibility", 8)) {
                Team.Visibility team$visibility = Team.Visibility.byName(compoundtag.getString("NameTagVisibility"));
                if (team$visibility != null) {
                    playerteam.setNameTagVisibility(team$visibility);
                }
            }

            if (compoundtag.contains("DeathMessageVisibility", 8)) {
                Team.Visibility team$visibility1 = Team.Visibility.byName(compoundtag.getString("DeathMessageVisibility"));
                if (team$visibility1 != null) {
                    playerteam.setDeathMessageVisibility(team$visibility1);
                }
            }

            if (compoundtag.contains("CollisionRule", 8)) {
                Team.CollisionRule team$collisionrule = Team.CollisionRule.byName(compoundtag.getString("CollisionRule"));
                if (team$collisionrule != null) {
                    playerteam.setCollisionRule(team$collisionrule);
                }
            }

            this.loadTeamPlayers(playerteam, compoundtag.getList("Players", 8));
        }
    }

    private void loadTeamPlayers(PlayerTeam p_83515_, ListTag p_83516_) {
        for (int i = 0; i < p_83516_.size(); i++) {
            this.scoreboard.addPlayerToTeam(p_83516_.getString(i), p_83515_);
        }
    }

    private void loadDisplaySlots(CompoundTag p_83531_) {
        for (String s : p_83531_.getAllKeys()) {
            DisplaySlot displayslot = DisplaySlot.CODEC.byName(s);
            if (displayslot != null) {
                String s1 = p_83531_.getString(s);
                Objective objective = this.scoreboard.getObjective(s1);
                this.scoreboard.setDisplayObjective(displayslot, objective);
            }
        }
    }

    private void loadObjectives(ListTag p_83529_, HolderLookup.Provider p_329349_) {
        for (int i = 0; i < p_83529_.size(); i++) {
            CompoundTag compoundtag = p_83529_.getCompound(i);
            String s = compoundtag.getString("CriteriaName");
            ObjectiveCriteria objectivecriteria = ObjectiveCriteria.byName(s).orElseGet(() -> {
                LOGGER.warn("Unknown scoreboard criteria {}, replacing with {}", s, ObjectiveCriteria.DUMMY.getName());
                return ObjectiveCriteria.DUMMY;
            });
            String s1 = compoundtag.getString("Name");
            Component component = Component.Serializer.fromJson(compoundtag.getString("DisplayName"), p_329349_);
            ObjectiveCriteria.RenderType objectivecriteria$rendertype = ObjectiveCriteria.RenderType.byId(compoundtag.getString("RenderType"));
            boolean flag = compoundtag.getBoolean("display_auto_update");
            NumberFormat numberformat = NumberFormatTypes.CODEC
                .parse(p_329349_.createSerializationContext(NbtOps.INSTANCE), compoundtag.get("format"))
                .result()
                .orElse(null);
            this.scoreboard.addObjective(s1, objectivecriteria, component, objectivecriteria$rendertype, flag, numberformat);
        }
    }

    @Override
    public CompoundTag save(CompoundTag p_83527_, HolderLookup.Provider p_331672_) {
        p_83527_.put("Objectives", this.saveObjectives(p_331672_));
        p_83527_.put("PlayerScores", this.scoreboard.savePlayerScores(p_331672_));
        p_83527_.put("Teams", this.saveTeams(p_331672_));
        this.saveDisplaySlots(p_83527_);
        return p_83527_;
    }

    private ListTag saveTeams(HolderLookup.Provider p_330101_) {
        ListTag listtag = new ListTag();

        for (PlayerTeam playerteam : this.scoreboard.getPlayerTeams()) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("Name", playerteam.getName());
            compoundtag.putString("DisplayName", Component.Serializer.toJson(playerteam.getDisplayName(), p_330101_));
            if (playerteam.getColor().getId() >= 0) {
                compoundtag.putString("TeamColor", playerteam.getColor().getName());
            }

            compoundtag.putBoolean("AllowFriendlyFire", playerteam.isAllowFriendlyFire());
            compoundtag.putBoolean("SeeFriendlyInvisibles", playerteam.canSeeFriendlyInvisibles());
            compoundtag.putString("MemberNamePrefix", Component.Serializer.toJson(playerteam.getPlayerPrefix(), p_330101_));
            compoundtag.putString("MemberNameSuffix", Component.Serializer.toJson(playerteam.getPlayerSuffix(), p_330101_));
            compoundtag.putString("NameTagVisibility", playerteam.getNameTagVisibility().name);
            compoundtag.putString("DeathMessageVisibility", playerteam.getDeathMessageVisibility().name);
            compoundtag.putString("CollisionRule", playerteam.getCollisionRule().name);
            ListTag listtag1 = new ListTag();

            for (String s : playerteam.getPlayers()) {
                listtag1.add(StringTag.valueOf(s));
            }

            compoundtag.put("Players", listtag1);
            listtag.add(compoundtag);
        }

        return listtag;
    }

    private void saveDisplaySlots(CompoundTag p_83533_) {
        CompoundTag compoundtag = new CompoundTag();

        for (DisplaySlot displayslot : DisplaySlot.values()) {
            Objective objective = this.scoreboard.getDisplayObjective(displayslot);
            if (objective != null) {
                compoundtag.putString(displayslot.getSerializedName(), objective.getName());
            }
        }

        if (!compoundtag.isEmpty()) {
            p_83533_.put("DisplaySlots", compoundtag);
        }
    }

    private ListTag saveObjectives(HolderLookup.Provider p_335863_) {
        ListTag listtag = new ListTag();

        for (Objective objective : this.scoreboard.getObjectives()) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("Name", objective.getName());
            compoundtag.putString("CriteriaName", objective.getCriteria().getName());
            compoundtag.putString("DisplayName", Component.Serializer.toJson(objective.getDisplayName(), p_335863_));
            compoundtag.putString("RenderType", objective.getRenderType().getId());
            compoundtag.putBoolean("display_auto_update", objective.displayAutoUpdate());
            NumberFormat numberformat = objective.numberFormat();
            if (numberformat != null) {
                NumberFormatTypes.CODEC
                    .encodeStart(p_335863_.createSerializationContext(NbtOps.INSTANCE), numberformat)
                    .ifSuccess(p_309378_ -> compoundtag.put("format", p_309378_));
            }

            listtag.add(compoundtag);
        }

        return listtag;
    }
}