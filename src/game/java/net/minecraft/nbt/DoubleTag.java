package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class DoubleTag extends NumericTag {
    private static final int SELF_SIZE_IN_BYTES = 16;
    public static final DoubleTag ZERO = new DoubleTag(0.0);
    public static final TagType<DoubleTag> TYPE = new TagType.StaticSize<DoubleTag>() {
        public DoubleTag load(DataInput p_128529_, NbtAccounter p_128531_) throws IOException {
            return DoubleTag.valueOf(readAccounted(p_128529_, p_128531_));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput p_197454_, StreamTagVisitor p_197455_, NbtAccounter p_301767_) throws IOException {
            return p_197455_.visit(readAccounted(p_197454_, p_301767_));
        }

        private static double readAccounted(DataInput p_301722_, NbtAccounter p_301770_) throws IOException {
            p_301770_.accountBytes(16L);
            return p_301722_.readDouble();
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Double";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private final double data;

    private DoubleTag(double p_128498_) {
        this.data = p_128498_;
    }

    public static DoubleTag valueOf(double p_128501_) {
        return p_128501_ == 0.0 ? ZERO : new DoubleTag(p_128501_);
    }

    @Override
    public void write(DataOutput p_128503_) throws IOException {
        p_128503_.writeDouble(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 16;
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public TagType<DoubleTag> getType() {
        return TYPE;
    }

    public DoubleTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object p_128512_) {
        return this == p_128512_ ? true : p_128512_ instanceof DoubleTag && this.data == ((DoubleTag)p_128512_).data;
    }

    @Override
    public int hashCode() {
        long i = Double.doubleToLongBits(this.data);
        return (int)(i ^ i >>> 32);
    }

    @Override
    public void accept(TagVisitor p_177860_) {
        p_177860_.visitDouble(this);
    }

    @Override
    public long getAsLong() {
        return (long)Math.floor(this.data);
    }

    @Override
    public int getAsInt() {
        return Mth.floor(this.data);
    }

    @Override
    public short getAsShort() {
        return (short)(Mth.floor(this.data) & 65535);
    }

    @Override
    public byte getAsByte() {
        return (byte)(Mth.floor(this.data) & 0xFF);
    }

    @Override
    public double getAsDouble() {
        return this.data;
    }

    @Override
    public float getAsFloat() {
        return (float)this.data;
    }

    @Override
    public Number getAsNumber() {
        return this.data;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor p_197452_) {
        return p_197452_.visit(this.data);
    }
}