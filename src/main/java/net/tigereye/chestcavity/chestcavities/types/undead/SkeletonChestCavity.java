package net.tigereye.chestcavity.chestcavities.types.undead;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory;
import net.tigereye.chestcavity.chestcavities.ChestCavityType;
import net.tigereye.chestcavity.chestcavities.types.BaseChestCavity;
import net.tigereye.chestcavity.registration.CCItems;
import net.tigereye.chestcavity.registration.CCOrganScores;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SkeletonChestCavity extends BaseChestCavity implements ChestCavityType {
    @Override
    public void fillChestCavityInventory(ChestCavityInventory chestCavity) {
        chestCavity.clear();
        chestCavity.setStack(1, new ItemStack(CCItems.ROTTEN_RIB, CCItems.ROTTEN_RIB.getMaxCount()));
        chestCavity.setStack(2, ItemStack.EMPTY);
        chestCavity.setStack(3, ItemStack.EMPTY);
        chestCavity.setStack(4, ItemStack.EMPTY);
        chestCavity.setStack(5, ItemStack.EMPTY);
        chestCavity.setStack(6, ItemStack.EMPTY);
        chestCavity.setStack(7, new ItemStack(CCItems.ROTTEN_RIB, CCItems.ROTTEN_RIB.getMaxCount()));
        chestCavity.setStack(10, new ItemStack(CCItems.ROTTEN_RIB, CCItems.ROTTEN_RIB.getMaxCount()));
        chestCavity.setStack(11, ItemStack.EMPTY);
        chestCavity.setStack(12, ItemStack.EMPTY);
        chestCavity.setStack(13, new ItemStack(CCItems.ROTTEN_SPINE, CCItems.ROTTEN_SPINE.getMaxCount()));
        chestCavity.setStack(14, ItemStack.EMPTY);
        chestCavity.setStack(15, ItemStack.EMPTY);
        chestCavity.setStack(16, new ItemStack(CCItems.ROTTEN_RIB, CCItems.ROTTEN_RIB.getMaxCount()));
    }

    @Override
    public void shapeChestCavity() {
        forbiddenSlots.add(0);
        forbiddenSlots.add(2);
        forbiddenSlots.add(3);
        forbiddenSlots.add(4);
        forbiddenSlots.add(5);
        forbiddenSlots.add(6);
        forbiddenSlots.add(8);
        forbiddenSlots.add(9);
        forbiddenSlots.add(11);
        forbiddenSlots.add(12);
        forbiddenSlots.add(14);
        forbiddenSlots.add(15);
        forbiddenSlots.add(17);
        forbiddenSlots.add(18);
        forbiddenSlots.add(19);
        forbiddenSlots.add(20);
        forbiddenSlots.add(21);
        forbiddenSlots.add(22);
        forbiddenSlots.add(23);
        forbiddenSlots.add(24);
        forbiddenSlots.add(25);
        forbiddenSlots.add(26);
    }

    @Override
    public void loadBaseOrganScores(Map<Identifier, Float> organScores){
        organScores.clear();
        organScores.put(CCOrganScores.LUCK, 1f);
        organScores.put(CCOrganScores.DEFENSE, 2.375f);
        organScores.put(CCOrganScores.HEALTH, 1f);
        organScores.put(CCOrganScores.NUTRITION, 4f);
        organScores.put(CCOrganScores.FILTRATION, 2f);
        organScores.put(CCOrganScores.DETOXIFICATION, 1f);
        organScores.put(CCOrganScores.STRENGTH, 8f);
        organScores.put(CCOrganScores.SPEED, 8f);
        organScores.put(CCOrganScores.NERVOUS_SYSTEM, .5f);
        organScores.put(CCOrganScores.METABOLISM, 1f);
        organScores.put(CCOrganScores.DIGESTION, 1f);
        organScores.put(CCOrganScores.BREATH, 2f);
        organScores.put(CCOrganScores.ENDURANCE, 2f);
    }

    @Override
    public void generateRareOrganDrops(Random random, int looting, List<ItemStack> loot) {
        LinkedList<Item> organPile = new LinkedList<>();
        for(int i = 0; i < 4; i++){
            organPile.add(CCItems.ROTTEN_RIB);
        }
        organPile.add(CCItems.ROTTEN_SPINE);
        int rolls = 1 + random.nextInt(1) + random.nextInt(1);
        for (int i = 0; i < rolls; i++){
            int roll = random.nextInt(organPile.size());
            int count = 1;
            Item rolledItem = organPile.get(roll);
            if(rolledItem.getMaxCount() > 1){
                count += random.nextInt(rolledItem.getMaxCount());
            }
            loot.add(new ItemStack(organPile.remove(roll),count));
        }
    }

}
