package immersive_paintings.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.*;

public class SerializableNbt implements Serializable {
    @Serial
    private static final long serialVersionUID = 1023636697251332929L;

    transient private NbtCompound nbt;

    public SerializableNbt(NbtCompound nbt) {
        this.nbt = nbt;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        NbtIo.write(nbt, out);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        nbt = NbtIo.read(in);
    }

    public NbtCompound getNbt() {
        return nbt;
    }
}
