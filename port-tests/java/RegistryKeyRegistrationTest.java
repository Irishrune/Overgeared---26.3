import net.stirdrem.overgeared.registry.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import java.util.List;

/** Exercises the actual deferred-registration source against isolated Minecraft API stubs. */
public final class RegistryKeyRegistrationTest {
    private static void check(boolean b, String msg) {if (!b) throw new AssertionError(msg);}

    public static void main(String[] args) {
        var items=DeferredRegister.createItems("overgeared");
        var visited=new ArrayList<String>();
        var steel=items.registerWithKey("steel_ingot", key -> {
            visited.add(key.location().path());
            return new Item(new Item.Properties().setId(key));
        });
        var handle=items.registerSimpleItem("handle");
        // Delayed factories must not be called during declaration.
        check(visited.isEmpty(),"Factory ran before register()");
        items.register();
        check(visited.equals(List.of("steel_ingot")),"Registry factory was not called once in order");
        check(steel.get().id.equals(steel.getKey()),"Steel key differs from registered id");
        check(handle.get().id.equals(handle.getKey()),"Simple item did not receive its id");

        var blocks=DeferredRegister.createBlocks("overgeared");
        var anvil=blocks.registerWithKey("smithing_anvil", Block::new);
        blocks.register();
        check(anvil.get().id.equals(anvil.getKey()),"Block constructor did not receive its id");

        var blockItems=DeferredRegister.createItems("overgeared");
        var blockItem=blockItems.registerSimpleBlockItem(anvil);
        blockItems.register();
        check(blockItem.get().id.equals(blockItem.getKey()),"BlockItem did not receive its id");
        check(blockItem.get().block==anvil.get(),"BlockItem points to wrong block");

        boolean duplicateRejected=false;
        try {
            var duplicates=DeferredRegister.createItems("overgeared");
            duplicates.registerSimpleItem("duplicate");
            duplicates.registerSimpleItem("duplicate");
        } catch (IllegalArgumentException expected) {duplicateRejected=true;}
        check(duplicateRejected,"Duplicate ids should be rejected");

        boolean lateRejected=false;
        try {items.registerSimpleItem("late");}
        catch (IllegalStateException expected) {lateRejected=true;}
        check(lateRejected,"Registration after registry lock should be rejected");
        System.out.println("RegistryKeyRegistrationTest: passed");
    }
}
