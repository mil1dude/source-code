package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements ContainerSingleItem.BlockContainerSingleItem {
    public static final String SONG_ITEM_TAG_ID = "RecordItem";
    public static final String TICKS_SINCE_SONG_STARTED_TAG_ID = "ticks_since_song_started";
    private ItemStack item = ItemStack.EMPTY;
    private final JukeboxSongPlayer jukeboxSongPlayer = new JukeboxSongPlayer(this::onSongChanged, this.getBlockPos());

    public JukeboxBlockEntity(BlockPos p_155613_, BlockState p_155614_) {
        super(BlockEntityType.JUKEBOX, p_155613_, p_155614_);
    }

    public JukeboxSongPlayer getSongPlayer() {
        return this.jukeboxSongPlayer;
    }

    public void onSongChanged() {
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }

    private void notifyItemChangedInJukebox(boolean p_342785_) {
        if (this.level != null && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(p_342785_)), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
        }
    }

    public void popOutTheItem() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            if (!itemstack.isEmpty()) {
                this.removeTheItem();
                Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7F);
                ItemStack itemstack1 = itemstack.copy();
                ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
                itementity.setDefaultPickUpDelay();
                this.level.addFreshEntity(itementity);
            }
        }
    }

    public static void tick(Level p_273615_, BlockPos p_273143_, BlockState p_273372_, JukeboxBlockEntity p_343932_) {
        p_343932_.jukeboxSongPlayer.tick(p_273615_, p_273372_);
    }

    public int getComparatorOutput() {
        return JukeboxSong.fromStack(this.level.registryAccess(), this.item).map(Holder::value).map(JukeboxSong::comparatorOutput).orElse(0);
    }

    @Override
    protected void loadAdditional(CompoundTag p_329712_, HolderLookup.Provider p_330255_) {
        super.loadAdditional(p_329712_, p_330255_);
        if (p_329712_.contains("RecordItem", 10)) {
            this.item = ItemStack.parse(p_330255_, p_329712_.getCompound("RecordItem")).orElse(ItemStack.EMPTY);
        } else {
            if (!this.item.isEmpty()) {
                this.jukeboxSongPlayer.stop(this.level, this.getBlockState());
            }

            this.item = ItemStack.EMPTY;
        }

        if (p_329712_.contains("ticks_since_song_started", 4)) {
            JukeboxSong.fromStack(p_330255_, this.item)
                .ifPresent(p_342562_ -> this.jukeboxSongPlayer.setSongWithoutPlaying((Holder<JukeboxSong>)p_342562_, p_329712_.getLong("ticks_since_song_started")));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag p_187507_, HolderLookup.Provider p_332390_) {
        super.saveAdditional(p_187507_, p_332390_);
        if (!this.getTheItem().isEmpty()) {
            p_187507_.put("RecordItem", this.getTheItem().save(p_332390_));
        }

        if (this.jukeboxSongPlayer.getSong() != null) {
            p_187507_.putLong("ticks_since_song_started", this.jukeboxSongPlayer.getTicksSinceSongStarted());
        }
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int p_309876_) {
        ItemStack itemstack = this.item;
        this.setTheItem(ItemStack.EMPTY);
        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack p_309430_) {
        this.item = p_309430_;
        boolean flag = !this.item.isEmpty();
        Optional<Holder<JukeboxSong>> optional = JukeboxSong.fromStack(this.level.registryAccess(), this.item);
        this.notifyItemChangedInJukebox(flag);
        if (flag && optional.isPresent()) {
            this.jukeboxSongPlayer.play(this.level, optional.get());
        } else {
            this.jukeboxSongPlayer.stop(this.level, this.getBlockState());
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public boolean canPlaceItem(int p_273369_, ItemStack p_273689_) {
        return p_273689_.has(DataComponents.JUKEBOX_PLAYABLE) && this.getItem(p_273369_).isEmpty();
    }

    @Override
    public boolean canTakeItem(Container p_273497_, int p_273168_, ItemStack p_273785_) {
        return p_273497_.hasAnyMatching(ItemStack::isEmpty);
    }

    @VisibleForTesting
    public void setSongItemWithoutPlaying(ItemStack p_343692_) {
        this.item = p_343692_;
        JukeboxSong.fromStack(this.level.registryAccess(), p_343692_).ifPresent(p_343857_ -> this.jukeboxSongPlayer.setSongWithoutPlaying((Holder<JukeboxSong>)p_343857_, 0L));
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }

    @VisibleForTesting
    public void tryForcePlaySong() {
        JukeboxSong.fromStack(this.level.registryAccess(), this.getTheItem())
            .ifPresent(p_343793_ -> this.jukeboxSongPlayer.play(this.level, (Holder<JukeboxSong>)p_343793_));
    }
}