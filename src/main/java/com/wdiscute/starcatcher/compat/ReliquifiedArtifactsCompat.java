package com.wdiscute.starcatcher.compat;

import com.wdiscute.starcatcher.bobberentity.FishingBobEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

//Reliquified Artifacts (and the Relics/Artifacts mods it bridges) have no Fabric 1.20.1 build at all
//(confirmed 2026-09-01, see FABRIC_PORT_PLAN.md §8) — "reliquified_artifacts" can never be loaded on
//Fabric, so this compat is gated off rather than ported; the private Relics-API helpers the original
//used are dropped since that API doesn't exist here to compile against.
public class ReliquifiedArtifactsCompat
{
    //todo: I have genuinely no clue how this works
    public static boolean shouldAwardBonusTreasure(Player player)
    {
/*        ItemStack stack = getEquippedHatStack(player);
        if (stack.isEmpty()) return false;

        AbilityData ability = getCatchAbility(player, stack);
        if (!ability.isRankModifierUnlocked("treasure")) return false;
          double treasureChance = ability.getStatData("treasure_chance").getValue();*/
        return true;
    }

    //todo: I have genuinely no clue how this works
    public static List<ItemStack> getBonusCatchItems(Player player, FishingBobEntity fbe)
    {
/*
        ItemStack stack = getEquippedHatStack(player);
        if (stack.isEmpty()) return List.of();

        AbilityData ability = getCatchAbility(player, stack);
        double chance = ability.getStatData("chance").getValue();
        int maxCasts = (int) Math.round(ability.getStatData("max_casts").getValue());

        int bonusCount = MathUtils.multicast(player.getRandom(), chance, maxCasts);
        if (bonusCount <= 0) return List.of();

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < bonusCount; i++)
        {
            float percentile = U.r.nextFloat(100);
            int size = FishProperties.SizeAndWeight.getRandomSize(fbe.fpToFish, percentile);
            int weight = FishProperties.SizeAndWeight.getRandomWeight(fbe.fpToFish, percentile);
            ItemStack is = FishProperties.makeItemStack(ItemStack.EMPTY, fbe.fpToFish, size, weight, percentile, false, player, false);
            items.add(is);
        }


        //award relic xp for bonus catches
        IRelicItem relic = (IRelicItem) stack.getItem();
        relic.getRelicData(player, stack).getLevelingData().addExperience("catch", "catch", bonusCount);
*/


        return List.of();
    }

    public static void awardRelicXP(Player player, boolean gotTreasure)
    {
/*        ItemStack stack = getEquippedHatStack(player);
        if (stack.isEmpty()) return;

        IRelicItem relic = (IRelicItem) stack.getItem();
        RelicData relicData = relic.getRelicData(player, stack);

        relicData.getLevelingData().addExperience("catch", "catch", 1.0);
        AbilityData ability = relicData.getAbilitiesData().getAbilityData("catch");
        ability.getStatisticData().getMetricData("fish_caught").addValue(1);

        if (gotTreasure)
        {
            if (ability.isRankModifierUnlocked("treasure"))
            {
                relicData.getLevelingData().addExperience("catch", "treasure_catch", 1.0);
                ability.getStatisticData().getMetricData("treasures_caught").addValue(1);
            }
        }*/
    }
}
