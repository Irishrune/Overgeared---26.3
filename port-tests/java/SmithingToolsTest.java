import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.item.custom.Tongs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.ArrayList;

public class SmithingToolsTest {
  public static void main(String[] args) {
    var hammer = new SmithingHammer(new Item.Properties());
    var tongs = new Tongs(new Item.Properties());
    var level = new Level(); var pos = new BlockPos(0,64,0);
    var player = new Player(false);
    var state = new BlockState(1f);
    var hammerStack = new ItemStack();
    if (!hammer.mineBlock(hammerStack,level,state,pos,player) || hammerStack.damage != 2) throw new AssertionError("hammer wear");
    hammer.mineBlock(hammerStack,level,new BlockState(0f),pos,player);
    if(hammerStack.damage != 2) throw new AssertionError("zero hardness should not wear hammer");
    var tongsStack = new ItemStack();
    if(!tongs.mineBlock(tongsStack,level,state,pos,player) || tongsStack.damage!=2) throw new AssertionError("tongs wear");
    if(!tongs.canDestroyBlock(tongsStack,state,level,pos,player)) throw new AssertionError("survival use");
    if(tongs.canDestroyBlock(tongsStack,state,level,pos,new Player(true))) throw new AssertionError("creative use");
    var lines = new ArrayList<Component>();
    tongs.appendHoverText(tongsStack,new Item.TooltipContext(),new TooltipDisplay(),lines::add,new TooltipFlag());
    if(lines.size()!=1 || !lines.get(0).text().equals("tooltip.overgeared.tongs.tooltip")) throw new AssertionError("tongs tooltip");
    System.out.println("SmithingToolsTest passed");
  }
}
