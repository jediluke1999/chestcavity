package net.tigereye.chestcavity.util;

import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.tigereye.chestcavity.ChestCavity;
import net.tigereye.chestcavity.chestcavities.ChestCavityInventory;
import net.tigereye.chestcavity.chestcavities.instance.ChestCavityInstance;
import net.tigereye.chestcavity.chestcavities.ChestCavityType;
import net.tigereye.chestcavity.crossmod.requiem.CCRequiem;
import net.tigereye.chestcavity.interfaces.ChestCavityEntity;
import net.tigereye.chestcavity.items.ChestCavityOrgan;
import net.tigereye.chestcavity.items.Organ;
import net.tigereye.chestcavity.listeners.*;
import net.tigereye.chestcavity.registration.CCEnchantments;
import net.tigereye.chestcavity.registration.CCOrganScores;
import net.tigereye.chestcavity.registration.CCOtherOrgans;
import net.tigereye.chestcavity.registration.CCStatusEffects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class ChestCavityUtil {

    public static void addOrganScore(Identifier id, float value, Map<Identifier,Float> organScores){
        organScores.put(id,organScores.getOrDefault(id,0f)+value);
    }

    public static float applyBoneDefense(ChestCavityInstance cc, float damage){
        float boneDiff = (cc.getOrganScore(CCOrganScores.DEFENSE) - cc.getChestCavityType().getDefaultOrganScore(CCOrganScores.DEFENSE))/4;
        return (float)(damage*Math.pow(1-ChestCavity.config.BONE_DEFENSE,boneDiff));
    }

    public static int applyBreathInWater(ChestCavityInstance cc, int oldAir, int newAir){
        //if your chest cavity is untouched or normal, we do nothing
        if(!cc.opened || ( cc.getChestCavityType().getDefaultOrganScore(CCOrganScores.BREATH) == cc.getOrganScore(CCOrganScores.BREATH) &&
                cc.getChestCavityType().getDefaultOrganScore(CCOrganScores.WATERBREATH) == cc.getOrganScore(CCOrganScores.WATERBREATH))){
            return newAir;
        }

        float airLoss = 1;
        //if you have waterbreath, you can breath underwater. Yay! This will overwrite any incoming air loss.
        float waterBreath = cc.getOrganScore(CCOrganScores.WATERBREATH);
        if(waterBreath > 0){
            airLoss += (-2*waterBreath)+cc.lungRemainder;
        }

        //if you don't (or you are still breath negative),
        //we check how well your lungs can hold oxygen
        if (airLoss > 0){
            if(oldAir == newAir){
                //this would indicate that resperation was a success
                airLoss = 0;
            }
            else {
                float breath = cc.getOrganScore(CCOrganScores.BREATH);
                airLoss *= (oldAir - newAir); //if you are downing at bonus speed, ok
                if (airLoss > 0) {
                    float lungRatio = 20f;
                    if (breath != 0) {
                        lungRatio = Math.min(2 / breath, 20f);
                    }
                    airLoss = (airLoss * lungRatio) + cc.lungRemainder;
                }
            }
        }

        cc.lungRemainder = airLoss % 1;
        int airResult = Math.min(oldAir - ((int) airLoss),cc.owner.getMaxAir());
        //I don't trust vanilla to do this job right, so I will choke you myself
        if (airResult <= -20) {
            airResult = 0;
            cc.lungRemainder = 0;
            cc.owner.damage(DamageSource.DROWN, 2.0F);
        }
        return airResult;
    }

    public static int applyBreathOnLand(ChestCavityInstance cc, int oldAir, int airGain){
        //we have to recreate breath mechanics here I'm afraid
        //if your chest cavity is untouched or normal, we do nothing

        if(!cc.opened|| ( cc.getChestCavityType().getDefaultOrganScore(CCOrganScores.BREATH) == cc.getOrganScore(CCOrganScores.BREATH) &&
                cc.getChestCavityType().getDefaultOrganScore(CCOrganScores.WATERBREATH) == cc.getOrganScore(CCOrganScores.WATERBREATH))){
            return oldAir;
        }

        float airLoss;
        if(cc.owner.hasStatusEffect(StatusEffects.WATER_BREATHING) || cc.owner.hasStatusEffect(StatusEffects.CONDUIT_POWER)){
            airLoss = 0;
        }
        else{airLoss = 1;}


        //if you have breath, you can breath on land. Yay!
        //if in contact with water or rain apply on quarter your water breath as well
        //(so 2 gills can survive in humid conditions)
        float breath = cc.getOrganScore(CCOrganScores.BREATH);
        if(cc.owner.isTouchingWaterOrRain()){
            breath += cc.getOrganScore(CCOrganScores.WATERBREATH)/4;
        }
        if(breath > 0){
            airLoss += (-airGain * (breath) / 2) + cc.lungRemainder;
        }

        //if you don't then unless you have the water breathing status effect you must hold your watery breath.
        if (airLoss > 0) {
            //first, check if resperation cancels the sequence.
            int resperation = EnchantmentHelper.getRespiration(cc.owner);
            if (cc.owner.getRandom().nextInt(resperation + 1) != 0) {
                airLoss = 0;
            }
            else{
                //then, we apply our beath capacity
                float waterbreath = cc.getOrganScore(CCOrganScores.WATERBREATH);
                float gillRatio = 20f;
                if (waterbreath != 0) {
                    gillRatio = Math.min(2 / waterbreath, 20f);
                }
                airLoss = airLoss * gillRatio + cc.lungRemainder;
            }
        }
        else{
            if(oldAir == cc.owner.getMaxAir()){
                return oldAir;
            }
        }

        cc.lungRemainder = airLoss % 1;
        //we finally undo the air gained in vanilla while calculating final results
        int airResult = Math.min(oldAir - ((int) airLoss) - airGain,cc.owner.getMaxAir());
        //I don't trust vanilla to do this job right, so I will choke you myself
        if (airResult <= -20) {
            airResult = 0;
            cc.lungRemainder = 0;
            cc.owner.damage(DamageSource.DROWN, 2.0F);
        }
        return airResult;
    }

    public static float applyDefenses(ChestCavityInstance cc, DamageSource source, float damage){
        if(!cc.opened){
            return damage;
        }
        if(attemptArrowDodging(cc,source)){
            return 0;
        }
        if(!source.bypassesArmor()) {
            damage = applyBoneDefense(cc,damage);
        }
        if(source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL){
            damage = applyImpactResistant(cc,damage);
        }
        if(source.isFire()){
            damage = applyFireResistant(cc,damage);
        }
        return damage;
    }

    public static float applyFireResistant(ChestCavityInstance cc, float damage){
        float fireproof = cc.getOrganScore(CCOrganScores.FIRE_RESISTANT);
        if(fireproof > 0){
            return (float)(damage*Math.pow(1-ChestCavity.config.FIREPROOF_DEFENSE,fireproof/4));
        }
        return damage;
    }

    public static float applyImpactResistant(ChestCavityInstance cc, float damage){
        float impactResistant = cc.getOrganScore(CCOrganScores.IMPACT_RESISTANT);
        if(impactResistant > 0){
            return (float)(damage*Math.pow(1-ChestCavity.config.IMPACT_DEFENSE,impactResistant/4));
        }
        return damage;
    }

    public static float applyNutrition(ChestCavityInstance cc, float nutrition, float saturation){
        if(nutrition == 4){
            return saturation;
        }
        if(nutrition < 0){
            cc.owner.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER,(int)(saturation*nutrition*800)));
            return 0;
        }
        return saturation*nutrition/4;
        //TODO: find a use for intestines for non-players
    }

    public static int applySpleenMetabolism(ChestCavityInstance cc, int foodStarvationTimer){
        if(!cc.opened){
            return foodStarvationTimer;
        }
        cc.spleenTimer++;
        if(cc.spleenTimer >= 2){
            foodStarvationTimer += cc.getOrganScore(CCOrganScores.METABOLISM) - 1;
            cc.spleenTimer = 0;
        }
        return foodStarvationTimer;
        //TODO: find a use for spleens for non-players
    }

    public static int applyDigestion(ChestCavityInstance cc, float digestion, int hunger){
        if(digestion == 1){
            return hunger;
        }
        if(digestion < 0){
            cc.owner.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,(int)(-hunger*digestion*400)));
            return 0;
        }
        //sadly, in order to get saturation at all we must grant at least half a haunch of food, unless we embrace incompatibility
        return Math.max((int)(hunger*digestion),1);
        //TODO: find a use for stomachs for non-players
    }

    public static boolean attemptArrowDodging(ChestCavityInstance cc, DamageSource source){
        float dodge = cc.getOrganScore(CCOrganScores.ARROW_DODGING);
        if(dodge == 0){
            return false;
        }
        if(cc.owner.hasStatusEffect(CCStatusEffects.ARROW_DODGE_COOLDOWN)){
            return false;
        }
        if (!(source instanceof ProjectileDamageSource)) {
            return false;
        }
        if(!OrganUtil.teleportRandomly(cc.owner,ChestCavity.config.ARROW_DODGE_DISTANCE/dodge)){
            return false;
        }
        cc.owner.addStatusEffect(new StatusEffectInstance(CCStatusEffects.ARROW_DODGE_COOLDOWN, (int) (ChestCavity.config.ARROW_DODGE_COOLDOWN/dodge), 0, false, false, true));
        return true;
    }

    public static void destroyOrgansWithKey(ChestCavityInstance cc, Identifier organ){
        for (int i = 0; i < cc.inventory.size(); i++)
        {
            ItemStack slot = cc.inventory.getStack(i);
            if (slot != null && slot != ItemStack.EMPTY)
            {
                Map<Identifier,Float> organScores = lookupOrganScore(slot,cc.owner);
                if(organScores != null && organScores.containsKey(organ)){
                    cc.inventory.removeStack(i);
                }
            }
        }
        cc.inventory.markDirty();
    }

    public static boolean determineDefaultOrganScores(ChestCavityType chestCavityType) {
        Map<Identifier,Float> organScores = chestCavityType.getDefaultOrganScores();
        chestCavityType.loadBaseOrganScores(organScores);
        try {
            for (int i = 0; i < chestCavityType.getDefaultChestCavity().size(); i++) {
                ItemStack itemStack = chestCavityType.getDefaultChestCavity().getStack(i);
                if (itemStack != null && itemStack != ItemStack.EMPTY) {
                    Item slotitem = itemStack.getItem();
                    if (!chestCavityType.catchExceptionalOrgan(itemStack, organScores)) {//if a manager handles an organ in a special way, this lets it skip the normal evaluation.
                        Map<Identifier, Float> organMap = lookupOrganScore(itemStack, null);
                        if (organMap != null) {
                            organMap.forEach((key, value) ->
                                    addOrganScore(key, value * Math.min(((float) itemStack.getCount()) / itemStack.getMaxCount(), 1), organScores));
                        }
                    }
                    CompoundTag tag = itemStack.getTag();
                }
            }
        }
        catch(IllegalStateException e){
            ChestCavity.LOGGER.warn(e.getMessage()+". Chest Cavity will attempt to calculate this default organ score later.");
            return false;
        }
        return true;
    }

    public static int getCompatibilityLevel(ChestCavityInstance cc, ItemStack itemStack){
        if(itemStack != null && itemStack != ItemStack.EMPTY) {
            if(EnchantmentHelper.getLevel(CCEnchantments.MALPRACTICE,itemStack)>0){
                return 0;
            }
            int oNegative = EnchantmentHelper.getLevel(CCEnchantments.O_NEGATIVE,itemStack);
            int ownership = 0;
            CompoundTag tag = itemStack.getTag();
            if (tag != null && tag.contains(ChestCavity.COMPATIBILITY_TAG.toString())) {
                tag = tag.getCompound(ChestCavity.COMPATIBILITY_TAG.toString());
                if (tag.getUuid("owner").equals(cc.compatibility_id)) {
                    ownership = 2;
                }
            } else {
                ownership = 1;
            }
            return Math.max(oNegative,ownership);
        }
        return 1;
    }

    public static void dropUnboundOrgans(ChestCavityInstance cc) {
        if(ChestCavity.config.REQUIEM_INTEGRATION){
            if(Registry.ENTITY_TYPE.getId(cc.owner.getType()).compareTo(CCRequiem.PLAYER_SHELL_ID) == 0){
                return; //player shells shall not drop organs
            }
        }
        try {
            cc.inventory.removeListener(cc);
        } catch(NullPointerException ignored){}
        for(int i = 0; i < cc.inventory.size(); i++){
            ItemStack itemStack = cc.inventory.getStack(i);
            if(itemStack != null && itemStack != ItemStack.EMPTY) {
                int compatibility = getCompatibilityLevel(cc,itemStack);
                if(compatibility < 2){
                    cc.owner.dropStack(cc.inventory.removeStack(i));
                }
            }
        }
        cc.inventory.addListener(cc);
        evaluateChestCavity(cc);
    }

    public static void evaluateChestCavity(ChestCavityInstance cc) {
        Map<Identifier,Float> organScores = cc.getOrganScores();
        if(!cc.opened){
            organScores.clear();
            organScores.putAll(cc.getChestCavityType().getDefaultOrganScores());
        }
        else {
            cc.onHitListeners.clear();
            cc.getChestCavityType().loadBaseOrganScores(organScores);

            for (int i = 0; i < cc.inventory.size(); i++) {
                ItemStack itemStack = cc.inventory.getStack(i);
                if (itemStack != null && itemStack != ItemStack.EMPTY) {
                    Item slotitem = itemStack.getItem();
                    if (!cc.getChestCavityType().catchExceptionalOrgan(itemStack,organScores)) {//if a manager chooses to handle some organ in a special way, this lets it skip the normal evaluation.
                        Map<Identifier, Float> organMap = lookupOrganScore(itemStack,cc.owner);
                        if (organMap != null) {
                            organMap.forEach((key, value) ->
                                    addOrganScore(key, value * Math.min(((float)itemStack.getCount()) / itemStack.getMaxCount(),1),organScores));
                        }
                        if(slotitem instanceof OrganOnHitListener){
                            cc.onHitListeners.add(new OrganOnHitContext(itemStack,(OrganOnHitListener)slotitem));
                        }
                    }
                    if (slotitem instanceof Organ) {
                        int compatibility = getCompatibilityLevel(cc,itemStack);
                        if(compatibility < 1){
                            addOrganScore(CCOrganScores.INCOMPATIBILITY, 1, organScores);
                        }
                    }
                }
            }
        }
        organUpdate(cc);
    }

    public static void generateChestCavityIfOpened(ChestCavityInstance cc){
        if(cc.opened) {
            cc.inventory.readTags(cc.getChestCavityType().getDefaultChestCavity().getTags());
            cc.getChestCavityType().setOrganCompatibility(cc);
        }
    }

    public static boolean isHydroPhobicOrAllergic(LivingEntity entity){
        Optional<ChestCavityEntity> optional = ChestCavityEntity.of(entity);
        if(optional.isPresent()){
            ChestCavityInstance cc = optional.get().getChestCavityInstance();
            return (cc.getOrganScore(CCOrganScores.HYDROALLERGENIC) > 0) || (cc.getOrganScore(CCOrganScores.HYDROPHOBIA) > 0);
        }
        return false;
    }

    protected static Map<Identifier,Float> lookupOrganScore(ItemStack itemStack, LivingEntity owner){
        Item item = itemStack.getItem();
        if(item instanceof ChestCavityOrgan){
            if(owner != null) {
                return ((ChestCavityOrgan) item).getOrganQualityMap(itemStack, owner);
            }
            else{
                return ((ChestCavityOrgan) item).getOrganQualityMap(itemStack);
            }
        }
        else if(CCOtherOrgans.map.containsKey(item)){
            return CCOtherOrgans.map.get(item);
        }
        else{
            for (Tag<Item> itemTag:
                    CCOtherOrgans.tagMap.keySet()) {
                if(item.isIn(itemTag)){
                    return CCOtherOrgans.tagMap.get(itemTag);
                }
            }
        }
        return null;
    }

    public static StatusEffectInstance onAddStatusEffect(ChestCavityInstance cc, StatusEffectInstance effect) {
        return OrganAddStatusEffectCallback.EVENT.invoker().onAddStatusEffect(cc.owner, cc,effect);
    }

    public static float onHit(ChestCavityInstance cc, DamageSource source, LivingEntity target, float damage){
        if(cc.opened) {
            //this is for individual organs
            for (OrganOnHitContext e:
                    cc.onHitListeners) {
                damage = e.listener.onHit(source,cc.owner,target,cc,e.organ,damage);
            }
            //this is for organ scores
            //OrganOnHitCallback.EVENT.invoker().onHit(source,cc.owner,target,cc,damage);
            organUpdate(cc);
        }
        return damage;
    }

    public static void onTick(ChestCavityInstance cc){
        if(cc.updatePacket != null){
            NetworkUtil.SendS2CChestCavityUpdatePacket(cc,cc.updatePacket);
        }/*
        if(CCRequiem.REQUIEM_ACTIVE) {
            if (cc.owner instanceof Possessable && ((Possessable) cc.owner).isBeingPossessed()) {
                Optional<ChestCavityEntity> option = ChestCavityEntity.of(((Possessable) cc.owner).getPossessor());
                if(option.isPresent()){
                    ChestCavityInstance possessorCC = option.get().getChestCavityInstance();
                    openChestCavity(possessorCC);
                    possessorCC.organScores.clear();
                    possessorCC.organScores.putAll(cc.organScores);
                }
            }
        }*/
        if(cc.opened) {
            OrganTickCallback.EVENT.invoker().onOrganTick(cc.owner, cc);
            organUpdate(cc);
        }
    }

    public static ChestCavityInventory openChestCavity(ChestCavityInstance cc){
        if(!cc.opened) {
            try {
                cc.inventory.removeListener(cc);
            }
            catch(NullPointerException ignored){}
            cc.opened = true;
            generateChestCavityIfOpened(cc);
            cc.inventory.addListener(cc);
        }
        return cc.inventory;
    }


    public static void organUpdate(ChestCavityInstance cc){
        Map<Identifier,Float> organScores = cc.getOrganScores();
        if(!cc.oldOrganScores.equals(organScores))
        {
            if(ChestCavity.DEBUG_MODE && cc.owner instanceof PlayerEntity) {
                ChestCavityUtil.outputOrganScoresString(System.out::println,cc);
            }
            OrganUpdateCallback.EVENT.invoker().onOrganUpdate(cc.owner, cc);
            cc.oldOrganScores.clear();
            cc.oldOrganScores.putAll(organScores);
            NetworkUtil.SendS2CChestCavityUpdatePacket(cc);
        }
    }

    public static void outputOrganScoresString(Consumer<String> output, ChestCavityInstance cc){
        try {
            Text name = cc.owner.getDisplayName();
            output.accept("[Chest Cavity] Displaying " + name.getString() +"'s organ scores:");
        }
        catch(Exception e){
            output.accept("[Chest Cavity] Displaying organ scores:");
        }
        cc.getOrganScores().forEach((key, value) ->
                output.accept(key.getPath() + ": " + value + " "));
    }

    public static float applySwimSpeedInWater(ChestCavityInstance cc) {
        if(!cc.opened || !cc.owner.isTouchingWater()){return 1;}
        float speedDiff = cc.getOrganScore(CCOrganScores.SWIM_SPEED) - cc.getChestCavityType().getDefaultOrganScore(CCOrganScores.SWIM_SPEED);
        if(speedDiff == 0){return 1;}
        else{
            return Math.max(0,1+(speedDiff*ChestCavity.config.SWIMSPEED_FACTOR/8));
        }

    }

    public static void clearForbiddenSlots(ChestCavityInstance cc) {
        try {
            cc.inventory.removeListener(cc);
        } catch(NullPointerException ignored){}
        for(int i = 0; i < cc.inventory.size();i++){
            if(cc.getChestCavityType().isSlotForbidden(i)){
                cc.owner.dropStack(cc.inventory.removeStack(i));
            }
        }
        cc.inventory.addListener(cc);
    }

    public static List<ItemStack> drawOrgansFromPile(List<Item> organPile, int rolls, Random random){
        List<ItemStack> loot = new ArrayList<>();
        drawOrgansFromPile(organPile,rolls,random,loot);
        return loot;
    }
    public static void drawOrgansFromPile(List<Item> organPile, int rolls, Random random, List<ItemStack> loot){
        for (int i = 0; i < rolls; i++) {
            if(organPile.isEmpty()){
                break;
            }
            int roll = random.nextInt(organPile.size());
            int count = 1;
            Item rolledItem = organPile.get(roll);
            if (rolledItem.getMaxCount() > 1) {
                count += random.nextInt(rolledItem.getMaxCount());
            }
            loot.add(new ItemStack(organPile.remove(roll), count));
        }
    }
}
