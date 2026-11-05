package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.entity.BaseSmithingAnvilBlockEntity;
import net.minecraft.util.RandomSource;

public abstract class BaseSmithingAnvilBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    protected BaseSmithingAnvilBlock(Properties properties) {
        super(properties);
    }

    public abstract void setQuality(ForgingQuality forgingQuality);

    public abstract ForgingQuality getQuality();

    @Override
    public abstract VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext ctx
    );

    @Override
    public abstract BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    );

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Minecraft 26.3 removes the block entity before this callback; inventory
     * drops happen in BaseSmithingAnvilBlockEntity.preRemoveSideEffects.
     * Only update adjacent hoppers/comparators here.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level,
                                               BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    protected void spawnHitParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            RandomSource random = server.getRandom();

            for (int i = 0; i < 6; i++) {
                double x = pos.getX() + 0.5 + random.nextFloat() - 0.5;
                double y = pos.getY() + 1 + random.nextFloat() * 0.5;
                double z = pos.getZ() + 0.5 + random.nextFloat() - 0.5;

                server.sendParticles(
                        new DustParticleOptions(0xFF8000, 1.0F),
                        x, y, z,
                        1,
                        0, 0.1, 0,
                        1
                );

                server.sendParticles(
                        ParticleTypes.CRIT,
                        x, y, z,
                        1,
                        0, 0.1, 0,
                        1
                );
            }
        }
    }
}
