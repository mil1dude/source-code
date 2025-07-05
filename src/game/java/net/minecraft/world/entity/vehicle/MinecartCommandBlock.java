package net.minecraft.world.entity.vehicle;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartCommandBlock extends AbstractMinecart {
    static final EntityDataAccessor<String> DATA_ID_COMMAND_NAME = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.STRING);
    static final EntityDataAccessor<Component> DATA_ID_LAST_OUTPUT = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.COMPONENT);
    private final BaseCommandBlock commandBlock = new MinecartCommandBlock.MinecartCommandBase();
    private static final int ACTIVATION_DELAY = 4;
    private int lastActivated;

    public MinecartCommandBlock(EntityType<? extends MinecartCommandBlock> p_38509_, Level p_38510_) {
        super(p_38509_, p_38510_);
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.COMMAND_BLOCK_MINECART);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_333825_) {
        super.defineSynchedData(p_333825_);
        p_333825_.define(DATA_ID_COMMAND_NAME, "");
        p_333825_.define(DATA_ID_LAST_OUTPUT, CommonComponents.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_38525_) {
        super.readAdditionalSaveData(p_38525_);
        this.commandBlock.load(p_38525_, this.registryAccess());
        this.getEntityData().set(DATA_ID_COMMAND_NAME, this.getCommandBlock().getCommand());
        this.getEntityData().set(DATA_ID_LAST_OUTPUT, this.getCommandBlock().getLastOutput());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_38529_) {
        super.addAdditionalSaveData(p_38529_);
        this.commandBlock.save(p_38529_, this.registryAccess());
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.COMMAND_BLOCK.defaultBlockState();
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    @Override
    public void activateMinecart(int p_38517_, int p_38518_, int p_38519_, boolean p_38520_) {
        if (p_38520_ && this.tickCount - this.lastActivated >= 4) {
            this.getCommandBlock().performCommand(this.level());
            this.lastActivated = this.tickCount;
        }
    }

    @Override
    public InteractionResult interact(Player p_38522_, InteractionHand p_38523_) {
        return this.commandBlock.usedBy(p_38522_);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_38527_) {
        super.onSyncedDataUpdated(p_38527_);
        if (DATA_ID_LAST_OUTPUT.equals(p_38527_)) {
            try {
                this.commandBlock.setLastOutput(this.getEntityData().get(DATA_ID_LAST_OUTPUT));
            } catch (Throwable throwable) {
            }
        } else if (DATA_ID_COMMAND_NAME.equals(p_38527_)) {
            this.commandBlock.setCommand(this.getEntityData().get(DATA_ID_COMMAND_NAME));
        }
    }

    public class MinecartCommandBase extends BaseCommandBlock {
        @Override
        public ServerLevel getLevel() {
            return (ServerLevel)MinecartCommandBlock.this.level();
        }

        @Override
        public void onUpdated() {
            MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_COMMAND_NAME, this.getCommand());
            MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_LAST_OUTPUT, this.getLastOutput());
        }

        @Override
        public Vec3 getPosition() {
            return MinecartCommandBlock.this.position();
        }

        public MinecartCommandBlock getMinecart() {
            return MinecartCommandBlock.this;
        }

        @Override
        public CommandSourceStack createCommandSourceStack() {
            return new CommandSourceStack(
                this,
                MinecartCommandBlock.this.position(),
                MinecartCommandBlock.this.getRotationVector(),
                this.getLevel(),
                2,
                this.getName().getString(),
                MinecartCommandBlock.this.getDisplayName(),
                this.getLevel().getServer(),
                MinecartCommandBlock.this
            );
        }

        @Override
        public boolean isValid() {
            return !MinecartCommandBlock.this.isRemoved();
        }
    }
}