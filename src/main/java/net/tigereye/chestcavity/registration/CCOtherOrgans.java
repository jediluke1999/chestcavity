package net.tigereye.chestcavity.registration;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CCOtherOrgans {

    public static Map<Item,Map<Identifier,Float>> map = new HashMap<>();
    public static Map<Tag<Item>,Map<Identifier,Float>> tagMap = new HashMap<>();

    public static void init(){
        Map<Identifier,Float> dirt = new HashMap<>();
        dirt.put(CCOrganScores.LUCK,1f/27);
        dirt.put(CCOrganScores.HEALTH,1f/27);
        dirt.put(CCOrganScores.STRENGTH,8f/27);
        dirt.put(CCOrganScores.SPEED,8f/27);
        dirt.put(CCOrganScores.NERVOUS_SYSTEM,1f/27);
        dirt.put(CCOrganScores.DETOXIFICATION,1f/27);
        dirt.put(CCOrganScores.FILTRATION,2f/27);
        dirt.put(CCOrganScores.ENDURANCE,2f/27);
        dirt.put(CCOrganScores.METABOLISM,1f/27);
        dirt.put(CCOrganScores.BREATH,2f/27);
        dirt.put(CCOrganScores.NUTRITION,4f/27);
        dirt.put(CCOrganScores.DEFENSE,4f/27);
        dirt.put(CCOrganScores.DIGESTION,1f/27);
        map.put(Items.DIRT,dirt);

        Map<Identifier,Float> rottenFlesh = new HashMap<>();
        rottenFlesh.put(CCOrganScores.STRENGTH,.5f);
        rottenFlesh.put(CCOrganScores.SPEED,.5f);
        map.put(Items.ROTTEN_FLESH,rottenFlesh);

        Map<Identifier,Float> animalFlesh = new HashMap<>();
        animalFlesh.put(CCOrganScores.STRENGTH,.75f);
        animalFlesh.put(CCOrganScores.SPEED,.75f);
        map.put(Items.BEEF,animalFlesh);
        map.put(Items.PORKCHOP,animalFlesh);
        map.put(Items.MUTTON,animalFlesh);

        Map<Identifier,Float> defense = new HashMap<>();
        defense.put(CCOrganScores.DEFENSE,.5f);
        map.put(Items.BONE,defense);
        //map.put(Items.IRON_BARS,defense);

        Map<Identifier,Float> ironbars = new HashMap<>();
        ironbars.put(CCOrganScores.DEFENSE,1.25f);
        ironbars.put(CCOrganScores.BUOYANT,-.5f);
        ironbars.put(CCOrganScores.SPEED,-.25f);
        ironbars.put(CCOrganScores.FIRE_RESISTANT,1f);
        map.put(Items.IRON_BARS,ironbars);

        Map<Identifier,Float> ironblock = new HashMap<>();
        ironblock.put(CCOrganScores.DEFENSE,2f);
        ironblock.put(CCOrganScores.BUOYANT,-1f);
        ironblock.put(CCOrganScores.SPEED,-1f);
        ironblock.put(CCOrganScores.FIRE_RESISTANT,1f);
        map.put(Items.IRON_BLOCK,ironblock);

        Map<Identifier,Float> goldblock = new HashMap<>();
        goldblock.put(CCOrganScores.LUCK,1.25f);
        goldblock.put(CCOrganScores.BUOYANT,-1f);
        goldblock.put(CCOrganScores.SPEED,-1f);
        goldblock.put(CCOrganScores.FIRE_RESISTANT,1f);
        map.put(Items.GOLD_BLOCK,goldblock);

        Map<Identifier,Float> emeraldblock = new HashMap<>();
        emeraldblock.put(CCOrganScores.LUCK,1f);
        emeraldblock.put(CCOrganScores.BUOYANT,-1f);
        emeraldblock.put(CCOrganScores.SPEED,-1f);
        emeraldblock.put(CCOrganScores.FIRE_RESISTANT,1f);
        map.put(Items.EMERALD_BLOCK,emeraldblock);

        Map<Identifier,Float> diamondblock = new HashMap<>();
        diamondblock.put(CCOrganScores.LUCK,1.25f);
        diamondblock.put(CCOrganScores.DEFENSE,2f);
        diamondblock.put(CCOrganScores.BUOYANT,-1f);
        diamondblock.put(CCOrganScores.SPEED,-1f);
        diamondblock.put(CCOrganScores.FIRE_RESISTANT,1f);
        map.put(Items.DIAMOND_BLOCK,diamondblock);

        Map<Identifier,Float> netheriteblock = new HashMap<>();
        netheriteblock.put(CCOrganScores.LUCK,1.25f);
        netheriteblock.put(CCOrganScores.DEFENSE,3f);
        netheriteblock.put(CCOrganScores.BUOYANT,-1.5f);
        netheriteblock.put(CCOrganScores.SPEED,-1.5f);
        netheriteblock.put(CCOrganScores.FIRE_RESISTANT,4f);
        map.put(Items.NETHERITE_BLOCK,netheriteblock);

        Map<Identifier,Float> gunpowder = new HashMap<>();
        gunpowder.put(CCOrganScores.EXPLOSIVE,3f*Items.GUNPOWDER.getMaxCount());
        map.put(Items.GUNPOWDER,gunpowder);

        Map<Identifier,Float> tnt = new HashMap<>();
        tnt.put(CCOrganScores.EXPLOSIVE,16f*Items.GUNPOWDER.getMaxCount());
        map.put(Items.TNT,tnt);

        Map<Identifier,Float> ease_of_access = new HashMap<>();
        ease_of_access.put(CCOrganScores.EASE_OF_ACCESS,1f*Items.OAK_DOOR.getMaxCount());
        tagMap.put(ItemTags.DOORS,ease_of_access);
        tagMap.put(ItemTags.TRAPDOORS,ease_of_access);

        Map<Identifier,Float> glowstoneDust = new HashMap<>();
        glowstoneDust.put(CCOrganScores.GLOWING,1f*Items.GLOWSTONE_DUST.getMaxCount());
        map.put(Items.GLOWSTONE_DUST,glowstoneDust);

        Map<Identifier,Float> glowstone = new HashMap<>();
        glowstone.put(CCOrganScores.GLOWING,2f*Items.GLOWSTONE.getMaxCount());
        map.put(Items.GLOWSTONE,glowstone);

        Map<Identifier,Float> blazerod = new HashMap<>();
        blazerod.put(CCOrganScores.PYROMANCY,1f);
        blazerod.put(CCOrganScores.HYDROALLERGENIC,1f);
        map.put(Items.BLAZE_ROD,blazerod);

        Map<Identifier,Float> obsidian = new HashMap<>();
        obsidian.put(CCOrganScores.DEFENSE,.5f);
        obsidian.put(CCOrganScores.FIRE_RESISTANT,1f);
        map.put(Items.OBSIDIAN,obsidian);
        map.put(Items.CRYING_OBSIDIAN,obsidian);
    }
}