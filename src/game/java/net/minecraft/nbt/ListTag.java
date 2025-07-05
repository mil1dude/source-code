package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
    private static final int SELF_SIZE_IN_BYTES = 37;
    public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>() {
        public ListTag load(DataInput p_128792_, NbtAccounter p_128794_) throws IOException {
            p_128794_.pushDepth();

            ListTag listtag;
            try {
                listtag = loadList(p_128792_, p_128794_);
            } finally {
                p_128794_.popDepth();
            }

            return listtag;
        }

        private static ListTag loadList(DataInput p_301758_, NbtAccounter p_301694_) throws IOException {
            p_301694_.accountBytes(37L);
            byte b0 = p_301758_.readByte();
            int i = p_301758_.readInt();
            if (b0 == 0 && i > 0) {
                throw new NbtFormatException("Missing type on ListTag");
            } else {
                p_301694_.accountBytes(4L, (long)i);
                TagType<?> tagtype = TagTypes.getType(b0);
                List<Tag> list = Lists.newArrayListWithCapacity(i);

                for (int j = 0; j < i; j++) {
                    list.add(tagtype.load(p_301758_, p_301694_));
                }

                return new ListTag(list, b0);
            }
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput p_197491_, StreamTagVisitor p_197492_, NbtAccounter p_301731_) throws IOException {
            p_301731_.pushDepth();

            StreamTagVisitor.ValueResult streamtagvisitor$valueresult;
            try {
                streamtagvisitor$valueresult = parseList(p_197491_, p_197492_, p_301731_);
            } finally {
                p_301731_.popDepth();
            }

            return streamtagvisitor$valueresult;
        }

        private static StreamTagVisitor.ValueResult parseList(DataInput p_301745_, StreamTagVisitor p_301695_, NbtAccounter p_301734_) throws IOException {
            p_301734_.accountBytes(37L);
            TagType<?> tagtype = TagTypes.getType(p_301745_.readByte());
            int i = p_301745_.readInt();
            switch (p_301695_.visitList(tagtype, i)) {
                case HALT:
                    return StreamTagVisitor.ValueResult.HALT;
                case BREAK:
                    tagtype.skip(p_301745_, i, p_301734_);
                    return p_301695_.visitContainerEnd();
                default:
                    p_301734_.accountBytes(4L, (long)i);
                    int j = 0;

                    while (true) {
                        label41: {
                            if (j < i) {
                                switch (p_301695_.visitElement(tagtype, j)) {
                                    case HALT:
                                        return StreamTagVisitor.ValueResult.HALT;
                                    case BREAK:
                                        tagtype.skip(p_301745_, p_301734_);
                                        break;
                                    case SKIP:
                                        tagtype.skip(p_301745_, p_301734_);
                                        break label41;
                                    default:
                                        switch (tagtype.parse(p_301745_, p_301695_, p_301734_)) {
                                            case HALT:
                                                return StreamTagVisitor.ValueResult.HALT;
                                            case BREAK:
                                                break;
                                            default:
                                                break label41;
                                        }
                                }
                            }

                            int k = i - 1 - j;
                            if (k > 0) {
                                tagtype.skip(p_301745_, k, p_301734_);
                            }

                            return p_301695_.visitContainerEnd();
                        }

                        j++;
                    }
            }
        }

        @Override
        public void skip(DataInput p_301743_, NbtAccounter p_301728_) throws IOException {
            p_301728_.pushDepth();

            try {
                TagType<?> tagtype = TagTypes.getType(p_301743_.readByte());
                int i = p_301743_.readInt();
                tagtype.skip(p_301743_, i, p_301728_);
            } finally {
                p_301728_.popDepth();
            }
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }
    };
    private final List<Tag> list;
    private byte type;

    ListTag(List<Tag> p_128721_, byte p_128722_) {
        this.list = p_128721_;
        this.type = p_128722_;
    }

    public ListTag() {
        this(Lists.newArrayList(), (byte)0);
    }

    @Override
    public void write(DataOutput p_128734_) throws IOException {
        if (this.list.isEmpty()) {
            this.type = 0;
        } else {
            this.type = this.list.get(0).getId();
        }

        p_128734_.writeByte(this.type);
        p_128734_.writeInt(this.list.size());

        for (Tag tag : this.list) {
            tag.write(p_128734_);
        }
    }

    @Override
    public int sizeInBytes() {
        int i = 37;
        i += 4 * this.list.size();

        for (Tag tag : this.list) {
            i += tag.sizeInBytes();
        }

        return i;
    }

    @Override
    public byte getId() {
        return 9;
    }

    @Override
    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    private void updateTypeAfterRemove() {
        if (this.list.isEmpty()) {
            this.type = 0;
        }
    }

    @Override
    public Tag remove(int p_128751_) {
        Tag tag = this.list.remove(p_128751_);
        this.updateTypeAfterRemove();
        return tag;
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public CompoundTag getCompound(int p_128729_) {
        if (p_128729_ >= 0 && p_128729_ < this.list.size()) {
            Tag tag = this.list.get(p_128729_);
            if (tag.getId() == 10) {
                return (CompoundTag)tag;
            }
        }

        return new CompoundTag();
    }

    public ListTag getList(int p_128745_) {
        if (p_128745_ >= 0 && p_128745_ < this.list.size()) {
            Tag tag = this.list.get(p_128745_);
            if (tag.getId() == 9) {
                return (ListTag)tag;
            }
        }

        return new ListTag();
    }

    public short getShort(int p_128758_) {
        if (p_128758_ >= 0 && p_128758_ < this.list.size()) {
            Tag tag = this.list.get(p_128758_);
            if (tag.getId() == 2) {
                return ((ShortTag)tag).getAsShort();
            }
        }

        return 0;
    }

    public int getInt(int p_128764_) {
        if (p_128764_ >= 0 && p_128764_ < this.list.size()) {
            Tag tag = this.list.get(p_128764_);
            if (tag.getId() == 3) {
                return ((IntTag)tag).getAsInt();
            }
        }

        return 0;
    }

    public int[] getIntArray(int p_128768_) {
        if (p_128768_ >= 0 && p_128768_ < this.list.size()) {
            Tag tag = this.list.get(p_128768_);
            if (tag.getId() == 11) {
                return ((IntArrayTag)tag).getAsIntArray();
            }
        }

        return new int[0];
    }

    public long[] getLongArray(int p_177992_) {
        if (p_177992_ >= 0 && p_177992_ < this.list.size()) {
            Tag tag = this.list.get(p_177992_);
            if (tag.getId() == 12) {
                return ((LongArrayTag)tag).getAsLongArray();
            }
        }

        return new long[0];
    }

    public double getDouble(int p_128773_) {
        if (p_128773_ >= 0 && p_128773_ < this.list.size()) {
            Tag tag = this.list.get(p_128773_);
            if (tag.getId() == 6) {
                return ((DoubleTag)tag).getAsDouble();
            }
        }

        return 0.0;
    }

    public float getFloat(int p_128776_) {
        if (p_128776_ >= 0 && p_128776_ < this.list.size()) {
            Tag tag = this.list.get(p_128776_);
            if (tag.getId() == 5) {
                return ((FloatTag)tag).getAsFloat();
            }
        }

        return 0.0F;
    }

    public String getString(int p_128779_) {
        if (p_128779_ >= 0 && p_128779_ < this.list.size()) {
            Tag tag = this.list.get(p_128779_);
            return tag.getId() == 8 ? tag.getAsString() : tag.toString();
        } else {
            return "";
        }
    }

    @Override
    public int size() {
        return this.list.size();
    }

    public Tag get(int p_128781_) {
        return this.list.get(p_128781_);
    }

    @Override
    public Tag set(int p_128760_, Tag p_128761_) {
        Tag tag = this.get(p_128760_);
        if (!this.setTag(p_128760_, p_128761_)) {
            throw new UnsupportedOperationException(
                String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", p_128761_.getId(), this.type)
            );
        } else {
            return tag;
        }
    }

    @Override
    public void add(int p_128753_, Tag p_128754_) {
        if (!this.addTag(p_128753_, p_128754_)) {
            throw new UnsupportedOperationException(
                String.format(Locale.ROOT, "Trying to add tag of type %d to list of %d", p_128754_.getId(), this.type)
            );
        }
    }

    @Override
    public boolean setTag(int p_128731_, Tag p_128732_) {
        if (this.updateType(p_128732_)) {
            this.list.set(p_128731_, p_128732_);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int p_128747_, Tag p_128748_) {
        if (this.updateType(p_128748_)) {
            this.list.add(p_128747_, p_128748_);
            return true;
        } else {
            return false;
        }
    }

    private boolean updateType(Tag p_128739_) {
        if (p_128739_.getId() == 0) {
            return false;
        } else if (this.type == 0) {
            this.type = p_128739_.getId();
            return true;
        } else {
            return this.type == p_128739_.getId();
        }
    }

    public ListTag copy() {
        Iterable<Tag> iterable = (Iterable<Tag>)(TagTypes.getType(this.type).isValue()
            ? this.list
            : Iterables.transform(this.list, Tag::copy));
        List<Tag> list = Lists.newArrayList(iterable);
        return new ListTag(list, this.type);
    }

    @Override
    public boolean equals(Object p_128766_) {
        return this == p_128766_ ? true : p_128766_ instanceof ListTag && Objects.equals(this.list, ((ListTag)p_128766_).list);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public void accept(TagVisitor p_177990_) {
        p_177990_.visitList(this);
    }

    @Override
    public byte getElementType() {
        return this.type;
    }

    @Override
    public void clear() {
        this.list.clear();
        this.type = 0;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor p_197487_) {
        switch (p_197487_.visitList(TagTypes.getType(this.type), this.list.size())) {
            case HALT:
                return StreamTagVisitor.ValueResult.HALT;
            case BREAK:
                return p_197487_.visitContainerEnd();
            default:
                int i = 0;

                while (i < this.list.size()) {
                    Tag tag = this.list.get(i);
                    switch (p_197487_.visitElement(tag.getType(), i)) {
                        case HALT:
                            return StreamTagVisitor.ValueResult.HALT;
                        case BREAK:
                            return p_197487_.visitContainerEnd();
                        default:
                            switch (tag.accept(p_197487_)) {
                                case HALT:
                                    return StreamTagVisitor.ValueResult.HALT;
                                case BREAK:
                                    return p_197487_.visitContainerEnd();
                            }
                        case SKIP:
                            i++;
                    }
                }

                return p_197487_.visitContainerEnd();
        }
    }
}