package com.envyful.mixins.magic.hot.reforged;

import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.status.MagicCoat;
import com.pixelmonmod.pixelmon.battles.status.StatusBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.MagicBounce;
import com.pixelmonmod.pixelmon.enums.battle.AttackCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;

@Mixin(MagicCoat.class)
public class MixinMagicCoat extends StatusBase {

    /**
     * @author danorris709
     */
    @Overwrite(remap = false)
    public static boolean reflectMove(Attack a, PixelmonWrapper pokemon, PixelmonWrapper user, String message) {
        if (a.getAttackCategory() == AttackCategory.STATUS && !a.isAttack(new String[]{"Bestow", "Curse", "Guard Swap", "Heart Swap", "Lock-On", "Memento", "Mimic", "Power Swap", "Psych Up", "Psycho Shift", "Role Play", "Skill Swap", "Snatch", "Switcheroo", "Transform", "Trick", "Extreme Evoboost", "Sketch"}) && (!a.getMove().getTargetingInfo().hitsAll || !a.getMove().getTargetingInfo().hitsSelf)) {
            user.bc.sendToAll(message, new Object[]{pokemon.getNickname()});
            pokemon.targetIndex = 0;
            boolean allowed = false;
            AbilityBase battleAbility = user.getBattleAbility(pokemon);

            if (!( battleAbility instanceof MagicBounce)) {
                allowed = battleAbility.allowsIncomingAttack(user, pokemon, a);
            }

            if (allowed) {
                allowed = user.getUsableHeldItem().allowsIncomingAttack(user, pokemon, a);
            }

            if (allowed) {
                Iterator var5 = user.bc.getTeamPokemon(user).iterator();

                while(var5.hasNext()) {
                    PixelmonWrapper ally = (PixelmonWrapper)var5.next();
                    if (!ally.getBattleAbility().allowsIncomingAttackTeammate(ally, user, pokemon, a)) {
                        allowed = false;
                        break;
                    }
                }
            }

            if (!allowed) {
                return true;
            } else if (!pokemon.getBattleAbility().allowsOutgoingAttack(pokemon, user, a)) {
                return true;
            } else if (a.hasNoEffect(pokemon, user)) {
                user.bc.sendToAll("pixelmon.battletext.noeffect", new Object[]{user.getNickname()});
                return true;
            } else {
                Attack oldAttack = user.attack;
                user.attack = a;
                a.applyAttackEffect(pokemon, user);
                user.attack = oldAttack;
                return true;
            }
        } else {
            return false;
        }
    }

}
